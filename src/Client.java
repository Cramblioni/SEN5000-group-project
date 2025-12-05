import Data.Co2Message;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

public class Client {
    public static void main(String[] args) {

        if (args.length != 5) printUsage("Wrong number of arguments");

        final float reading = validateReading(args[4]);

        short port;
        try {
            port = (short)Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            printUsage("Port must be a port number");
            return;
        }

        /*
        final Co2Message test = new Co2Message(args[2], args[3], reading);
        final byte[] raw_test = test.toBytes();
        System.out.println("record of  " + test);
        System.out.println("with bytes " + Arrays.toString(raw_test));

        final Co2Message got = Co2Message.fromBytes(raw_test);
        System.out.println("loaded " + got);
        */
        final Co2Message message = new Co2Message(args[2], args[3], reading);
        try {
            sendUpdate(args[0], port, message);
        } catch (IOException e) {
            System.out.println("Failed to send message");
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Message sent successfully");
    }

    private static void sendUpdate(String address, short port, Co2Message message) throws IOException {
        try (Socket connection = new Socket(address, port)) {
            final DataInputStream inStream = new DataInputStream(new BufferedInputStream(connection.getInputStream()));
            final OutputStream outStream = new BufferedOutputStream(connection.getOutputStream());

            // checking for the OK
            byte[] code = new byte[2];
            inStream.readFully(code);
            if (!Arrays.equals(code, "OK".getBytes())) {
                System.out.println("Connection Rejected");
            }

            // printing welcome message
            int length = inStream.readInt();
            final byte[] welcomeMessage = new byte[length];
            inStream.readFully(welcomeMessage);
            System.out.println(new String(welcomeMessage));

            // finally we send
            message.intoStream(outStream);
        };
    }

    private static void printUsage(String hint) {
        System.out.println(
                "\nClient [address] [port] [ID] [email] [reading]\n" +
                "\t- [address] : Server address\n" +
                "\t- [port] : Port to connect to\n" +
                "\t- [ID] : YOUR id\n" +
                "\t- [postcode] : postcode of your reading\n" +
                "\t- [reading] : The Co2 PPM reading, without units\n"
        );
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