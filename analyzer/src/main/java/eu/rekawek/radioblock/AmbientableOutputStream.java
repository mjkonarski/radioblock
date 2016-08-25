package eu.rekawek.radioblock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicBoolean;

public class AmbientableOutputStream extends OutputStream {

    private final ByteBuffer buffer;

    private final OutputStream os;

    private final InputStream ambientStream;

    private AtomicBoolean adPresent = new AtomicBoolean(false);

    public AmbientableOutputStream(OutputStream os, InputStream ambientStream) {
        this.buffer = ByteBuffer.allocate(4);
        this.buffer.order(ByteOrder.LITTLE_ENDIAN);
        this.os = os;
        this.ambientStream = ambientStream;
    }

    @Override
    public void write(int b) throws IOException {
        buffer.put((byte) b);
        if (buffer.position() == 4) {
            if (adPresent.get()) {
                buffer.clear();

                byte ambientBytes[] = new byte[4];

                if (ambientStream.read(ambientBytes) == ambientBytes.length) {
                    buffer.put(ambientBytes);
                } else {
                    buffer.clear();
                }
            }

            buffer.rewind();
            while (buffer.hasRemaining()) {
                os.write(buffer.get());
            }
            buffer.clear();
        }
    }

    public void setAdPresent(boolean adPresent) {
        this.adPresent.set(adPresent);
    }
}