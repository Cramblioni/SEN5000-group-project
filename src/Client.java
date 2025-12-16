import Data.Co2Message;
import Validators.Exceptions.InvalidIdException;
import Validators.Exceptions.PostCodeFormatException;
import Validators.IdValidator;
import Validators.PostCodeValidator;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        // Splitting args
        String[] addr_args;
        String[] data_args;
        switch (args.length) {
            case 2: // Address Only
                addr_args = args;
                data_args = new String[0];
                break;
            case 3: // Data Only
                addr_args = new String[0];
                data_args = args;
                break;
            case 5: // Both provided
                addr_args = Arrays.copyOfRange(args, 0, 2);
                data_args = Arrays.copyOfRange(args, 2, 5);
                break;
            default: // Error
                printUsage("Wrong number of arguments");
                return;
        }
        try {
            DoYourJob(addr_args, data_args);
        } catch (IOException e) {
            System.out.println("Unexpected IO error");
            System.out.println(e.getMessage());
        }
    }
    private static void DoYourJob(String[] addr_args, String[] data_args) throws IOException {
            Socket socket;
            try {
                socket = MakeConnection(addr_args);
            } catch (IOException e) {
                System.out.println("Failed to connect");
                System.out.println(e.getMessage());
                return;
            }
            assert socket != null;

            if (!HandleConnectionStatus(socket)) {
                System.out.println("Connection rejected");
                socket.close();
                return;
            }

            final Co2Message message = MakeMessage(data_args);

            if (!SendUpdate(socket, message)) {
                System.out.println("Failed to send");
                socket.close();
                return;
            }

            System.out.println("Message sent successfully");
            socket.close();
        }

    private static boolean HandleConnectionStatus(Socket connection) throws IOException {
        final DataInputStream inStream = new DataInputStream(new BufferedInputStream(connection.getInputStream()));

        byte[] statusBuffer = new byte[2];
        inStream.readFully(statusBuffer);

        if (!Arrays.equals(statusBuffer, "OK".getBytes())) {
            return false;
        }

        // printing welcome message
        int length = inStream.readInt();
        final byte[] welcomeMessage = new byte[length];
        inStream.readFully(welcomeMessage);
        System.out.println(new String(welcomeMessage));
        return true;
    }
    private static boolean SendUpdate(Socket connection, Co2Message message) throws IOException {
        final OutputStream outStream = new BufferedOutputStream(connection.getOutputStream());

        message.intoStream(outStream);
        outStream.flush();
        return true;
    }


    private static void printUsage(String hint) {
        System.out.println(
                "\nClient ([address] [port]) ([ID] [postcode] [reading])\n" +
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

    private static Socket MakeConnection(String[] args) throws IOException {
        String address;
        String port;
        Scanner stdin = new Scanner(System.in);
        if (args.length == 2) {
            address = args[0];
            port = args[1];
        } else {
            System.out.print("Please input the server's address.\n? ");
            address = stdin.nextLine().trim();
            System.out.print("Please input the server's port .\n? ");
            port = stdin.nextLine().trim();
        }
        try {
            return new Socket(address, Integer.parseInt(port));
        } catch (NumberFormatException e) {
            printUsage("Invalid port number (should be an integer between 0 and 65536)");
            return null; // UNREACHABLE
        }
    }
    private static Co2Message MakeMessage(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[3];
            GatherMessageArgs(args);
        }

        // Validating ID
        while (true) {
            try { IdValidator.valid(args[0]); }
            catch (InvalidIdException e) {
                System.out.println(e.getMessage());
                args[0] = null;
                GatherMessageArgs(args);
                continue;
            }
            break;
        }

        // Validating postcode
        while (true) {
            try { PostCodeValidator.valid(args[1]); }
            catch (PostCodeFormatException e) {
                System.out.println(e.getMessage());
                args[1] = null;
                GatherMessageArgs(args);
                continue;
            }
            break;
        }

        // Validating Reading
        float reading;
        while (true) {
            try {
                reading = Float.parseFloat(args[2]);
            } catch (NumberFormatException e) {
                System.out.println("The reading should be a number");
                args[2] = null;
                GatherMessageArgs(args);
                continue;
            }
            break;
        }

        return new Co2Message(args[0], args[1], reading);
    }
    private static void GatherMessageArgs(String[] target) {
        final Scanner inputScanner = new Scanner(System.in);

        if (target[0] == null) {
            System.out.print("Enter your ID\n? ");
            target[0] = inputScanner.nextLine().trim();
        }

        if (target[1] == null) {
            System.out.print("Enter your postcode\n? ");
            target[1] = inputScanner.nextLine().trim();
        }

        if (target[2] == null) {
            System.out.print("Enter the reading\n? ");
            target[2] = inputScanner.nextLine().trim();
        }
    }

}