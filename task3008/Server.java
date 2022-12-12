package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.javarush.task.task30.task3008.MessageType.*;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static void sendBroadcastMessage(Message message) {
        connectionMap.forEach((name, connection) -> {
            try {
                connection.send(message);
            } catch (IOException e) {
                ConsoleHelper.writeMessage("Server failed to send message to " + name);
            }
        });
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Enter server port:");
        int port = ConsoleHelper.readInt();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ConsoleHelper.writeMessage("Chat-Server is running.");
            while (true) {
                Socket socket = serverSocket.accept();
                new Handler(socket).start();
            }
        } catch (IOException e) {
            ConsoleHelper.writeMessage("Server error.");
        }

    }

    private static class Handler extends Thread {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            Connection connection = null;
            String userName = null;
            ConsoleHelper.writeMessage("New connection with: " + socket.getRemoteSocketAddress());
            try {
                connection = new Connection(socket);
                userName = serverHandshake(connection);
                sendBroadcastMessage(new Message(USER_ADDED, userName));
                notifyUsers(connection, userName);
                serverMainLoop(connection, userName);
            } catch (IOException | ClassNotFoundException e) {
                ConsoleHelper.writeMessage("Exchange error with the remote address: " + socket.getRemoteSocketAddress());
            } finally {
                ConsoleHelper.writeMessage("Connection to " + socket.getRemoteSocketAddress() + " is closed.");
                if (userName != null) {
                    connectionMap.remove(userName);
                    sendBroadcastMessage(new Message(USER_REMOVED, userName));
                }
                try {
                    if (connection != null) connection.close();
                } catch (IOException ignored) {}
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
            for (String name : connectionMap.keySet())
                if (!name.equals(userName)) {
                    connection.send(new Message(USER_ADDED, name));
                }
        }

        private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException {
            while (true) {
                Message message = connection.receive();
                if (message.getType() == TEXT) {
                    sendBroadcastMessage(new Message(TEXT, userName + ": " + message.getData()));
                } else {
                    ConsoleHelper.writeMessage("Protocol error, message from " + userName + ".");
                }
            }
        }
    }
}
