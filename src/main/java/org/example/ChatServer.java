package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class ChatServer {

    private static Map<String, List<ClientHandler>> rooms = new HashMap<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server started...");

        while (true) {
            Socket socket = serverSocket.accept();
            new ClientHandler(socket).start();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String roomCode;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                out.println("Enter room code to join or 'new' to create one:");
                String input = in.readLine();

                if (input.equals("new")) {
                    roomCode = generateRoomCode();
                    out.println("Room created with code: " + roomCode);
                } else {
                    roomCode = input;
                    out.println("Joined room: " + roomCode);
                }

                synchronized (rooms) {
                    rooms.putIfAbsent(roomCode, new ArrayList<>());
                    rooms.get(roomCode).add(this);
                }

                String message;
                while ((message = in.readLine()) != null) {
                    broadcast(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                leaveRoom();
            }
        }

        private void broadcast(String message) {
            synchronized (rooms) {
                for (ClientHandler client : rooms.get(roomCode)) {
                    client.out.println(message);
                }
            }
        }

        private void leaveRoom() {
            synchronized (rooms) {
                rooms.get(roomCode).remove(this);
                if (rooms.get(roomCode).isEmpty()) {
                    rooms.remove(roomCode);
                }
            }
        }

        private String generateRoomCode() {
            return UUID.randomUUID().toString().substring(0, 8);
        }
    }
}
