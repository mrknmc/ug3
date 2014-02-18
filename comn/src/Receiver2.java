/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver2 extends Receiver1 {

    private int curAck = 0;
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

    public int getCurAck() {
        return curAck;
    }

    public void setCurAck(int curAck) {
        this.curAck = curAck;
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
    protected void sendAck(DatagramPacket receivedPacket) {
        DatagramSocket socket = getSocket();
        DatagramPacket packet = makeAckPacket(receivedPacket);
        int curACK = getCurAck();
        try {
            System.out.printf("Sending ACK %d\n", curACK);
            socket.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        curACK = curACK == 0 ? 1 : 0;
        setCurAck(curACK);
    }

    @Override
    protected short extractPacket(DatagramPacket packet, ByteBuffer dest) throws RepeatedPacket {
        short sequence = super.extractPacket(packet, dest);
        if (sequence == getCurSeq()) {
            throw new RepeatedPacket();
        }
        setCurSeq(sequence);
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
        boolean repeated;

        while (sequence != -1) {
            repeated = true;
            socket.receive(packet);
            while (repeated) {
                try {
                    sequence = extractPacket(packet, packetData);
                    System.out.printf("Received packet %d\n", sequence);
                    sendAck(packet);
                    repeated = false;
                } catch (RepeatedPacket e) {
                    System.out.printf("Received duplicate packet %d\n", sequence);
                    e.printStackTrace();
                }
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
    protected DatagramPacket makeAckPacket(DatagramPacket receivedPacket) {
        byte[] data = {(byte) getCurAck()};
        return new DatagramPacket(data, data.length, receivedPacket.getAddress(), receivedPacket.getPort());
    }

}
