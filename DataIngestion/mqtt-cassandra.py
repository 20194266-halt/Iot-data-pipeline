import json
import uuid
import datetime
from paho.mqtt.client import Client
from cassandra.cluster import Cluster

# Cassandra setup
cluster = Cluster(['127.0.0.1'])  # Cassandra IP address
session = cluster.connect('mykeyspace')

# MQTT setup
mqtt_broker = "10.155.121.104"
mqtt_topic = "home/room-201"

def on_connect(client, userdata, flags, rc):
    print("Connected to MQTT broker")
    client.subscribe(mqtt_topic)

def on_message(client, userdata, msg):
    print(f"Received MQTT message: {msg.payload.decode()}")
    data = json.loads(msg.payload.decode())
    timestamp = datetime.datetime.now()
    record_id = uuid.uuid4()

    # Insert temperature data
    if data.get('temp') is not None:
        prepared_stmt_temp = session.prepare("""
            INSERT INTO room_condition_temp (id, temp_value, room_id, device_id, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """)
        session.execute(prepared_stmt_temp, (record_id, data['temp'], data['room_id'], data['device_id'], timestamp))
    
    # Insert humidity data
    if data.get('humidity') is not None:
        prepared_stmt_humidity = session.prepare("""
            INSERT INTO room_condition_humidity (id, humidity_value, room_id, device_id, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """)
        session.execute(prepared_stmt_humidity, (record_id, data['humidity'], data['room_id'], data['device_id'], timestamp))
    
    # Insert AQI data
    if data.get('AQI') is not None:
        prepared_stmt_aqi = session.prepare("""
            INSERT INTO room_condition_aqi (id, aqi, room_id, device_id, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """)
        session.execute(prepared_stmt_aqi, (record_id, data['AQI'], data['room_id'], data['device_id'], timestamp))
    
    # Insert energy consumption data
    if data.get('energy_consumption') is not None:
        prepared_stmt_energy = session.prepare("""
            INSERT INTO room_condition_energy (id, energy_consumption, room_id, device_id, timestamp)
            VALUES (?, ?, ?, ?, ?)
        """)
        session.execute(prepared_stmt_energy, (record_id, data['energy_consumption'], data['room_id'], data['device_id'], timestamp))

mqtt_client = Client()
mqtt_client.on_connect = on_connect
mqtt_client.on_message = on_message

mqtt_client.connect(mqtt_broker, 1883, 60)
mqtt_client.loop_forever()
