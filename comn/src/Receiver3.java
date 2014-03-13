/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver3 {
    private static final int MSG_SIZE = 1024;
    private static final int HEADER_SIZE = 3;
    private DatagramSocket socket;
    private FileOutputStream outStream;
    private int curACK = 0;
    private int curSeq = -1;

    /**
     * Constructs a Receiver1 object from the given properties.
     *
     * @param port     port number the receiver listens on.
     * @param fileName name of the file the receiver writes to.
     * @throws IOException
     */
    public Receiver3(int port, String fileName) throws IOException {
        this.socket = new DatagramSocket(port);
        this.outStream = new FileOutputStream(fileName);
    }

    /**
     * Constructs a Receiver object from properties encoded in a String array.
     *
     * @param args arguments of the Receiver1
     * @throws IOException
     */
    public Receiver3(String[] args) throws IOException {
        this(Integer.parseInt(args[0]), args[1]);
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
     * Returns true if the arguments are valid for this receiver.
     *
     * @param args arguments to be validated.
     * @return the validity of the arguments.
     */
    public static boolean validateArgs(String[] args) {
        if (args.length < 2) {
            if (args.length < 1) {
                System.err.println("No port number specified!");
            } else {
                System.err.println("No file name specified!");
            }
            return false;
        }
        return true;
    }

    public static void main(String[] args) throws IOException {
        if (!validateArgs(args)) {
            System.exit(1);
        }
        Receiver3 receiver = null;

        try {
            receiver = new Receiver3(args);
            receiver.receive();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (receiver != null) {
                receiver.close();
            }
        }
    }

    /**
     * Closes all of the running transactions etc.
     */
    public void close() throws IOException {
        socket.close();
        outStream.flush();
        outStream.close();
    }

    public int getMsgSize() {
        return MSG_SIZE - HEADER_SIZE;
    }

    public int getTotalSize() {
        return MSG_SIZE;
    }

    private int extractPacket(DatagramPacket packet, ByteBuffer dest) {
        dest.clear();
        byte[] data = packet.getData();
        int sequence = ByteBuffer.wrap(new byte[]{0, 0, data[1], data[0]}).getInt();
        byte eof = data[2];
        dest.put(data, HEADER_SIZE, packet.getLength() - HEADER_SIZE);
        if (sequence == curSeq) {
            return -2;
        }
        curSeq = sequence;
        return eof == 1 ? -1 : sequence;
    }

    public void receive() throws IOException {
        byte[] buf = new byte[getTotalSize()];
        ByteBuffer packetData = ByteBuffer.allocate(getMsgSize());
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int sequence;

        while (true) {
            socket.receive(packet);
            sequence = extractPacket(packet, packetData);
            System.out.printf("Received packet %d.\n", sequence);
            if (sequence == curACK) {
                // We ok
                sendACK(packet);
                outStream.write(packetData.array());
                curACK += 1;
            } else if (sequence == -1) {
                // It over
                sendACK(packet);
                break;
            }
        }
    }

    /**
     * Sends the acknowledgement to the originator of the received packet.
     *
     * @param receivedPacket packet we are acknowledging.
     */
    private void sendACK(DatagramPacket receivedPacket) throws IOException {
        DatagramPacket packet = makeACKPacket(receivedPacket);
        socket.send(packet);
        System.out.printf("Sent ACK %d.\n", curACK);
    }

    private DatagramPacket makeACKPacket(DatagramPacket receivedPacket) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        byte[] byteExpectedSeqNum = intToBytes(curACK, 2);
        buffer.put(byteExpectedSeqNum);
        return new DatagramPacket(buffer.array(), buffer.capacity(), receivedPacket.getAddress(), receivedPacket.getPort());
    }
}
