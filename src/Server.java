import Data.Co2Message;
import Data.TimestampedCo2Record;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.OptionalInt;

public class Server {

    public static final Path CSVPATH = Path.of("./Co2_Readings.csv");

    static HandleClient[] clients = new HandleClient[4];

    public static void main(String[] args) throws IOException {

        System.out.println("Opening connection");

        ServerSocket socket = new ServerSocket(getPort(args), 0, getAddress(args));

        System.out.printf("Address: %s\nport: %d\n", socket.getInetAddress(), socket.getLocalPort());

        while (true) {
            final Socket clientConnection = socket.accept();
            final OptionalInt slot = getFreeClientSlot();
            if (slot.isEmpty()) {
                clientConnection.getOutputStream().write("NO".getBytes());
                clientConnection.close();
                continue;
            }
            final HandleClient handler = clients[slot.getAsInt()];
            handler.client = clientConnection;
            handler.start();
        }
    }

    private static OptionalInt getFreeClientSlot() {
        for (int i = 0; i < clients.length; i += 1) {
            if (clients[i] == null) {
                clients[i] = new HandleClient(null);
                return OptionalInt.of(i);
            }
            if (clients[i].isAlive() && !clients[i].isInterrupted())
                return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    private static InetAddress getAddress(String[] _args) {
        return InetAddress.getLoopbackAddress();
    }
    private static short getPort(String[] _args) {
        return (short)13337;
    }
}

final class HandleClient extends Thread {
    static final String WELCOME_MESSAGE = "all your data is belong to us";
    Socket client;

    HandleClient(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        super.run();
        try {
            serveOne();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void serveOne() throws IOException {
        // give the all clear and send the expected message

        final DataOutputStream streamToClient = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));

        streamToClient.write("OK".getBytes());
        streamToClient.writeInt(WELCOME_MESSAGE.length());
        streamToClient.write(WELCOME_MESSAGE.getBytes());
        streamToClient.flush();

        // receive message
        final var streamFromClient = new BufferedInputStream(client.getInputStream());

        // Convert into correct format
        final Co2Message reading = Co2Message.fromStream(streamFromClient);

        // Timestamp and record
        final TimestampedCo2Record entry = reading.timestamp(LocalDateTime.now());

        // For the moment, we're printing to stdout :)
        entry.intoStream(new BufferedOutputStream(System.out));
        System.out.println();

        // Close client connection

        client.close();
    }
}