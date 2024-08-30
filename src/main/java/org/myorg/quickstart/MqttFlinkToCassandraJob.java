package org.myorg.quickstart;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.cassandra.CassandraSink;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.apache.flink.api.common.functions.MapFunction;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.myorg.model.RoomCondition;
import java.util.UUID;

class MqttSource implements SourceFunction<String> {

    private final String brokerUrl;
    private final String topic;
    private final String clientId;
    private transient MqttClient mqttClient;
    private volatile boolean isRunning = true;

    public MqttSource(String brokerUrl, String topic, String clientId) {
        this.brokerUrl = brokerUrl;
        this.topic = topic;
        this.clientId = clientId;
    }

    @Override
    public void run(SourceContext<String> ctx) throws Exception {
        try {
            mqttClient = new MqttClient(brokerUrl, topic);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            mqttClient.connect(options);
            mqttClient.subscribe(topic);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    isRunning = false;
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    synchronized (ctx.getCheckpointLock()) {
                        ctx.collect(new String(message.getPayload()));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // No-op
                }
            });

            while (isRunning) {
                // Keep the source running to receive messages
                Thread.sleep(100);
            }
        } catch (MqttException e) {
            throw new RuntimeException("Error while setting up the MQTT source.", e);
        } finally {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
        }
    }

    @Override
    public void cancel() {
        isRunning = false;
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                // Log exception
            }
        }
    }
}
public class MqttFlinkToCassandraJob {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        String brokerUrl = "tcp://localhost:1883";
        String clientId = "ha";
        String topic = "home/room-201";

        DataStream<String> mqttStream = env.addSource(new MqttSource(brokerUrl, topic, clientId));

        // Convert each MQTT message to a format suitable for Cassandra
        DataStream<RoomCondition> processedStream = mqttStream
                .map(new MapFunction<String, RoomCondition>() {
                    @Override
                    public RoomCondition map(String message) throws Exception {
                        JsonNode jsonNode = objectMapper.readTree(message);
                        String deviceId = jsonNode.get("deviceId").asText();
                        Double temp = jsonNode.has("temp") ? jsonNode.get("temp").asDouble() : null;
                        Double humidity = jsonNode.has("humidity") ? jsonNode.get("humidity").asDouble() : null;

                        // Generate a unique ID for each record
                        UUID id = UUID.randomUUID();

                        return new RoomCondition(id, temp, humidity, null, null);
                    }
                }).returns(TypeInformation.of(RoomCondition.class));

        // Cassandra Sink Configuration
        CassandraSink.addSink(processedStream)
                .setHost("127.0.0.1", 9042) 
                // .setKeyspace("mykeyspace")
                // .setTable("mykeyspace.room_condition")
                // .setQuery("INSERT INTO mykeyspace.room_conditions (id, temp_value, humidity_value, aqi, energy_consumption) VALUES (?, ?, ?, ?, ?);")
                .build();

        env.execute("MQTT to Cassandra Job");
    }

    // Message class that represents the data to be inserted into Cassandra
    
}
