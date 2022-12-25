package code;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;


public class Robot extends Configured implements Tool {

    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, NullWritable> {
        private int depth;
        private Queue<String> myQueue = new LinkedList<String>();
        private Queue<Integer> level = new LinkedList<Integer>();
        private Set<String> visited = new HashSet<String>();

        public void configure(JobConf job) {
            depth = job.getInt("depth", 1);
        }

        public String readUrl(String path) {
            try {
                URL url = new URL(path);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
                String inputLine = in.readLine();
                String rawHTML = new String();
                while(inputLine  != null){
                    rawHTML += inputLine;
                    inputLine = in.readLine();
                }
                in.close();
                return rawHTML;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return null;

        }
    

        public void map(LongWritable key, Text value, OutputCollector<Text, NullWritable> output, Reporter reporter)
                throws IOException {
            String line = value.toString();
            myQueue.add(line);
            level.add(0);

            System.out.println("Line 57, url added to queue: " + line);

            while (!myQueue.isEmpty()) {
                int currentLevel = level.poll();
                String currentLink = myQueue.poll();
                System.out.println("Line 62, currentLevel: " + currentLevel + "currentLink: " + currentLink);

                output.collect(new Text(currentLink), NullWritable.get());

                // System.out.println("After output.collect, currentLink: " + currentLink);

                // System.out.println("Line 47? IN QUEUE: Visiting: " + currentLink + " (level " + currentLevel + ")");

                visited.add(currentLink);

                if (currentLevel > depth) {
                    break;
                }
                try {
                    String rawHTML = readUrl(currentLink);
                    // System.out.println("Line 75, Raw HTML: " + rawHTML);
                    
                    Pattern pattern = Pattern.compile("https?:\\/\\/(?:www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b(?:[-a-zA-Z0-9()@:%_\\+.~#?&\\/=]*)");
                    Matcher matcher = pattern.matcher(rawHTML);
                    
                    while (matcher.find()) {
                        String found = matcher.group();
                        if (visited.contains(found))
                            continue;
                        System.out.println("Found next url: " + found);
                        myQueue.add(found);
                        level.add(currentLevel + 1);
                    }

                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    public static class Reduce extends MapReduceBase implements Reducer<Text, NullWritable, Text, NullWritable> {
        public void reduce(Text key, Iterator<NullWritable> values, OutputCollector<Text, NullWritable> output, Reporter reporter)
                throws IOException {
            output.collect(key, NullWritable.get());
        }
    }

    public int run(String[] args) throws Exception {
        JobConf conf = new JobConf(getConf(), Robot.class);
        conf.setJobName("web_robot");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(NullWritable.class);

        conf.setMapperClass(Map.class);
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        ArrayList<String> other_args = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if ("-depth".equals(args[i])) {
                conf.setInt("depth", Integer.parseInt(args[++i]));
            } else {
                other_args.add(args[i]);
            }
        }
        System.out.println(other_args);
        FileInputFormat.setInputPaths(conf, new Path(other_args.get(1)));
        FileOutputFormat.setOutputPath(conf, new Path(other_args.get(2)));

        JobClient.runJob(conf);
        return 0;
    }

    public static void main(String[] args) throws Exception {
        int res = ToolRunner.run(new Configuration(), new Robot(), args);
        System.exit(res);
    }
}
