package com.kuang.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerHandler implements Runnable {

    private Server server;
    private Socket socket;

    public ServerHandler(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            // add client
            server.addClient(socket);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                String broadcastMsg = "客户端[" + socket.getPort() + "]: " + msg;
                System.out.println(broadcastMsg);

                // broadcast msg
                server.broadcastMessage(socket, broadcastMsg);

                if (server.quit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.removeClient(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
