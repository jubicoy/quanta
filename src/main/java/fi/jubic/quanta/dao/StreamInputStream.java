package fi.jubic.quanta.dao;

import java.io.InputStream;
import java.util.Spliterator;
import java.util.stream.Stream;

public class StreamInputStream extends InputStream {
    private int position;
    private byte[] row;
    private Spliterator<byte[]> spliterator;

    StreamInputStream(Stream<String> stream) {
        this();
        spliterator = stream.map(String::getBytes)
            .spliterator();
    }

    private StreamInputStream() {
        position = 0;
    }

    @Override
    public int read() {
        if (row == null && !spliterator.tryAdvance((r) -> row = r)) {
            return -1;
        }

        byte b;

        if (position == row.length) {
            b = '\n';
            position = 0;
            row = null;
        }
        else {
            b = row[position++];
        }

        return (b & 0xFF);
    }
}
