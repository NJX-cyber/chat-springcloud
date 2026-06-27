package com.example.messagemanager.websocket.netty;

import com.example.model.config.AppConfig;
import com.example.model.utils.StringUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * author:normal
 * date:2026/1/24 20:56
 * description:
 */

@Component("nettyWebSocketStarter")
public class NettyWebSocketStarter implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(NettyWebSocketStarter.class);

    private static EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    private static EventLoopGroup workGroup = new NioEventLoopGroup();

    @Resource
    private HandleWebSocket handleWebSocket;

    @Resource
    private AppConfig appConfig;

    @PreDestroy
    public void close() {
        bossGroup.shutdownGracefully();
        workGroup.shutdownGracefully();
    }

    @Override
    public void run() {
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workGroup);
            serverBootstrap.channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG)).childHandler(new ChannelInitializer() {
                @Override
                protected void initChannel(Channel channel) {
                    ChannelPipeline pipeline = channel.pipeline();
                    // 设置几个重要的处理器
                    // 对http协议的支持，使用http的编码器，解码器
                    pipeline.addLast(new HttpServerCodec());
                    // 聚合解码 httpRequest/httpContent/lastHttpContent到fullHttpRequest
                    // 保证接收的http请求的完整性
                    pipeline.addLast(new HttpObjectAggregator(64 * 1024));
                    // 心跳 long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit
                    pipeline.addLast(new IdleStateHandler(10, 0, 0, TimeUnit.SECONDS));
                    pipeline.addLast(new HandlerHeartBeat());
                    // 将http协议升级成ws协议，对websocket支持
                    pipeline.addLast(new WebSocketServerProtocolHandler("/ws", null, true, 64 * 1024, true, true, 10000L));
                    pipeline.addLast(handleWebSocket);
                }
            });
            String port = System.getProperty("ws.port");
            ChannelFuture channelFuture = null;
            if ((StringUtils.isEmpty(port))) {
                channelFuture = serverBootstrap.bind(appConfig.getWsPort()).sync();
            } else {
                try {
                    channelFuture = serverBootstrap.bind(Integer.parseInt(port)).sync();
                } catch (Exception e) {
                    logger.info("ws端口输入有误,port:{}",port);
                }
            }
            logger.info("netty服务启动成功，端口{}",appConfig.getWsPort());
            assert channelFuture != null;
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            logger.error("启动netty失败{}", e.toString());
        } finally {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}