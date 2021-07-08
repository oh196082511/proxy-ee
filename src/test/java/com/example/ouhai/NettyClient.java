package com.example.ouhai;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class NettyClient {
    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 1088;
        Socket socket = new Socket(host, port);
        final OutputStream outputStream = socket.getOutputStream();
        outputStream.write("hello,i'm client".getBytes());
        outputStream.flush();

        final InputStream inputStream = socket.getInputStream();

        while (true) {
            byte[] bytes = new byte[15];
            final int read = inputStream.read(bytes);
            System.out.println(new String(bytes, StandardCharsets.UTF_8));
        }

    }
}
