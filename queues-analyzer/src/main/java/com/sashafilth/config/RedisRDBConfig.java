package com.sashafilth.config;

import com.sashafilth.receiver.RedisRDBReceiver;
import com.sashafilth.receiver.RedisReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisRDBConfig {

    private final RedisReceiver receiver;

    @Autowired
    public RedisRDBConfig(RedisRDBReceiver receiver) {
        this.receiver = receiver;
    }

    @Bean(name = "RDBConnectionFactory")
    public RedisConnectionFactory connectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6380);
        JedisClientConfiguration clientConfiguration = JedisClientConfiguration.builder().build();
        return new JedisConnectionFactory(config, clientConfiguration);
    }

    @Bean(name = "redisTemplate")
    RedisTemplate<String, Object> redisRDBTemplate(@Qualifier("RDBConnectionFactory") RedisConnectionFactory connectionFactory) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    @Bean(name = "RDBMessageListenerAdapter")
    MessageListenerAdapter listenerAdapter() {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean(name = "RDBMessageListenerContainer")
    RedisMessageListenerContainer container(@Qualifier("RDBConnectionFactory") RedisConnectionFactory connectionFactory,
                                            @Qualifier("RDBMessageListenerAdapter")MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));

        return container;
    }
}
