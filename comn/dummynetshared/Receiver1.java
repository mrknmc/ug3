/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver1 {
    private static final int MSG_SIZE = 1024;
    private static final int HEADER_SIZE = 3;
    private DatagramSocket socket;
    private FileOutputStream outStream;

    /**
     * Constructs a Receiver1 object from the given properties.
     *
     * @param port     port number the receiver listens on.
     * @param fileName name of the file the receiver writes to.
     * @throws IOException
     */
    public Receiver1(int port, String fileName) throws IOException {
        this.socket = new DatagramSocket(port);
        this.outStream = new FileOutputStream(fileName);
    }

    /**
     * Constructs a Receiver object from properties encoded in a String array.
     *
     * @param args arguments of the Receiver1
     * @throws IOException
     */
    public Receiver1(String[] args) throws IOException {
        this(Integer.parseInt(args[0]), args[1]);
    }

    public static void main(String[] args) throws IOException {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Receiver1 receiver = null;
        try {
            receiver = new Receiver1(args);
            receiver.receive();
            System.out.println("File received.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (receiver != null) {
                receiver.close();
            }
        }
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

    /**
     * Returns the message size of a packet.
     * @return size of a message of a packet.
     */
    public int getMsgSize() {
        return MSG_SIZE;
    }

    /**
     * Returns the total size of a packet.
     * @return total size of a packet.
     */
    public int getTotalSize() {
        return MSG_SIZE + HEADER_SIZE;
    }

    /**
     * Extracts information from the packet.
     *
     * @param packet packet that we extract from.
     * @param dest   destination ByteBuffer that we write to.
     * @return sequence number of this packet.
     */
    private int extractPacket(DatagramPacket packet, ByteBuffer dest) {
        dest.clear();
        byte[] data = packet.getData();
        int sequence = ByteBuffer.wrap(new byte[]{0, 0, data[1], data[0]}).getInt();
        byte eof = data[2];
        dest.put(data, HEADER_SIZE, packet.getLength() - HEADER_SIZE);
        return eof == 1 ? -1 : sequence;
    }

    /**
     * Receives packets sent from the sender.
     *
     * @throws IOException
     */
    public void receive() throws IOException {
        byte[] buf = new byte[getTotalSize()];
        ByteBuffer packetData = ByteBuffer.allocate(getMsgSize());
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int sequence;

        do {
            socket.receive(packet);
            sequence = extractPacket(packet, packetData);
            System.out.printf("Received packet %d.\n", sequence);
            outStream.write(packetData.array());
        } while (sequence != -1);
    }

    /**
     * Closes all of the running transactions etc.
     */
    public void close() throws IOException {
        socket.close();
        outStream.flush();
        outStream.close();
    }

}