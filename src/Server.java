import Data.Co2Message;
import Data.TimestampedCo2Record;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final Object lock = new Object();

    public static final Path CSVPATH = Path.of("./Co2_Readings.csv");

    final static ThreadPoolExecutor clientPool = new ThreadPoolExecutor(
            4, 4, 2, TimeUnit.MINUTES,
            new ArrayBlockingQueue<Runnable>(4)
    );

    public static void main(String[] args) throws IOException {

        System.out.println("Opening connection");

        ServerSocket socket = new ServerSocket(getPort(args), 0, getAddress(args));

        System.out.printf("Address: %s\nport: %d\n", socket.getInetAddress(), socket.getLocalPort());

        while (true) {
            final Socket clientConnection = socket.accept();
            try {
                clientPool.execute(new HandleClient(clientConnection));
            } catch (RejectedExecutionException e) {
                clientConnection.getOutputStream().write("NO".getBytes());
                clientConnection.close();
            }
        }
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
        synchronized (Server.lock) {
            try (FileOutputStream output = new FileOutputStream(Server.CSVPATH.toFile(), true)) {
                entry.intoStream(new BufferedOutputStream(System.out));
                entry.intoStream(new BufferedOutputStream(output));
            }
        }
        // Close client connection

        client.close();
    }
}