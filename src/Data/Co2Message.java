package Data;

import java.io.*;
import java.time.LocalDateTime;

public class Co2Message {
    String ID;
    String postcode;
    float co2ppm;

    public Co2Message(String ID, String postcode, float co2ppm) {
        this.ID = ID;
        this.postcode = postcode;
        this.co2ppm = co2ppm;
    }

    public static Co2Message fromStream(InputStream stream) throws IOException {
        final DataInputStream reader = new DataInputStream(stream);

        final int id_length = reader.readShort();
        final byte[] raw_id = new byte[id_length];
        reader.readFully(raw_id);
        final String id = new String(raw_id);

        final int postcode_length = reader.readShort();
        final byte[] raw_postcode = new byte[postcode_length];
        reader.readFully(raw_postcode);
        final String postcode = new String(raw_postcode);

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