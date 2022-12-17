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
        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            ConsoleHelper.writeMessage("Chat server is running.");
            while (true) {
                Socket newConnection = serverSocket.accept();
                new Handler(newConnection).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Server error.");
        }
    }

    public static void sendBroadcastMessage(Message message) {
        connectionMap.values().forEach(connection -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Server failed to send a message to " + connection.getRemoteSocketAddress());
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
            String userName = null;
            ConsoleHelper.writeMessage("New connection: " + socket.getRemoteSocketAddress());
            try (Connection connection = new Connection(socket)) {
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Exchange error with: " + socket.getRemoteSocketAddress());
            } finally {
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(USER_REMOVED, userName));
                    ConsoleHelper.writeMessage("Connection closed. " + socket.getRemoteSocketAddress());
                }
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(NAME_REQUEST));
                Message reply = connection.receive();
                if (reply.getType() != USER_NAME ||
                        reply.getData().isEmpty() ||
                        connectionMap.containsKey(reply.getData()))
                    continue;
                connectionMap.put(reply.getData(), connection);
                connection.send(new Message(NAME_ACCEPTED));
                return reply.getData();
            }
        }

        private void notifyUsers(Connection connection, String userName) throws IOException {
            for (String name : connectionMap.keySet()) {
                if (!userName.equals(name)) connection.send(new Message(USER_ADDED, name));
            }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == TEXT) {
                    sendBroadcastMessage(new Message(TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Message type error. " + connection.getRemoteSocketAddress());
                }
            }
        }
    }
}
