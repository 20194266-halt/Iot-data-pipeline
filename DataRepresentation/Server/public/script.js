const socket = io('http://localhost:3000');

socket.on('roomConditionUpdate', (data) => {
    console.log('Received roomConditionUpdate:', data);

    const container = document.getElementById('data-container');
    container.innerHTML = ''; // Clear existing data

    if (data.length > 0) {
        data.forEach(item => {
            const div = document.createElement('div');
            div.className = 'data-item';
            div.innerHTML = `
                <p>ID: ${item.id}</p>
                <p>Temperature: ${item.temp_value} °C</p>
                <p>Humidity: ${item.humidity_value} %</p>
                <p>AQI: ${item.aqi}</p>
                <p>Energy Consumption: ${item.energy_consumption} kWh</p>
            `;
            container.appendChild(div);
        });
    } else {
        container.innerHTML = '<p>No data available</p>';
    }
});
