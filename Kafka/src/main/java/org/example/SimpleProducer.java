package org.example;
import java.util.ArrayList;
import java.util.Properties;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
public class SimpleProducer {
    //创建生产者对象
    public KafkaProducer<String,String> getProducer (){
        //编写配置
        Properties props=new Properties();
        props.put("bootstrap.servers","localhost:9092,localhost:9093,localhost:9094");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
        //创建对象
        KafkaProducer<String,String> producer=new KafkaProducer<String,String>(props);
        return producer;
    }
    //生产数据，传入主题和生产者对象
    public void generateData(String topic, KafkaProducer<String,String> producer, ArrayList<String >array){
        for(String value :array){
            producer.send(new ProducerRecord<String,String>(topic,null,value));
        }
        producer.close();
    }
}
