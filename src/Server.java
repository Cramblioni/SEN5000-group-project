import Data.Co2Message;
import Data.TimestampedCo2Record;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final Object lock = new Object();

    public static final String CSVPATH = "./Co2_Readings.csv";

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
        if (_args.length < 1) {
            return InetAddress.getLoopbackAddress();
        }
        if (
                !Arrays.equals(_args[0].toCharArray(), "localhost".toCharArray())
            && !Character.isDigit(_args[0].toCharArray()[0])
        ) {
            System.out.println("Unknown target host (IP expected), resorting to default");
            return getAddress(new String[0]);
        }
        try {
            return InetAddress.getByName(_args[0].trim());
        } catch (UnknownHostException e) {
            System.out.println("Unknown target host, resorting to default");
            return getAddress(new String[0]);
        }
    }
    private static short getPort(String[] _args) {
        if (_args.length < 2) {
            return (short) 13337;
        }
        try {
            return Short.parseShort(_args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Malformed Port, resorting to default");
            return getPort(new String[0]);
        }
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
        final InputStream streamFromClient = new BufferedInputStream(client.getInputStream());

        // Convert into correct format
        final Co2Message reading = Co2Message.fromStream(streamFromClient);

        // Timestamp and record
        final TimestampedCo2Record entry = reading.timestamp(LocalDateTime.now());

        // Printing to both our `.csv` (saving data) file and stdout (logging data)
        synchronized (Server.lock) {
            try (FileOutputStream output = new FileOutputStream(Server.CSVPATH, true)) {
                entry.intoStream(new BufferedOutputStream(System.out));
                entry.intoStream(new BufferedOutputStream(output));
            }
        }
        // Close client connection

        client.close();
    }
}