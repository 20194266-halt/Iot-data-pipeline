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

// Serve static files from the 'Frontend' directory
app.use(express.static('Frontend'));

// Cassandra client configuration
const client = new cassandra.Client({
    contactPoints: ['127.0.0.1'],
    localDataCenter: 'datacenter1',
    keyspace: 'your_keyspace'
});

const getLatestRoomConditions = async () => {
    const query = 'SELECT * FROM room_condition ORDER BY timestamp DESC LIMIT 10';
    try {
        const result = await client.execute(query);
        return result.rows ?? [];
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
