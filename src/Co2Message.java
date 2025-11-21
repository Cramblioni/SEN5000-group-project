import java.nio.ByteBuffer;
import java.time.LocalDateTime;

public record Co2Message ( String ID, String email, float co2ppm ) {
    public byte[] toBytes() {

        // Using `u16Le` to store string lengths
        // Fun
        assert ID.length() <= 65535;
        assert email.length() <= 65535;

        final int size =
                  (ID.length() + 2) // including size tag
                + (email.length() + 2) // including size tag
                + 4; // The size of a f32

        ByteBuffer output = ByteBuffer.allocate(size);
        output.putShort((short)ID.length());
        output.put(ID.getBytes());
        output.putShort((short)email.length());
        output.put(email.getBytes());
        output.putFloat(co2ppm);

        return output.array();
    }
    public static Co2Message fromBytes(byte[] raw) {
        ByteBuffer reader = ByteBuffer.wrap(raw).asReadOnlyBuffer();

        final int ID_length = reader.getShort();
        final byte[] ID_raw = new byte[ID_length];
        reader.get(ID_raw);
        final String ID = new String(ID_raw);

        final int email_length = reader.getShort();
        final byte[] email_raw = new byte[email_length];
        reader.get(email_raw);
        final String email = new String(email_raw);

        final float co2ppm = reader.getFloat();
        return new Co2Message(ID, email, co2ppm);
    }
    public  TimestampedCo2Message timestamp(LocalDateTime timestamp) {
        return new TimestampedCo2Message(timestamp, ID, email, co2ppm);
    }
};
;
