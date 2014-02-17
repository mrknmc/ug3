/* Mark Nemec s1140740 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import static java.util.Arrays.copyOfRange;


public class Receiver1 {

    public static void main(String[] args) {
        FileOutputStream outStream = null;
        DatagramSocket socket = null;

        if (!validateArgs(args)) {
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try {
            File outFile = createFile(args[1]);
            outStream = new FileOutputStream(outFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        byte[] buf = new byte[Sender1.MSG_SIZE + Sender1.HEADER_SIZE + Sender1.EOF_FLAG_SIZE];
        ByteBuffer packetData = ByteBuffer.allocate(Sender1.MSG_SIZE);
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        boolean eof = false;

        try {
            socket = new DatagramSocket(portNumber);
            while (!eof) {
                socket.receive(packet);
                eof = extractPacket(packet, packetData);
                outStream.write(packetData.array());
                packetData.clear();
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
            try {
                outStream.flush();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("File received.");
    }

    private static File createFile(String fileName) throws IOException {
        File outFile = new File(fileName);
        if (!outFile.exists()) {
            if (!outFile.createNewFile()) {
                throw new IOException("Could not create the output file!");
            }
        }
        return outFile;
    }

    public static boolean extractPacket(DatagramPacket packet, ByteBuffer dest) {
        byte[] data = packet.getData();
        int sequence = ByteBuffer.wrap(copyOfRange(data, 0, 4)).getInt();
        byte eof = data[4];
        System.out.println(sequence);
        dest.put(data, 5, data.length - 5);
        return (int) eof == 1;
    }

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
}
