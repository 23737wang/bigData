import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.SparkConf;
 
public class SimpleApp {
    public static void main(String[] args) {
        String logFile = "file:///home/hadoop/下载/export.csv"; // Should be some file on your system
        SparkConf conf=new SparkConf().setMaster("local").setAppName("SimpleApp");
        JavaSparkContext sc=new JavaSparkContext(conf);
        JavaRDD<String> logData = sc.textFile(logFile).cache(); 
        long lineCount=logData.count();
	System.out.println("Number of lines:"+lineCount);
	sc.close();
    }
}
