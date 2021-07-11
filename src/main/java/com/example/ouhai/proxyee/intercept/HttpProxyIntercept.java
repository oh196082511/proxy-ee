package com.example.ouhai.proxyee.intercept;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

public class HttpProxyIntercept {

    /**
     * 拦截代理服务器到目标服务器的请求头
     */
    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest) throws Exception {
    }

    /**
     * 拦截代理服务器到目标服务器的请求体
     */
    public void beforeRequest(Channel clientChannel, HttpContent httpContent) throws Exception {
    }

    /**
     * 拦截代理服务器到客户端的响应头
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse) throws Exception {
    }


    /**
     * 拦截代理服务器到客户端的响应体
     */
    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent)
            throws Exception {
    }
}
