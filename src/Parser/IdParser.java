package Parser;

import Parser.Exceptions.InvalidIdException;

import java.io.*;

// We're using student IDs as our ID format
// Essentially ST[0-9]*8
// :)

public class IdParser {
    final static int ID_LENGTH = "ST01234567".length();
    public static String parse(PushbackInputStream source) throws InvalidIdException, IOException {
            byte[] out = new byte[ID_LENGTH];
            final int bytes_read = source.read(out);
            if (bytes_read != ID_LENGTH) throw new InvalidIdException("ID too short");
            final String result = new String(out);
            if (!valid(result)) throw new InvalidIdException("Invalid ID format");
            return result;
    }
    public static boolean valid(String value) {
        // Length check
        if (value.length() != ID_LENGTH) return false;

        byte[] raw = value.getBytes();

        // Checking for the "ST"
        if (raw[0] != 'S' || raw[1] != 'T') return false;

        // Checking if the rest are digits
        for (int i = 2; i < ID_LENGTH; i += 1)
            if (!Character.isDigit((char)raw[i])) return false;
        return true;
    }
}
