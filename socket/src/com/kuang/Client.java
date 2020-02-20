package com.kuang;

import java.io.*;
import java.net.Socket;

public class Client {
    private static final String QUIT = "quit";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    public static void main(String[] args) {
        Socket socket;
        BufferedWriter bufferedWriter = null;

        try {
            socket = new Socket(DEFAULT_SERVER_HOST, DEFAULT_SERVER_PORT);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            while (true) {
                BufferedReader bufferedReader1 = new BufferedReader(new InputStreamReader(System.in));
                String msg = bufferedReader1.readLine();
                bufferedWriter.write(msg + "\n");
                bufferedWriter.flush();
                System.out.println("收到服务器返回的消息 " + bufferedReader.readLine());

                if (QUIT.equals(msg)) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
