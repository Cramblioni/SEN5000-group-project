package Data;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TimestampedCo2Record {
    LocalDateTime time;
    String ID;
    String postcode;
    float co2ppm;
    public TimestampedCo2Record(LocalDateTime time, String ID, String postcode, float co2ppm) {
        this.time = time;
        this.ID = ID;
        this.postcode = postcode;
        this.co2ppm = co2ppm;
    }
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
