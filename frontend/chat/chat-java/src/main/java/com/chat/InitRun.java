package com.chat;

import com.chat.Redis.RedisUtils;
import com.chat.websocket.netty.NettyWebSocketStarter;
import io.lettuce.core.RedisConnectionException;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * author:normal
 * date:2026/1/5 19:28
 * description:
 */
@Component("initRun")
public class InitRun implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(InitRun.class);

    @Resource
    private DataSource dataSource;

    @Resource
    private RedisUtils redisUtils;

    @Resource
    private NettyWebSocketStarter nettyWebSocketStarter;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            dataSource.getConnection();
            redisUtils.get("test");
            new Thread(nettyWebSocketStarter).start();
            logger.info("服务启动成功！");
        } catch (SQLException e){
            logger.error("数据库连接失败！");
        } catch (RedisConnectionException e){
            logger.error("redis连接失败！");
        } catch (Exception e){
            logger.error("服务启动失败，error:" + e);
        }
    }
}