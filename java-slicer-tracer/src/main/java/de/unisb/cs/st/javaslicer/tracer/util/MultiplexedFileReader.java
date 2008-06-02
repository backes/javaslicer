package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class MultiplexedFileReader {

    public class MultiplexInputStream extends InputStream {

        private final int id;
        private final int depth;
        private final long dataLength;
        private final int[] blockAddr;
        private final int[] pos;
        private int remainingInCurrentBlock;
        private final byte[] currentBlock = new byte[BLOCK_SIZE];

        protected MultiplexInputStream(final int id, final int beginningBlockAddr) throws IOException {
            this.id = id;

            // read the length and the depth
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(beginningBlockAddr*BLOCK_SIZE);
                this.dataLength = (MultiplexedFileReader.this.file.readLong() << 8) >> 8;
                MultiplexedFileReader.this.file.seek(beginningBlockAddr*BLOCK_SIZE);
                this.depth = MultiplexedFileReader.this.file.readByte();
            }

            this.pos = new int[this.depth+1];
            this.blockAddr = new int[this.depth+1];
            this.remainingInCurrentBlock = (int) Math.min(this.dataLength, BLOCK_SIZE);
            this.blockAddr[0] = beginningBlockAddr;
            this.pos[0] = 8;

            // initialize blockAddr and currentBlock
            for (int i = 1; i <= this.depth; ++i) {
                this.pos[i] = 0;
                synchronized (MultiplexedFileReader.this.file) {
                    MultiplexedFileReader.this.file.seek(this.blockAddr[i-1]*BLOCK_SIZE+(i==1?8:0));
                    this.blockAddr[i] = MultiplexedFileReader.this.file.readInt();
                }
            }
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(this.blockAddr[this.depth]*BLOCK_SIZE);
                MultiplexedFileReader.this.file.read(this.currentBlock, 0, this.remainingInCurrentBlock+(this.depth==0?8:0));
            }
        }

        @Override
        public int read() throws IOException {
            if (this.remainingInCurrentBlock <= 0) {
                moveToNextBlock();
                if (this.remainingInCurrentBlock <= 0)
                    return -1;
            }
            --this.remainingInCurrentBlock;
            return this.currentBlock[this.pos[this.depth]++] & 0xff;
        }

        private void moveToNextBlock() throws IOException {
            long read = this.pos[0]-8;
            for (int i = 1; i < this.depth; ++i)
                read = BLOCK_SIZE*read + this.pos[i];
            if (this.depth > 0)
                read = read*BLOCK_SIZE/4 + this.pos[this.depth];
            final long remaining = this.dataLength - read;
            if (remaining <= 0)
                return;

            int moved = this.depth-1;
            while (moved >= 0) {
                if (this.pos[moved] < BLOCK_SIZE) {
                    this.pos[moved] += 4;
                    break;
                }
                this.pos[moved] = 0;
                moved--;
            }
            if (moved < 0)
                throw new IOException("inconsistent data");

            // read the block addresses
            for (int d = moved+1; d <= this.depth; ++d) {
                synchronized (MultiplexedFileReader.this.file) {
                    MultiplexedFileReader.this.file.seek(this.blockAddr[d-1]*BLOCK_SIZE+this.pos[d-1]);
                    this.blockAddr[d] = MultiplexedFileReader.this.file.readInt();
                }
            }

            // read the current block
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(this.blockAddr[this.depth]*BLOCK_SIZE);
                MultiplexedFileReader.this.file.read(this.currentBlock);
            }
            this.remainingInCurrentBlock = (int) Math.min(remaining, BLOCK_SIZE);
            this.pos[this.depth] = 0;
            return;
        }

        public int getId() {
            return this.id;
        }

        public long getDataLength() {
            return this.dataLength;
        }

        @Override
        public void close() {
            // nothing to do
        }

    }

    // remember to keep synchronous with MultiplexedFileWriter
    private static final int BLOCK_SIZE = 1024; // MUST be divideable by 4

    protected final RandomAccessFile file;

    private final int[] streamBeginningBlocks;

    public MultiplexedFileReader(final RandomAccessFile file) throws IOException {
        this.file = file;

        // read the stream defs
        final MultiplexInputStream streamDefStream = new MultiplexInputStream(-1, 0);
        final DataInputStream str = new DataInputStream(streamDefStream);
        final int noStreams = str.readInt();
        this.streamBeginningBlocks = new int[noStreams];
        for (int i = 0; i < noStreams; ++i) {
            this.streamBeginningBlocks[i] = str.readInt();
        }
        str.close();
    }

    public MultiplexedFileReader(final File filename) throws IOException {
        this(new RandomAccessFile(filename, "r"));
    }

    public int getNoStreams() {
        return this.streamBeginningBlocks.length;
    }

    public MultiplexInputStream getInputStream(final int index) throws IOException {
        if (index < 0 || index >= this.streamBeginningBlocks.length)
            throw new IllegalArgumentException("stream " + index + " does not exist");
        return new MultiplexInputStream(index, this.streamBeginningBlocks[index]);
    }

    public void close() throws IOException {
        this.file.close();
    }

}
