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
        if (args.length != 1) {
            System.err.println("Usage: " + EchoServer.class.getSimpleName() +
                    " <port>"
            );
            return;
        }
        int port = Integer.parseInt(args[0]);
        new EchoServer(port).start();
    }

    public void start() throws Exception {
        //0、创建 ChannelHandler
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
                    //5、添加一个EchoServerHandler 到子Channel的ChannelPipeline
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
            //7 获取channel的closefuture 并且阻塞当前线程直到它完成
            channelFuture.channel().closeFuture().sync();
        } finally {
            //8 关闭eventLoopGroup 释放所有的资源
            eventLoopGroup.shutdownGracefully().sync();
        }
    }
}
