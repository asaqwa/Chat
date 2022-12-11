package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.javarush.task.task30.task3008.MessageType.*;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter server port:");
        try (ServerSocket socket =  new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Chat-Server is running.");

            while (true) {
                new Handler(socket.accept()).start();
            }

        } catch(IOException e){
            ConsoleHelper.writeMessage("Error occurred while starting or running the server.");
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((name, connection) -> {
            try {
                connection.send(message);
            } catch (IOException ignored) {
                ConsoleHelper.writeMessage("Server failed to send message to " + name);
            }
        });
    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                try {
                    connection.send(new Message(NAME_REQUEST, "Enter your name:"));
                    Message message = connection.receive();
                    if (message.getType() != USER_NAME
                    || message.getData().isEmpty()
                    || connectionMap.containsKey(message.getData())) {
                        continue;
                    }
                    connectionMap.put(message.getData(), connection);
                    connection.send(new Message(NAME_ACCEPTED, "Connection established."));
                    ConsoleHelper.writeMessage("New user " + message.getData() + " connected.");
                    return message.getData();
                } catch (IOException | ClassNotFoundException ignored) {
                }
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (!name.equals(userName))
                    connection.send(new Message(USER_ADDED, name));
            }
        }
    }
}
