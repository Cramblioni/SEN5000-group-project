import Data.Co2Message;

import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {

        if (args.length != 5) printUsage("Wrong number of arguments");

        final float reading = validateReading(args[4]);

        final Co2Message test = new Co2Message(args[2], args[3], reading);
        final byte[] raw_test = test.toBytes();
        System.out.println("record of  " + test);
        System.out.println("with bytes " + Arrays.toString(raw_test));

        final Co2Message got = Co2Message.fromBytes(raw_test);
        System.out.println("loaded " + got);

    }

    private static void sendUpdate(String address, short port, Co2Message message) throws IOException {
        try (var connection = new Socket(address, port)) {
            final var inStream = connection.getInputStream();
            final var outStream = connection.getOutputStream();

            //
            byte[] code = inStream.readNBytes(2);
            if (!Arrays.equals(code, "OK".getBytes())) {
                System.out.println("Connection Rejected");
            }
            // finally we send
            message.intoStream(outStream);
        };
    }

    private static void printUsage(String hint) {
        System.out.println("""
                Client [address] [port] [ID] [email] [reading]
                    - [address] : Server address
                    - [port] : Port to connect to
                    - [ID] : YOUR id
                    - [postcode] : postcode of your reading
                    - [reading] : The Co2 PPM reading, without units
                """);
        if (hint != null) {
            System.out.println(hint);
        }
        System.exit(1);
    }

    private static float validateReading(String reading) {
        try {
            return Float.parseFloat(reading);
        } catch (NumberFormatException e) {
            printUsage("malformed reading");
        }
        throw new RuntimeException();
    }
}