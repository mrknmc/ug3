/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class Sender3 {
    private static final int MSG_SIZE = 1024;
    private static final int HEADER_SIZE = 3;
    private static final int DEFAULT_TIMEOUT = 2000;
    private static final int WINDOW_SIZE = 2;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;
    private FileInputStream inStream;
    private DatagramPacket[] sendPackets = new DatagramPacket[Short.MAX_VALUE];
    private short nextSeqNum = 0;
    private int base = 0;

    private volatile boolean listen = false;
    private volatile boolean done = false;
    private Thread receiverThread = new Thread(new SocketReceiver());

    /**
     * Constructs a Sender1 object with given properties.
     *
     * @param hostName the address of the server.
     * @param port     the port of the server.
     * @param fileName the file name of the file to transfer.
     */
    public Sender3(String hostName, int port, String fileName) throws IOException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.socket = new DatagramSocket();
        this.inStream = new FileInputStream(fileName);
        this.receiverThread.start();
    }

    /**
     * Constructs a Sender1 object with properties encoded in an array of Strings;
     *
     * @param args arguments of the Sender1
     */
    public Sender3(String[] args) throws IOException {
        this(args[0], Integer.parseInt(args[1]), args[2]);
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
        if (args.length < 3) {
            if (args.length < 1) {
                System.err.println("No host name specified!");
            } else if (args.length < 2) {
                System.err.println("No port number specified!");
            } else {
                System.err.println("No file name specified!");
            }
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender3 sender = null;
        try {
            sender = new Sender3(args);
            sender.send();
        } catch (IOException e) {
            System.err.println(e.getMessage());
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
        int size = 0;
        int counter = 0;

        while (size != -1) {
            size = inStream.read(byteArray);
            packet = makePacket(byteArray, counter, size);
            sendPacket(packet);
            System.out.printf("Sent Packet %d.\n", counter);
            counter++;
        }
        while (base + 1 < nextSeqNum) {
        }
    }

    /**
     * Resend all the packets that are not yet ACKed.
     */
    private synchronized void timeout() throws IOException {
        System.out.println("Timed out.");
        for (int i = base; i < nextSeqNum; i++) {
            socket.send(sendPackets[i]);
            System.out.printf("Resent Packet %d.\n", i);
        }
    }

    /**
     * Send an individual packet.
     *
     * @param packet packet to be sent
     * @throws IOException
     */
    private void sendPacket(DatagramPacket packet) throws IOException {
        while (true) {
            if (nextSeqNum < base + WINDOW_SIZE) {
                // We've not sent all possible packets yet
                sendPackets[nextSeqNum] = packet;
                socket.send(packet);
                if (base == nextSeqNum) {
                    listen = true;
                }
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

    public void close() throws IOException, InterruptedException {
        done = true;
        receiverThread.join();
        socket.close();
        inStream.close();
    }

    public int getMsgSize() {
        return MSG_SIZE - HEADER_SIZE;
    }

    public int getTotalSize() {
        return MSG_SIZE;
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
    private DatagramPacket makePacket(byte[] data, int sequence, int size) {
        ByteBuffer buffer = ByteBuffer.allocate(getTotalSize());
        byte eof = size == -1 ? (byte) 1 : (byte) 0;
        size = size == -1 ? 0 : size;
        byte[] byteSequence = intToBytes(sequence, 2);
        buffer.put(byteSequence);
        buffer.put(eof);
        buffer.put(data, 0, size);
        return new DatagramPacket(buffer.array(), 0, buffer.position(), address, port);
    }

    /**
     * @param packet
     */
    private synchronized void receivePacket(DatagramPacket packet) {
        int recACK = extractACK(packet);
        System.out.printf("Received ACK %d\n", recACK);
        base = recACK + 1;
        if (base == nextSeqNum) {
            listen = true; // Stop the timer
        }
    }

    private class SocketReceiver implements Runnable {

        @Override
        public void run() {
            DatagramPacket packet = new DatagramPacket(new byte[2], 2);
            try {
                socket.setSoTimeout(DEFAULT_TIMEOUT);
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while (!done) {
                if (listen) {
                    try {
                        socket.receive(packet);
                        receivePacket(packet);
                    } catch (SocketTimeoutException e) {
                        if (done) {
                            return;
                        }
                        try {
                            timeout();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
