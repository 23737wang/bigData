package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws FileNotFoundException, JsonProcessingException {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        String path="/student_dataset.txt";
        DataReader dataReader=new DataReader();
        ArrayList<String>comments=new ArrayList<String>();
        ArrayList<String>likes=new ArrayList<String>();
        ArrayList<String>shares=new ArrayList<String>();
        comments=dataReader.readContent(path,"comment");
        likes=dataReader.readContent(path,"like");
        shares=dataReader.readContent(path,"share");
        //设置三个主题
        String commentTopic="comment",likeTopic="like",shareTopic="share";
        //实例化生产者类
        SimpleProducer sp=new SimpleProducer();
        //实例化消费者
        SimpleConsumer sc=new SimpleConsumer();
        //处理评论
        KafkaConsumer<String,String>cConsumer=sc.getConsumer();
        KafkaProducer<String,String> cProducer=sp.getProducer();
        sp.generateData(commentTopic,cProducer,comments);
        ArrayList<String>comms=sc.consumeData(cConsumer,commentTopic);
        //处理点赞
        KafkaConsumer<String,String>lConsumer=sc.getConsumer();
        KafkaProducer<String,String> lProducer=sp.getProducer();
        sp.generateData(likeTopic,lProducer,likes);
        ArrayList<String>liks=sc.consumeData(lConsumer,likeTopic);
        //处理分享
        KafkaConsumer<String,String>sConsumer=sc.getConsumer();
        KafkaProducer<String,String> sProducer=sp.getProducer();
        sp.generateData(shareTopic,sProducer,shares);
        ArrayList<String>shars=sc.consumeData(sConsumer,shareTopic);

        //定义路径

        String cPath="comment.json";
        String lPath="like.json";
        String pPath="popularity.json";
        //处理数据
        Processor processor=new Processor();
        Map<String,ArrayList<String>>posterComment=processor.processComments(comms);
        Map<String,Map<String,Integer>>posterLike=processor.processLikes(liks);
        Map<String,Integer>posterShare=processor.processShares(shars);
        Map<String,Double>posterPopularity=processor.calPopularity(posterComment,posterLike,posterShare);

        processor.writeToJson(posterComment,cPath);
        processor.writeToJson(posterLike,lPath);
        processor.writeToJson(processor.calPopularity(posterComment,posterLike,posterShare),pPath);*/
        //创建redis对象

        RedisSave redisSave=new RedisSave();
        redisSave.setJedis();
        redisSave.save(posterComment,"comment");
        redisSave.save(posterPopularity,"popularity");
        redisSave.save(posterLike,"like");
        redisSave.close();
        for(Map.Entry<String,ArrayList<String>> entry:posterComment.entrySet())
            System.out.println(entry);
    }
}