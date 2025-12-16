package Validators;

import Validators.Exceptions.InvalidIdException;

import java.io.*;

// We're using student IDs as our ID format
// Essentially ST[0-9]*8
// :)

public class IdValidator {
    final static int ID_LENGTH = "ST01234567".length();
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
