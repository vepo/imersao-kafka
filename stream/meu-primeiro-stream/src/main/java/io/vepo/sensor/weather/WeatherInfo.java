package io.vepo.sensor.weather;

import java.util.Objects;

public class WeatherInfo {
    private final Geolocation location;
    private final double temperature;
    private final double wind;
    private final long timestamp;

    public WeatherInfo(Geolocation location,
                       double temperature,
                       double wind,
                       long timestamp) {
        this.location = location;
        this.temperature = temperature;
        this.wind = wind;
        this.timestamp = timestamp;
    }

    public Geolocation getLocation() {
        return location;
    }

    public double getTemperature() {
        return temperature;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getWind() {
        return wind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, temperature, wind, timestamp);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this){
            return true;
        } else 
        if (!(obj instanceof WeatherInfo)){
            return false;
        } else {
            WeatherInfo other = (WeatherInfo) obj;
            return Objects.equals(location, other.location) &&
                   temperature == other.temperature&&
                   wind == other.wind &&
                  timestamp == other.timestamp;
        }
    }

    @Override
    public String toString() {
        return String.format("WeatherInfo[location=%s, temperature=%f, wind=%f, timestamp=%d]",
                             location, temperature, wind, timestamp);
    }
}
