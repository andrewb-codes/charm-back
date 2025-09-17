package ru.andrewb.charm.back;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class CharmBackServerRunner {

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080);
             Socket socket = serverSocket.accept();
             DataInputStream requestStream = new DataInputStream(socket.getInputStream());
             DataOutputStream responseStream = new DataOutputStream(socket.getOutputStream());
             Scanner scanner = new Scanner(System.in)
        ) {
            String request;
            while (!(request = requestStream.readUTF()).equals("stop")) {
                System.out.println("Client request: " + request);
                String response = scanner.nextLine();
                responseStream.writeUTF(response);
            }
        }
    }
}
