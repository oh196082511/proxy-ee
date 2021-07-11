package com.example.ouhai;

import com.example.ouhai.proxyee.server.HttpProxyServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class OuhaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(OuhaiApplication.class, args);
        new HttpProxyServer().start(9999);
    }

}
