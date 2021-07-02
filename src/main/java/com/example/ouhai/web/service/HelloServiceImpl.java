package com.example.ouhai.web.service;

import org.springframework.stereotype.Service;

public class HelloServiceImpl implements HelloService {

    @Override
    public void sayHello(String name) {
        if (name == null || name.trim().equals("")) {
            throw new RuntimeException("sayHello parameter is null!");
        }
        System.out.println("hello " + name);
    }
}
