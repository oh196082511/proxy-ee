package com.example.ouhai;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class NettyServer {

    private static int HEADER_LENGTH = 4;

    public void bind(int port) throws Exception {
        ServerBootstrap b = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));

        // 构造对应的pipeline
        b.setPipelineFactory(new ChannelPipelineFactory() {
            @Override
            public ChannelPipeline getPipeline() throws Exception {
                ChannelPipeline pipelines = Channels.pipeline();
                pipelines.addLast(MessageHandler.class.getName(), new MessageHandler());
                return pipelines;
            }
        });
        b.bind(new InetSocketAddress(port));
    }

    static class MessageHandler extends SimpleChannelHandler {

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            // 接受客户端请求
            ChannelBuffer buffer = (ChannelBuffer) e.getMessage();
            String message = new String(buffer.readBytes(buffer.readableBytes()).array(), StandardCharsets.UTF_8);
            System.out.println("<服务端>收到内容=" + message);

            // 回复客户端
//            byte[] body = "服务端已收到".getBytes();
//            byte[] header = ByteBuffer.allocate(HEADER_LENGTH).order(ByteOrder.BIG_ENDIAN).putInt(body.length).array();
//            Channels.write(ctx.getChannel(), ChannelBuffers.wrappedBuffer(header, body));
            Channels.write(ctx.getChannel(), ChannelBuffers.wrappedBuffer("服务端收到".getBytes()));
            System.out.println("<服务端>发送回执,time=" + System.currentTimeMillis());
        }
    }

    public static void main(String[] args) {
        try {
            new NettyServer().bind(1088);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
