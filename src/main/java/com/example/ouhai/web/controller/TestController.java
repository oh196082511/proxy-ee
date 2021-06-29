package com.example.ouhai.web.controller;

import com.example.ouhai.configuration.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/ouhai/test")
public class TestController {

    @Autowired
    private User user;

    @GetMapping(value = "/hello")
    public String hello() {
        return user.name;
    }
}
