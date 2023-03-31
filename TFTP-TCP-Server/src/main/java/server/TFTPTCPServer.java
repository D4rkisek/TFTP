package server;

import java.io.*;
import java.net.*;

public class TFTPTCPServer {
    private static final int DEFAULT_PORT = 9222;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept(); // Accepting client's connection
                // Ensuring multi-client functionality
                new Thread(new TFTPTCPServerThread(clientSocket)).start();
            }
        } catch (IOException e) { // Exception handler
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}