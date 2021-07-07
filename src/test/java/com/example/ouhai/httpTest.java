package com.example.ouhai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class httpTest {

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8081;
        Socket socket = new Socket(host, port);
        String path = "/ouhai/test/hello";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
        final BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
        bufferedWriter.write("GET " + path + " HTTP/1.1\r\n");
        bufferedWriter.write("Host: " + host + "\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.flush();
        while (true) {
            System.out.println(in.readLine());
            Thread.sleep(1000L);
        }


    }
}
