package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;

public class MultiplexedFileWriter {

    public class MultiplexOutputStream extends OutputStream {

        private final int id;
        private int depth = 0;
        protected long dataLength = 0;
        protected int startBlockAddr = 0; // is set on close()
        private byte[][] dataBlocks = new byte[MultiplexedFileWriter.this.maxDepth+1][];
        private int[] full = new int[MultiplexedFileWriter.this.maxDepth+1];
        private volatile boolean streamClosed = false;

        protected MultiplexOutputStream(final int id) {
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

            if (this.full[this.depth] == MultiplexedFileWriter.this.blockSize) {
                // current block is full...
                moveToNextBlock();
            }
            int pos = off;
            final int end = off + len;
            while (true) {
                final int write = Math.min(end - pos,
                    MultiplexedFileWriter.this.blockSize-this.full[this.depth]);
                System.arraycopy(b, pos, this.dataBlocks[this.depth], this.full[this.depth], write);
                pos += write;
                if (pos < end) {
                    moveToNextBlock();
                } else {
                    this.full[this.depth] += write;
                    break;
                }
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
            return true;
        }

        private void writeInt(final byte[] buf, final int pos, final int value) {
            buf[pos] = (byte) (value >>> 24);
            buf[pos+1] = (byte) (value >>> 16);
            buf[pos+2] = (byte) (value >>> 8);
            buf[pos+3] = (byte) value;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public synchronized void close() throws IOException {
            if (this.streamClosed)
                return;
            this.streamClosed = true;

            this.dataLength += this.full[this.depth];

            // write out procedure:
            // 1) if depth is > 0 and level 0 is full, increase the depth
            // 2) write back all levels that are full, except the deepest one, starting from 1 (upwards)
            // 3) write back all levels (downwards), starting with the deepest level, ending on 1
            // 4) write back level 0 (manually) and store block nr in startBlockAddr

            if (this.depth > 0 && this.full[0] == MultiplexedFileWriter.this.blockSize)
                increaseDepth();

            for (int i = 1; i < this.depth; ++i)
                if (this.full[i] == MultiplexedFileWriter.this.blockSize)
                    if (!writeBack(i))
                        throw new RuntimeException("writeBack in close() should always succeed");
            for (int i = this.depth; i > 0; --i)
                if (!writeBack(i))
                    throw new RuntimeException("writeBack in close() should always succeed");

            this.startBlockAddr = writeBlock(this.dataBlocks[0]);

            // now we can release most buffers
            this.dataBlocks = null;
            this.full = null;

            // after all this work, store the information about this stream to the streamDefs stream
            if (this != MultiplexedFileWriter.this.streamDefs) {
                synchronized (MultiplexedFileWriter.this) {
                    MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.id);
                    MultiplexedFileWriter.this.streamDefsDataOut.writeInt(this.startBlockAddr);
                    MultiplexedFileWriter.this.streamDefsDataOut.writeLong(this.dataLength);
                }
            }
        }

    }

    private static final int DEFAULT_BLOCK_SIZE = 1024; // MUST be divideable by 4

    private static final int DEFAULT_MAX_DEPTH = 5;

    // this is just some random integer
    public static final int MAGIC_HEADER = 0xB7A332B2;

    protected static final int headerSize = 20; // bytes

    protected final RandomAccessFile file;

    private int nextBlockAddr = 0;

    private int nextStreamNr = 0;

    // may be set when an error occurs asynchronously. is thrown on the next
    // operation on this file.
    protected IOException exception = null;

    // holds all open streams. they still have to be written out on close()
    private final Map<MultiplexOutputStream, MultiplexOutputStream> openStreams =
        new WeakIdentityHashMap<MultiplexOutputStream, MultiplexOutputStream>() {
            @Override
            protected void removing(final MultiplexOutputStream value) {
                try {
                    value.close();
                } catch (final IOException e) {
                    synchronized (MultiplexedFileWriter.this) {
                        MultiplexedFileWriter.this.exception = e;
                    }
                }
            }
        };

    private boolean closed = false;

    protected final MultiplexOutputStream streamDefs;
    protected final DataOutputStream streamDefsDataOut;

    protected final int blockSize;
    protected final int maxDepth;

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
        this.streamDefs = new MultiplexOutputStream(-1);
        this.streamDefsDataOut = new DataOutputStream(this.streamDefs);
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

    public synchronized MultiplexOutputStream newOutputStream() {
        if (this.closed)
            throw new IllegalStateException(getClass().getSimpleName() + " closed");
        final int streamNr = this.nextStreamNr++;
        final MultiplexOutputStream newStream = new MultiplexOutputStream(streamNr);
        this.openStreams.put(newStream, newStream);
        return newStream;
    }

    public synchronized int writeBlock(final byte[] data) throws IOException {
        if (this.nextBlockAddr == 0 && this.file.length() > headerSize)
            throw new IOException("Maximum file size reached");
        this.file.write(data, 0, this.blockSize);
        return this.nextBlockAddr++;
    }

    public synchronized void close() throws IOException {
        checkException();
        if (this.closed)
            return;
        this.closed = true;

        for (final MultiplexOutputStream str: this.openStreams.keySet())
            str.close();
        this.openStreams.clear();

        this.streamDefs.close();

        // write some meta information to the file
        this.file.seek(0);
        this.file.writeInt(MAGIC_HEADER);
        this.file.writeInt(this.blockSize);
        this.file.writeInt(this.streamDefs.startBlockAddr);
        this.file.writeLong(this.streamDefs.dataLength);

        this.file.close();
        checkException();
    }

    private synchronized void checkException() throws IOException {
        if (this.exception != null) {
            final IOException e = this.exception;
            this.exception = null;
            throw e;
        }
    }

}
