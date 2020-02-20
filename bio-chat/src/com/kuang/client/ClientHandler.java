package com.kuang.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ClientHandler implements Runnable {

    private final Client client;

    public ClientHandler(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String msg = bufferedReader.readLine();
                client.send(msg);

                if (client.quit(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
