/* Mark Nemec s1140740 */

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;


public class Sender1 {

    public static final int MSG_SIZE = 1024;
    public static final int HEADER_SIZE = 4;
    public static final int EOF_FLAG_SIZE = 1;

    public static void main(String[] args) {
        DatagramPacket packet;
        InetAddress address = null;
        DatagramSocket socket = null;
        FileInputStream fileInStream = null;

        if (!validateArgs(args)) {
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[1]);
        String fileName = args[2];

        try {
            address = InetAddress.getByName(args[0]);
            socket = new DatagramSocket();
        } catch (UnknownHostException e) {
            System.out.printf("%s is an unknown host!\n", args[0]);
            System.exit(1);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        try {
            byte[] byteArray = new byte[MSG_SIZE];
            boolean fileRead = false;
            int counter = 0;
            fileInStream = new FileInputStream(fileName);
            while (!fileRead) {
                fileRead = fileInStream.read(byteArray) == -1;
                packet = makePacket(byteArray, counter, fileRead, address, portNumber);
                socket.send(packet);
                System.out.println(counter);
                counter++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Given file doesn't exist!");
            System.exit(1);
        } catch (IOException e) {
            // IO Error when reading file
            e.printStackTrace();
            System.exit(1);
        } finally {
            try {
                fileInStream.close();
                socket.close();
            } catch (IOException e) {
                // IOError when closing file - lol Java
                e.printStackTrace();
            }
        }
        System.out.println("File sent.");
    }

    public static DatagramPacket makePacket(byte[] data, int sequence, boolean fileRead,
                                            InetAddress address, int portNumber) {
        ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE + MSG_SIZE + EOF_FLAG_SIZE);
        buffer.putInt(sequence);
        byte eof = fileRead ? (byte) 1 : (byte) 0;
        buffer.put(eof);
        buffer.put(data);
        return new DatagramPacket(buffer.array(), 0, buffer.capacity(), address, portNumber);
    }

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

}
