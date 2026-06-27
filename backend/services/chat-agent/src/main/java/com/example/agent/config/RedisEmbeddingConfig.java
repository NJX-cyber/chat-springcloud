package com.example.agent.config;

import dev.langchain4j.community.store.embedding.redis.RedisEmbeddingStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisEmbeddingConfig {

    @Value("${langchain4j.community.redis.host}")
    private String host;

    @Value("${langchain4j.community.redis.password}")
    private String password;

    @Value("${langchain4j.community.redis.port}")
    private int port;

    @Value("${langchain4j.community.redis.dimension}")
    private int dimension;

    @Value("${langchain4j.community.redis.index-name}")
    private String indexName;

    @Value("${langchain4j.community.redis.prefix}")
    private String prefix;

    @Bean
    public RedisEmbeddingStore redisEmbeddingStore() {

        return RedisEmbeddingStore.builder()
                .host(host)
                .port(port)
                .user("default")
                .password(password)
                .dimension(dimension)
                .indexName(indexName)
                .prefix(prefix)
                .build();
    }
}