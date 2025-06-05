CREATE TEMP TABLE raw_data(
    "Name" TEXT,
    Latitude FLOAT,
    Longitude FLOAT,
    Address TEXT,
    "Charger Type" TEXT,
    "Cost (USD/kWh)" TEXT,
    "Availability" TEXT,
    "Distance to City (km)" TEXT,
    "Usage Stats (avg users/day)" TEXT,
    "Station Operator" TEXT,
    "Charging Capacity (kW)" FLOAT,
    "Connector Types" TEXT,
    "Installation Year" TEXT,
    "Renewable Energy Source" TEXT,
    "Reviews (Rating)" TEXT,
    "Parking Spots" TEXT,
    "Maintenance Frequency" TEXT
);

\copy raw_data FROM 'infra/detailed_ev_charging_stations.csv' WITH CSV HEADER;

INSERT INTO locations(name, lat, lon)
SELECT "Name", Latitude, Longitude
FROM raw_data;

INSERT INTO chargers(name, available, max_power, location_id)
SELECT 'AAA1', true, rd."Charging Capacity (kW)", loc.id
FROM raw_data rd
JOIN locations loc ON rd."Name" = loc.name;
