package Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public record Co2Message ( String ID, String postcode, float co2ppm ) {
    public byte[] toBytes() {

        // Using `u16Le` (AFAIK) to store string lengths
        // Fun
        assert ID.length() <= 65535;
        assert postcode.length() <= 65535;

        final int size =
                  (ID.length() + 2) // including size tag
                  + (postcode.length() + 2) // including size tag
                + 4; // The size of a f32

        ByteBuffer output = ByteBuffer.allocate(size);
        output.putShort((short)ID.length());
        output.put(ID.getBytes());
        output.putShort((short)postcode.length());
        output.put(postcode.getBytes());
        output.putFloat(co2ppm);

        return output.array();
    }
    public static Co2Message fromBytes(byte[] raw) {
        ByteBuffer reader = ByteBuffer.wrap(raw).asReadOnlyBuffer();

        final int ID_length = reader.getShort();
        final byte[] ID_raw = new byte[ID_length];
        reader.get(ID_raw);
        final String ID = new String(ID_raw);

        final int postcode_length = reader.getShort();
        final byte[] postcode_raw = new byte[postcode_length];
        reader.get(postcode_raw);
        final String postcode = new String(postcode_raw);

        final float co2ppm = reader.getFloat();
        return new Co2Message(ID, postcode, co2ppm);
    }

    public static Co2Message fromStream(InputStream stream) throws IOException {
        final int message_size = ByteBuffer.wrap(stream.readNBytes(4)).getInt();
        final byte[] raw_message = stream.readNBytes(message_size);
        return fromBytes(raw_message);
    }

    public void intoStream(OutputStream stream) throws IOException {
        final byte[] encoded_message = toBytes();
        byte[] size = new byte[4];
        ByteBuffer.wrap(size).putInt(encoded_message.length,0);
        stream.write(size);
        stream.write(encoded_message);
    }

    public TimestampedCo2Record timestamp(LocalDateTime timestamp) {
        return new TimestampedCo2Record(timestamp, ID, postcode, co2ppm);
    }
};
;
