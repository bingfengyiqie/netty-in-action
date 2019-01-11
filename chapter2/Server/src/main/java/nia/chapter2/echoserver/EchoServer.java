package nia.chapter2.echoserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

/**
 * Listing 2.2 EchoServer class
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */
public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port =9090;
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        //0、创建 ChannelHandler 用于实现业务逻辑
        final EchoServerHandler echoServerHandler = new EchoServerHandler();
        //1、创建EventLoopGroup
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            //2、创建ServerBootstrap
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    //3、指定所使用的NIO 传输 channel
                    .channel(NioServerSocketChannel.class)
                    //4、指定port
                    .localAddress(new InetSocketAddress(port))
                    /**
                     * 5、添加一个EchoServerHandler 到子Channel的ChannelPipeline
                     当接收到一个新的连接时，一个新的子Channel 将会被创建，而ChannelInitializer 将会把一个你的
                     EchoServerHandler 的实例添加到该Channel 的ChannelPipeline 中。正如我们之前所
                     解释的，这个ChannelHandler 将会收到有关入站消息的通知
                     */
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(echoServerHandler);
                        }
                    });
            //6 异步的绑定服务器,[调用sync方法阻塞等待]直到绑定完成
            ChannelFuture channelFuture = serverBootstrap.bind().sync();
            System.out.println(EchoServer.class.getName() +
                    " started and listening for connections on " + channelFuture.channel().localAddress());
            //7 获取channel的closefuture然后调用sync() 并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();
        } finally {
            //8 关闭eventLoopGroup 释放所有资源、包括所有被创建的线程
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
