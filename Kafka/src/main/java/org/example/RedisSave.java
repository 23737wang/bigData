package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import redis.clients.jedis.Jedis;

import java.util.Map;

public class RedisSave {
    private Jedis jedis;
    public void setJedis(){
        jedis=new Jedis("localhost",6379);
        jedis.select(0);
    }
    public <T>void save(Map<String,T> data,String field) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        for(String key :data.keySet()){
            T value = data.get(key);
            // 使用 ObjectMapper 将值序列化为 JSON 字符串
            String valueStr = objectMapper.writeValueAsString(value);
            this.jedis.hset(key,field,valueStr);
        }
    }
    public void close(){
        this.jedis.close();
    }
}
