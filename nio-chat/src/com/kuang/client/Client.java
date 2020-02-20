package com.kuang.client;

import com.kuang.config.Config;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 当接收的类使用的是BufferedReader，发送的类是BufferedWriter的时候，要注意发送的一行要有换行标识符。
 */
public class Client {
    private ExecutorService executorService;
    private SocketChannel socketChannel;
    private ByteBuffer rBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer wBuffer = ByteBuffer.allocate(1024);
    private Selector selector;
    private Charset charset = StandardCharsets.UTF_8;

    public static void main(String[] args) {
        new Client().start();
    }

    public void start() {
        executorService = Executors.newFixedThreadPool(10);

        try {
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_CONNECT);
            socketChannel.connect(new InetSocketAddress(Config.SERVER_URL, Config.SERVER_PORT));

            while (true) {
                selector.select();
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                for (SelectionKey selectionKey : selectionKeys) {
                    handle(selectionKey);
                }
                selectionKeys.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    private void handle(SelectionKey selectionKey) throws IOException {
        // 连接就绪事件
        if (selectionKey.isConnectable()) {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            if (channel.isConnectionPending()) {
                channel.finishConnect();
                // 处理用户的输入
                executorService.submit(new ClientHandler(this));
            }
            channel.register(selector, SelectionKey.OP_READ);
        } else if (selectionKey.isReadable()) {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            String msg = receive(channel);
            if (msg.isEmpty()) {
                close(selector);
            } else {
                System.out.println(msg);
            }
        }
    }

    private String receive(SocketChannel channel) throws IOException {
        rBuffer.clear();
        while (channel.read(rBuffer) > 0) ;
        rBuffer.flip();
        return String.valueOf(charset.decode(rBuffer));
    }

    private void close(Closeable closeable) throws IOException {
        closeable.close();
    }

    public void send(String msg) throws IOException {
        if (msg == null || msg.isEmpty()) {
            return;
        }
        wBuffer.clear();
        wBuffer.put(charset.encode(msg));
        wBuffer.flip();
        while (wBuffer.hasRemaining()) {
            socketChannel.write(wBuffer);
        }

        if (quit(msg)) {
            close(selector);
        }
    }

    public boolean quit(String msg) {
        return Config.QUIT_COMMAND.equalsIgnoreCase(msg);
    }
}
