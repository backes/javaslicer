package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.Option;
import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.ReferenceType;
import de.unisb.cs.st.javaslicer.tracer.util.ConcurrentReferenceHashMap.RemoveStaleListener;
import de.unisb.cs.st.javaslicer.tracer.util.MultiplexedFileWriter.MultiplexOutputStream.InnerOutputStream;

public class MultiplexedFileWriter {

    public class MultiplexOutputStream extends OutputStream {

        protected class InnerOutputStream extends OutputStream {

            private final int id;
            protected int depth = 0;
            protected long dataLength = 0;
            protected int startBlockAddr = 0; // is set on close()
            protected byte[][] dataBlocks = new byte[MultiplexedFileWriter.this.maxDepth+1][];
            protected int[] full = new int[MultiplexedFileWriter.this.maxDepth+1];
            private volatile boolean streamClosed = false;
            private ArrayList<Reader> readers = null;

            public InnerOutputStream(final int id) {
                this.id = id;
                this.dataBlocks[0] = new byte[MultiplexedFileWriter.this.blockSize];
            }

            @Override
            public void write(final int b) throws IOException {
                if (this.streamClosed)
                    throw new IOException("stream closed");
                if (this.full[this.depth] == MultiplexedFileWriter.this.blockSize) {
                    // current block is full...
                    moveToNextBlock();
                }
                this.dataBlocks[this.depth][this.full[this.depth]++] = (byte) b;
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                if (this.streamClosed)
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
                    if (this.full[this.depth] == MultiplexedFileWriter.this.blockSize) {
                        // current block is full...
                        moveToNextBlock();
                    }
                    final int write = Math.min(end - pos,
                            MultiplexedFileWriter.this.blockSize-this.full[this.depth]);
                    System.arraycopy(b, pos, this.dataBlocks[this.depth], this.full[this.depth], write);
                    pos += write;
                    this.full[this.depth] += write;
                }
            }

            private void moveToNextBlock() throws IOException {
                this.dataLength += MultiplexedFileWriter.this.blockSize;

                if (!writeBack(this.depth)) {
                    // could not write back directly, so we have to increase the depth
                    increaseDepth();
                    if (!writeBack(this.depth))
                        throw new RuntimeException("Increase of tree depth did not work as expected");
                }
            }

            private void increaseDepth() throws IOException {
                if (this.depth == MultiplexedFileWriter.this.maxDepth)
                    throw new IOException("Maximum stream length reached");
                // the depth of all entries except the first one is increased
                for (int i = this.depth; i >= 0; --i)
                    this.dataBlocks[i+1] = this.dataBlocks[i];
                this.dataBlocks[0] = new byte[MultiplexedFileWriter.this.blockSize];
                System.arraycopy(this.full, 0, this.full, 1, this.depth+1);
                this.full[0] = 0;
                this.depth++;
            }

            private boolean writeBack(final int level) throws IOException {
                // we cannot write back level 0 (have to increase depth)
                if (level == 0)
                    return false;

                // if the next lower level is full too, we first have to write back this level
                if (this.full[level-1] == MultiplexedFileWriter.this.blockSize && !writeBack(level-1))
                    return false;

                // now write back the data block
                final int newDataBlockNr = writeBlock(this.dataBlocks[level]);

                writeInt(this.dataBlocks[level-1], this.full[level-1], newDataBlockNr);
                this.full[level-1] += 4;
                this.full[level] = 0;
                if (this.readers != null) {
                    for (final Reader reader: this.readers) {
                        reader.initialize(reader.getPosition());
                    }
                }
                return true;
            }

            public int getId() {
                return this.id;
            }

