package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import de.unisb.cs.st.javaslicer.tracer.UntracedThread;
import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.Option;
import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.ReferenceType;
import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.RemoveStaleListener;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream.InnerOutputStream;

public class MultiplexedFileWriter {

    private class FlushThread extends UntracedThread {

        private final AtomicBoolean doFlush;
        private final Semaphore waitForFlushRequest;
        private final AtomicReference<MappedByteBuffer> omitBuffer;

        public FlushThread() {
            super("MultiplexedFile Flusher");
            setDaemon(true);
            this.doFlush = new AtomicBoolean(false);
            this.waitForFlushRequest = new Semaphore(0);
            this.omitBuffer = new AtomicReference<MappedByteBuffer>();
        }

        @Override
        public void run() {
            try {
                long nanoSeconds = 0;
                while (true) {
                    MappedByteBuffer omit;
                    if (this.waitForFlushRequest.tryAcquire(Math.max(nanoSeconds, 10*1000*1000*1000l), TimeUnit.NANOSECONDS)) {
                        final boolean oldValue = this.doFlush.getAndSet(false);
                        assert oldValue == true;
                        omit = this.omitBuffer.getAndSet(null);
                    } else {
                        omit = null;
                    }
                    final long nanosBefore = System.nanoTime();
                    try {
                        flush0(omit);
                    } catch (final IOException e) {
                        // ignore
                    }
                    final long nanosAfter = System.nanoTime();
                    nanoSeconds = (long) (0.8*nanoSeconds + 0.2*3*(nanosAfter-nanosBefore));
                }
            } catch (final InterruptedException e) {
                return;
            }
        }

        public void doFlush(final MappedByteBuffer omit) {
            this.omitBuffer.set(omit);
            if (this.doFlush.compareAndSet(false, true))
                this.waitForFlushRequest.release();
        }
    }

    public class MultiplexOutputStream extends OutputStream {

        protected class InnerOutputStream extends OutputStream {

            private final int id;
            protected long dataLength = 0;
            protected int depth = 0;
            protected int startBlockAddr = 0; // is set on close()
            protected int[][] pointerBlocks = null;
            protected byte[] dataBlock = new byte[16];
            protected int[] full = new int[1];
            private final AtomicReference<Set<Reader>> readers = new AtomicReference<Set<Reader>>(null);

            public InnerOutputStream(final int id) {
                this.id = id;
            }

            @Override
            public void write(final int b) throws IOException {
                if (this.dataBlock == null)
                    throw new IOException("stream closed");
                if (this.full[this.depth] == this.dataBlock.length) {
                    if (this.dataBlock.length < MultiplexedFileWriter.this.blockSize) {
                        final byte[] newDataBlock = new byte[Math.min(2*this.dataBlock.length, MultiplexedFileWriter.this.blockSize)];
                        System.arraycopy(this.dataBlock, 0, newDataBlock, 0, this.dataBlock.length);
                        this.dataBlock = newDataBlock;
                    } else
                        moveToNextBlock();
                }
                this.dataBlock[this.full[this.depth]++] = (byte) b;
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                if (this.dataBlock == null)
                    throw new IOException("stream closed");
                if (b == null)
                    throw new NullPointerException();
                if (off < 0 || len  < 0 || off+len > b.length)
                    throw new IndexOutOfBoundsException();
                if (len == 0)
                    return;

                int pos = off;
                final int end = off + len;
                while (pos < end) {
                    if (this.full[this.depth] == this.dataBlock.length) {
                        if (this.dataBlock.length < MultiplexedFileWriter.this.blockSize) {
                            final byte[] newDataBlock = new byte[Math.min(Math.max(2*this.dataBlock.length, len), MultiplexedFileWriter.this.blockSize)];
                            System.arraycopy(this.dataBlock, 0, newDataBlock, 0, this.dataBlock.length);
                            this.dataBlock = newDataBlock;
                        } else
                            moveToNextBlock();
                    }
                    final int write = Math.min(end - pos,
                            this.dataBlock.length-this.full[this.depth]);
                    System.arraycopy(b, pos, this.dataBlock, this.full[this.depth], write);
                    pos += write;
                    this.full[this.depth] += write;
                }
            }

            private void moveToNextBlock() throws IOException {
                synchronized (MultiplexOutputStream.this) {
                    this.dataLength += MultiplexedFileWriter.this.blockSize;

                    if (!writeBack(this.depth)) {
                        // could not write back directly, so we have to increase the depth
                        increaseDepth();
                        if (!writeBack(this.depth))
                            throw new RuntimeException("Increase of tree depth did not work as expected");
                    }
                    final Set<Reader> readers0 = this.readers.get();
                    if (readers0 != null) {
                        synchronized (readers0) {
                            for (final Reader reader: readers0)
                                reader.seek(reader.getPosition());
                        }
                    }
                }
            }

            private void increaseDepth() {
                // the depth of all entries except the first one is increased
                final int[][] newPointerBlocks = new int[this.depth+1][];
                if (this.depth > 0)
                    System.arraycopy(this.pointerBlocks, 0, newPointerBlocks, 1, this.depth);
                newPointerBlocks[0] = new int[MultiplexedFileWriter.this.blockSize/4];
                this.pointerBlocks = newPointerBlocks;
                final int[] newFull = new int[this.depth+2];
                System.arraycopy(this.full, 0, newFull, 1, this.depth+1);
                newFull[0] = 0;
                this.full = newFull;
                ++this.depth;
            }

