package com.example.ouhai.proxyee.server;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lombok.Data;

@Data
public class HttpProxyServerConfig {

    private int bossGroupThreads;
    private int workerGroupThreads;
    private int proxyGroupThreads;
    private boolean handleSsl;
    private SslContext clientSslCtx;
    private EventLoopGroup proxyLoopGroup;
}
