package com.kuang.client;

import com.kuang.config.Config;

import java.io.*;
import java.net.Socket;

/**
 * 当接收的类使用的是BufferedReader，发送的类是BufferedWriter的时候，要注意发送的一行要有换行标识符。
 */
public class Client {
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        Socket socket = null;
        try {
            socket = new Socket(Config.SERVER_URL, Config.SERVER_PORT);

            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            new Thread(new ClientHandler(this)).start();

            String msg;
            while ((msg = bufferedReader.readLine()) != null) {
                System.out.println(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void send(String msg) throws IOException {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        System.out.println("客户端发送的消息：" + msg);
        bufferedWriter.write(msg);
        bufferedWriter.newLine();
        bufferedWriter.flush();
    }

    public boolean quit(String msg) {
        return Config.QUIT_COMMAND.equalsIgnoreCase(msg);
    }
}