            @Override
            public synchronized void close() throws IOException {
                if (this.streamClosed)
                    return;
                this.streamClosed = true;

                if (this.readers != null)
                    for (final Reader reader: this.readers)
                        reader.close();

                this.dataLength += this.full[this.depth];

                if (this.depth > 0) {
                    if (this.full[0] == MultiplexedFileWriter.this.blockSize)
                        increaseDepth();

                    outer:
                        while (true) {
                            for (int i = 1; i < this.depth; ++i) {
                                if (this.full[i] == MultiplexedFileWriter.this.blockSize) {
                                    if (!writeBack(i))
                                        throw new RuntimeException("writeBack in close() should always succeed");
                                    if (this.full[0] == MultiplexedFileWriter.this.blockSize)
                                        increaseDepth();
                                    continue outer;
                                }
                            }
                            break;
                        }

                    for (int i = this.depth; i > 0; --i) {
                        // zero out the remaining part of the block
                        Arrays.fill(this.dataBlocks[i], this.full[i], MultiplexedFileWriter.this.blockSize, (byte)0);
                        if (!writeBack(i))
                            throw new RuntimeException("writeBack in close() should always succeed");
                    }
                }

                this.startBlockAddr = writeBlock(this.dataBlocks[0]);

                // now we can release most buffers
                this.dataBlocks = null;
                this.full = null;

                // after all this work, store the information about this stream to the streamDefs stream
                if (this != MultiplexedFileWriter.this.streamDefs.innerOut) {
                    synchronized (MultiplexedFileWriter.this.streamDefsDataOut) {
                        MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.id);
                        MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.startBlockAddr);
                        MultiplexedFileWriter.this.streamDefsDataOut.writeLong(this.dataLength);
                    }
                }
            }

            public long length() {
                return this.full == null ? this.dataLength : this.dataLength + this.full[this.depth];
            }

            public Reader getReader(final long pos) throws IOException {
                final Reader reader = new Reader(pos);
                if (this.readers == null) {
                    this.readers = new ArrayList<Reader>(2);
                }
                this.readers.add(reader);
                return reader;
            }

            protected void removeReader(final Reader reader) {
                if (this.readers != null) {
                    synchronized (this.readers) {
                        this.readers.remove(reader);
                    }
                }
            }

            public void remove() throws IOException {
                if (this.streamClosed)
                    throw new IOException("a closed stream cannot be removed any more");
                this.streamClosed = true;

                if (this.readers != null)
                    for (final Reader reader: this.readers)
                        reader.close();

                if (this.depth > 0) {
                    int noBlocks = (int)divUp(length(), MultiplexedFileWriter.this.blockSize);
                    int tmp = noBlocks; // no data blocks
                    while (tmp > 1) {
                        tmp = divUp(tmp, MultiplexedFileWriter.this.blockSize/4);
                        noBlocks += tmp;
                    }
                    noBlocks -= this.depth + 1;

                    releaseBlocks:
                        while (true) {
                            while (this.full[this.depth-1] > 0) {
                                this.full[this.depth-1] -= 4;
                                --noBlocks;
                                MultiplexedFileWriter.this.freeBlocks.add(readInt(this.dataBlocks[this.depth-1], this.full[this.depth-1]));
                            }
                            for (int i = this.depth-2; i >= 0; --i) {
                                if (this.full[i] > 0) {
                                    this.full[i] -= 4;
                                    final int blockAddr = readInt(this.dataBlocks[i], this.full[i]);
                                    readBlock(blockAddr, this.dataBlocks[i+1]);
                                    --noBlocks;
                                    MultiplexedFileWriter.this.freeBlocks.add(blockAddr);
                                    this.full[i+1] = MultiplexedFileWriter.this.blockSize;
                                    continue releaseBlocks;
                                }
                            }
                            break;
                        }

                    assert noBlocks == 0;
                }

                this.dataLength = 0;

                // now we can release most buffers
                this.dataBlocks = null;
                this.full = null;

                if (MultiplexedFileWriter.this.reuseStreamIds)
                    MultiplexedFileWriter.this.streamIdsToReuse.add(this.id);
            }

        }

        /**
         * An InputStream that works directly on the {@link MultiplexOutputStream},
         * even while it is written.
         *
         * Caution: The classes are not thread-safe, so you can write to the {@link MultiplexOutputStream}
         * and read from this {@link Reader} interleaved, but not concurrently.
         *
         * @author Clemens Hammacher
         */
        public class Reader extends InputStream {

            private byte[][] readDataBlocks;
            private int[] pos;
            private int remainingInCurrentBlock;
            private boolean readerClosed = false;

            protected Reader() throws IOException {
                this(0);
            }

            protected Reader(final long pos) throws IOException {
                initialize(pos);
            }

            protected void initialize(final long initPos) throws IOException {
                if (this.readDataBlocks == null || this.readDataBlocks.length != MultiplexOutputStream.this.innerOut.depth+1)
                    this.readDataBlocks = new byte[MultiplexOutputStream.this.innerOut.depth+1][];

                if (this.pos == null || this.pos.length != MultiplexOutputStream.this.innerOut.depth+1)
                    this.pos = new int[MultiplexOutputStream.this.innerOut.depth+1];
                seek(initPos, true);
            }

            public void seek(final long toPos) throws IOException {
                if (this.readerClosed)
                    throw new IOException("closed");
                seek(toPos, false);
            }

            private void seek(final long toPos, final boolean initial) throws IOException {
                final long streamLength = MultiplexOutputStream.this.length();

                if (toPos < 0 || toPos > streamLength)
                    throw new IOException("pos must be in the range 0 .. <stream length>");

                final int[] newPos = getBlocksPos(toPos);
                // read the data blocks
                if (initial) {
                    this.readDataBlocks[0] = MultiplexOutputStream.this.innerOut.dataBlocks[0];
                }
                for (int i = 0; i < MultiplexOutputStream.this.innerOut.depth; ++i) {
                    if (newPos[i] != this.pos[i] || initial ||
                            (this.pos[i] == MultiplexedFileWriter.this.blockSize && newPos[i] != MultiplexedFileWriter.this.blockSize)) {
                        if (newPos[i] == MultiplexOutputStream.this.innerOut.full[i]) {
                            this.readDataBlocks[i+1] = MultiplexOutputStream.this.innerOut.dataBlocks[i+1];
                        } else if (newPos[i] != MultiplexedFileWriter.this.blockSize)  {
                            this.readDataBlocks[i+1] = new byte[MultiplexedFileWriter.this.blockSize];
                            readBlock(readInt(this.readDataBlocks[i], newPos[i]), this.readDataBlocks[i+1]);
                        }
                        this.pos[i] = newPos[i];
                    }
                }
                this.pos[MultiplexOutputStream.this.innerOut.depth] = newPos[MultiplexOutputStream.this.innerOut.depth];

                this.remainingInCurrentBlock = (int) Math.min(streamLength-toPos,
                        MultiplexedFileWriter.this.blockSize-this.pos[MultiplexOutputStream.this.innerOut.depth]);
            }

            private int[] getBlocksPos(final long position) throws IOException {
                if (position == length() && position > 0) {
                    final int[] newPos = getBlocksPos(position-1);
                    ++newPos[MultiplexOutputStream.this.innerOut.depth];
                    return newPos;
                }

                final int[] newPos = new int[MultiplexOutputStream.this.innerOut.depth+1];
                long remaining;
                if (MultiplexOutputStream.this.innerOut.depth > 0) {
                    newPos[MultiplexOutputStream.this.innerOut.depth] = (int) (position % MultiplexedFileWriter.this.blockSize);
                    remaining = position / MultiplexedFileWriter.this.blockSize * 4;
                } else {
                    remaining = position;
                }
                for (int d = MultiplexOutputStream.this.innerOut.depth-1; d > 0; --d) {
                    newPos[d] = (int) ((remaining-1) % MultiplexedFileWriter.this.blockSize) + 1;
                    remaining = remaining / MultiplexedFileWriter.this.blockSize * 4;
                }
                if (remaining > MultiplexedFileWriter.this.blockSize)
                    throw new IOException("internal error");
                newPos[0] = (int) remaining;
                return newPos;
            }

            @Override
            public int read() throws IOException {
                if (this.readerClosed)
                    throw new IOException("closed");
                if (this.remainingInCurrentBlock == 0) {
                    seek(getPosition(), false);
                    if (this.remainingInCurrentBlock == 0)
                        return -1;
                }
                --this.remainingInCurrentBlock;
                return this.readDataBlocks[MultiplexOutputStream.this.innerOut.depth][this.pos[MultiplexOutputStream.this.innerOut.depth]++] & 0xff;
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
                        final long position = getPosition();
                        seek(position, false);
                        if (this.remainingInCurrentBlock == 0)
                            return ptr == off ? -1 : ptr - off;
                    }

                    final int read = Math.min(end - ptr, this.remainingInCurrentBlock);
                    System.arraycopy(this.readDataBlocks[MultiplexOutputStream.this.innerOut.depth], this.pos[MultiplexOutputStream.this.innerOut.depth], b, ptr, read);
                    ptr += read;
                    this.remainingInCurrentBlock -= read;
                    this.pos[MultiplexOutputStream.this.innerOut.depth] += read;
                }
                assert ptr == end;
                return len;
            }

            public long getPosition() {
                long read = this.pos[0];
                for (int i = 1; i <= MultiplexOutputStream.this.innerOut.depth; ++i)
                    read = MultiplexedFileWriter.this.blockSize/4*read + this.pos[i];
                return read;
            }

            @Override
            public int available() throws IOException {
                return (int) Math.min(Integer.MAX_VALUE, length() - getPosition());
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

    private static final int DEFAULT_BLOCK_SIZE = 1024; // MUST be divideable by 4

    private static final int DEFAULT_MAX_DEPTH = 5;

    // this is just some random integer
    public static final int MAGIC_HEADER = 0xB7A332B2;

    protected static final int headerSize = 20; // bytes

    private static final long POS_INT_MASK = 0xffffffffL;

    protected final RandomAccessFile file;

    private final AtomicInteger nextBlockAddr = new AtomicInteger(0);

    private final AtomicInteger nextStreamNr = new AtomicInteger(0);

    // may be set when an error occurs asynchronously. is thrown on the next
    // operation on this file.
    protected IOException exception = null;

    // holds all open streams. they still have to be written out on close()
    public final ConcurrentMap<MultiplexOutputStream, InnerOutputStream> openStreams;

    private boolean closed = false;

    protected final MultiplexOutputStream streamDefs;
    protected final MyDataOutputStream streamDefsDataOut;

    protected final int blockSize;
    protected final int maxDepth;

    protected boolean reuseStreamIds = false;
    protected ConcurrentLinkedQueue<Integer> streamIdsToReuse = null;
    protected final ConcurrentSkipListSet<Integer> freeBlocks =
        new ConcurrentSkipListSet<Integer>();

    /**
     * Constructs a new multiplexed file writer with all options available.
     *
     * Each stream will have a limit of <code>blockSize*((blockSize/4)^maxDepth)</code>
     * (1 PB (1024 TB) for blockSize 1024 and maxDepth 5) bytes.
     *
     * The whole file can have at most 2^32*blockSize (4 TB for blockSize 1024) bytes.
     *
     * @param file the file to write the multiplexed streams to
     * @param blockSize the block size of the file (each stream will allocate
     *                  at least <code>blockSize</code> bytes)
     * @param maxDepth the maximum depth of the internal tree to the data blocks
     * @throws IOException
     */
    public MultiplexedFileWriter(final RandomAccessFile file, final int blockSize, final int maxDepth) throws IOException {
        if (file == null)
            throw new NullPointerException();
        if ((blockSize & 0x3) != 0)
            throw new IllegalArgumentException("blockSize must be dividable by 4");
        if (blockSize <= 0)
            throw new IllegalArgumentException("blockSize must be > 0");
        if (maxDepth < 0)
            throw new IllegalArgumentException("maxDepth must be >= 0");

        // first, reset the file
        file.seek(0);
        file.setLength(headerSize);
        // this replaces no seek, but writes 0 to the magic header
        for (int i = 0; i < headerSize; ++i)
            file.writeByte(0);

        this.file = file;
        this.blockSize = blockSize;
        this.maxDepth = maxDepth;
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
     * @see #MultiplexedFileWriter(RandomAccessFile, int, int)
     */
    public MultiplexedFileWriter(final RandomAccessFile file) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE, DEFAULT_MAX_DEPTH);
    }

    /**
     * @see #MultiplexedFileWriter(RandomAccessFile, int, int)
     */
    public MultiplexedFileWriter(final File filename) throws IOException {
        this(new RandomAccessFile(filename, "rw"));
    }

    /**
     * @see #MultiplexedFileWriter(RandomAccessFile, int, int)
     */
    public MultiplexedFileWriter(final File filename, final int blockSize, final int maxDepth) throws IOException {
        this(new RandomAccessFile(filename, "rw"), blockSize, maxDepth);
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

    protected int writeBlock(final byte[] data) throws IOException {
        final int newBlockAddr;
        final Integer freeBlock = this.freeBlocks.pollFirst();
        if (freeBlock != null) {
            newBlockAddr = freeBlock;
        } else {
            newBlockAddr = this.nextBlockAddr.getAndIncrement();
            if (newBlockAddr == 0 && this.file.length() > headerSize)
                throw new IOException("Maximum file size reached (length: " + this.file.length() + " bytes)");
        }
        writeBlock(newBlockAddr, data);
        return newBlockAddr;
    }

//    private final IntegerMap<Boolean> written = new IntegerMap<Boolean>();
    protected synchronized void writeBlock(final int blockAddr, final byte[] data) throws IOException {
//        if (this.written.get(blockAddr) != null)
//            throw new RuntimeException("same block written twice");
//        this.written.put(blockAddr, Boolean.TRUE);
        this.file.seek(headerSize + (blockAddr&POS_INT_MASK)*this.blockSize);
        this.file.write(data, 0, this.blockSize);
    }

    public synchronized void readBlock(final int blockAddr, final byte[] buf) throws IOException {
        this.file.seek(headerSize + (blockAddr&POS_INT_MASK)*this.blockSize);
        this.file.readFully(buf, 0, this.blockSize);
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

            final int numFreeBlocks = this.freeBlocks.size();
            int newBlockCount = this.nextBlockAddr.get();
            if (numFreeBlocks > 0 && (this.blockSize & 15) == 0) {
                newBlockCount -= numFreeBlocks;
                streamDefsStartBlock = compactStream(streamDefsStartBlock, this.streamDefs.length(), newBlockCount);
                final long numStreams = this.streamDefs.length()/16;
                final int depth = this.streamDefs.innerOut.depth;
                final byte[][] block = new byte[depth+1][this.blockSize];
                final int[] pos = new int[depth+1];
                boolean changed = false;
                readBlock(streamDefsStartBlock, block[0]);
                for (int d = 0; d < depth; ++d)
                    readBlock(readInt(block[d], 0), block[d+1]);
                for (long l = 0; l < numStreams; ++l) {
                    if (pos[depth] == this.blockSize) {
                        if (changed) {
                            writeBlock(readInt(block[depth-1], pos[depth-1]), block[depth]);
                            changed = false;
                        }
                        for (int d = depth-1; d >= 0; --d) {
                            if (pos[d] + 4 < this.blockSize) {
                                pos[d] += 4;
                                for (int du = d+1; du <= depth; ++du) {
                                    readBlock(readInt(block[du-1], pos[du-1]), block[du]);
                                    pos[du] = 0;
                                }
                                break;
                            }
                        }
                    }
                    assert pos[depth] < this.blockSize;
                    final int startBlock = readInt(block[depth], pos[depth]+4);
                    final long length = ((long)readInt(block[depth], pos[depth]+8) << 32)
                        | (readInt(block[depth], pos[depth]+12)&POS_INT_MASK);
                    final int newStartBlock = compactStream(startBlock, length, newBlockCount);
                    if (newStartBlock != startBlock) {
                        changed = true;
                        writeInt(block[depth], pos[depth]+4, newStartBlock);
                    }
                    pos[depth] += 16;
                }
            }

            // write some meta information to the file
            this.file.seek(0);
            this.file.writeInt(MAGIC_HEADER);
            this.file.writeInt(this.blockSize);
            this.file.writeInt(streamDefsStartBlock);
            this.file.writeLong(this.streamDefs.innerOut.dataLength);

            // and (possibly) truncate the file
            this.file.setLength(headerSize+(long)newBlockCount*this.blockSize);

            this.file.close();
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
        final byte[][] block = new byte[depth][this.blockSize];
        byte[] tmpBlock = null;
        final int[] pos = new int[depth];
        final boolean[] changed = new boolean[depth];
        if (depth > 0) {
            readBlock(streamStartBlock, block[0]);
            if (streamStartBlock >= newBlockCount) {
                newStartBlock = this.freeBlocks.pollFirst();
                changed[0] = true;
            }
            for (int d = 0; d < depth-1; ++d) {
                final int blockAddr = readInt(block[d], 0);
                readBlock(blockAddr, block[d+1]);
                if (blockAddr >= newBlockCount) {
                    final int newAddr = this.freeBlocks.pollFirst();
                    changed[d+1] = true;
                    writeInt(block[d], 0, newAddr);
                    changed[d] = true;
                }
            }
            for (long l = 0; l < numBlocks; ++l) {
                if (pos[depth-1] == this.blockSize) {
                    for (int d = depth-1; d >= 0; --d) {
                        if (pos[d] + 4 < this.blockSize) {
                            pos[d] += 4;
                            for (int du = d+1; du < depth; ++du) {
                                final int blockAddr = readInt(block[du-1], pos[du-1]);
                                readBlock(blockAddr, block[du]);
                                if (blockAddr >= newBlockCount) {
                                    final int newAddr = this.freeBlocks.pollFirst();
                                    changed[du] = true;
                                    writeInt(block[du-1], pos[du-1], newAddr);
                                    changed[du-1] = true;
                                }
                                pos[du] = 0;
                            }
                            break;
                        } else {
                            if (d == 0)
                                throw new RuntimeException("should not get here");
                            if (changed[d]) {
                                writeBlock(readInt(block[d-1], pos[d-1]), block[d]);
                                changed[d] = false;
                            }
                        }
                    }
                }
                assert pos[depth-1] < this.blockSize;
                final int blockAddr = readInt(block[depth-1], pos[depth-1]);
                if (blockAddr >= newBlockCount) {
                    final int newAddr = this.freeBlocks.pollFirst();
                    readBlock(blockAddr, tmpBlock == null ? tmpBlock = new byte[this.blockSize] : tmpBlock);
                    writeBlock(newAddr, tmpBlock);
                    writeInt(block[depth-1], pos[depth-1], newAddr);
                    changed[depth-1] = true;
                }
                pos[depth-1] += 4;
            }
            for (int i = 0; i < depth; ++i) {
                if (changed[i]) {
                    writeBlock(i == 0 ? newStartBlock : readInt(block[i-1], pos[i-1]), block[i]);
                }
            }
        } else if (streamStartBlock >= newBlockCount) {
            tmpBlock = new byte[this.blockSize];
            readBlock(streamStartBlock, tmpBlock);
            newStartBlock = this.freeBlocks.pollFirst();
            writeBlock(newStartBlock, tmpBlock);
        }

        return newStartBlock;
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

    public static void writeInt(final byte[] buf, final int pos, final int value) {
        buf[pos] = (byte) (value >>> 24);
        buf[pos+1] = (byte) (value >>> 16);
        buf[pos+2] = (byte) (value >>> 8);
        buf[pos+3] = (byte) value;
    }

    public static int readInt(final byte[] buf, final int offset) {
        return ((buf[offset] & 0xff) << 24)
            | ((buf[offset+1] & 0xff) << 16)
            | ((buf[offset+2] & 0xff) << 8)
            | (buf[offset+3] & 0xff);
    }

    public static long divUp(final long a, final int b) {
        return (a+b-1)/b;
    }
    public static int divUp(final int a, final int b) {
        return (a+b-1)/b;
    }

}
