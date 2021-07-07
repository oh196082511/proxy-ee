package com.example.ouhai.web.controller;

import com.example.ouhai.configuration.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(value = "/ouhai/test")
public class TestController {

    @Autowired
    private User user;

    @GetMapping(value = "/hello")
    public String hello() throws Exception {
        String ans = "欧海";
        for (int i = 0;i < 1000;i++) {
            ans += "欧海";
        }
        return ans;
    }

    @PostMapping(value = "/upload")
    public String upload(@RequestBody int id) {
        System.out.println(id);
        return "" + id;
    }
}
