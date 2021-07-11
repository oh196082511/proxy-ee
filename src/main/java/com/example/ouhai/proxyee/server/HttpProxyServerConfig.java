package com.example.ouhai.proxyee.server;

import io.netty.channel.EventLoopGroup;
import io.netty.handler.ssl.SslContext;
import lombok.Data;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

@Data
public class HttpProxyServerConfig {

    // 服务端解密用的钥
    private PrivateKey serverPriKey;
    // 下面是用来生成返回给客户端的cert
    private PublicKey serverPubKey;
    private String issuer;
    private Date caNotBefore;
    private Date caNotAfter;
    private PrivateKey caPriKey;

    private int bossGroupThreads;
    private int workerGroupThreads;
    private int proxyGroupThreads;
    private boolean handleSsl;
    private SslContext clientSslCtx;
    private EventLoopGroup proxyLoopGroup;
}
