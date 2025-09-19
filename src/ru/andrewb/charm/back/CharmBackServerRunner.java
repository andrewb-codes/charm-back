package ru.andrewb.charm.back;

import ru.andrewb.charm.back.controller.ProfileController;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.service.ProfileService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static ru.andrewb.charm.back.model.Commands.*;

public class CharmBackServerRunner {

    public static void main(String[] args) throws IOException {

        ProfileController controller = new ProfileController(new ProfileService(new ProfileDao()));

        try (ServerSocket serverSocket = new ServerSocket(8080);
             Socket socket = serverSocket.accept();
             DataInputStream requestStream = new DataInputStream(socket.getInputStream());
             DataOutputStream responseStream = new DataOutputStream(socket.getOutputStream());
        ) {
            String request;
            while (!(request = requestStream.readUTF()).equals("stop")) {
                String response;
                if (request.startsWith(SAVE.getPrefix())) {
                    response = controller.save(request.substring(SAVE.getPrefix().length()));
                } else if (request.startsWith(FIND_BY_ID.getPrefix())) {
                    response = controller.findById(request.substring(FIND_BY_ID.getPrefix().length()));
                } else if (request.startsWith(FIND_ALL.getPrefix())) {
                    response = controller.findAll(); // без аргументов
                } else if (request.startsWith(UPDATE.getPrefix())) {
                    response = controller.update(request.substring(UPDATE.getPrefix().length()));
                } else if (request.startsWith(DELETE.getPrefix())) {
                    response = controller.delete(request.substring(DELETE.getPrefix().length()));
                } else {
                    response = "Unsupported command";
                }

                System.out.println("Client request: " + request);
                responseStream.writeUTF(response);
            }
        }
    }
}
