package com.sashafilth.controller;

import com.dinstone.beanstalkc.Job;
import com.dinstone.beanstalkc.JobConsumer;
import com.dinstone.beanstalkc.JobProducer;
import com.sashafilth.receiver.RedisAOFReceiver;
import com.sashafilth.receiver.RedisRDBReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Set;


@RestController
@RequestMapping(value = "/messages")
public class QueueController {


    private final RedisTemplate redisRDBTemplate;
    private final RedisTemplate redisAOFTemplate;
    private final JobProducer jobProducer;
    private final JobConsumer jobConsumer;

    @Autowired
    public QueueController(@Qualifier("redisTemplate") RedisTemplate redisRDBTemplate,
                           @Qualifier("AOFRedisTemplate") RedisTemplate redisAOFTemplate,
                           @Qualifier("producer") JobProducer jobProducer,
                           @Qualifier("consumer") JobConsumer jobConsumer
    ) {
        this.redisRDBTemplate = redisRDBTemplate;
        this.redisAOFTemplate = redisAOFTemplate;
        this.jobProducer = jobProducer;
        this.jobConsumer = jobConsumer;
    }

    @GetMapping(path = "/redis-rdb/write")
    public ResponseEntity<String> sendToRedisRDB() {
        redisRDBTemplate.convertAndSend("chat", "Hello");
        return new ResponseEntity<>(HttpStatus.OK.toString(), HttpStatus.OK);
    }

    @GetMapping(path = "/redis-rdb/read")
    public ResponseEntity<String> readFromRedisRDB() {
        try {
            if (RedisRDBReceiver.messageList.size() == 0) {
                return new ResponseEntity(HttpStatus.OK, HttpStatus.OK);
            }
            String msg = RedisRDBReceiver.messageList.get(0);
            RedisRDBReceiver.messageList.remove(0);
            return new ResponseEntity(msg, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @GetMapping(path = "/redis-aof/write")
    public ResponseEntity<String> sendToRedisAOF() {
        redisAOFTemplate.convertAndSend("chat", "Hello");
        return new ResponseEntity<>(HttpStatus.OK.toString(), HttpStatus.OK);
    }

    @GetMapping(path = "/redis-aof/read")
    public ResponseEntity<String> readFromRedisAOF() {
        try {
            if (RedisAOFReceiver.messageList.size() == 0) {
                return new ResponseEntity(HttpStatus.OK, HttpStatus.OK);
            }
            String msg = RedisAOFReceiver.messageList.get(0);
            RedisAOFReceiver.messageList.remove(0);
            return new ResponseEntity(msg, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping(path = "/beanstalkd/write")
    public ResponseEntity<String> sendToBeanstalkd() {
        try {
            jobProducer.putJob(0, 0, 0, "Hello".getBytes(StandardCharsets.UTF_8));
            return new ResponseEntity<>(HttpStatus.OK.toString(), HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @GetMapping(path = "/beanstalkd/read")
    public ResponseEntity<String> readFromBeanstalkd() {
        try {
            Job job = jobConsumer.reserveJob(10);
            if (job != null) {
                String message = new String(job.getData(), StandardCharsets.UTF_8);
                jobConsumer.deleteJob(job.getId());
                return new ResponseEntity<>(message, HttpStatus.OK);
            }
            return new ResponseEntity<>("Message not found", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


}
