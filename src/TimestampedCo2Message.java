import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record TimestampedCo2Message(LocalDateTime time, String ID, String email, float co2ppm) {

    public byte[] toCsvEntry() {
        String some_random_temp_value =
                time.format(DateTimeFormatter.ISO_DATE_TIME) + "," +
                ID + "," +
                email + "," +
                co2ppm;

        return some_random_temp_value.getBytes(StandardCharsets.UTF_8);
    }
}
