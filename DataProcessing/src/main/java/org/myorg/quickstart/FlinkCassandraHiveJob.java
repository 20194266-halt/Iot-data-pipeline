package org.myorg.quickstart;

import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.table.catalog.hive.HiveCatalog;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row as CassandraRow;
import com.datastax.driver.core.Session;

import java.time.LocalDate;
import java.util.UUID;

public class FlinkCassandraHiveJob {

    public static class CassandraSourceFunction implements SourceFunction<Tuple5<Integer, Double, Double, Double, Double>> {
        private transient Cluster cluster;
        private transient Session session;
        private volatile boolean isRunning = true;
        private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public void run(SourceContext<Tuple5<Integer, Double, Integer, String, String>> ctx) throws Exception {
            cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
            session = cluster.connect("mykeyspace");

            processTable("room_condition_temp", ctx);

            processTable("room_condition_humidity", ctx);

            processTable("room_condition_energy", ctx);

            processTable("room_condition_aqi", ctx);
        }

        private void processTable(String tableName, SourceContext<Tuple5<Integer, Double, Integer, String, String>> ctx) {
            ResultSet resultSet = session.execute("SELECT room_id, temp_value, device_id, timestamp, id FROM " + tableName);

            for (Row row : resultSet) {
                if (isRunning) {
                    Date timestamp = row.getTimestamp("timestamp");
                    String formattedTimestamp = dateFormat.format(timestamp);

                    ctx.collect(new Tuple5<Integer, Double, Integer, String, String>(
                        row.getInt("room_id"),
                        row.getDouble("temp_value"),
                        row.getInt("device_id"),
                        formattedTimestamp,
                        row.getString("id")
                    ));
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

        // Create a Table Environment
        EnvironmentSettings settings = EnvironmentSettings.newInstance().useBlinkPlanner().inStreamingMode().build();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env, settings);

        // Define Hive catalog
        String name = "myhive";
        String database = "iotData";
        String hiveConfDir = "/path/to/hive/conf"; // Path to hive-site.xml
        String version = "2.3.7";

        // Register HiveCatalog
        HiveCatalog hive = new HiveCatalog(name, database, hiveConfDir, version);
        tableEnv.registerCatalog("myhive", hive);
        tableEnv.useCatalog("myhive");

        // Source data streams for all tables
        DataStream<Tuple5<Integer, Double, Double, Double, Double>> dataStream = env.addSource(new CassandraSourceFunction());

        // Aggregate data by room_id and day (sum energy, average temp, humidity, aqi)
        DataStream<Tuple5<Integer, Double, Double, Double, Double>> aggregatedStream = dataStream
            .keyBy(value -> value.f0) 
            .timeWindow(Time.days(1)) 
            .reduce(new ReduceFunction<Tuple5<Integer, Double, Double, Double, Double>>() {
                @Override
                public Tuple5<Integer, Double, Double, Double, Double> reduce(Tuple5<Integer, Double, Double, Double, Double> value1, Tuple5<Integer, Double, Double, Double, Double> value2) {
                    return new Tuple5<Integer, Double, Double, Double, Double>(
                        value1.f0, 
                        (value1.f1 + value2.f1) / 2,
                        (value1.f2 + value2.f2) / 2, 
                        (value1.f3 + value2.f3) / 2,
                        value1.f4 + value2.f4 
                    );
                }
            });

        // Convert DataStream to Table
        Table resultTable = tableEnv.fromDataStream(aggregatedStream, "room_id, avg_temp, avg_humidity, avg_aqi, total_energy, day");

        // Write the result to Hive
        tableEnv.executeSql("INSERT INTO default.room_condition SELECT room_id, avg_temp, avg_humidity, avg_aqi, total_energy, day FROM " + resultTable);

        env.execute("Flink Cassandra Read, Aggregate and Write to Hive Job");
    }
}
