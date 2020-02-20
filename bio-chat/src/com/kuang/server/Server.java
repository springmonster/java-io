package com.kuang.server;

import com.kuang.config.Config;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<Integer, BufferedWriter> clientMap = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        new Server().start();
    }

    private void start() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(Config.SERVER_PORT);
            System.out.println("server start...");
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("client connected, port is " + socket.getPort());
                new Thread(new ServerHandler(this, socket)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized boolean quit(String msg) {
        return Config.QUIT_COMMAND.equalsIgnoreCase(msg);
    }

    public synchronized void addClient(Socket socket) throws IOException {
        if (socket == null) {
            return;
        }
        int key = socket.getPort();
        BufferedWriter value = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        clientMap.put(key, value);
    }

    public synchronized void removeClient(Socket socket) throws IOException {
        if (socket == null) {
            return;
        }
        int key = socket.getPort();
        if (clientMap.containsKey(key)) {
            socket.close();
            clientMap.remove(key);
        }
    }

    public synchronized void broadcastMessage(Socket socket, String msg) throws IOException {
        for (Integer port :
                clientMap.keySet()) {
            if (!port.equals(socket.getPort())) {
                BufferedWriter writer = clientMap.get(port);
                writer.write(msg);
                writer.newLine();
                writer.flush();
            }
        }
    }
}