            private boolean writeBack(final int level) throws IOException {
                // we cannot write back level 0 (have to increase depth)
                if (level == 0)
                    return false;

                // if the next lower level is full too, we first have to write back this level
                if (this.full[level-1] == MultiplexedFileWriter.this.blockSize/4 && !writeBack(level-1))
                    return false;

                // now write back the data block
                final int newBlockAddr = getNewBlockAddress();
                if (level == this.depth)
                    writeBlock(newBlockAddr, this.dataBlock);
                else
                    writeBlock(newBlockAddr, this.pointerBlocks[level]);

                this.pointerBlocks[level-1][this.full[level-1]++] = newBlockAddr;
                this.full[level] = 0;
                return true;
            }

            public int getId() {
                return this.id;
            }

            @Override
            public void close() throws IOException {
                synchronized (MultiplexOutputStream.this) {
                    if (this.dataBlock == null)
                        return;

                    final Set<Reader> readers0 = this.readers.get();
                    if (readers0 != null) {
                        synchronized (readers0) {
                            for (final Reader reader: readers0)
                                reader.close();
                        }
                    }

                    this.dataLength += this.full[this.depth];

                    if (this.dataBlock.length < MultiplexedFileWriter.this.blockSize) {
                        final byte[] newDataBlock = new byte[MultiplexedFileWriter.this.blockSize];
                        System.arraycopy(this.dataBlock, 0, newDataBlock, 0, this.full[this.depth]);
                        this.dataBlock = newDataBlock;
                    } else {
                        Arrays.fill(this.dataBlock, this.full[this.depth], MultiplexedFileWriter.this.blockSize, (byte)0);
                    }
                    if (this.depth == 0) {
                        this.startBlockAddr = getNewBlockAddress();
                        writeBlock(this.startBlockAddr, this.dataBlock);
                    } else {
                        if (this.full[0] == MultiplexedFileWriter.this.blockSize/4)
                            increaseDepth();

                        outer:
                            while (true) {
                                for (int i = 1; i < this.depth; ++i) {
                                    if (this.full[i] == MultiplexedFileWriter.this.blockSize/4) {
                                        if (!writeBack(i))
                                            throw new RuntimeException("writeBack in close() should always succeed");
                                        if (this.full[0] == MultiplexedFileWriter.this.blockSize/4)
                                            increaseDepth();
                                        continue outer;
                                    }
                                }
                                break;
                            }

                        if (!writeBack(this.depth))
                            throw new RuntimeException("writeBack in close() should always succeed");
                        for (int i = this.depth-1; i > 0; --i) {
                            // zero out the remaining part of the block
                            Arrays.fill(this.pointerBlocks[i], this.full[i], MultiplexedFileWriter.this.blockSize/4, 0);
                            if (!writeBack(i))
                                throw new RuntimeException("writeBack in close() should always succeed");
                        }

                        this.startBlockAddr = getNewBlockAddress();
                        writeBlock(this.startBlockAddr, this.pointerBlocks[0]);
                    }

                    // now we can release most buffers
                    this.pointerBlocks = null;
                    this.dataBlock = null;
                    this.full = null;
                    this.readers.set(null);

                    // after all this work, store the information about this stream to the streamDefs stream
                    if (this != MultiplexedFileWriter.this.streamDefs.innerOut) {
                        synchronized (MultiplexedFileWriter.this.streamDefsDataOut) {
                            MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.id);
                            MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.startBlockAddr);
                            MultiplexedFileWriter.this.streamDefsDataOut.writeLong(this.dataLength);
                        }
                    }
                }
            }

            public long length() {
                return this.full == null ? this.dataLength : this.dataLength + this.full[this.depth];
            }

            public Reader getReader(final long pos) throws IOException {
                final Reader reader = new Reader(pos);
                Set<Reader> readers0;
                while ((readers0 = this.readers.get()) == null) {
                    if (this.readers.compareAndSet(null, readers0 = new HashSet<Reader>()))
                        break;
                }
                synchronized (readers0) {
                    readers0.add(reader);
                }
                return reader;
            }

            protected void removeReader(final Reader reader) {
                final Set<Reader> readers0 = this.readers.get();
                if (readers0 != null) {
                    synchronized (readers0) {
                        readers0.remove(reader);
                    }
                }
            }

