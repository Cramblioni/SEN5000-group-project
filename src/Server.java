import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.OptionalInt;

public class Server {

    public static final Path CSVPATH = Path.of("./Co2_Readings.csv");

    static HandleClient[] clients;

    public static void main(String[] args) throws IOException {

        System.out.println("Opening connection");

        ServerSocket socket = new ServerSocket(getPort(args), 1, getAddress(args));

        while (true) {
            final Socket clientConnection = socket.accept();
        }
    }

    private static OptionalInt getFreeClientSlot() {
        for (int i = 0; i < clients.length; i += 1) {
            continue;
        }
        return OptionalInt.empty();
    }

    private static InetAddress getAddress(String[] _args) {
        return InetAddress.getLoopbackAddress();
    }
    private static short getPort(String[] _args) {
        return (short)80085;
    }
}

final class HandleClient extends Thread {
    final Socket client;

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
        // receive message

        final byte[] message = {0,1,53,0,1,64,-127,0,0};

        // Convert into correct format
        final Co2Message reading = Co2Message.fromBytes(message);

        // Timestamp and record
        final TimestampedCo2Message entry = reading.timestamp(LocalDateTime.now());

        Files.write(Server.CSVPATH, entry.toCsvEntry(), StandardOpenOption.APPEND);

        // Close client connection

        client.close();
    }
}