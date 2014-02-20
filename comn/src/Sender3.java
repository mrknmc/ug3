/* Mark Nemec s1140740 */

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;


public class Sender3 extends Sender2 {

    private static final int WINDOW_SIZE = 2;
    private DatagramPacket[] sendPackets = new DatagramPacket[Short.MAX_VALUE];
    private short nextSeqNum = 0;
    private int base = 0;
    private SocketReceiver socketReceiver = new SocketReceiver(this);
    private Thread receiverThread = new Thread(socketReceiver);

    public Sender3(String[] args) {
        super(args);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender3 sender = new Sender3(args);
        try {
            sender.send();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            sender.close();
        }
    }

    /**
     * Starts the timer of the receiver.
     */
    private void startTimer() {
        if (!receiverThread.isAlive()) {
            receiverThread.start();
        } else {
            socketReceiver.restart();
        }
    }

    /**
     * Resend all the packets that are not yet ACKed.
     */
    private void resend() {
        try {
            for (int i = base; i < nextSeqNum; i++) {
                sendPacket(i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void sendPacket(DatagramPacket packet) throws IOException {
        DatagramSocket socket = getSocket();
        while (true) {
            // Busy wait until it's true
            if (nextSeqNum < base + WINDOW_SIZE) {
                sendPackets[nextSeqNum] = packet;
                socket.send(packet);
                if (base == nextSeqNum) {
                    startTimer();
                }
                nextSeqNum += 1;
                break;
            }
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            receiverThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    private class SocketReceiver implements Runnable {

        private Sender3 sender;
        private boolean stop = false;

        public SocketReceiver(Sender3 sender) {
            this.sender = sender;
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

        private void restart() {
            this.stop = false;
        }

        @Override
        public void run() {
            DatagramSocket socket = sender.getSocket();
            byte[] buf = new byte[2];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);

            while (true) {
                if (!stop) {
                    try {
                        socket.setSoTimeout(DEFAULT_TIMEOUT);
                        socket.receive(packet);
                        int recACK = extractACK(packet);
                        base = recACK + 1;
                        if (base == nextSeqNum) {
//                        Stop the timer
                            System.out.printf("Received ACK %d\n", recACK);
                            stop = true;
                        } else {
//                    Start the timer with the same timeout I guess
//                    This will just re-run automagically
                        }
                    } catch (SocketTimeoutException e) {
//                    Start the timer
//                    This will just re-run automagically
//                    Tell the sender to resend all the packets
                        sender.resend();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

}
