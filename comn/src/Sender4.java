/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class Sender4 {
    private static final int MSG_SIZE = 1024;
    private static final int HEADER_SIZE = 3;
    private int timeout;
    private int windowSize;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;
    private FileInputStream inStream;
    private volatile ConcurrentMap<Integer, Boolean> sendPackets = new ConcurrentHashMap<Integer, Boolean>();
    private volatile int nextSeqNum = 1;
    private volatile int base = 1;

    private volatile boolean done = false;
    private Thread receiverThread = new Thread(new SocketReceiver());
    private ExecutorService service;

    /**
     * Constructs a Sender1 object with given properties.
     *
     * @param hostName   the address of the server.
     * @param port       the port of the server.
     * @param fileName   the file name of the file to transfer.
     * @param timeout    timeout time
     * @param windowSize size of the window
     */
    public Sender4(String hostName, int port, String fileName, int timeout, int windowSize) throws IOException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.socket = new DatagramSocket();
        this.socket.setSoTimeout(timeout);
        this.inStream = new FileInputStream(fileName);
        this.timeout = timeout;
        this.windowSize = windowSize;
        this.receiverThread.start();
        this.service = Executors.newFixedThreadPool(windowSize);
    }

    /**
     * Constructs a Sender1 object with properties encoded in an array of Strings;
     *
     * @param args arguments of the Sender1
     */
    public Sender4(String[] args) throws IOException {
        this(args[0], Integer.parseInt(args[1]), args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
    }

    /**
     * Converts an int into a specified number of bytes
     *
     * @param value      integer value to convert
     * @param numOfBytes how many bytes to use for the int
     * @return int converted into a byte array
     */
    public static byte[] intToBytes(int value, int numOfBytes) {
        byte[] byteArray = new byte[numOfBytes];
        for (int i = 0; i < numOfBytes; i++) {
            int shift = 8 * i;
            byteArray[i] = (byte) ((value >> shift) & 0xFF);
        }
        return byteArray;
    }

    /**
     * Returns true if the arguments are valid for this sender.
     *
     * @param args arguments to be validated.
     * @return the validity of the arguments.
     */
    public static boolean validateArgs(String[] args) {
        if (args.length < 5) {
            if (args.length < 1) {
                System.err.println("No host name specified!");
            } else if (args.length < 2) {
                System.err.println("No port number specified!");
            } else if (args.length < 3) {
                System.err.println("No file name specified!");
            } else if (args.length < 4) {
                System.err.println("No timeout specified!");
            } else if (args.length < 5) {
                System.err.println("No window size specified!");
            }
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        long time, size;

        Sender4 sender = null;
        try {
            sender = new Sender4(args);
            size = sender.inStream.getChannel().size();
            time = System.currentTimeMillis();
            sender.send();
            time = System.currentTimeMillis() - time;
            System.out.println(size / (double) time);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sender != null) {
                sender.close();
            }
        }
    }

    /**
     * Reads a file and sends packets to the receiver.
     *
     * @throws IOException
     */
    public void send() throws IOException {
        DatagramPacket packet;
        byte[] byteArray = new byte[getMsgSize()];
        byte[] nextByteArray = new byte[getMsgSize()];
        int size = inStream.read(byteArray);
        int nextSize;
        boolean fileRead = size == -1;
        int counter = 1;

        while (!fileRead) {
            nextSize = inStream.read(nextByteArray);
            fileRead = nextSize == -1;
            packet = makePacket(byteArray, counter, size, fileRead);
            sendPacket(packet);
            counter++;
            size = nextSize;
            byteArray = nextByteArray.clone();
        }
        //System.out.println("File sent.");
    }

    /**
     * Resend all the packets that are not yet ACKed.
     */
    private void timeout(DatagramPacket packet) throws IOException {
        System.out.println("Resending Packet.");
        socket.send(packet);
    }

    /**
     * Send an individual packet.
     *
     * @param packet packet to be sent
     * @throws IOException
     */
    private void sendPacket(DatagramPacket packet) throws IOException {
        while (true) {
            if (nextSeqNum < base + windowSize) {
                // We've not sent all possible packets yet
                sendPackets.put(nextSeqNum, false);
                System.out.printf("Sent packet %d\n", nextSeqNum);
                socket.send(packet);
                // Start a timer for this packet
                service.submit(new Timer(nextSeqNum, packet));
                nextSeqNum += 1;
                break;
            } else {
                // We've sent all the possible packets - now we wait
                Thread.yield();
            }
        }
    }

    /**
     * Extracts an ACK number from the packet.
     *
     * @param packet packet that was received.
     * @return ACK number
     */
    private int extractACK(DatagramPacket packet) {
        byte[] data = packet.getData();
        return ByteBuffer.wrap(new byte[]{0, 0, data[1], data[0]}).getInt();
    }

    /**
     * Closes all of the running transactions etc.
     */
    public void close() throws IOException, InterruptedException {
        while (base < nextSeqNum) {
            // Wait until all packets are ACKed
            Thread.yield();
        }
        done = true;
        receiverThread.join();
        service.shutdown();
        socket.close();
        inStream.close();
    }

    /**
     * Returns the message size of a packet.
     *
     * @return size of a message of a packet.
     */
    public int getMsgSize() {
        return MSG_SIZE;
    }

    /**
     * Returns the total size of a packet.
     *
     * @return total size of a packet.
     */
    public int getTotalSize() {
        return MSG_SIZE + HEADER_SIZE;
    }

    /**
     * Creates the packet to be sent. This contains the sequence number,
     * whether we are sending the last packet and the data array.
     *
     * @param data     message data that we are sending.
     * @param sequence sequence number sent in the header.
     * @param size     size of the data to be sent.
     * @return packet to be sent to the receiver.
     */
    private DatagramPacket makePacket(byte[] data, int sequence, int size, boolean fileRead) {
        ByteBuffer buffer = ByteBuffer.allocate(getTotalSize());
        byte eof = fileRead ? (byte) 1 : (byte) 0;
        byte[] byteSequence = intToBytes(sequence, 2);
        buffer.put(byteSequence);
        buffer.put(eof);
        buffer.put(data, 0, size);
        return new DatagramPacket(buffer.array(), 0, buffer.position(), address, port);
    }

    /**
     * Receives an acknowledgement packet.
     *
     * @param packet packet that was received.
     */
    private synchronized void receivePacket(DatagramPacket packet) throws IOException {
        socket.receive(packet);
        int recACK = extractACK(packet);
        System.out.printf("Received ACK %d\n", recACK);
        if (recACK < base) {
            // Ignore smaller ACKs
            return;
        }
        if (recACK >= base && recACK <= base + windowSize) {
            // acknowledge this
            sendPackets.put(recACK, true);
            if (recACK == base) {
                base += 1;
            }
        }
    }

    private class Timer implements Runnable {

        private long startTime;
        private int sequence;
        private DatagramPacket packet;

        public Timer(int sequence, DatagramPacket packet) {
            this.sequence = sequence;
            this.packet = packet;
            startTime = System.currentTimeMillis();
        }

        /**
         * Runs on a background thread for each packet
         * computing whether it should time out.
         */
        @Override
        public void run() {
            long nowTime;
            // While not acked
            while (!sendPackets.get(sequence)) {
                // Check if we should timeout
                nowTime = System.currentTimeMillis();
                if (nowTime - startTime >= timeout) {
                    // Timeout the current packet
                    System.out.println("Timed out.");
                    try {
                        timeout(packet);
                        startTime = nowTime;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Restarting timer.");
                }
            }
            // Let the thread die if it's acked and remove from the map
            sendPackets.remove(sequence);
        }
    }

    private class SocketReceiver implements Runnable {

        /**
         * Runs on a background thread listening for incoming
         * acknowledgement packets which are then sent to
         * the main thread.
         */
        @Override
        public void run() {
            DatagramPacket packet = new DatagramPacket(new byte[2], 2);
            while (!done) {
                try {
                    receivePacket(packet);
                } catch (SocketTimeoutException e) {
                    if (done) {
                        // we've sent the file
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
