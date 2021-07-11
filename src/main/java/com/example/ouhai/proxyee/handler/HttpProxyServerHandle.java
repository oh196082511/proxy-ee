package com.example.ouhai.proxyee.handler;

import com.example.ouhai.proxyee.intercept.HttpProxyInterceptInitializer;
import com.example.ouhai.proxyee.intercept.HttpProxyInterceptPipeline;
import com.example.ouhai.proxyee.proxyenum.ServerHandleStatus;
import com.example.ouhai.proxyee.server.HttpProxyServer;
import com.example.ouhai.proxyee.server.HttpProxyServerConfig;
import com.example.ouhai.proxyee.util.ProtoUtil;
import com.example.ouhai.proxyee.util.ProtoUtil.RequestProto;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;

// InboundHandler在读数据或者channel状态发生变化时执行
@Data
public class HttpProxyServerHandle extends ChannelInboundHandlerAdapter {

    private ChannelFuture cf;
    private String host;
    private int port;
    private int status = 0;
    private HttpProxyServerConfig serverConfig;
    private HttpProxyInterceptInitializer interceptInitializer; //TODO 在init阶段，可以为目标pineline增加一些特殊intercept,比如CertDownIntercept
    private HttpProxyInterceptPipeline interceptPipeline;// serverHandle真正的pipeline
    private boolean isSsl = false;
    private List requestList;
    private boolean isConnect;

    public HttpProxyServerHandle(HttpProxyServerConfig serverConfig, HttpProxyInterceptInitializer interceptInitializer) {
        this.serverConfig = serverConfig;
        this.interceptInitializer = interceptInitializer;
        this.interceptPipeline = buildPipeline();
    }

    private HttpProxyInterceptPipeline buildPipeline() {
        HttpProxyInterceptPipeline interceptPipeline = new HttpProxyInterceptPipeline(this);
        interceptPipeline.setHttpProxyServerHandle(this);
        interceptInitializer.init(interceptPipeline);
        return interceptPipeline;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            // 第一次建立连接取host和端口号和处理代理握手
            if (status == ServerHandleStatus.NOT_CONNECT.toInt()) {
                RequestProto requestProto = ProtoUtil.getRequestProto(request);
                if (requestProto == null) { // bad request
                    ctx.channel().close();
                    return;
                }
                status = ServerHandleStatus.HTTP.toInt();
                this.host = requestProto.getHost();
                this.port = requestProto.getPort();
                if ("CONNECT".equalsIgnoreCase(request.method().name())) {// 建立代理握手
                    status = ServerHandleStatus.SSL.toInt();
                    HttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpProxyServer.SUCCESS);
                    ctx.writeAndFlush(response);
                    ctx.channel().pipeline().remove("httpCodec");
                    // fix issue #42
                    ReferenceCountUtil.release(msg);
                    return;
                }
            }
            interceptPipeline.setRequestProto(new RequestProto(host, port, isSsl));
            // fix issue #27
            if (request.uri().indexOf("/") != 0) {
                URL url = new URL(request.uri());
                request.setUri(url.getFile());
            }
            interceptPipeline.beforeRequest(ctx.channel(), request);
        } else if (msg instanceof HttpContent) {
            if (status != ServerHandleStatus.SSL.toInt()) {
                interceptPipeline.beforeRequest(ctx.channel(), (HttpContent) msg);
            } else {
                // TODO 没看懂啥意思？
                ReferenceCountUtil.release(msg);
                status = 1;
            }
        } else { // ssl和websocket的握手处理
            // TODO 处理SSL
        }
    }

    public void handleProxyData(Channel channel, Object msg, boolean isHttp) throws Exception {
        if (cf == null) {
            // connection异常 还有HttpContent进来，不转发
            if (isHttp && !(msg instanceof HttpRequest)) {
                return;
            }
            /*
             * 添加SSL client hello的Server Name Indication extension(SNI扩展) 有些服务器对于client
             * hello不带SNI扩展时会直接返回Received fatal alert: handshake_failure(握手错误)
             * 例如：https://cdn.mdn.mozilla.net/static/img/favicon32.7f3da72dcea1.png
             */
            RequestProto requestProto;
            if (!isHttp) {
                // 不是http不处理
                requestProto = new RequestProto(host, port, isSsl);
            } else {
                requestProto = interceptPipeline.getRequestProto();
                HttpRequest httpRequest = (HttpRequest) msg;
                // 检查requestProto是否有修改
                RequestProto newRP = ProtoUtil.getRequestProto(httpRequest);
                if (!newRP.equals(requestProto)) {
                    // 更新Host请求头
                    if ((requestProto.isSsl() && requestProto.getPort() == 443)
                            || (!requestProto.isSsl() && requestProto.getPort() == 80)) {
                        httpRequest.headers().set(HttpHeaderNames.HOST, requestProto.getHost());
                    } else {
                        httpRequest.headers().set(HttpHeaderNames.HOST, requestProto.getHost() + ":" + requestProto.getPort());
                    }
                }
            }
            ChannelInitializer channelInitializer = isHttp ? new HttpProxyInitializer(channel, requestProto)
                    : new TunnelProxyInitializer(channel);
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(serverConfig.getProxyLoopGroup()) // 注册线程池
                    .channel(NioSocketChannel.class) // 使用NioSocketChannel来作为连接用的channel类
                    .handler(channelInitializer);
            requestList = new LinkedList();
            cf = bootstrap.connect(requestProto.getHost(), requestProto.getPort());
            cf.addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    future.channel().writeAndFlush(msg);
                    synchronized (requestList) {
                        requestList.forEach(obj -> future.channel().writeAndFlush(obj));
                        requestList.clear();
                        isConnect = true;
                    }
                } else {
                    requestList.forEach(obj -> ReferenceCountUtil.release(obj));
                    requestList.clear();
                    future.channel().close();
                    channel.close();
                }
            });
        }
    }
}
