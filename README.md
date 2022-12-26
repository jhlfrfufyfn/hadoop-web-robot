# Web robot made with Hadoop MapReduce and Java

## Description
Specify a website and a depth, and the program will crawl the website and all the links it finds, up to the specified depth. The program will then output the links it found.

Input: files in an input hdfs folder containing a list of links to crawl.
Output: files in output hdfs folder containing the links found.
## Usage
1. Install Hadoop (you can clone [docker-hadoop](https://github.com/big-data-europe/docker-hadoop)):

```
git clone https://github.com/big-data-europe/docker-hadoop
```

2. Build and run the docker container in the directory you put hadoop from step 1:

```
docker-compose up -d
```

3. Build the jar file of Robot.java with dependency "hadoop-core-1.2.1.jar"

4. Copy the jar file and input files to the docker container

```
docker cp ${NAME_OF_JAR_FILE} namenode:/tmp
docker cp input/input-github.txt namenode:/tmp
docker cp input/input-stackoverflow.txt namenode:/tmp
```

5. Copy the input files to the hdfs

```
docker exec -it namenode hdfs dfs -mkdir /user/root/input
docker exec -it namenode hdfs dfs -put /tmp/input-github.txt /user/root/input
docker exec -it namenode hdfs dfs -put /tmp/input-stackoverflow.txt /user/root/input
```

6. Run the jar file

```
docker exec -it namenode hadoop jar /tmp/${NAME_OF_JAR_FILE} code.Robot /user/root/input /user/root/output -depth ${DEPTH}
```

7. Copy the output files from the hdfs to the docker container

```
docker exec -it namenode hdfs dfs -get /user/root/output /tmp
```

8. Copy the output files from the docker container to the host

```
docker cp namenode:/tmp/output output/
```

Output file is generated with depth of 2.