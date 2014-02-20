/* Mark Nemec s1140740 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;


public class Sender2 extends Sender1 {

    public static final int DEFAULT_TIMEOUT = 2000;
    private int curACK = 0;

    public Sender2(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender2 sender = new Sender2(args);
        try {
            sender.send();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sender.close();
        }
        System.out.println("File sent.");
    }

    public int getCurACK() {
        return curACK;
    }

    public void setCurAck(int newAck) {
        this.curACK = newAck;
    }

    /**
     * Waits until it gets acknowledgement from the receiver.
     *
     * @param timeout timeout in milliseconds.
     * @throws IOException
     */
    public int waitForACK(int timeout) throws IOException {
        DatagramSocket socket = getSocket();
        byte[] buf = new byte[1];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        socket.setSoTimeout(timeout);
        while (true) {
            socket.receive(packet);
            int recACK = (int) packet.getData()[0];
            if (recACK == curACK) {
                System.out.printf("Received ACK %d\n", recACK);
                curACK = curACK == 0 ? 1 : 0;
                return recACK;
            }
        }
    }

    @Override
    protected void sendPacket(DatagramPacket packet) throws IOException {
        DatagramSocket socket = getSocket();
        while (true) {
            try {
                socket.send(packet);
                waitForACK(DEFAULT_TIMEOUT);
                break;
            } catch (SocketTimeoutException e) {
                System.out.printf("Timed out waiting for ACK %d\n", getCurACK());
            }
        }
    }

}
