package org.myorg.quickstart;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

public class FlinkCassandraReadJob {

    public static class CassandraSourceFunction implements SourceFunction<Tuple2<Double, Integer>> {
        private transient Cluster cluster;
        private transient Session session;
        private volatile boolean isRunning = true;

        @Override
        public void run(SourceContext<Tuple2<Double, Integer>> ctx) throws Exception {
            cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
            session = cluster.connect("mykeyspace");

            ResultSet resultSet = session.execute("SELECT temp_value, room_id FROM room_condition_temp");

            for (Row row : resultSet) {
                if (isRunning) {
                    ctx.collect(new Tuple2<Double, Integer>(row.getDouble("temp_value"), row.getInt("room_id")));
                }
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
            if (session != null) {
                session.close();
            }
            if (cluster != null) {
                cluster.close();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Tuple2<Double, Integer>> dataStream = env.addSource(new CassandraSourceFunction());

        dataStream.print();

        env.execute("Flink Cassandra Read Job");
    }
}
