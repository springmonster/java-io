package com.kuang.server;

import com.kuang.config.Config;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;

public class Server {
    private Selector selector;
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer wBuffer = ByteBuffer.allocate(1024);
    private Charset charset = StandardCharsets.UTF_8;

    public static void main(String[] args) {
        new Server().start();
    }

    public void start() {
        ServerSocketChannel serverSocketChannel;
        try {
            serverSocketChannel = ServerSocketChannel.open();
            // 非阻塞式调用
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(Config.SERVER_PORT));

            selector = Selector.open();
            // 将serverSocketChannel注册到selector，监听OP_CONNECT
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("启动服务器，监听端口: " + Config.SERVER_PORT);

            while (true) {
                selector.select();

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handles(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handles(SelectionKey selectionKey) throws IOException {
        // accept事件，建立连接OP_CONNECT
        if (selectionKey.isAcceptable()) {
            ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
            SocketChannel client = serverSocketChannel.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            System.out.println(getClientName(client) + "已连接");
        } else if (selectionKey.isReadable()) {
            SocketChannel client = (SocketChannel) selectionKey.channel();
            String msg = receive(client);
            if (msg.isEmpty()) {
                // 客户端异常
                selectionKey.cancel();
                selector.wakeup();
            } else {
                System.out.println(getClientName(client) + ":" + msg);
                forwardMessage(client, msg);

                // 检查用户退出
                if (Config.QUIT_COMMAND.equalsIgnoreCase(msg)) {
                    selectionKey.cancel();
                    selector.wakeup();
                    System.out.println("客户端 " + client.socket().getPort() + "已断开");
                }
            }
        }
    }

    private void forwardMessage(SocketChannel sendMsgClient, String msg) throws IOException {
        for (SelectionKey key : selector.keys()) {
            Channel connectedClient = key.channel();
            if (connectedClient instanceof ServerSocketChannel) {
                continue;
            }
            if (key.isValid() && !connectedClient.equals(sendMsgClient)) {
                wBuffer.clear();
                wBuffer.put(charset.encode(getClientName(sendMsgClient) + ":" + msg));
                wBuffer.flip();
                while (wBuffer.hasRemaining()) {
                    ((SocketChannel) connectedClient).write(wBuffer);
                }
            }
        }
    }

    private String receive(SocketChannel client) throws IOException {
        rBuffer.clear();
        while (client.read(rBuffer) > 0) ;
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private String getClientName(SocketChannel client) {
        return "客户端[" + client.socket().getPort() + "]";
    }
}
