/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;


public class Sender2 extends Sender1 {

    public static final int DEFAULT_TIMEOUT = 2000;
    private int curACK = 0;

    public Sender2(String[] args) throws UnknownHostException, SocketException, FileNotFoundException {
        super(args);
    }

    public static void main(String[] args) {
        if (!validateArgs(args)) {
            System.exit(1);
        }

        Sender2 sender = null;

        try {
            sender = new Sender2(args);
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
    public void waitForAck(int timeout) throws IOException {
        DatagramSocket socket = getSocket();
        int curACK = getCurACK();
        byte[] buf = new byte[1];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);

        socket.setSoTimeout(timeout);
        while (true) {
            socket.receive(packet);
            int recACK = (int) packet.getData()[0];
            if (recACK == curACK) {
                System.out.printf("Received ACK %d\n", curACK);
                curACK = curACK == 0 ? 1 : 0;
                setCurAck(curACK);
                break;
            }
        }
    }

    @Override
    public void send() throws IOException {
        DatagramPacket packet;
        DatagramSocket socket = getSocket();
        FileInputStream inStream = getInStream();
        byte[] byteArray = new byte[getMsgSize()];
        boolean fileRead = false;
        short counter = 0;
        boolean acked;

        while (!fileRead) {
            acked = false;
            fileRead = inStream.read(byteArray) == -1;
            packet = makePacket(byteArray, counter, fileRead);
            while (!acked) {
                socket.send(packet);
                System.out.printf("Sent packet %d\n", counter);
                try {
                    waitForAck(DEFAULT_TIMEOUT);
                    acked = true;
                } catch (SocketTimeoutException e) {
                    System.out.printf("Timed out waiting for ACK %d\n", getCurACK());
                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            counter++;
        }
    }

}
