package com.javarush.task.task30.task3008;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    }
}
