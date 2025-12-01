package Data;

import Parser.Exceptions.PostCodeFormatException;

import java.util.Arrays;
import java.util.OptionalInt;

// Using `https://ideal-postcodes.co.uk/guides/uk-postcode-format` as a reference
// Neat, I know.
// TODO: Convert to a validation class

public record PostCode(char[] outcode, char[] incode) {
    public PostCode fromString(String postcode) throws PostCodeFormatException {
        final char[] raw = postcode.toCharArray();

        // Split by space
        final int index = findSpace(raw).orElseThrow(() ->  new PostCodeFormatException("Missing Space"));
        final char[] potential_outcode = Arrays.copyOfRange(raw, 0, index);
        final char[] potential_incode = Arrays.copyOfRange(raw, index, raw.length);

        // Outcodes must be between 2 and 4 characters old
        if (potential_outcode.length < 2 || potential_outcode.length > 4)
            throw new PostCodeFormatException("Outcode must be between 2 and 4 characters long");

        // Outcodes must start with an uppercase letter
        if (!Character.isAlphabetic(potential_outcode[0]) || Character.isLowerCase(potential_outcode[0]))
            throw new PostCodeFormatException("Outcode must start with a capital letter");

        // Incodes must be 3 characters long
        if (potential_incode.length != 3)
            throw new PostCodeFormatException("Incodes must be 3 characters long");

        // Incodes must start with a number
        if (!Character.isDigit(potential_incode[0]))
            throw new PostCodeFormatException("Incodes must start with a digit");

        return new PostCode(potential_outcode, potential_incode);
    }

    public String toString() {
        return new String(outcode) + " " + new String(incode);
    }

    private OptionalInt findSpace(char[] source) {
        for (int i = 0; i < source.length; i += 1) {
            if (source[i] == ' ') return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }
}

