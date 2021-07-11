package com.example.ouhai.proxyee.intercept;


import com.example.ouhai.proxyee.handler.HttpProxyServerHandle;
import com.example.ouhai.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;

@Data
@Slf4j
public class HttpProxyInterceptPipeline {
    private List<HttpProxyIntercept> intercepts;

    private HttpProxyServerHandle httpProxyServerHandle;

    private int posBeforeHead = 0;
    private int posBeforeContent = 0;
    private int posAfterHead = 0;
    private int posAfterContent = 0;

    private RequestProto requestProto;
    private HttpRequest httpRequest;
    private HttpResponse httpResponse;

    // 默认1个
    public HttpProxyInterceptPipeline(HttpProxyServerHandle httpProxyServerHandle) {
        this.intercepts = new LinkedList<>();
        this.intercepts.add(new HttpProxyIntercept() {
            @Override
            public void beforeRequest(Channel clientChannel, HttpRequest httpRequest)
                    throws Exception {
                log.info("beforeRequest httpRequest" + clientChannel.remoteAddress());
                httpProxyServerHandle.handleProxyData(clientChannel, httpRequest, true);
            }

            @Override
            public void beforeRequest(Channel clientChannel, HttpContent httpContent)
                    throws Exception {
                log.info("beforeRequest httpContent" + clientChannel.remoteAddress());
                httpProxyServerHandle.handleProxyData(clientChannel, httpContent, true);
            }

            @Override
            public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse) throws Exception {
                log.info("afterResponse httpResponse" + clientChannel.remoteAddress());
                // 具体替换的业务逻辑写这里
                httpResponse.headers().add("ouhai", "proxy-test");
                clientChannel.writeAndFlush(httpResponse);
                if (HttpHeaderValues.WEBSOCKET.toString().equals(httpResponse.headers().get(HttpHeaderNames.UPGRADE))) {
                    // websocket转发原始报文
                    proxyChannel.pipeline().remove("httpCodec");
                    clientChannel.pipeline().remove("httpCodec");
                }
            }

            @Override
            public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent) throws Exception {
                log.info("afterResponse httpContent" + clientChannel.remoteAddress());
                clientChannel.writeAndFlush(httpContent);
            }
        });
    }

    public void addLast(HttpProxyIntercept intercept) {
        this.intercepts.add(this.intercepts.size() - 1, intercept);
    }

    public void addFirst(HttpProxyIntercept intercept) {
        this.intercepts.add(0, intercept);
    }

    public void beforeRequest(Channel clientChannel, HttpRequest httpRequest) throws Exception {
        this.httpRequest = httpRequest;
        if (this.posBeforeHead < intercepts.size()) {
            HttpProxyIntercept intercept = intercepts.get(this.posBeforeHead++);
            intercept.beforeRequest(clientChannel, this.httpRequest);
        }
        this.posBeforeHead = 0;
    }

    public void beforeRequest(Channel clientChannel, HttpContent httpContent) throws Exception {
        if (this.posBeforeContent < intercepts.size()) {
            HttpProxyIntercept intercept = intercepts.get(this.posBeforeContent++);
            intercept.beforeRequest(clientChannel, httpContent);
        }
        this.posBeforeContent = 0;
    }

    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpResponse httpResponse)
            throws Exception {
        this.httpResponse = httpResponse;
        if (this.posAfterHead < intercepts.size()) {
            HttpProxyIntercept intercept = intercepts.get(this.posAfterHead++);
            intercept.afterResponse(clientChannel, proxyChannel, this.httpResponse);
        }
        this.posAfterHead = 0;
    }

    public void afterResponse(Channel clientChannel, Channel proxyChannel, HttpContent httpContent)
            throws Exception {
        if (this.posAfterContent < intercepts.size()) {
            HttpProxyIntercept intercept = intercepts.get(this.posAfterContent++);
            intercept.afterResponse(clientChannel, proxyChannel, httpContent);
        }
        this.posAfterContent = 0;
    }

}
