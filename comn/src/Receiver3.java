/* Mark Nemec s1140740 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;


public class Receiver3 extends Receiver2 {

    public Receiver3(String[] args) throws IOException {
        super(args);
    }

    public static void main(String[] args) {
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
            sequence = extractPacket(packet, packetData);
            if (sequence != getCurACK()) {
                continue;
            }
            outStream.write(packetData.array());
            sendACK(packet);
            setCurACK(getCurACK() + 1);
        }
    }

    @Override
    protected DatagramPacket makeACKPacket(DatagramPacket receivedPacket) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        byte[] byteExpectedSeqNum = Sender1.intToBytes(getCurACK(), 2);
        buffer.put(byteExpectedSeqNum);
        return new DatagramPacket(buffer.array(), buffer.capacity(), receivedPacket.getAddress(), receivedPacket.getPort());
    }
}
