package Validators;

import Validators.Exceptions.InvalidIdException;

// We're using student IDs as our ID format
// Essentially ST[0-9]*8
// :)

public class IdValidator {
    final static int ID_LENGTH = "ST01234567".length();
    public static void valid(String value) throws InvalidIdException {
        // Length check
        if (value.length() != ID_LENGTH)
            throw new InvalidIdException("ID must be " + ID_LENGTH + " characters long");

        char[] raw = value.toCharArray();

        // Checking for the "ST"
        if (raw[0] != 'S' || raw[1] != 'T')
            throw new InvalidIdException("ID must start with \"ST\"");

        // Checking if the rest are digits
        for (int i = 2; i < ID_LENGTH; i += 1) if (!Character.isDigit((char)raw[i]))
            throw new InvalidIdException("The final 8 digits of an ID must be digits");

    }
}
