/* Mark Nemec s1140740 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;


public class Sender3 extends Sender2 {

    private static final int WINDOW_SIZE = 2;
    private DatagramPacket[] sendPackets = new DatagramPacket[Short.MAX_VALUE];
    private short nextSeqNum = 0;
    private int base = 0;

    private volatile boolean listen = false;
    private volatile boolean done = false;
    private Thread receiverThread = new Thread(new SocketReceiver());

    public Sender3(String[] args) {
        super(args);
        receiverThread.start();
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender3 sender = new Sender3(args);
        try {
            sender.send();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        } finally {
            sender.close();
        }
    }

    /**
     * Resend all the packets that are not yet ACKed.
     */
    private synchronized void timeout() {
        System.out.println("Timed out.");
        try {
            for (int i = base; i < nextSeqNum; i++) {
                sendPacket(i);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    protected void sendPacket(DatagramPacket packet) throws IOException {
        DatagramSocket socket = getSocket();
        while (true) {
            if (nextSeqNum < base + WINDOW_SIZE) {
                // We've not sent all possible packets yet
                sendPackets[nextSeqNum] = packet;
                socket.send(packet);
                if (base == nextSeqNum) {
                    listen = true;
                }
                nextSeqNum += 1;
                break;
            } else {
                // We've sent all the possible packets - now we wait
                Thread.yield();
            }
        }
    }

    /**
     * Sends a packet at a particular index of sendPackets.
     *
     * @param idx index of the packet to be sent.
     * @throws IOException
     */
    private void sendPacket(int idx) throws IOException {
        System.out.println("Resending Packet.");
        getSocket().send(sendPackets[idx]);
    }

    /**
     * Extracts an ACK number from the packet.
     *
     * @param packet packet that was received.
     * @return
     */
    private int extractACK(DatagramPacket packet) {
        byte[] data = packet.getData();
        return ByteBuffer.wrap(new byte[]{0, 0, data[1], data[0]}).getInt();
    }

    @Override
    public void close() {
        this.done = true;
        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        super.close();
    }

    /**
     * @param packet
     */
    private synchronized void receivePacket(DatagramPacket packet) {
        int recACK = extractACK(packet);
        base = recACK + 1;
        if (base == nextSeqNum) {
            // Stop the timer
            System.out.printf("Received ACK %d\n", recACK);
            listen = true;
        }
    }

    private class SocketReceiver implements Runnable {
        DatagramSocket socket = getSocket();
        DatagramPacket packet = new DatagramPacket(new byte[2], 2);

        @Override
        public void run() {
            try {
                socket.setSoTimeout(DEFAULT_TIMEOUT);
            } catch (SocketException e) {
                System.err.println(e.getMessage());
            }
            while (!done) {
                if (listen) {
                    try {
                        socket.receive(packet);
                        receivePacket(packet);
                    } catch (SocketTimeoutException e) {
                        if (done) {
                            return;
                        }
                        timeout();
                    } catch (IOException e) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }

}
