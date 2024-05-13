package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Processor {
    public Map<String, ArrayList<String>> processComments(ArrayList<String>comments) {
        Pattern pattern = Pattern.compile("\"([^\"]*)\"");
        Map<String, ArrayList<String>> posterComments = new HashMap<String, ArrayList<String>>();

        for (String comment : comments) {
            String[] parts = comment.split("\\s+");
            String poster = parts[2];
            if (!posterComments.containsKey(poster))
                posterComments.put(poster, new ArrayList<>());
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String comm = matcher.group();
                posterComments.get(poster).add(comm);
            }
        }
        return posterComments;
    }

    public Map<String,Map<String,Integer>> processLikes(ArrayList<String>likes){
        Map<String,Map<String,Integer>>posterLikes=new HashMap<String,Map<String,Integer>>();
        for(String like :likes){
            String[] parts = like.split("\\s+");
            String poster = parts[2];
            String id=parts[3];
            if (!posterLikes.containsKey(poster))
                posterLikes.put(poster, new HashMap<String,Integer>());
            if(!posterLikes.get(poster).containsKey(id))
                posterLikes.get(poster).put(id,1);
            else{
                Integer count=posterLikes.get(poster).get(id)+1;
                posterLikes.get(poster).put(id,count);
            }
        }
        return posterLikes;
    }
    public Map<String,Integer> processShares(ArrayList<String>shares){
        Map<String,Integer>posterShares=new HashMap<String,Integer>();
        for(String share:shares){
            String[]parts=share.split("\\s+");
            String poster=parts[2];
            int shareNum=parts.length-4;
            if(!posterShares.containsKey(poster))
                posterShares.put(poster,0);
            Integer count=posterShares.get(poster)+shareNum;
            posterShares.put(poster,count);
        }
        return posterShares;
    }
    public Map<String,Double> calPopularity(Map<String, ArrayList<String>>posterComments,Map<String,Map<String,Integer>>posterLikes,
                                Map<String,Integer>posterShares){
        Map<String,Double>posterPopularity=new HashMap<String,Double>();
        //遍历用户列表
        for(String poster:posterShares.keySet()){
            //计算点赞数
            int likeNum=0;
            Map<String,Integer>like=posterLikes.get(poster);
            for(String id :like.keySet())
                likeNum+=likeNum+like.get(id);
            //计算评论数
            int commentNum=posterComments.get(poster).size();
            //计算分享数
            int shareNum=posterShares.get(poster);
            //计算受欢迎程度
            double popularity=(double)(likeNum+20*shareNum+5*commentNum)/1000.0;
            //放入字典
            posterPopularity.put(poster,popularity);
        }
        return posterPopularity;
    }

    public <V,T> void writeToJson(Map<V,T>map,String path){
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // 将Map对象转换为JSON字符串
            String jsonString = objectMapper.writeValueAsString(map);
            System.out.println(jsonString); // 输出JSON字符串到控制台
            // 把JSON字符串保存到文件，可以这样做
            File file = new File(path);
            objectMapper.writeValue(file, map); // 直接将Map对象写入文件
            //
            System.out.println("Data saved to map.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
