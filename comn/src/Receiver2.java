/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver2 {
    public static final int MSG_SIZE = 1024;
    public static final int HEADER_SIZE = 3;
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
    public Receiver2(int port, String fileName) throws IOException {
        this.socket = new DatagramSocket(port);
        this.outStream = new FileOutputStream(fileName);
    }

    /**
     * Constructs a Receiver object from properties encoded in a String array.
     *
     * @param args arguments of the Receiver1
     * @throws IOException
     */
    public Receiver2(String[] args) throws IOException {
        this(Integer.parseInt(args[0]), args[1]);
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

        Receiver2 receiver = null;
        try {
            receiver = new Receiver2(args);
            receiver.receive();
            System.out.println("File received.");
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
    protected void close() throws IOException {
        socket.close();
        outStream.flush();
        outStream.close();
    }

    /**
     * Sends the acknowledgement to the originator of the received packet.
     *
     * @param receivedPacket packet we are acknowledging.
     */
    protected void sendACK(DatagramPacket receivedPacket) throws IOException {
        DatagramPacket packet = makeACKPacket(receivedPacket);
        socket.send(packet);
        System.out.printf("Sent ACK %d.\n", curACK);
    }

    public int getMsgSize() {
        return MSG_SIZE - HEADER_SIZE;
    }

    public int getTotalSize() {
        return MSG_SIZE;
    }

    protected int extractPacket(DatagramPacket packet, ByteBuffer dest) {
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

    protected void receive() throws IOException {
        byte[] buf = new byte[getTotalSize()];
        ByteBuffer packetData = ByteBuffer.allocate(getMsgSize());
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int sequence = 0;

        while (true) {
            socket.receive(packet);
            sequence = extractPacket(packet, packetData);
            System.out.printf("Received packet %d.\n", sequence);
            if (sequence == -2) {
                // Repeated packet
                System.out.printf("Received duplicate packet %d.\n", sequence);
            } else {
                sendACK(packet);
                if (sequence == -1) {
                    break;
                } else {
                    // Only write data if not last packet
                    curACK = curACK == 0 ? 1 : 0;
                    outStream.write(packetData.array());
                }
            }
        }
    }

    /**
     * Creates the acknowledgement packet for a given received packet.
     *
     * @param receivedPacket packet we are acknowledging.
     * @return packet to be sent as acknowledgement
     */
    protected DatagramPacket makeACKPacket(DatagramPacket receivedPacket) {
        byte[] data = {(byte) curACK};
        return new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort());
    }

}
