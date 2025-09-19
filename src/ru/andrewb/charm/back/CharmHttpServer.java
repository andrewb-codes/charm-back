package ru.andrewb.charm.back;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CharmHttpServer {

    private final ExecutorService executorService;

    public CharmHttpServer(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }

    public void start() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            while (true) {
                Socket connection = serverSocket.accept();
                System.out.println("-----Client connect-----");
                executorService.submit(() -> processConnection(connection));
            }
        }
    }

    private void processConnection(Socket connection) {
        try (connection;
             BufferedReader requestReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             DataOutputStream responseStream = new DataOutputStream(connection.getOutputStream())
        ) {
            while (!(requestReader.ready()));

            while (requestReader.ready()) {
                System.out.println(requestReader.readLine());
            }

            byte[] body = "Hi from CharmServer".getBytes();
            byte[] startString = "HTTP/1.1 200 OK\r\n".getBytes();
            byte[] headers = ("Content-Type: text/plain\r\n" +
                              "Content-Length: " + body.length + "\r\n" +
                              "\r\n").getBytes();

            responseStream.write(startString);
            responseStream.write(headers);
            responseStream.write(body);
            System.out.println("-----Client disconnect-----");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
