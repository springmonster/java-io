package com.kuang;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final String QUIT = "quit";
    private static final int DEFAULT_SERVER_PORT = 8888;
    private static final String DEFAULT_SERVER_HOST = "127.0.0.1";

    public static void main(String[] args) {
        ServerSocket serverSocket = null;

        try {
            serverSocket = new ServerSocket(DEFAULT_SERVER_PORT);
            System.out.println("启动服务器，监听端口" + DEFAULT_SERVER_PORT);

            while (true) {
                Socket accept = serverSocket.accept();
                System.out.println("客户端[" + accept.getPort() + "]已连接");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(accept.getOutputStream()));

                String msg;
                while ((msg = bufferedReader.readLine()) != null) {
                    System.out.println("客户端message is " + msg);
                    bufferedWriter.write("服务器发送消息 " + msg + "\n");
                    bufferedWriter.flush();

                    if (QUIT.equals(msg)) {
                        System.out.println("退出的client端口号：" + accept.getPort());
                        break;
                    }
                }
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
}
