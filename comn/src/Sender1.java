/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;


public class Sender1 {
    public static final int MSG_SIZE = 1024;
    public static final int HEADER_SIZE = 3;
    private InetAddress address;
    private int port;
    private DatagramSocket socket;
    private FileInputStream inStream;

    /**
     * Constructs a Sender1 object with given properties.
     *
     * @param hostName the address of the server.
     * @param port     the port of the server.
     * @param fileName the file name of the file to transfer.
     */
    public Sender1(String hostName, int port, String fileName) throws IOException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.socket = new DatagramSocket();
        this.inStream = new FileInputStream(fileName);
    }

    /**
     * Constructs a Sender1 object with properties encoded in an array of Strings;
     *
     * @param args arguments of the Sender1
     */
    public Sender1(String[] args) throws IOException {
        this(args[0], Integer.parseInt(args[1]), args[2]);
    }

    public static void main(String[] args) throws IOException {
        if (!validateArgs(args)) {
            System.exit(1);
        }
        Sender1 sender = null;
        try {
            sender = new Sender1(args);
            sender.send();
            System.out.println("File sent.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sender != null) {
                sender.close();
            }
        }
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
            byteArray[i] = (byte) ((value >> 8 * i) & 0xFF);
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
     * Closes all of the running transactions etc.
     */
    public void close() throws IOException {
        socket.close();
        inStream.close();
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
        int counter = 0;

        while (!fileRead) {
            nextSize = inStream.read(nextByteArray);
            fileRead = nextSize == -1;
            packet = makePacket(byteArray, counter, size, fileRead);
            socket.send(packet);
            System.out.printf("Sent packet %d\n", counter);
            counter++;
            size = nextSize;
            byteArray = nextByteArray;
        }
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

}
