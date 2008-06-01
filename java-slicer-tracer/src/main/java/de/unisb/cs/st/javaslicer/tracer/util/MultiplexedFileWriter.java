package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MultiplexedFileWriter {

    public class MultiplexOutputStream extends OutputStream {

        private final int id;
        private int depth = 0;
        private long dataLength = -8; // is increased to at least 0 on close
        protected int[] blockAddr = new int[MAX_DEPTH];
        private int[] full = new int[MAX_DEPTH];
        private byte[] currentBlock = new byte[BLOCK_SIZE];

        protected MultiplexOutputStream(final int id, final int beginningBlockAddr) {
            this.id = id;
            this.blockAddr[0] = beginningBlockAddr;
            this.full[0] = 8;
        }

        @Override
        public void write(final int b) throws IOException {
            if (this.full == null)
                throw new IOException("stream closed");
            if (this.full[this.depth] == BLOCK_SIZE) {
                // current block is full...
                moveToNextBlock();
            }
            this.currentBlock[this.full[this.depth]++] = (byte) b;
        }

        private void moveToNextBlock() throws IOException {
            this.dataLength += BLOCK_SIZE;

            // first, write back the current block
            synchronized (MultiplexedFileWriter.this.file) {
                MultiplexedFileWriter.this.file.seek(this.blockAddr[this.depth]);
                MultiplexedFileWriter.this.file.write(this.currentBlock);
            }

            // get next free block nr
            int freeBlockNr = MultiplexedFileWriter.this.nextBlockAddr.getAndIncrement();

            // search for a free table entry (up the tree)
            int inserted = this.depth-1;
            while (inserted >= 0) {
                if (this.full[inserted] < BLOCK_SIZE) {
                    synchronized (MultiplexedFileWriter.this.file) {
                        MultiplexedFileWriter.this.file.seek(this.blockAddr[inserted]*BLOCK_SIZE+this.full[inserted]);
                        MultiplexedFileWriter.this.file.writeInt(freeBlockNr);
                    }
                    this.full[inserted] += 4;
                }
                --inserted;
            }

            if (inserted < 0) {
                // shit, no table entry free. we have to increase the depth
                if (this.depth + 1 == MAX_DEPTH)
                    throw new IOException("Maximum stream length reached");
                this.depth++;
                // the depth of all entries except the first increased
                System.arraycopy(this.full, 1, this.full, 2, MAX_DEPTH-2);
                System.arraycopy(this.blockAddr, 1, this.blockAddr, 2, MAX_DEPTH-2);
                synchronized (MultiplexedFileWriter.this.file) {
                    // read beginning block data (beginning at offset 8)
                    MultiplexedFileWriter.this.file.seek(this.blockAddr[0]*BLOCK_SIZE+8);
                    MultiplexedFileWriter.this.file.read(this.currentBlock, 0, BLOCK_SIZE-8);
                    // copy data from beginning block to new block
                    MultiplexedFileWriter.this.file.seek(freeBlockNr*BLOCK_SIZE);
                    MultiplexedFileWriter.this.file.write(this.currentBlock, 0, BLOCK_SIZE-8);
                    // then write the new block addr to the beginning block
                    MultiplexedFileWriter.this.file.seek(this.blockAddr[0]*BLOCK_SIZE+8);
                    MultiplexedFileWriter.this.file.writeInt(freeBlockNr);
                    // set the full values correctly
                    this.full[0] = 12;
                    this.full[1] = BLOCK_SIZE-8;
                    inserted = 0;
                }
            }

            // fill up the tree again
            while (++inserted < this.depth) {
                this.blockAddr[inserted] = freeBlockNr;
                final int freeBlockNr2 = MultiplexedFileWriter.this.nextBlockAddr.getAndIncrement();
                synchronized (MultiplexedFileWriter.this.file) {
                    MultiplexedFileWriter.this.file.seek(freeBlockNr*BLOCK_SIZE);
                    MultiplexedFileWriter.this.file.writeInt(freeBlockNr2);
                }
                this.full[inserted] = 4;
                freeBlockNr = freeBlockNr2;
            }
            this.blockAddr[inserted] = freeBlockNr;
            this.full[inserted] = 0;
            return;
        }

        public int getId() {
            return this.id;
        }

        @Override
        public void close() throws IOException {
            if (this.currentBlock == null && this.full == null && this.blockAddr == null)
                return;
            if (this.currentBlock == null || this.full == null || this.blockAddr == null)
                throw new IOException("stream in inconsistent state");

            this.dataLength += this.full[this.depth];
            synchronized (MultiplexedFileWriter.this.file) {
                // write back the current block
                MultiplexedFileWriter.this.file.seek(this.blockAddr[this.depth]);
                MultiplexedFileWriter.this.file.write(this.currentBlock);

                // write length and depth information
                // (first writing 8 bytes length (high byte first), then
                // overwriting the 0th byte by the depth)
                MultiplexedFileWriter.this.file.seek(this.blockAddr[0]);
                MultiplexedFileWriter.this.file.writeLong(this.dataLength);
                MultiplexedFileWriter.this.file.seek(this.blockAddr[0]);
                MultiplexedFileWriter.this.file.writeByte(this.depth);
            }
            this.currentBlock = null;
            this.full = null;
            this.blockAddr = null;
        }

    }

    // remember to keep synchronous with MultiplexedFileReader
    private static final int BLOCK_SIZE = 1024; // MUST be divideable by 4

    // one stream can hold at most (BLOCK_SIZE)^MAX_DEPTH - 8 bytes
    private static final int MAX_DEPTH = 5; // MUST fit into 1 byte (unsigned)

    protected final RandomAccessFile file;

    protected final AtomicInteger nextBlockAddr = new AtomicInteger(1);

    // holds stream beginning (block) addresses
    private final List<MultiplexOutputStream> streams = new ArrayList<MultiplexOutputStream>();
    private final MultiplexOutputStream streamDefStream;

    private boolean closed = false;

    public MultiplexedFileWriter(final RandomAccessFile file) {
        this.file = file;
        this.streamDefStream = new MultiplexOutputStream(-1, 0);
    }

    public MultiplexedFileWriter(final File filename) throws FileNotFoundException {
        this(new RandomAccessFile(filename, "rw"));
    }

    public MultiplexOutputStream newOutputStream() {
        if (this.closed)
            throw new IllegalStateException(getClass().getSimpleName() + " closed");
        final int nextFreeBlock = this.nextBlockAddr.getAndIncrement();
        MultiplexOutputStream newStream;
        synchronized (this.streams) {
            final int streamNr = this.streams.size();
            newStream = new MultiplexOutputStream(streamNr, nextFreeBlock);
            this.streams.add(newStream);
        }
        return newStream;
    }

    public void close() throws IOException {
        this.closed = true;

        // write the stream defs
        final DataOutputStream str = new DataOutputStream(this.streamDefStream);
        str.writeInt(this.streams.size());
        for (final MultiplexOutputStream stream: this.streams) {
            stream.close();
            str.writeInt(stream.blockAddr[0]);
        }
        this.streamDefStream.close();
        this.file.close();
    }

}
