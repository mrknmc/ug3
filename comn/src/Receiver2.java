/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver2 extends Receiver1 {

    private int curACK = 0;
    private int curSeq = -1;

    public Receiver2(String[] args) throws IOException {
        super(args);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }
        Receiver2 receiver = null;

        try {
            receiver = new Receiver2(args);
            receiver.receive();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (receiver != null) {
                receiver.close();
            }
        }
    }

    public int getCurACK() {
        return curACK;
    }

    public void setCurACK(int curACK) {
        this.curACK = curACK;
    }

    public int getCurSeq() {
        return curSeq;
    }

    public void setCurSeq(int curSeq) {
        this.curSeq = curSeq;
    }

    /**
     * Sends the acknowledgement to the originator of the received packet.
     *
     * @param receivedPacket packet we are acknowledging.
     */
    protected void sendACK(DatagramPacket receivedPacket) {
        DatagramSocket socket = getSocket();
        DatagramPacket packet = makeACKPacket(receivedPacket);
        try {
            System.out.printf("Sending ACK %d\n", curACK);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected int extractPacket(DatagramPacket packet, ByteBuffer dest) {
        int sequence = super.extractPacket(packet, dest);
        if (sequence == getCurSeq()) {
            return -2;
        }
        curSeq = sequence;
        return sequence;
    }

    @Override
    protected void receive() throws IOException {
        byte[] buf = new byte[getTotalSize()];
        DatagramSocket socket = getSocket();
        FileOutputStream outStream = getOutStream();
        ByteBuffer packetData = ByteBuffer.allocate(getMsgSize());
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        int sequence = 0;

        while (sequence != -1) {
            socket.receive(packet);
            while (true) {
                sequence = extractPacket(packet, packetData);
                if (sequence != -2) {
                    System.out.printf("Received packet %d\n", sequence);
                    sendACK(packet);
                    curACK = curACK == 0 ? 1 : 0;
                    break;
                }
                System.out.printf("Received duplicate packet %d\n", sequence);
            }
            outStream.write(packetData.array());
        }
    }

    /**
     * Creates the acknowledgement packet for a given received packet.
     *
     * @param receivedPacket packet we are acknowledging.
     * @return packet to be sent as acknowledgement
     */
    protected DatagramPacket makeACKPacket(DatagramPacket receivedPacket) {
        byte[] data = {(byte) getCurACK()};
        return new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort());
    }

}
