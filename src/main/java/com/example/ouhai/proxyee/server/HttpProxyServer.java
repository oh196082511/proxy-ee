package com.example.ouhai.proxyee.server;

import com.example.ouhai.proxyee.handler.HttpProxyServerHandle;
import com.example.ouhai.proxyee.intercept.HttpProxyInterceptInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class HttpProxyServer {

    //http代理隧道握手成功
    public final static HttpResponseStatus SUCCESS = new HttpResponseStatus(200,
            "Connection established");

    private HttpProxyServerConfig serverConfig;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private HttpProxyInterceptInitializer proxyInterceptInitializer;

    private void init() {
        if (serverConfig == null) {
            serverConfig = new HttpProxyServerConfig();
        }
        serverConfig.setProxyLoopGroup(new NioEventLoopGroup(serverConfig.getProxyGroupThreads()));
        if (proxyInterceptInitializer == null) {
            proxyInterceptInitializer = new HttpProxyInterceptInitializer();
        }
        // TODO SSL

    }

    public void start(int port) {
        start(null, port);
    }

    public void start(String ip, int port) {
        init();
        bossGroup = new NioEventLoopGroup(serverConfig.getBossGroupThreads());
        workerGroup = new NioEventLoopGroup(serverConfig.getWorkerGroupThreads());
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<Channel>() {

                        @Override
                        protected void initChannel(Channel ch) throws Exception {
                            ch.pipeline().addLast("httpCodec", new HttpServerCodec());
                            ch.pipeline().addLast("serverHandle",
                                    new HttpProxyServerHandle(serverConfig, proxyInterceptInitializer));
                        }
                    });
            ChannelFuture f;
            if (ip == null) {
                f = b.bind(port).sync();
            } else {
                f = b.bind(ip, port).sync();
            }
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
