package com.chat.websocket.netty;

import com.chat.Redis.RedisComponent;
import com.chat.entity.dto.TokenUserInfoDto;
import com.chat.utils.StringUtils;
import com.chat.websocket.ChannelContextUtils;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * author:normal
 * date:2026/1/25 14:47
 * description:
 */

@Component("handleWebSocket")
@ChannelHandler.Sharable
public class HandleWebSocket extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(HandleWebSocket.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChannelContextUtils channelContextUtils;


    /**
     * 通道就绪后调用，用户来做初始化
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有新的连接加入....");

        // TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        // tokenUserInfoDto.setUserId("123");
        // String token = StringUtils.encodeMd5("123" + StringUtils.getRandomString(Constants.LENGTH_TWENTY));
        // tokenUserInfoDto.setToken(token);
        // this.redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("有连接断开");
        channelContextUtils.removeContext(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame textWebSocketFrame) throws Exception {
        Channel channel = ctx.channel();
        Attribute<String> attribute = channel.attr(AttributeKey.valueOf(channel.id().toString()));
        String userId = attribute.get();
        // logger.info("收到消息用户名为{}，的信息:{}", userId, textWebSocketFrame.text());
        redisComponent.saveUserHeartBeat(userId);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            WebSocketServerProtocolHandler.HandshakeComplete complete = (WebSocketServerProtocolHandler.HandshakeComplete) evt;
            String uri = complete.requestUri();
            String token = getToken(uri);
            if (token == null) {
                ctx.channel().close();
                return;
            }
            TokenUserInfoDto dto = redisComponent.getTokenUserInfoDto(token);
            if (dto == null) {
                ctx.channel().close();
                return;
            }
            this.channelContextUtils.addContext(dto.getUserId(), ctx.channel());
        }
    }

    private String getToken(String url) {
        if (StringUtils.isEmpty(url) || !url.contains("?")) {
            return null;
        }
        String[] params = url.split("\\?");
        if ((params.length != 2)) {
            return null;
        }
        return params[1].split("=")[1];
    }
}