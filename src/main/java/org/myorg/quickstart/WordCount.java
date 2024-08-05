package org.myorg.quickstart;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class WordCount {
    public static void main(String[] args) throws Exception {
        // set up the execution environment
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // get input data
        env.fromElements("Hello", "Flink", "Streaming").print();

        // execute program
        env.execute("Simple Job Example");
    }
}

