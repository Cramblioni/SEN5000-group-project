package Validators;

import Validators.Exceptions.PostCodeFormatException;

import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.Arrays;
import java.util.OptionalInt;

// Using `https://ideal-postcodes.co.uk/guides/uk-postcode-format` as a reference

public class PostCodeValidator {

    // ========== validating ==========
    public static boolean valid(String value) {
        final char[] raw = value.toCharArray();

        // Split by space
        final OptionalInt potential_index = findSpace(raw);
        if (!potential_index.isPresent()) return false;
        final int index = potential_index.getAsInt();
        final char[] potential_outcode = Arrays.copyOfRange(raw, 0, index);
        final char[] potential_incode = Arrays.copyOfRange(raw, index, raw.length);

        // Outcodes must be between 2 and 4 characters old, else Fail
        if (potential_outcode.length < 2 || potential_outcode.length > 4)
            return false;

        // Outcodes must start with an uppercase letter, else Fail
        if (!Character.isAlphabetic(potential_outcode[0]) || Character.isLowerCase(potential_outcode[0]))
            return false;

        // Incodes must be 3 characters long, else Fail
        if (potential_incode.length != 3)
            return false;

        // Incodes must start with a number, else Fail
        if (!Character.isDigit(potential_incode[0]))
            return false;

        return true;
    }
    private static OptionalInt findSpace(char[] source) {
        for (int i = 0; i < source.length; i += 1) {
            if (source[i] == ' ') return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }
}