            public synchronized void remove() throws IOException {
                if (this.dataBlock == null)
                    throw new IOException("a closed stream cannot be removed any more");

                final Set<Reader> readers0 = this.readers.get();
                if (readers0 != null) {
                    synchronized (readers0) {
                        for (final Reader reader: readers0)
                            reader.close();
                    }
                }

                if (this.depth > 0) {
                    int noBlocks = (int)divUp(length(), MultiplexedFileWriter.this.blockSize)-1;
                    int tmp = noBlocks; // no data blocks
                    while (tmp > 1) {
                        tmp = divUp(tmp, MultiplexedFileWriter.this.blockSize/4) - 1;
                        noBlocks += tmp;
                    }

                    releaseBlocks:
                        while (true) {
                            while (this.full[this.depth-1] > 0) {
                                --noBlocks;
                                MultiplexedFileWriter.this.freeBlocks.add(
                                        this.pointerBlocks[this.depth-1][--this.full[this.depth-1]]);
                            }
                            for (int i = this.depth-2; i >= 0; --i) {
                                if (this.full[i] > 0) {
                                    final int blockAddr = this.pointerBlocks[i][--this.full[i]];
                                    readBlock(blockAddr, this.pointerBlocks[i+1]);
                                    --noBlocks;
                                    MultiplexedFileWriter.this.freeBlocks.add(blockAddr);
                                    this.full[i+1] = MultiplexedFileWriter.this.blockSize/4;
                                    continue releaseBlocks;
                                }
                            }
                            break;
                        }

                    assert noBlocks == 0;
                }

                this.dataLength = 0;

                // now we can release most buffers
                this.pointerBlocks = null;
                this.dataBlock = null;
                this.full = null;
                this.readers.set(null);

                if (MultiplexedFileWriter.this.reuseStreamIds)
                    MultiplexedFileWriter.this.streamIdsToReuse.add(this.id);
            }

        }

        /**
         * An InputStream that works directly on the {@link MultiplexOutputStream},
         * even while it is written.
         *
         * @author Clemens Hammacher
         */
        public class Reader extends InputStream {

            private int[][] readPointerBlocks;
            private byte[] readDataBlock;
            private int[] pos;
            private final byte[] dataBlockBuf = new byte[MultiplexedFileWriter.this.blockSize];
            private int[][] pointerBlockBufs = null;
            private int remainingInCurrentBlock;
            private boolean readerClosed = false;

            protected Reader() throws IOException {
                this(0);
            }

            protected Reader(final long pos) throws IOException {
                seek(pos);
            }

            public void seek(final long toPos) throws IOException {
                synchronized (MultiplexOutputStream.this) {
                    if (this.readerClosed)
                        throw new IOException("closed");
                    seek(getBlocksPos(toPos));
                }
            }

            private void seek(final int[] newPos) throws IOException {
                final boolean reInitialize = this.pos == null || this.pos.length != newPos.length;
                final int depth = MultiplexOutputStream.this.innerOut.depth;
                if (reInitialize) {
                    this.readPointerBlocks = new int[depth][];
                    this.pointerBlockBufs = new int[depth][MultiplexedFileWriter.this.blockSize/4];
                }
                if (depth == 0) {
                    if (reInitialize) {
                        this.readDataBlock = MultiplexOutputStream.this.innerOut.dataBlock;
                    }
                    this.remainingInCurrentBlock = (MultiplexOutputStream.this.innerOut.full[0] - newPos[0]);
                } else {
                    boolean reRead = reInitialize;
                    boolean atEnd = true;
                    for (int i = 0; i < depth; ++i) {
                        if (reRead) {
                            if (atEnd) {
                                this.readPointerBlocks[i] = MultiplexOutputStream.this.innerOut.pointerBlocks[i];
                            } else {
                                this.readPointerBlocks[i] = this.pointerBlockBufs[i];
                                final int blockAddr = this.readPointerBlocks[i-1][newPos[i-1]];
                                readBlock(blockAddr, this.readPointerBlocks[i]);
                            }
                        }
                        reRead = reRead || this.pos[i] != newPos[i];
                        atEnd = atEnd && newPos[i] == MultiplexOutputStream.this.innerOut.full[i];
                    }
                    if (reRead) {
                        if (atEnd) {
                            this.readDataBlock = MultiplexOutputStream.this.innerOut.dataBlock;
                        } else {
                            this.readDataBlock = this.dataBlockBuf;
                            readBlock(this.readPointerBlocks[depth-1][newPos[depth-1]], this.readDataBlock);
                        }
                    }
                    this.remainingInCurrentBlock = (atEnd ? MultiplexOutputStream.this.innerOut.full[depth]
                            : MultiplexedFileWriter.this.blockSize) - newPos[depth];
                }
                this.pos = newPos;
            }

            private int[] getBlocksPos(final long position) throws IOException {
                if (position < 0)
                    throw new IOException("Seek position must be >= 0");

                final int depth = MultiplexOutputStream.this.innerOut.depth;
                final int[] full = MultiplexOutputStream.this.innerOut.full;
                final int[] newPos = new int[depth+1];
                if (depth == 0) {
                    if (position > full[0])
                        throw new IOException("Cannot seek behind end of stream");
                    newPos[0] = (int) position;
                } else {
                    newPos[depth] = (int) (position % MultiplexedFileWriter.this.blockSize);
                    long remaining = position / MultiplexedFileWriter.this.blockSize;
                    for (int d = depth-1; d > 0; --d) {
                        newPos[d] = (int) (remaining % (MultiplexedFileWriter.this.blockSize/4));
                        remaining = remaining / (MultiplexedFileWriter.this.blockSize/4);
                    }
                    assert remaining <= MultiplexedFileWriter.this.blockSize/4;
                    newPos[0] = (int) remaining;
                    boolean atEnd = true;
                    for (int d = 0; d < depth; ++d) {
                        if (newPos[d] <= full[d]) {
                            atEnd = false;
                            break;
                        }

                        if (newPos[d] != full[d]+1 || newPos[d+1] != 0)
                            throw new IOException("Cannot seek behind end of stream");
                        --newPos[d];
                        newPos[d+1] = d == depth-1 ? MultiplexedFileWriter.this.blockSize : MultiplexedFileWriter.this.blockSize/4;
                    }
                    if (atEnd && newPos[depth] > full[depth])
                        throw new IOException("Cannot seek behind end of stream");
                }
                return newPos;
            }

            @Override
            public int read() throws IOException {
                if (this.readerClosed)
                    throw new IOException("closed");
                if (this.remainingInCurrentBlock == 0) {
                    moveToNextBlock();
                    if (this.remainingInCurrentBlock == 0)
                        return -1;
                }
                --this.remainingInCurrentBlock;
                return this.readDataBlock[this.pos[this.pos.length-1]++] & 0xff;
            }

            @Override
            public int read(final byte[] b, final int off, final int len) throws IOException {
                if (this.readerClosed)
                    throw new IOException("closed");
                if (b == null)
                    throw new NullPointerException();
                if (off < 0 || len < 0 || len + off > b.length)
                    throw new IndexOutOfBoundsException();
                if (len == 0)
                    return 0;

                int ptr = off;
                final int end = off + len;
                while (ptr < end) {
                    while (this.remainingInCurrentBlock == 0) {
                        moveToNextBlock();
                        if (this.remainingInCurrentBlock == 0)
                            return ptr == off ? -1 : ptr - off;
                    }

                    final int read = Math.min(end - ptr, this.remainingInCurrentBlock);
                    System.arraycopy(this.readDataBlock, this.pos[this.pos.length-1], b, ptr, read);
                    ptr += read;
                    this.remainingInCurrentBlock -= read;
                    this.pos[this.pos.length-1] += read;
                }
                assert ptr == end;
                return len;
            }

            private void moveToNextBlock() throws IOException {
                synchronized (MultiplexOutputStream.this) {
                    final int depth = MultiplexOutputStream.this.innerOut.depth;
                    if (depth == 0)
                        return;
                    if (this.pos.length != depth+1) {
                        seek(getBlocksPos(getPosition()));
                        return;
                    }
                    final int[] newPos = new int[depth+1];

                    if (this.pos[depth] == MultiplexedFileWriter.this.blockSize) {
                        for (int d = depth-1; d >= 0; --d) {
                            if (this.pos[d] == MultiplexedFileWriter.this.blockSize/4) {
                                if (d == 0)
                                    return;
                                newPos[d] = 0;
                            } else {
                                newPos[d] = this.pos[d]+1;
                                if (d > 0)
                                    System.arraycopy(this.pos, 0, newPos, 0, d);
                                break;
                            }
                        }
                    } else {
                        System.arraycopy(this.pos, 0, newPos, 0, this.pos.length);
                    }

                    boolean atEnd = true;
                    final int[] full = MultiplexOutputStream.this.innerOut.full;
                    for (int d = 0; d < depth; ++d) {
                        if (newPos[d] <= full[d]) {
                            atEnd = false;
                            break;
                        }

                        assert newPos[d] == full[d]+1;
                        assert newPos[d+1] == 0;
                        --newPos[d];
                        newPos[d+1] = d == depth-1 ? MultiplexedFileWriter.this.blockSize : MultiplexedFileWriter.this.blockSize/4;
                    }
                    assert !atEnd || newPos[depth] <= full[depth];

                    seek(newPos);
                }
            }

            public long getPosition() {
                if (this.pos.length == 1)
                    return this.pos[0];

                long read = this.pos[0];
                for (int i = 1; i < this.pos.length-1; ++i)
                    read = MultiplexedFileWriter.this.blockSize/4*read + this.pos[i];
                read = MultiplexedFileWriter.this.blockSize*read + this.pos[this.pos.length-1];
                return read;
            }

            @Override
            public int available() {
                return this.remainingInCurrentBlock;
            }

            public int getId() {
                return MultiplexOutputStream.this.getId();
            }

            public long length() {
                return MultiplexOutputStream.this.length();
            }

            @Override
            public void close() {
                if (this.readerClosed)
                    return;
                this.readerClosed = true;
                MultiplexOutputStream.this.innerOut.removeReader(this);
            }

            @Override
            protected void finalize() throws Throwable {
                close();
                super.finalize();
            }
        }

        protected final InnerOutputStream innerOut;

        protected MultiplexOutputStream(final int id) {
            this.innerOut = new InnerOutputStream(id);
        }

        @Override
        public void close() throws IOException {
            this.innerOut.close();
        }

        public int getId() {
            return this.innerOut.getId();
        }

        public Reader getReader(final long pos) throws IOException {
            return this.innerOut.getReader(pos);
        }

        public long length() {
            return this.innerOut.length();
        }

        public void remove() throws IOException {
            this.innerOut.remove();
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
            this.innerOut.write(b, off, len);
        }

        @Override
        public void write(final byte[] b) throws IOException {
            this.innerOut.write(b);
        }

        @Override
        public void write(final int b) throws IOException {
            this.innerOut.write(b);
        }

        public int getBlockSize() {
            return MultiplexedFileWriter.this.blockSize;
        }

    }

    public static final int DEFAULT_BLOCK_SIZE = 1024; // MUST be divisible by 4

    // this is just some random integer
    public static final int MAGIC_HEADER = 0xB7A332B2;

    private static final int headerSize = 21; // bytes

    private static final long POS_INT_MASK = 0xffffffffL;

    private final ByteOrder byteOrder;

    // each mapped slice has 1<<26 = 64M Bytes
    protected static final int MAPPING_SLICE_SIZE_BITS = 26;

    public static final boolean is64bitVM =
           "64".equals(System.getProperty("sun.arch.data.model"))
        || System.getProperty("os.arch", "").contains("64");

    private final RandomAccessFile file;
    protected final FileChannel fileChannel;
    private int fileLengthBlocks;

    private final boolean useMemoryMapping;
    private final boolean autoFlush;
    private final FlushThread autoFlushThread;

    private final AtomicInteger nextBlockAddr = new AtomicInteger(0);

    private final AtomicInteger nextStreamNr = new AtomicInteger(0);

    private final AtomicBoolean firstWrite = new AtomicBoolean(true);

    // may be set when an error occurs asynchronously. is thrown on the next
    // operation on this file.
    protected IOException exception = null;

    // holds all open streams. they still have to be written out on close()
    public final ConcurrentMap<MultiplexOutputStream, InnerOutputStream> openStreams;

    private boolean closed = false;

    protected final MultiplexOutputStream streamDefs;
    protected final MyDataOutputStream streamDefsDataOut;

    protected final int blockSize;

    protected boolean reuseStreamIds = false;
    protected ConcurrentLinkedQueue<Integer> streamIdsToReuse = null;
    protected final ConcurrentSkipListSet<Integer> freeBlocks =
        new ConcurrentSkipListSet<Integer>();

    /**
     * Constructs a new multiplexed file writer with all options available.
     *
     * The whole file can have at most 2^32*blockSize (4 TB for blockSize 1024) bytes.
     *
     * @param filename the name of the file to write the multiplexed streams to
     * @param blockSize the block size of the file (each stream will allocate
     *                  at least <code>blockSize</code> bytes).
     *                  must be divisible by 4, and 1<<31 must be divisible by the
     *                  blockSize.
     * @param useMemoryMapping whether or not to use memory mapping (java.nio package)
     * @param byteOrder the byte order to use to write out block addresses (only used internally)
     *
     * @throws IOException
     */
    public MultiplexedFileWriter(final File filename, final int blockSize,
            final boolean useMemoryMapping, final ByteOrder byteOrder, final boolean autoFlush)
            throws IOException {
        if (filename == null)
            throw new NullPointerException();
        if ((blockSize & 0x3) != 0)
            throw new IllegalArgumentException("blockSize must be dividable by 4");
        if (blockSize <= 0)
            throw new IllegalArgumentException("blockSize must be > 0");
        if ((1 << MAPPING_SLICE_SIZE_BITS) % blockSize != 0)
            throw new IllegalArgumentException("1<<"+MAPPING_SLICE_SIZE_BITS+" must be divisible by the blockSize");

        this.useMemoryMapping = useMemoryMapping;
        this.autoFlush = autoFlush;
        if (autoFlush) {
            this.autoFlushThread = new FlushThread();
            this.autoFlushThread.start();
        } else {
            this.autoFlushThread = null;
        }
        this.byteOrder = byteOrder;
        this.blockSize = blockSize;

        this.file = new RandomAccessFile(filename, "rw");
        this.file.seek(0);
        this.fileChannel = this.file.getChannel();
        // first, reset the file channel
        this.fileChannel.position(0);
        this.fileLengthBlocks = useMemoryMapping ? 0 : Math.max(1000, 10*1024*1024/blockSize);
        this.file.setLength(headerSize+(long)this.fileLengthBlocks*this.blockSize);
        // zero out the magic header
        this.fileChannel.write(ByteBuffer.allocate(headerSize), 0);

        final ConcurrentReferenceHashMap<MultiplexOutputStream, InnerOutputStream> openStreamsTmp = new ConcurrentReferenceHashMap<MultiplexOutputStream, InnerOutputStream>(
            65535, .75f, 16, ReferenceType.WEAK, ReferenceType.STRONG,
            EnumSet.of(Option.IDENTITY_COMPARISONS));
        openStreamsTmp.addRemoveStaleListener(new RemoveStaleListener<InnerOutputStream>() {
            @Override
            public void removed(final InnerOutputStream removedValue) {
                try {
                    removedValue.close();
                } catch (final IOException e) {
                    if (MultiplexedFileWriter.this.exception == null)
                        MultiplexedFileWriter.this.exception = e;
                }
            }
        });

        this.openStreams = openStreamsTmp;
        this.streamDefs = new MultiplexOutputStream(-1);
        this.streamDefsDataOut = new MyDataOutputStream(this.streamDefs);
    }

    /**
     * Uses
     * <ul>
     *  <li>memory mapped files when running inside a 64-bit VM</li>
     *  <li>the native byte order of the system the VM is running on</li>
     * </ul>
     *
     * @see #MultiplexedFileWriter(File, int, boolean, ByteOrder, boolean)
     */
    public MultiplexedFileWriter(final File file, final int blockSize) throws IOException {
        this(file, blockSize, is64bitVM, ByteOrder.nativeOrder(), is64bitVM);
    }

    /**
     * Uses
     * <ul>
     *  <li>the default block size ({@link #DEFAULT_BLOCK_SIZE})</li>
     *  <li>memory mapped files when running inside a 64-bit VM</li>
     *  <li>the native byte order of the system the VM is running on</li>
     * </ul>
     *
     * @see #MultiplexedFileWriter(File, int, boolean, ByteOrder, boolean)
     */
    public MultiplexedFileWriter(final File filename) throws IOException {
        this(filename, DEFAULT_BLOCK_SIZE);
    }

    public MultiplexOutputStream newOutputStream() {
        if (this.closed)
            throw new IllegalStateException(getClass().getSimpleName() + " closed");
        final Integer reusedStreamId = this.reuseStreamIds ? this.streamIdsToReuse.poll() : null;
        final int streamNr = reusedStreamId == null ? this.nextStreamNr.getAndIncrement() : reusedStreamId;
        final MultiplexOutputStream newStream = new MultiplexOutputStream(streamNr);
        this.openStreams.put(newStream, newStream.innerOut);
        return newStream;
    }

    protected int getNewBlockAddress() throws IOException {
        final Integer freeBlock = this.freeBlocks.pollFirst();
        if (freeBlock != null)
            return freeBlock;

        final int newBlockAddr = this.nextBlockAddr.getAndIncrement();
        if (newBlockAddr == 0 && !this.firstWrite.compareAndSet(true, false))
            throw new IOException("Maximum file size reached (length: " +
                    ((1l<<32)*this.blockSize+headerSize) + " bytes)");
        return newBlockAddr;
    }

    protected void writeBlock(final int blockAddr, final byte[] data) throws IOException {
        assert data.length == this.blockSize;
        if (this.useMemoryMapping) {
            final ByteBuffer duplicate = getRawBlockMapping(blockAddr);
            duplicate.put(data, 0, this.blockSize);
        } else {
            ensureFileLength(blockAddr);
            final ByteBuffer buf = ByteBuffer.wrap(data, 0, this.blockSize);
            while (buf.hasRemaining()) {
                this.fileChannel.write(buf,
                        headerSize + ((blockAddr&POS_INT_MASK)*this.blockSize) + buf.position());
            }
        }
    }

    protected void writeBlock(final int blockAddr, final int[] data) throws IOException {
        assert data.length == this.blockSize/4;
        if (this.useMemoryMapping) {
            final ByteBuffer duplicate = getRawBlockMapping(blockAddr).order(this.byteOrder);
            duplicate.asIntBuffer().put(data, 0, this.blockSize/4);
        } else {
            ensureFileLength(blockAddr);
            final ByteBuffer buf = ByteBuffer.allocate(this.blockSize);
            buf.order(this.byteOrder).asIntBuffer().put(data, 0, this.blockSize/4);
            while (buf.hasRemaining()) {
                this.fileChannel.write(buf,
                        headerSize + ((blockAddr&POS_INT_MASK)*this.blockSize) + buf.position());
            }
        }
    }

    protected void readBlock(final int blockAddr, final byte[] buf) throws IOException {
        assert buf.length == this.blockSize;
        if (this.useMemoryMapping) {
            final ByteBuffer mapping = getRawBlockMapping(blockAddr);
            mapping.get(buf, 0, this.blockSize);
        } else {
            final ByteBuffer bbuf = ByteBuffer.wrap(buf, 0, this.blockSize);
            while (bbuf.hasRemaining()) {
                this.fileChannel.read(bbuf,
                        headerSize + ((blockAddr&POS_INT_MASK)*this.blockSize) + bbuf.position());
            }
        }
    }

    protected void readBlock(final int blockAddr, final int[] buf) throws IOException {
        assert buf.length == this.blockSize/4;
        if (this.useMemoryMapping) {
            final ByteBuffer mapping = getRawBlockMapping(blockAddr);
            mapping.order(this.byteOrder).asIntBuffer().get(buf, 0, this.blockSize/4);
        } else {
            final ByteBuffer bbuf = ByteBuffer.allocate(this.blockSize);
            final IntBuffer intBuf = bbuf.order(this.byteOrder).asIntBuffer();
            while (bbuf.hasRemaining()) {
                this.fileChannel.read(bbuf,
                        headerSize + ((blockAddr&POS_INT_MASK)*this.blockSize) + bbuf.position());
            }
            intBuf.get(buf, 0, this.blockSize/4);
        }
    }

    private ByteBuffer getRawBlockMapping(final int blockAddr) throws IOException {
        final long position = (blockAddr&POS_INT_MASK)*this.blockSize;
        final int mappingNr = (int) (position >>> MAPPING_SLICE_SIZE_BITS);
        final int posInMapping = ((int)position) & ((1<<MAPPING_SLICE_SIZE_BITS)-1);
        final ByteBuffer mapping = getMappedSlice(mappingNr);
        final ByteBuffer duplicate = mapping.slice();
        duplicate.position(posInMapping);
        return duplicate;
    }

    private final Object fileMappingsLock = new Object();
    private MappedByteBuffer[] fileMappings = new MappedByteBuffer[1];
    private ByteBuffer getMappedSlice(final int mappingNr) throws IOException {
        assert mappingNr >= 0;
        if (this.fileMappings.length <= mappingNr || this.fileMappings[mappingNr] == null) {
            synchronized (this.fileMappingsLock) {
                if (this.fileMappings.length <= mappingNr) {
                    final MappedByteBuffer[] newMappings = new MappedByteBuffer[2*Math.max(this.fileMappings.length, mappingNr+1)];
                    System.arraycopy(this.fileMappings, 0, newMappings, 0, this.fileMappings.length);
                    this.fileMappings = newMappings;
                }
                if (this.fileMappings[mappingNr] == null) {
                    try {
                        this.fileMappings[mappingNr] = this.fileChannel.map(
                                MapMode.READ_WRITE, headerSize+((long)mappingNr << MAPPING_SLICE_SIZE_BITS),
                                1 << MAPPING_SLICE_SIZE_BITS);
                    } catch (final IOException e) {
                        throw new IOException("Error mapping additional " + (1<<(MAPPING_SLICE_SIZE_BITS-20))
                                + " MB of the trace file: " + e.getMessage(), e.getCause());
                    }
                    if (this.autoFlush)
                        this.autoFlushThread.doFlush(this.fileMappings[mappingNr]);
                }
            }
        }

        return this.fileMappings[mappingNr];
    }

    private void ensureFileLength(final int blockAddr) throws IOException {
        if ((blockAddr & POS_INT_MASK) >= (this.fileLengthBlocks & POS_INT_MASK)) {
            synchronized (this.fileChannel) {
                if ((blockAddr & POS_INT_MASK) >= (this.fileLengthBlocks & POS_INT_MASK)) {
                    this.fileLengthBlocks = (int) Math.min(Integer.MAX_VALUE, Math.max((long)this.fileLengthBlocks*5/4, (long)this.fileLengthBlocks+10*1024*1024/this.blockSize));
                    this.file.setLength(headerSize+(long)this.fileLengthBlocks*this.blockSize);
                }
            }
        }
    }

    private final Object closingLock = new Object();

    public void close() throws IOException {
        checkException();
        synchronized (this.closingLock) {
            if (this.closed)
                return;
            this.closed = true;

            for (final MultiplexOutputStream str: this.openStreams.keySet())
                str.close();
            this.openStreams.clear();

            this.streamDefs.close();
            int streamDefsStartBlock = this.streamDefs.innerOut.startBlockAddr;

            if (this.autoFlush)
                this.autoFlushThread.interrupt();

            System.out.println("Starting compaction...");
            long startTime = System.nanoTime();
            int newBlockCount = this.nextBlockAddr.get();
            final int numFreeBlocks = this.freeBlocks.size();
            if (numFreeBlocks > 0 && (this.blockSize & 15) == 0) {
                newBlockCount -= numFreeBlocks;
                streamDefsStartBlock = compactStream(streamDefsStartBlock, this.streamDefs.length(), newBlockCount);
                final long numStreams = this.streamDefs.length()/16;
                final int depth = this.streamDefs.innerOut.depth;
                final int[][] pointerBlocks = new int[depth][this.blockSize/4];
                final byte[] dataBlock = new byte[this.blockSize];
                final int[] pos = new int[depth+1];
                boolean changed = false;
                for (int d = 0; d < depth; ++d)
                    readBlock(d == 0 ? streamDefsStartBlock : pointerBlocks[d-1][0], pointerBlocks[d]);
                readBlock(depth == 0 ? streamDefsStartBlock : pointerBlocks[depth-1][0], dataBlock);
                final MyByteArrayInputStream dataBlockIn = new MyByteArrayInputStream(dataBlock);
                final MyDataInputStream dataBlockDataIn = new MyDataInputStream(dataBlockIn);
                final MyByteArrayOutputStream dataBlockOut = new MyByteArrayOutputStream(dataBlock);
                final MyDataOutputStream dataBlockDataOut = new MyDataOutputStream(dataBlockOut);
                for (long l = 0; l < numStreams; ++l) {
                    if (pos[depth] == this.blockSize) {
                        if (changed) {
                            writeBlock(pointerBlocks[depth-1][pos[depth-1]], dataBlock);
                            changed = false;
                        }
                        for (int d = depth-1; d >= 0; --d) {
                            if (pos[d] + 1 < this.blockSize/4) {
                                ++pos[d];
                                for (int du = d+1; du < depth; ++du) {
                                    readBlock(pointerBlocks[du-1][pos[du-1]], pointerBlocks[du]);
                                    pos[du] = 0;
                                }
                                readBlock(pointerBlocks[depth-1][pos[depth-1]], dataBlock);
                                pos[depth] = 0;
                                break;
                            }
                        }
                    }
                    assert pos[depth] < this.blockSize;
                    dataBlockIn.seek(pos[depth]+4);
                    final int startBlock = dataBlockDataIn.readInt();
                    final long length = dataBlockDataIn.readLong();
                    final int newStartBlock = compactStream(startBlock, length, newBlockCount);
                    if (newStartBlock != startBlock) {
                        changed = true;
                        dataBlockOut.seek(pos[depth]+4);
                        dataBlockDataOut.writeInt(newStartBlock);
                    }
                    pos[depth] += 16;
                }
            }
            long endTime = System.nanoTime();
            System.out.format("Compaction took %.3f seconds%n", 1e-9*(endTime - startTime));

            System.out.println("Forcing mappings out...");
            startTime = System.nanoTime();
            int runs = 0;
            // erase references to mapped file regions
            synchronized (this.fileMappingsLock) {
                // bug 4938372 requires us to force writing out all changes in the mappings
                while (true) {
                    try {
                        ++runs;
                        flush0(null);
                        break;
                    } catch (final IOException e) {
                        // bug 6539707: force() DOES throw an IOException in some cases
                        // ignore if it is that odd error message
                        if (e.getMessage() == null || !e.getMessage().contains("another process has locked"))
                            throw e;
                    }
                }
                this.fileMappings = null;
            }
            endTime = System.nanoTime();
            System.out.format("Forcing took %.3f seconds, %d runs%n", 1e-9*(endTime - startTime), runs);

            System.out.println("Truncating file...");
            startTime = System.nanoTime();
            runs=0;
            // and (possibly) truncate the file
            // WARNING: this is a really bad hack!
            // there is a severe bug (4724038) in jdk that files whose content is still mapped
            // cannot be truncated, but there is no way to unmap file content.
            // so we have to rely on the garbage collector to remove the mappings...
            LinkedList<byte[]> memoryConsumingList = new LinkedList<byte[]>();
            while (true) {
                try {
                    ++runs;
                    System.gc();
                    this.fileChannel.truncate(headerSize+(long)newBlockCount*this.blockSize);
                    // if there was no exception, then break this loop
                    break;
                } catch (final IOException e) {
                    // if the error has nothing to do with open mapped sections, then throw it
                    if (e.getMessage() == null || !e.getMessage().contains("user-mapped"))
                        throw e;
                    // consume some more memory to motivate the garbage collector to clear the mappings
                    memoryConsumingList.add(new byte[1024*1024]); // consumes 1 MB
                }
            }
            memoryConsumingList = null;
            endTime = System.nanoTime();
            System.out.format("Truncation took %.3f seconds, %d runs%n", 1e-9*(endTime - startTime), runs);

            // write some meta information to the file to make it valid
            final ByteBuffer header = ByteBuffer.allocate(headerSize);
            header.putInt(MAGIC_HEADER);
            header.putInt(this.blockSize);
            header.put(this.byteOrder == ByteOrder.BIG_ENDIAN ? (byte)0 : (byte)1);
            header.putInt(streamDefsStartBlock);
            header.putLong(this.streamDefs.innerOut.dataLength);
            header.position(0);
            this.fileChannel.write(header, 0);

            System.out.println("Closing file...");
            startTime = System.nanoTime();
            this.fileChannel.close();
            this.file.close();
            endTime = System.nanoTime();
            System.out.format("Closing took %.3f seconds%n", 1e-9*(endTime - startTime));
            checkException();
        }
    }

    private int compactStream(final int streamStartBlock, final long streamLength, final int newBlockCount) throws IOException {
        final int numBlocks = (int)divUp(streamLength, this.blockSize);

        int depth = 0;
        long max = this.blockSize;
        while (max < streamLength) {
            ++depth;
            max *= this.blockSize/4;
        }

        int newStartBlock = streamStartBlock;
        final int[][] pointerBlocks = new int[depth][this.blockSize/4];
        final int[] pos = new int[depth];
        final boolean[] changed = new boolean[depth];
        if (depth > 0) {
            readBlock(streamStartBlock, pointerBlocks[0]);
            if (streamStartBlock >= newBlockCount) {
                newStartBlock = this.freeBlocks.pollFirst();
                changed[0] = true;
            }
            for (int d = 0; d < depth-1; ++d) {
                final int blockAddr = pointerBlocks[d][0];
                readBlock(blockAddr, pointerBlocks[d+1]);
                if (blockAddr >= newBlockCount) {
                    final int newAddr = this.freeBlocks.pollFirst();
                    changed[d+1] = true;
                    pointerBlocks[d][0] = newAddr;
                    changed[d] = true;
                }
            }
            for (long l = 0; l < numBlocks; ++l) {
                if (pos[depth-1] == this.blockSize/4) {
                    for (int d = depth-1; d >= 0; --d) {
                        if (pos[d] + 1 < this.blockSize/4) {
                            ++pos[d];
                            for (int du = d+1; du < depth; ++du) {
                                final int blockAddr = pointerBlocks[du-1][pos[du-1]];
                                readBlock(blockAddr, pointerBlocks[du]);
                                if (blockAddr >= newBlockCount) {
                                    final int newAddr = this.freeBlocks.pollFirst();
                                    changed[du] = true;
                                    pointerBlocks[du-1][pos[du-1]] = newAddr;
                                    changed[du-1] = true;
                                }
                                pos[du] = 0;
                            }
                            break;
                        }

                        if (d == 0)
                            throw new RuntimeException("should not get here");
                        if (changed[d]) {
                            writeBlock(pointerBlocks[d-1][pos[d-1]], pointerBlocks[d]);
                            changed[d] = false;
                        }
                    }
                }
                final int blockAddr = pointerBlocks[depth-1][pos[depth-1]];
                if (blockAddr >= newBlockCount) {
                    final int newAddr = this.freeBlocks.pollFirst();
                    transferBlock(blockAddr, newAddr);
                    pointerBlocks[depth-1][pos[depth-1]] = newAddr;
                    changed[depth-1] = true;
                }
                ++pos[depth-1];
            }
            for (int i = 0; i < depth; ++i) {
                if (changed[i]) {
                    writeBlock(i == 0 ? newStartBlock : pointerBlocks[i-1][pos[i-1]], pointerBlocks[i]);
                }
            }
        } else if (streamStartBlock >= newBlockCount) {
            newStartBlock = this.freeBlocks.pollFirst();
            transferBlock(streamStartBlock, newStartBlock);
        }

        return newStartBlock;
    }

    private void transferBlock(final int oldAddr, final int newAddr) throws IOException {
        assert oldAddr < this.nextBlockAddr.get() && newAddr < this.nextBlockAddr.get();
        if (this.useMemoryMapping) {
            final ByteBuffer oldMapping = getRawBlockMapping(oldAddr);
            oldMapping.limit(oldMapping.position() + this.blockSize);
            final ByteBuffer newMapping = getRawBlockMapping(newAddr);
            newMapping.put(oldMapping);
        } else {
            long oldPos = headerSize + (oldAddr&POS_INT_MASK)*this.blockSize;
            final long newPos = headerSize + (newAddr&POS_INT_MASK)*this.blockSize;
            int count = this.blockSize;
            this.fileChannel.position(newPos);
            while (count > 0) {
                final int newTransfered = (int) this.fileChannel.transferTo(oldPos, count, this.fileChannel);
                count -= newTransfered;
                oldPos += newTransfered;
            }
        }
    }

    private synchronized void checkException() throws IOException {
        if (this.exception != null) {
            final IOException e = this.exception;
            this.exception = null;
            throw e;
        }
    }

    /**
     * Sets whether the {@link MultiplexedFileWriter} should reuse stream ids whose
     * streams have been {@link MultiplexOutputStream#remove()}d.
     *
     * Note: This method should only be called before any streams have been removed!
     *
     * @return the previous value of reuseStreamIds
     */
    public boolean setReuseStreamIds(final boolean val) {
        final boolean oldVal = this.reuseStreamIds;
        if (val && this.streamIdsToReuse == null)
            this.streamIdsToReuse = new ConcurrentLinkedQueue<Integer>();
        this.reuseStreamIds = val;
        return oldVal;
    }

    protected static long divUp(final long a, final int b) {
        return (a+b-1)/b;
    }
    protected static int divUp(final int a, final int b) {
        return (a+b-1)/b;
    }

    /**
     * Forces outstanding changes to be written to the disk. Most usefull if using
     * memory mapping. This operation would force all changes in the memory sections
     * to be written to the file.
     *
     * @throws  IOException
     *          If some I/O error occurs
     */
    public void flush() throws IOException {
        if (this.autoFlush) {
            this.autoFlushThread.doFlush(null);
        } else {
            flush0(null);
        }
    }

    protected void flush0(final MappedByteBuffer omit) throws IOException {
        if (this.useMemoryMapping) {
            MappedByteBuffer[] buffers;
            synchronized (this.fileMappingsLock) {
                buffers = new MappedByteBuffer[this.fileMappings.length];
                System.arraycopy(this.fileMappings, 0, buffers, 0, this.fileMappings.length);
            }
            for (final MappedByteBuffer buf: buffers) {
                if (buf != null && buf != omit) {
                    buf.force();
                }
            }
        } else {
            this.fileChannel.force(false);
        }
    }

}
