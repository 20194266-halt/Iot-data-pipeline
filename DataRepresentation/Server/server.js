const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const cassandra = require('cassandra-driver');
const helmet = require('helmet');

const app = express();
const server = http.createServer(app);
const io = new Server(server);

// Configure helmet with a custom Content Security Policy
app.use(helmet({
    contentSecurityPolicy: {
        directives: {
            defaultSrc: ["'self'"],
            imgSrc: ["'self'", "data:", "http://localhost:3000"], // Allow images from localhost
            scriptSrc: ["'self'"],
            styleSrc: ["'self'"],
            connectSrc: ["'self'", "http://localhost:3000"]
        }
    }
}));

app.use(express.static('public'));

// Cassandra client configuration
const client = new cassandra.Client({
    contactPoints: ['127.0.0.1'],
    localDataCenter: 'datacenter1',
    keyspace: 'mykeyspace'
});

const getLatestRoomConditions = async () => {
    const tempQuery = 'SELECT * FROM room_condition_temp LIMIT 10';
    const humidityQuery = 'SELECT * FROM room_condition_humidity LIMIT 10';
    const aqiQuery = 'SELECT * FROM room_condition_aqi LIMIT 10';
    const energyQuery = 'SELECT * FROM room_condition_energy LIMIT 10';

    try {
        const [tempResult, humidityResult, aqiResult, energyResult] = await Promise.all([
            client.execute(tempQuery),
            client.execute(humidityQuery),
            client.execute(aqiQuery),
            client.execute(energyQuery)
        ]);

        const tempData = tempResult.rows ?? [];
        const humidityData = humidityResult.rows ?? [];
        const aqiData = aqiResult.rows ?? [];
        const energyData = energyResult.rows ?? [];

        // Assuming you want to combine the results into a single array
        // Adjust according to your data structure and requirements
        const combinedData = tempData.map(item => ({
            id: item.room_id,
            temp_value: item.temp_value,
            humidity_value: humidityData.find(h => h.room_id === item.room_id)?.humidity_value || null,
            aqi: aqiData.find(a => a.room_id === item.room_id)?.aqi || null,
            energy_consumption: energyData.find(e => e.room_id === item.room_id)?.energy_consumption || null
        }));

        return combinedData;
    } catch (err) {
        console.error('Error fetching data:', err);
        return [];
    }
};


io.on('connection', (socket) => {
    console.log('Client connected');

    setInterval(async () => {
        const data = await getLatestRoomConditions();
        socket.emit('roomConditionUpdate', data);
    }, 5000);

    socket.on('disconnect', () => {
        console.log('Client disconnected');
    });
});

server.listen(3000, () => {
    console.log('Server is running on port 3000');
});
