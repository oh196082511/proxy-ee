package com.example.ouhai;

import com.example.ouhai.intercept.MyInterceptor;
import com.example.ouhai.intercept.ProxyBean;
import com.example.ouhai.web.service.HelloService;
import com.example.ouhai.web.service.HelloServiceImpl;

public class cmd {

    public static void main(String[] args) {
        HelloService helloService = new HelloServiceImpl();
        HelloService proxy = (HelloService) ProxyBean.getProxyBean(helloService, new MyInterceptor());
        proxy.sayHello("ouhai");
        System.out.println("\n################ name is null! ###########");
        proxy.sayHello(null);
    }
}
