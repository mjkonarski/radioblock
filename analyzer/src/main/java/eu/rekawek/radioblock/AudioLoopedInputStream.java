package eu.rekawek.radioblock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AudioLoopedInputStream extends InputStream implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(AudioLoopedInputStream.class);

    private InputStream inputStream;
    private String filename;

    private boolean inputStreamReady = false;
    private Lock lock = new ReentrantLock();
    private Condition inputStreamEmpty = lock.newCondition();

    public AudioLoopedInputStream(String filename) {
        this.filename = filename;
        openInputFile();
        (new Thread(this)).start();
    }

    @Override
    public void run() {
        while (true) {
            lock.lock();
            try {
                inputStreamEmpty.await();
                inputStream.close();
                openInputFile();
                inputStreamReady = true;
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }

    @Override
    public int read() throws IOException {
        int returnValue = 0;
        if (lock.tryLock()) {
             returnValue = inputStream.read();

            if (returnValue == -1) {
                inputStreamEmpty.signal();
                returnValue = 0;
            }

            lock.unlock();
        }
        return returnValue;
    }

    private void openInputFile() {
        try {
            File file = new File(filename);
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            inputStream = AudioSystem.getAudioInputStream(decodedFormat, in);
        } catch (Exception e) {
            LOG.error(e.toString());
            throw new RuntimeException();
        }
    }

}
