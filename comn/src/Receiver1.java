/* Mark Nemec s1140740 */

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;

import static java.util.Arrays.copyOfRange;


public class Receiver1 {

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
        File outFile = new File(fileName);
        if (!outFile.exists()) {
            if (!outFile.createNewFile()) {
                throw new IOException("Could not create the output file!");
            }
        }
        this.outStream = new FileOutputStream(outFile);

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

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Receiver1 receiver = null;

        try {
            receiver = new Receiver1(args);
            receiver.receive();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (receiver != null) {
                receiver.close();
            }
        }
        System.out.println("File received.");
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
                System.out.println("No port number specified!");
            } else {
                System.out.println("No file name specified!");
            }
            return false;
        }
        return true;
    }

    public int getMsgSize() {
        return Sender1.MSG_SIZE;
    }

    public int getTotalSize() {
        return Sender1.HEADER_SIZE + Sender1.MSG_SIZE + Sender1.EOF_FLAG_SIZE;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public FileOutputStream getOutStream() {
        return outStream;
    }

    /**
     * Extracts information from the packet.
     *
     * @param packet packet that we extract from.
     * @param dest   destination ByteBuffer that we write to.
     * @return sequence number of this packet.
     * @throws RepeatedPacket
     */
    protected short extractPacket(DatagramPacket packet, ByteBuffer dest) throws RepeatedPacket {
        dest.clear();
        byte[] data = packet.getData();
        short sequence = ByteBuffer.wrap(copyOfRange(data, 0, 2)).getShort();
        byte eof = data[2];
        dest.put(data, 3, data.length - 3);
        return eof == 1 ? -1 : sequence;
    }

    /**
     * Receives packets sent from the sender.
     *
     * @throws IOException
     * @throws NotImplementedException
     */
    protected void receive() throws IOException, NotImplementedException {
        byte[] buf = new byte[getTotalSize()];
        DatagramSocket socket = getSocket();
        FileOutputStream outStream = getOutStream();
        ByteBuffer packetData = ByteBuffer.allocate(getMsgSize());
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int sequence = 0;

        while (sequence != -1) {
            socket.receive(packet);
            try {
                sequence = extractPacket(packet, packetData);
            } catch (RepeatedPacket e) {
                throw new NotImplementedException();
            }
            outStream.write(packetData.array());
        }
    }

    /**
     * Closes all of the running transactions etc.
     */
    protected void close() {
        getSocket().close();
        FileOutputStream outStream = getOutStream();
        try {
            outStream.flush();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Exception thrown when a packet has been sent twice in a row.
     */
    protected class RepeatedPacket extends Exception {
    }
}
