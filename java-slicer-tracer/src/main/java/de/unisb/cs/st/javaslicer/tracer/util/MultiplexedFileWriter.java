package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.unisb.cs.st.javaslicer.tracer.Tracer;

public class MultiplexedFileWriter {

    public class MultiplexOutputStream extends OutputStream {

        private final int id;
        private int depth = 0;
        private long dataLength = -8; // is increased to at least 0 on close
        protected int[] blockAddr = new int[MultiplexedFileWriter.this.maxDepth];
        private final int[] full = new int[MultiplexedFileWriter.this.maxDepth];
        private final byte[] currentBlock = new byte[MultiplexedFileWriter.this.blockSize];
        private boolean streamClosed = false;

        protected MultiplexOutputStream(final int id, final int beginningBlockAddr) {
            this.id = id;
            this.blockAddr[0] = beginningBlockAddr;
            this.full[0] = 8;
        }

        @Override
        public void write(final int b) throws IOException {
            if (this.streamClosed)
                throw new IOException("stream closed");
            if (this.full[this.depth] == MultiplexedFileWriter.this.blockSize) {
                // current block is full...
                moveToNextBlock();
            }
            this.currentBlock[this.full[this.depth]++] = (byte) b;
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
                int write = end - pos;
                if (MultiplexedFileWriter.this.blockSize-this.full[this.depth] < write)
                    write = MultiplexedFileWriter.this.blockSize-this.full[this.depth];
                System.arraycopy(b, pos, this.currentBlock, this.full[this.depth], write);
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

            // first, write back the current block
            synchronized (MultiplexedFileWriter.this.file) {
                MultiplexedFileWriter.this.file.seek(this.blockAddr[this.depth]*MultiplexedFileWriter.this.blockSize);
                MultiplexedFileWriter.this.file.write(this.currentBlock);
            }

            // search for a free table entry (up the tree)
            int insert = this.depth-1;
            while (insert >= 0) {
                this.full[insert+1] = 0;
                if (this.full[insert] < MultiplexedFileWriter.this.blockSize) {
                    break;
                }
                --insert;
            }

            if (insert < 0) {
                // hmm, no table entry free. we have to increase the depth
                if (this.depth + 1 == MultiplexedFileWriter.this.maxDepth)
                    throw new IOException("Maximum stream length reached");
                this.depth++;
                // the depth of all entries except the first increased
                System.arraycopy(this.full, 1, this.full, 2, MultiplexedFileWriter.this.maxDepth-2);
                System.arraycopy(this.blockAddr, 1, this.blockAddr, 2, MultiplexedFileWriter.this.maxDepth-2);
                // we need a new block to insert between the 0th and the 1st (now 2nd) depth
                final int freeBlockNr = MultiplexedFileWriter.this.nextBlockAddr.getAndIncrement();
                this.blockAddr[1] = freeBlockNr;

                synchronized (MultiplexedFileWriter.this.file) {
                    // read beginning block data (beginning at offset 8)
                    MultiplexedFileWriter.this.file.seek(this.blockAddr[0]*MultiplexedFileWriter.this.blockSize+8);
                    MultiplexedFileWriter.this.file.read(this.currentBlock, 0, MultiplexedFileWriter.this.blockSize-8);
                    // copy data from beginning block to new block
                    MultiplexedFileWriter.this.file.seek(freeBlockNr*MultiplexedFileWriter.this.blockSize);
                    MultiplexedFileWriter.this.file.write(this.currentBlock, 0, MultiplexedFileWriter.this.blockSize-8);
                    // then write the new block addr to the beginning block
                    MultiplexedFileWriter.this.file.seek(this.blockAddr[0]*MultiplexedFileWriter.this.blockSize+8);
                    MultiplexedFileWriter.this.file.writeInt(freeBlockNr);
                }
                // set the full values correctly
                this.full[0] = 12;
                this.full[1] = MultiplexedFileWriter.this.blockSize-8;
                if (this.depth == 1)
                    this.dataLength -= MultiplexedFileWriter.this.blockSize-8; // would otherwise be counted twice
                // and insert the new block in the 1st depth
                insert = 1;
            }

            // fill up the tree again
            for (int down = insert; down < this.depth; ++down) {
                // we need another free block on this depth
                final int nextFreeBlockNr = MultiplexedFileWriter.this.nextBlockAddr.getAndIncrement();
                synchronized (MultiplexedFileWriter.this.file) {
                    MultiplexedFileWriter.this.file.seek(this.blockAddr[down]*MultiplexedFileWriter.this.blockSize+this.full[down]);
                    MultiplexedFileWriter.this.file.writeInt(nextFreeBlockNr);
                }
                this.full[down] += 4;
                this.blockAddr[down+1] = nextFreeBlockNr;
            }
            return;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public void close() throws IOException {
            if (this.streamClosed)
                return;
            this.streamClosed = true;

            this.dataLength += this.full[this.depth];
            synchronized (MultiplexedFileWriter.this.file) {
                // write back the current block
                MultiplexedFileWriter.this.file.seek(this.blockAddr[this.depth]*MultiplexedFileWriter.this.blockSize);
                MultiplexedFileWriter.this.file.write(this.currentBlock, 0, this.full[this.depth]);

                // write length information to the beginning of this stream (8 byte)
                MultiplexedFileWriter.this.file.seek(this.blockAddr[0]*MultiplexedFileWriter.this.blockSize);
                MultiplexedFileWriter.this.file.writeLong(this.dataLength);
            }
        }

    }

