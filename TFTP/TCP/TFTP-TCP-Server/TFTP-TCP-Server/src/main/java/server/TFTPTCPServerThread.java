package server;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

class TFTPTCPServerThread extends Thread {

    private final Socket clientSocket;
    private static final int BUFFER_SIZE = 512;

    public TFTPTCPServerThread(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() { // Stream objects form the client
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            // Initialisations
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = in.read(buffer);
            String request = new String(buffer, 0, bytesRead);

            String[] requestParts = request.split("\0");
            String requestType = requestParts[0];
            String fileName = requestParts[1];

            // Handling requests from the client
            if ("RRQ".equalsIgnoreCase(requestType)) {
                handleReadRequest(fileName, out);
            } else if ("WRQ".equalsIgnoreCase(requestType)) {
                handleWriteRequest(fileName, in);
            } else {
                System.err.println("Invalid request type: " + requestType);
            }
        } catch (IOException e) {
            System.err.println("Client handler exception: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Unable to close client socket: " + e.getMessage());
            }
        }
    }

    // RRQ
    private void handleReadRequest(String fileName, DataOutputStream out) throws IOException {
        // Checking whether the file exists, if true, then send an error packet instead
        File file = new File(fileName);
        if (!file.exists()) {
            sendErrorPacket(out, "File not found");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE]; // 512
            int bytesRead;
            // Reads the contents of a specified file
            while ((bytesRead = fis.read(buffer)) != -1) {
                // Sends them back to the client through the use of DataOutputStream object
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    // WRQ
    private void handleWriteRequest(String fileName, DataInputStream in) throws IOException {
        // Checking whether the file exists, if true, then delete
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                // Writes the contents of a file received from a DataInputStream object
                fos.write(buffer, 0, bytesRead);
                // If the last packet is not full size meaning not 512 size then break the loop since all the data has been sent
                if (bytesRead < BUFFER_SIZE) {
                    break;
                }
            }
        }
    }

    // Sends an error message to the client via a DataOutputStream object by creating a ByteBuffer object and writing the error message to it
    private void sendErrorPacket(DataOutputStream out, String errorMessage) throws IOException {
        byte[] errorBytes = errorMessage.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + errorBytes.length);
        buffer.putShort((short) 5);
        buffer.putShort((short) 1);
        buffer.put(errorBytes);
        out.write(buffer.array());
    }
}
