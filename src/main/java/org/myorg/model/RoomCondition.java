package org.myorg.model;
import org.apache.flink.api.java.tuple.Tuple2;
import com.datastax.driver.mapping.annotations.*;
import java.util.UUID;


@Table(keyspace = "mykeyspace", name = "room_condition")    
public class RoomCondition {
    @PartitionKey
    public UUID id;

    @Column(name = "temp_value")
    public Double temp_value;
    
    @Column(name = "humidity_value")
    public Double humidity_value;

    @Column(name = "aqi")
    public Double aqi;

    @Column(name = "energy_consumption")
    public Double energy_consumption;

    public RoomCondition() {}

    public RoomCondition(UUID id, Double temp_value, Double humidity_value, Double aqi, Double energy_consumption) {
        this.id = id;
        this.temp_value = temp_value;
        this.humidity_value = humidity_value;
        this.aqi = aqi;
        this.energy_consumption = energy_consumption;
    }
}
