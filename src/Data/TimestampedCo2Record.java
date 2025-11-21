package Data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record TimestampedCo2Record(LocalDateTime time, String ID, String postcode, float co2ppm) {

    public void intoStream(OutputStream stream) throws IOException {
                stream.write(time.format(DateTimeFormatter.ISO_DATE_TIME).getBytes());
                stream.write(',');
                stream.write(ID.getBytes());
                stream.write(',');
                stream.write(postcode.getBytes());
                stream.write(',');
                stream.write(Float.toString(co2ppm).getBytes());
                stream.write('\n');
                stream.flush();
    }
}
