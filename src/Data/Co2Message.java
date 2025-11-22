package Data;

import java.io.*;
import java.time.LocalDateTime;

public record Co2Message ( String ID, String postcode, float co2ppm ) {

    public static Co2Message fromStream(InputStream stream) throws IOException {
        final DataInputStream reader = new DataInputStream(stream);

        final int id_length = reader.readShort();
        final String id = new String(reader.readNBytes(id_length));

        final int postcode_length = reader.readShort();
        final String postcode = new String(reader.readNBytes(postcode_length));

        final float c20ppm = reader.readFloat();

        return new Co2Message(id, postcode, c20ppm);
    }

    public void intoStream(OutputStream stream) throws IOException {
        assert ID.length() <= 65535;
        assert postcode.length() <= 65535;

        final DataOutputStream output = new DataOutputStream(stream);

        output.writeShort((short)ID.length());
        output.write(ID.getBytes());

        output.writeShort((short)postcode.length());
        output.write(postcode.getBytes());

        output.writeFloat(co2ppm);
        output.flush();
    }

    public TimestampedCo2Record timestamp(LocalDateTime timestamp) {
        return new TimestampedCo2Record(timestamp, ID, postcode, co2ppm);
    }
}