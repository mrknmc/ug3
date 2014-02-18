/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class Sender1 {
    public static final int MSG_SIZE = 1024;
    public static final int HEADER_SIZE = 2;
    public static final int EOF_FLAG_SIZE = 1;
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
     * @throws UnknownHostException
     * @throws SocketException
     * @throws FileNotFoundException
     */
    public Sender1(String hostName, int port, String fileName) throws UnknownHostException, SocketException, FileNotFoundException {
        this.address = InetAddress.getByName(hostName);
        this.port = port;
        this.socket = new DatagramSocket();
        inStream = new FileInputStream(fileName);

    }

    /**
     * Constructs a Sender1 object with properties encoded in an array of Strings;
     *
     * @param args arguments of the Sender1
     * @throws UnknownHostException
     * @throws SocketException
     * @throws FileNotFoundException
     */
    public Sender1(String[] args) throws UnknownHostException, SocketException, FileNotFoundException {
        this(args[0], Integer.parseInt(args[1]), args[2]);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender1 sender = null;

        try {
            sender = new Sender1(args);
            sender.send();
        } catch (UnknownHostException e) {
            System.out.printf("Host %s is unknown!\n", args[0]);
        } catch (FileNotFoundException e) {
            System.out.printf("File %s not found!", args[2]);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sender != null) {
                sender.close();
            }
        }
        System.out.println("File sent.");
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
                System.out.println("No host name specified!");
            } else if (args.length < 2) {
                System.out.println("No port number specified!");
            } else {
                System.out.println("No file name specified!");
            }
            return false;
        }
        return true;
    }

    protected int getMsgSize() {
        return MSG_SIZE;
    }

    int getTotalSize() {
        return HEADER_SIZE + MSG_SIZE + EOF_FLAG_SIZE;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public FileInputStream getInStream() {
        return inStream;
    }

    /**
     * Closes all of the running transactions etc.
     */
    public void close() {
        getSocket().close();
        try {
            getInStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends packets to the receiver.
     *
     * @throws IOException
     */
    public void send() throws IOException {
        DatagramPacket packet;
        DatagramSocket socket = getSocket();
        FileInputStream inStream = getInStream();

        byte[] byteArray = new byte[getMsgSize()];
        boolean fileRead = false;
        short counter = 0;
        while (!fileRead) {
            fileRead = inStream.read(byteArray) == -1;
            packet = makePacket(byteArray, counter, fileRead);
            socket.send(packet);
            counter++;
        }
    }

    /**
     * Creates the packet to be sent. This contains the sequence number,
     * whether we are sending the last packet and the data array.
     *
     * @param data     message data that we are sending.
     * @param sequence sequence number sent in the header.
     * @param fileRead whether this is the last packet to be sent.
     * @return packet to be sent to the receiver.
     */
    public DatagramPacket makePacket(byte[] data, short sequence, boolean fileRead) {
        ByteBuffer buffer = ByteBuffer.allocate(getTotalSize());
        byte eof = fileRead ? (byte) 1 : (byte) 0;
        buffer.putShort(sequence);
        buffer.put(eof);
        buffer.put(data);
        return new DatagramPacket(buffer.array(), 0, buffer.capacity(), this.address, this.port);
    }

}
