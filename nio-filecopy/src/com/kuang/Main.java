package com.kuang;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Main {

    public static void main(String[] args) throws IOException {
        // write your code here
        nioBufferCopy(new File("1.txt"), new File("1-1.txt"));

        nioTransferCopy(new File("2.txt"), new File("2-2.txt"));
    }

    private static void nioBufferCopy(File source, File target) throws IOException {
        FileChannel fileChannelIn = null;
        FileChannel fileChannelOut = null;

        fileChannelIn = new FileInputStream(source).getChannel();
        fileChannelOut = new FileOutputStream(target).getChannel();

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        // 从fileChannelIn写入byteBuffer
        while (fileChannelIn.read(byteBuffer) != -1) {
            // 转换成读模式
            byteBuffer.flip();
            // 从byteBuffer写入fileChannelOut
            while (byteBuffer.hasRemaining()) {
                fileChannelOut.write(byteBuffer);
            }
            // 转换成写模式
            byteBuffer.clear();
        }
    }

    private static void nioTransferCopy(File source, File target) throws IOException {
        FileChannel fileChannelIn = null;
        FileChannel fileChannelOut = null;

        fileChannelIn = new FileInputStream(source).getChannel();
        fileChannelOut = new FileOutputStream(target).getChannel();

        long copySize = 0l;
        long size = fileChannelIn.size();
        while (copySize != size) {
            copySize += fileChannelIn.transferTo(0, fileChannelIn.size(), fileChannelOut);
        }
    }
}
