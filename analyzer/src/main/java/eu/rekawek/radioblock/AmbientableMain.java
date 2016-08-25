package eu.rekawek.radioblock;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;

public class AmbientableMain {

    private static final Logger LOG = LoggerFactory.getLogger(JingleLocator.class);

    public static void main(String... args) throws IOException, URISyntaxException {
        Rate rate = Rate.RATE_48;
        if (args.length == 2) {
            rate = Rate.valueOf(args[0]);
        }
        LOG.info("Using rate {}", rate);

        AudioLoopedInputStream ambientInputStream = new AudioLoopedInputStream(args[1]);
        AmbientingPipe pipe = new AmbientingPipe(rate, ambientInputStream);
        pipe.copyStream(System.in, System.out);
    }

}
