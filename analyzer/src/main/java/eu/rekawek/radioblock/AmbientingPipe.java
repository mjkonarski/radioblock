package eu.rekawek.radioblock;

import eu.rekawek.radioblock.JingleLocator.JingleListener;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.TeeInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;

public class AmbientingPipe {

    private final JingleLocator locator;

    private InputStream ambientStream;

    public AmbientingPipe(Rate rate, InputStream ambientStream) throws IOException {
        this.ambientStream = ambientStream;

        List<InputStream> jingles = new ArrayList<InputStream>();
        for (String name : asList(rate.getSamples())) {
            jingles.add(Main.class.getClassLoader().getResourceAsStream(name));
        }
        locator = new JingleLocator(jingles, Arrays.asList(500, 800), rate.getChannels());
    }

    public void copyStream(InputStream is, OutputStream os) throws IOException {
        final AmbientableOutputStream aos = new AmbientableOutputStream(os, ambientStream);
        locator.addListener(new JingleListener() {
            @Override
            public void gotJingle(int index, float level) {
                if (index == 0) {
                    aos.setAdPresent(true);
                } else {
                    aos.setAdPresent(false);
                }
            }
        });
        TeeInputStream tis = new TeeInputStream(is, aos);
        locator.analyse(tis);
    }
}
