package org.myorg.quickstart;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;

public class MqttFlinkExample {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        String brokerUrl = "tcp://10.155.121.104:1883";
        String topic = "home/room-201";

        DataStream<String> stream = env.addSource(new MqttSource(brokerUrl, topic));

        stream.print();

        env.execute("Flink MQTT Example");
    }

    // Custom MQTT Source Function
    public static class MqttSource implements SourceFunction<String>, MqttCallback {
        private final String brokerUrl;
        private final String topic;
        private volatile boolean isRunning = true;
        private MqttClient client;
        private SourceContext<String> ctx;

        public MqttSource(String brokerUrl, String topic) {
            this.brokerUrl = brokerUrl;
            this.topic = topic;
        }

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            this.ctx = ctx;  // Initialize the SourceContext
            client = new MqttClient(brokerUrl, MqttClient.generateClientId());
            client.setCallback(this);
            client.connect();
            client.subscribe(topic);
            while (isRunning) {
                // Keep the source running
                Thread.sleep(1000);  // Avoid tight loop
            }
            client.disconnect();
        }

        @Override
        public void cancel() {
            isRunning = false;
            if (client != null && client.isConnected()) {
                try {
                    client.disconnect();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void connectionLost(Throwable throwable) {
            // Handle connection lost
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            // Emit the message to Flink
            if (message != null) {
                String payload = new String(message.getPayload());
                // Add message to the source context
                ctx.collect(payload);
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
            // Handle delivery complete
        }
    }
}
