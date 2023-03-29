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
    public void run() {
        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream());
             DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream())) {

            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead = in.read(buffer);
            String request = new String(buffer, 0, bytesRead);

            String[] requestParts = request.split("\0");
            String requestType = requestParts[0];
            String fileName = requestParts[1];
            String transferMode = requestParts[2];

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

    private void handleReadRequest(String fileName, DataOutputStream out) throws IOException {
        File file = new File(fileName);
        if (!file.exists()) {
            sendErrorPacket(out, "File not found");
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
    }

    private void handleWriteRequest(String fileName, DataInputStream in) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }

        try (FileOutputStream fos = new FileOutputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = in.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                if (bytesRead < BUFFER_SIZE) {
                    break;
                }
            }
        }
    }

    private void sendErrorPacket(DataOutputStream out, String errorMessage) throws IOException {
        byte[] errorBytes = errorMessage.getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(4 + errorBytes.length);
        buffer.putShort((short) 5);
        buffer.putShort((short) 1);
        buffer.put(errorBytes);
        out.write(buffer.array());
    }
}
