// const socket = io('http://localhost:3000');

// socket.on('roomConditionUpdate', (data) => {
//     console.log('Received roomConditionUpdate:', data);

//     const container = document.getElementById('data-container');
//     container.innerHTML = ''; // Clear existing data

//     if (data.length > 0) {
//         data.forEach(item => {
//             const div = document.createElement('div');
//             div.className = 'data-item';
//             div.innerHTML = `
//                 <p>ID: ${item.id}</p>
//                 <p>Temperature: ${item.temp_value} °C</p>
//                 <p>Humidity: ${item.humidity_value} %</p>
//                 <p>AQI: ${item.aqi}</p>
//                 <p>Energy Consumption: ${item.energy_consumption} kWh</p>
//             `;
//             container.appendChild(div);
//         });
//     } else {
//         container.innerHTML = '<p>No data available</p>';
//     }
// });
document.addEventListener('DOMContentLoaded', () => {
    const ctxEnergy = document.getElementById('energyChart').getContext('2d');
    const ctxTemp = document.getElementById('tempChart').getContext('2d');
    const ctxHumidity = document.getElementById('humidityChart').getContext('2d');
    const ctxAqi = document.getElementById('aqiChart').getContext('2d');

    const generateMockData = (numPoints) => {
        const data = [];
        for (let i = 0; i < numPoints; i++) {
            data.push({
                temp: (Math.random() * 15 + 15).toFixed(2), // Temperature between 15°C and 30°C
                humidity: (Math.random() * 50 + 30).toFixed(2), // Humidity between 30% and 80%
                aqi: (Math.random() * 100 + 50).toFixed(2), // AQI between 50 and 150
                energy: (Math.random() * 500 + 100).toFixed(2) // Energy between 100 kWh and 600 kWh
            });
        }
        return data;
    };

    const updateSummary = (data) => {
        const sumEnergy = data.reduce((acc, item) => acc + parseFloat(item.energy), 0).toFixed(2);
        const avgTemp = (data.reduce((acc, item) => acc + parseFloat(item.temp), 0) / data.length).toFixed(2);
        const avgHumidity = (data.reduce((acc, item) => acc + parseFloat(item.humidity), 0) / data.length).toFixed(2);
        const avgAqi = (data.reduce((acc, item) => acc + parseFloat(item.aqi), 0) / data.length).toFixed(2);

        document.getElementById('energySum').textContent = `${sumEnergy} kWh`;
        document.getElementById('avgTemp').textContent = `${avgTemp} °C`;
        document.getElementById('avgHumidity').textContent = `${avgHumidity} %`;
        document.getElementById('avgAqi').textContent = `${avgAqi}`;
    };

    const updateCharts = (data) => {
        const labels = Array.from({ length: 60 }, (_, i) => `Time ${i + 1}`);
        const energyData = data.map(d => d.energy);
        const tempData = data.map(d => d.temp);
        const humidityData = data.map(d => d.humidity);
        const aqiData = data.map(d => d.aqi);

        new Chart(ctxEnergy, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Energy Consumption (kWh)',
                    data: energyData,
                    borderColor: 'rgba(75, 192, 192, 1)',
                    backgroundColor: 'rgba(75, 192, 192, 0.2)',
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => `Energy: ${context.raw.toFixed(2)} kWh`
                        }
                    }
                },
                scales: {
                    x: { title: { display: true, text: 'Time' } },
                    y: { title: { display: true, text: 'kWh' }, beginAtZero: true }
                }
            }
        });

        new Chart(ctxTemp, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Temperature (°C)',
                    data: tempData,
                    borderColor: 'rgba(255, 99, 132, 1)',
                    backgroundColor: 'rgba(255, 99, 132, 0.2)',
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => `Temperature: ${context.raw.toFixed(2)} °C`
                        }
                    }
                },
                scales: {
                    x: { title: { display: true, text: 'Time' } },
                    y: { title: { display: true, text: '°C' }, beginAtZero: true }
                }
            }
        });

        new Chart(ctxHumidity, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'Humidity (%)',
                    data: humidityData,
                    borderColor: 'rgba(54, 162, 235, 1)',
                    backgroundColor: 'rgba(54, 162, 235, 0.2)',
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => `Humidity: ${context.raw.toFixed(2)} %`
                        }
                    }
                },
                scales: {
                    x: { title: { display: true, text: 'Time' } },
                    y: { title: { display: true, text: '%' }, beginAtZero: true }
                }
            }
        });

        new Chart(ctxAqi, {
            type: 'line',
            data: {
                labels,
                datasets: [{
                    label: 'AQI',
                    data: aqiData,
                    borderColor: 'rgba(255, 206, 86, 1)',
                    backgroundColor: 'rgba(255, 206, 86, 0.2)',
                    fill: false
                }]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { display: false },
                    tooltip: {
                        callbacks: {
                            label: (context) => `AQI: ${context.raw.toFixed(2)}`
                        }
                    }
                },
                scales: {
                    x: { title: { display: true, text: 'Time' } },
                    y: { title: { display: true, text: 'AQI' }, beginAtZero: true }
                }
            }
        });
    };

    const refreshData = () => {
        const data = generateMockData(60);
        updateSummary(data);
        updateCharts(data);
    };

    // Refresh data every 5 seconds (5000 ms)
    setInterval(refreshData, 5000);
    refreshData(); // Initial load
});
