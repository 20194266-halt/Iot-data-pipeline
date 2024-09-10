# Real-Time Dashboard with Node.js, Cassandra, and Socket.IO

This project is a real-time dashboard that fetches data from a Cassandra database and displays it on a front-end web interface using Socket.IO for real-time updates. The dashboard shows sensor data like temperature, humidity, AQI, and energy consumption from different rooms.

## Table of Contents
- [Technologies Used](#technologies-used)
- [Project Architecture](#project-architecture)
- [Flow of the Application](#flow-of-the-application)
- [Setup Instructions](#setup-instructions)
- [Running the Project](#running-the-project)

## Technologies Used

### 1. **Node.js**
   - The core of the backend, responsible for serving the web interface and handling real-time data updates.
   - Used for building the server that listens for connections from clients and interacts with the database.
   - Version: Node.js 16

### 2. **Express**
   - A lightweight framework for creating the web server.
   - Serves static files from the `Frontend` folder and handles basic routing.
   
### 3. **Socket.IO**
   - A library for real-time, bi-directional communication between the server and the client.
   - Allows pushing data from the backend to the front-end without the need for frequent polling.
   
### 4. **Cassandra**
   - A NoSQL distributed database used to store sensor data such as temperature, humidity, AQI, and energy consumption.
   - Ensures that data is replicated and highly available.

### 5. **Helmet**
   - A middleware that helps secure the Express app by setting various HTTP headers, such as Content Security Policy (CSP).
   - It helps prevent common vulnerabilities like Cross-Site Scripting (XSS) attacks.

### 6. **Frontend (HTML/CSS/JavaScript)**
   - The front-end is a basic HTML page that connects to the Node.js backend using Socket.IO.
   - It displays real-time data updates in a user-friendly format, showing charts or tables representing sensor data.

## Project Architecture
![Dashoard](/Images/real-time-dashboard.webp)

### Components

1. **Frontend**: A web interface that displays the real-time data updates received from the Node.js server.
   
2. **Node.js Server**: Handles the interaction between the front-end and the database. It fetches data from Cassandra and pushes updates to the client using Socket.IO.
   
3. **Cassandra**: Stores the sensor data. The Node.js server queries this database and retrieves the latest sensor readings for display.

## Flow of the Application

1. **Data Ingestion**: Sensor data is ingested into the Cassandra database via various tables such as:
   - `room_condition_temp`: Stores temperature values.
   - `room_condition_humidity`: Stores humidity values.
   - `room_condition_aqi`: Stores AQI values.
   - `room_condition_energy`: Stores energy consumption values.

2. **Backend (Node.js) Queries Cassandra**:
   - The server queries the latest 10 records from each of the tables mentioned above using Cassandra’s CQL (Cassandra Query Language).
   - These records are merged based on a common `id` or timestamp to create a combined room condition data structure.

3. **Real-Time Updates**:
   - Using Socket.IO, the server sends updates to all connected clients every 5 seconds.
   - The data is emitted as a `roomConditionUpdate` event, which the front-end listens to.

4. **Frontend Displays Data**:
   - The front-end, built using HTML/JavaScript, listens to the `roomConditionUpdate` event and dynamically updates the displayed data without the need for refreshing the page.
   - Users see real-time data updates on room conditions such as temperature, humidity, AQI, and energy consumption.

## Setup Instructions

### Prerequisites

- Node.js (v16 or higher) installed on your machine.
- Cassandra installed and running on your local machine or a remote server.
- `npm` package manager.
