# Data Processing Pipeline

## Overview

This project involves a data processing pipeline that reads data from Apache Cassandra, performs calculations, and stores the processed results into Apache Hive. The pipeline operates on an hourly basis and is designed to facilitate reporting and analytics.

## Workflow

1. **Data Extraction**: The pipeline extracts data from four tables in Apache Cassandra every hour.
    - `room_condition_temp`
    - `room_condition_humidity`
    - `room_condition_energy`
    - `room_condition_aqi`

2. **Data Processing**:
    - **Sum Calculation**: Computes the total `energy_consumption` for each group.
    - **Average Calculation**: Calculates the average values for `humidity`, `aqi`, and `temp` columns.
    - **Grouping**: Aggregates the data by day to summarize the results.

3. **Data Storage**: The processed results are stored in a single table in Apache Hive:
    - `room_condition`

## Details

### Data Extraction

- **Source**: Apache Cassandra
- **Frequency**: Every hour
- **Tables**:
  - `room_condition_temp`
  - `room_condition_humidity`
  - `room_condition_energy`
  - `room_condition_aqi`

### Data Processing

1. **Aggregation**:
   - **Energy Consumption**: `SUM(energy_consumption)` grouped by hour.
   - **Average Calculations**:
     - `AVG(humidity)`
     - `AVG(aqi)`
     - `AVG(temp)`

2. **Grouping**: Data is grouped by day to generate daily summaries.

### Data Storage

- **Destination**: Apache Hive
- **Table**: `room_condition`
- **Format**: The results are stored with appropriate columns to facilitate reporting.

## Setup

### Requirements

- Apache Cassandra
- Apache Hive
- Java 6
- Apache Flink
- Maven
