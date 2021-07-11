package com.example.ouhai.proxyee.handler;

import com.example.ouhai.proxyee.util.ProtoUtil.RequestProto;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpClientCodec;

public class HttpProxyInitializer extends ChannelInitializer {

    private Channel clientChannel;
    private RequestProto requestProto;

    public HttpProxyInitializer(Channel clientChannel, RequestProto requestProto) {
        this.clientChannel = clientChannel;
        this.requestProto = requestProto;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (requestProto.isSsl()) {
            ch.pipeline().addLast(
                    ((HttpProxyServerHandle) clientChannel.pipeline().get("serverHandle")).getServerConfig()
                            .getClientSslCtx()
                            .newHandler(ch.alloc(), requestProto.getHost(), requestProto.getPort()));
        }
        ch.pipeline().addLast("httpCodec", new HttpClientCodec());
        ch.pipeline().addLast("proxyClientHandle", new HttpProxyClientHandle(clientChannel));
    }
}
