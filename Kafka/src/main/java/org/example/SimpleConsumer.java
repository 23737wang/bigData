package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
public class SimpleConsumer {

    public KafkaConsumer<String,String> getConsumer(){
        //编写配置
        Properties prop=new Properties();
        prop.put("bootstrap.servers","localhost:9092,localhost:9093,localhost:9094");
        prop.put("key.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("value.deserializer","org.apache.kafka.common.serialization.StringDeserializer");
        prop.put("group.id","consumer1");

        //创建消费者
        KafkaConsumer<String,String>consumer=new KafkaConsumer<>(prop);
        return consumer;
    }
    public ArrayList<String> consumeData(KafkaConsumer <String,String>consumer,String topic){
        consumer.subscribe(Arrays.asList(topic));
        ArrayList<String>contents=new ArrayList<String>();
        System.out.println("消费者开始运行");
        boolean keepPolling=true;
        while (keepPolling) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(2));
            if (consumerRecords.isEmpty()) {
                keepPolling = false;
            } else {
                for (ConsumerRecord<String, String> consumerRecord : consumerRecords) {
                    contents.add(consumerRecord.value());
                }
            }
        }
        consumer.close();
        return contents;
    }
    public void clear(KafkaConsumer<String,String>consumer){
        consumer.subscribe(Arrays.asList("comment","like","share"));
        ArrayList<String>contents=new ArrayList<String>();
        System.out.println("消费者开始运行");
        boolean keepPolling=true;
        while (keepPolling) {
            ConsumerRecords<String, String> consumerRecords = consumer.poll(Duration.ofSeconds(5));
            if (consumerRecords.isEmpty()) {
                keepPolling = false;}
        }
        consumer.close();
    }

}
