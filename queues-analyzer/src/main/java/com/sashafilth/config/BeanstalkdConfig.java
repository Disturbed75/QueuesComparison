package com.sashafilth.config;

import com.dinstone.beanstalkc.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanstalkdConfig {

    @Bean
    BeanstalkClientFactory beanstalkClientFactory() {
        com.dinstone.beanstalkc.Configuration config = new com.dinstone.beanstalkc.Configuration();
        config.setServiceHost("localhost");
        config.setServicePort(11300);
        config.setConnectTimeout(2000);
        config.setReadTimeout(3000);
        return new BeanstalkClientFactory(config);
    }

    @Bean(name = "producer")
    JobProducer producer(BeanstalkClientFactory factory) {
        return factory.createJobProducer("chat");
    }

    @Bean(name = "consumer")
    JobConsumer consumer(BeanstalkClientFactory factory) {
        return factory.createJobConsumer("chat");
    }

    @Bean(name = "client")
    BeanstalkClient client(BeanstalkClientFactory factory) {
        return factory.createBeanstalkClient();
    }
}
