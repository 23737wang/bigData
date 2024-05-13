package org.example;

import java.io.*;
import java.util.ArrayList;

public class DataReader {
    public ArrayList<String> readContent(String path,String type) throws FileNotFoundException {
        ArrayList<String>array=new ArrayList<String>();
        InputStream inputStream = getClass().getResourceAsStream(path);
        if (inputStream == null) {
            System.err.println("Resource not found at path: " + path);
            return array;
        }
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line=reader.readLine())!=null){
                String content=line.split("\\s+")[0];
                if(content.equals(type))
                    array.add(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return array;
    }
}
