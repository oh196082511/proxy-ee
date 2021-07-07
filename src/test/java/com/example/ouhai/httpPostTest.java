package com.example.ouhai;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class httpPostTest {

    public static void main(String[] args) throws Exception {
        String host = "127.0.0.1";
        int port = 8081;
        Socket socket = new Socket(host, port);
        String path = "/ouhai/test/upload";
        String data = "1";
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        OutputStreamWriter streamWriter = new OutputStreamWriter(socket.getOutputStream());
        final BufferedWriter bufferedWriter = new BufferedWriter(streamWriter);
        bufferedWriter.write("POST " + path + " HTTP/1.1\r\n");
        bufferedWriter.write("Host: " + host + "\r\n");
        bufferedWriter.write("Content-Length: " + data.length() + "\r\n");
        bufferedWriter.write("Content-Type: application/json\r\n");
        bufferedWriter.write("\r\n");
        bufferedWriter.write(data);
        bufferedWriter.flush();
        String line = "";
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }

    }
}
