package org.myorg.sink;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import org.myorg.model.RoomCondition;
public class CassandraSink<T> implements SinkFunction<T> {

    private transient Cluster cluster;
    private transient Session session;
    private String keyspace;
    private String table;

    public CassandraSink(String keyspace, String table) {
        this.keyspace = keyspace;
        this.table = table;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        cluster = Cluster.builder().addContactPoint("127.0.0.1").build(); // Cassandra contact point
        session = cluster.connect(keyspace);
    }

    @Override
    public void invoke(T value, Context context) throws Exception {
        RoomCondition condition = (RoomCondition) value;
    
        // Prepare the query
        String query = "INSERT INTO " + table + " (id, humidity_value, temp_value, aqi, energy_consumption) VALUES (uuid(), ?, ?, ?, ?)";
        
        // Execute the query with the appropriate parameters
        session.execute(query, 
            condition.getHumidityValue(), 
            condition.getTempValue(), 
            condition.getAqi(), 
            condition.getEnergyConsumption());
        }

    @Override
    public void close() throws Exception {
        super.close();
        if (session != null) {
            session.close();
        }
        if (cluster != null) {
            cluster.close();
        }
    }
}
