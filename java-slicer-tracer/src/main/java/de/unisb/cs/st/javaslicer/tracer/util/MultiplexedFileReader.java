package de.unisb.cs.st.javaslicer.tracer.util;

import java.io.DataInputStream;
import java.io.EOFException;
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
        private final byte[] currentBlock = new byte[MultiplexedFileReader.this.blockSize];

        protected MultiplexInputStream(final int id, final int beginningBlockAddr) throws IOException {
            this.id = id;

            // read the length and the depth
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(beginningBlockAddr*MultiplexedFileReader.this.blockSize);
                this.dataLength = MultiplexedFileReader.this.file.readLong();
                // compute the depth needed to store the data
                this.depth = compDepth(this.dataLength);
            }

            this.pos = new int[this.depth+1];
            this.blockAddr = new int[this.depth+1];
            this.remainingInCurrentBlock = (int) Math.min(this.dataLength, MultiplexedFileReader.this.blockSize);
            this.blockAddr[0] = beginningBlockAddr;
            this.pos[0] = 8;

            // initialize blockAddr and currentBlock
            for (int i = 1; i <= this.depth; ++i) {
                this.pos[i] = 0;
                synchronized (MultiplexedFileReader.this.file) {
                    MultiplexedFileReader.this.file.seek(this.blockAddr[i-1]*MultiplexedFileReader.this.blockSize+(i==1?8:0));
                    this.blockAddr[i] = MultiplexedFileReader.this.file.readInt();
                }
            }
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(this.blockAddr[this.depth]*MultiplexedFileReader.this.blockSize);
                final int read = this.remainingInCurrentBlock+(this.depth==0?8:0);
                if (MultiplexedFileReader.this.file.read(this.currentBlock, 0, read) < read) {
                    throw new EOFException();
                }
            }
        }

        private int compDepth(final long len) {
            int d = 0;
            long max = MultiplexedFileReader.this.blockSize-8;
            while (max < len) {
                d++;
                max *= 1024/4;
            }
            return d;
        }

        public void seek(final long toPos) throws IOException {
            if (toPos < 0 || toPos > this.dataLength)
                throw new IOException("pos must be in the range 0 .. dataLength");
            long rem = toPos;
            final int[] newPos = new int[this.depth+1];
            for (int d = this.depth; d >= 0; --d) {
                newPos[d] = (int) (rem % MultiplexedFileReader.this.blockSize);
                rem = rem / MultiplexedFileReader.this.blockSize * 4;
            }
            newPos[0] += 8;
            if (rem != 0)
                throw new IOException("internal error");

            if (toPos == this.dataLength) {
                this.remainingInCurrentBlock = 0;
                // move the pos pointer(s) after the end of the last block instead in front of the new one
                if (this.pos[this.depth] == 0) {
                    for (int i = this.depth; i >= 0; ++i) {
                        if (this.pos[i] == 0)
                            this.pos[i] = MultiplexedFileReader.this.blockSize;
                        else {
                            this.pos[i] -= 4;
                            break;
                        }
                    }
                }
            } else {
                // read the block addresses
                for (int i = 0; i < this.depth; ++i) {
                    if (newPos[i] != this.pos[i]) {
                        this.pos[i] = newPos[i];
                        synchronized (MultiplexedFileReader.this.file) {
                            MultiplexedFileReader.this.file.seek(this.blockAddr[i] * MultiplexedFileReader.this.blockSize + this.pos[i]);
                            this.blockAddr[i+1] = MultiplexedFileReader.this.file.readInt();
                        }
                    }
                }
                this.pos[this.depth] = newPos[this.depth];

                this.remainingInCurrentBlock = MultiplexedFileReader.this.blockSize - this.pos[this.depth];
                if (this.dataLength - toPos < this.remainingInCurrentBlock)
                    this.remainingInCurrentBlock = (int) (this.dataLength - toPos);
                // read the current block (remaining part)
                synchronized (MultiplexedFileReader.this.file) {
                    MultiplexedFileReader.this.file.seek(this.blockAddr[this.depth] * MultiplexedFileReader.this.blockSize + this.pos[this.depth]);
                    MultiplexedFileReader.this.file.read(this.currentBlock, this.pos[this.depth], this.remainingInCurrentBlock);
                }
            }
        }

        @Override
        public int read() throws IOException {
            if (this.remainingInCurrentBlock == 0) {
                moveToNextBlock();
                if (this.remainingInCurrentBlock == 0)
                    return -1;
            }
            --this.remainingInCurrentBlock;
            return this.currentBlock[this.pos[this.depth]++] & 0xff;
        }

        @Override
        public int read(final byte[] b, final int off, final int len) throws IOException {
            if (b == null)
                throw new NullPointerException();
            if (off < 0 || len < 0 || len + off > b.length)
                throw new IndexOutOfBoundsException();
            if (len == 0)
                return 0;

            if (this.remainingInCurrentBlock == 0) {
                moveToNextBlock();
                if (this.remainingInCurrentBlock == 0)
                    return -1;
            }

            int ptr = off;
            final int end = off + len;
            while (true) {
                int read = end - ptr;
                if (this.remainingInCurrentBlock < read)
                    read = this.remainingInCurrentBlock;
                System.arraycopy(this.currentBlock, this.pos[this.depth], b, ptr, read);
                ptr += read;
                this.remainingInCurrentBlock -= read;
                if (ptr < end) {
                    moveToNextBlock();
                    if (this.remainingInCurrentBlock == 0)
                        return ptr-off;
                } else {
                    this.pos[this.depth] = read;
                    break;
                }
            }
            return len;
        }

        private void moveToNextBlock() throws IOException {
            final long read = pos();
            final long remaining = this.dataLength - read;
            if (remaining <= 0)
                return;

            int moved = this.depth-1;
            while (moved >= 0) {
                this.pos[moved + 1] = 0;
                if (this.pos[moved] < MultiplexedFileReader.this.blockSize - 4) {
                    this.pos[moved] += 4;
                    break;
                }
                moved--;
            }
            if (moved < 0)
                throw new IOException("corrupted data");

            // read the block addresses
            for (int d = moved+1; d <= this.depth; ++d) {
                synchronized (MultiplexedFileReader.this.file) {
                    MultiplexedFileReader.this.file.seek(this.blockAddr[d-1]*MultiplexedFileReader.this.blockSize + this.pos[d-1]);
                    this.blockAddr[d] = MultiplexedFileReader.this.file.readInt();
                }
            }

            // read the current block
            this.remainingInCurrentBlock = MultiplexedFileReader.this.blockSize;
            if (remaining < MultiplexedFileReader.this.blockSize)
                this.remainingInCurrentBlock = (int) remaining;
            synchronized (MultiplexedFileReader.this.file) {
                MultiplexedFileReader.this.file.seek(this.blockAddr[this.depth]*MultiplexedFileReader.this.blockSize);
                if (MultiplexedFileReader.this.file.read(this.currentBlock, 0, this.remainingInCurrentBlock) < this.remainingInCurrentBlock)
                    throw new EOFException();
            }
            this.pos[this.depth] = 0;
            return;
        }

        public long pos() {
            long read = this.pos[0]-8;
            for (int i = 1; i <= this.depth; ++i)
                read = MultiplexedFileReader.this.blockSize/4*read + this.pos[i];
            return read;
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

    protected final int blockSize; // MUST be divideable by 4

    protected final RandomAccessFile file;

    private final int[] streamBeginningBlocks;

    public MultiplexedFileReader(final RandomAccessFile file) throws IOException {
        this.file = file;

        // blockSize is always on offsets 8-11 on the file (after the header of the def stream)
        if (file.length() < 12)
            throw new IOException("corrupted data");
        file.seek(8);
        this.blockSize = file.readInt();

        // read the stream defs
        final MultiplexInputStream streamDefStream = new MultiplexInputStream(-1, 0);
        final DataInputStream str = new DataInputStream(streamDefStream);
        this.streamBeginningBlocks = new int[(int) (streamDefStream.getDataLength()/4)-1];
        if (str.readInt() != this.blockSize)
            throw new IOException("corrupted data");
        for (int i = 0; i < this.streamBeginningBlocks.length; ++i)
            this.streamBeginningBlocks[i] = str.readInt();
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
            throw new IndexOutOfBoundsException("stream " + index + " does not exist");
        return new MultiplexInputStream(index, this.streamBeginningBlocks[index]);
    }

    public void close() throws IOException {
        this.file.close();
    }

}
