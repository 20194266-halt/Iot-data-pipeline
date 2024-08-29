package org.myorg.quickstart;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.connectors.mqtt.MQTTSourceFunction;
import org.apache.flink.streaming.connectors.mqtt.SimpleStringSchema;
import org.myorg.sink.CassandraSink;

public class MqttToCassandraJob {
    public static void main(String[] args) throws Exception {
        // Set up the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // Configure the MQTT source
        MQTTSourceFunction<String> mqttSource = new MQTTSourceFunction<>(
            "tcp://localhost:1883",     // MQTT broker address
            "home/room-201",                  // MQTT topic to subscribe to
            new SimpleStringSchema()    // Deserialization schema
        );

        DataStream<String> stream = env.addSource(mqttSource);

        // Set up the Cassandra sink
        stream.addSink(new CassandraSink<>("mykeyspace", "room_condition"));

        // Execute the Flink job
        env.execute("MQTT to Cassandra Job");
    }
}
