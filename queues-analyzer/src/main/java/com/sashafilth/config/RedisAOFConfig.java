package com.sashafilth.config;

import com.sashafilth.receiver.RedisAOFReceiver;
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
public class RedisAOFConfig {

    private final RedisReceiver receiver;

    @Autowired
    public RedisAOFConfig(RedisAOFReceiver receiver) {
        this.receiver = receiver;
    }

    @Bean(name = "AOFConnectionFactory")
    public RedisConnectionFactory aofConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration("localhost", 6381);
        JedisClientConfiguration c = JedisClientConfiguration.builder().build();
        return new JedisConnectionFactory(config, c);
    }

    @Bean(name = "AOFRedisTemplate")
    RedisTemplate<String, Object> redisAOFTemplate(@Qualifier("AOFConnectionFactory") RedisConnectionFactory aofConnectionFactory) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());
        redisTemplate.setConnectionFactory(aofConnectionFactory);
        return redisTemplate;
    }

    @Bean(name = "AOFMessageListenerAdapter")
    MessageListenerAdapter listenerAdapter() {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }

    @Bean(name = "AOFMessageListenerContainer")
    RedisMessageListenerContainer container(@Qualifier("AOFConnectionFactory") RedisConnectionFactory connectionFactory,
                                            @Qualifier("AOFMessageListenerAdapter")MessageListenerAdapter listenerAdapter) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, new PatternTopic("chat"));
        return container;
    }
}