    private static final int DEFAULT_BLOCK_SIZE = 1024; // MUST be divideable by 4

    // one stream can hold at most (blockSize)^maxDepth - 8 bytes
    private static final int DEFAULT_MAX_DEPTH = 5;

    protected final RandomAccessFile file;

    // 0 is reserved to the stream definition block
    protected final AtomicInteger nextBlockAddr = new AtomicInteger(1);

    private int nextStreamNr = 0;

    // holds stream beginning (block) addresses
    private final Map<MultiplexOutputStream, MultiplexOutputStream> openStreams =
        new WeakIdentityHashMap<MultiplexOutputStream, MultiplexOutputStream>() {
            @Override
            protected void removing(MultiplexOutputStream value) {
                try {
                    value.close();
                } catch (IOException e) {
                    Tracer.error(e);
                }
            }
        };

    private boolean closed = false;

    private final DataOutputStream streamDefs;

    protected final int blockSize;
    protected final int maxDepth;

    /**
     * Constructs a new multiplexed file writer with all options available.
     *
     * // TODO fix this
     * Each stream will have a limit of <code>blockSize^maxDepth-8</code> bytes.
     *
     * The whole file can have at most 2^32*blockSize bytes.
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

        this.file = file;
        this.blockSize = blockSize;
        this.maxDepth = maxDepth;
        this.streamDefs = new DataOutputStream(new MultiplexOutputStream(-1, 0));
        this.streamDefs.writeInt(blockSize);
    }

    public MultiplexedFileWriter(final RandomAccessFile file) throws IOException {
        this(file, DEFAULT_BLOCK_SIZE, DEFAULT_MAX_DEPTH);
    }

    public MultiplexedFileWriter(final File filename) throws IOException {
        this(new RandomAccessFile(filename, "rw"));
    }

    public synchronized MultiplexOutputStream newOutputStream() throws IOException {
        if (this.closed)
            throw new IllegalStateException(getClass().getSimpleName() + " closed");
        final int streamNr = this.nextStreamNr++;
        final int beginningBlockAddr = this.nextBlockAddr.getAndIncrement();
        final MultiplexOutputStream newStream = new MultiplexOutputStream(streamNr, beginningBlockAddr);
        this.openStreams.put(newStream, newStream);
        this.streamDefs.writeInt(beginningBlockAddr);
        return newStream;
    }

    public synchronized void close() throws IOException {
        this.closed = true;

        for (final MultiplexOutputStream str: this.openStreams.keySet())
            str.close();
        this.openStreams.clear();

        this.streamDefs.close();

        // if necessary, truncate the file
        final int fileLength = this.nextBlockAddr.get()*this.blockSize;
        if (this.file.length() > fileLength)
            this.file.setLength(fileLength);

        this.file.close();
    }

}
