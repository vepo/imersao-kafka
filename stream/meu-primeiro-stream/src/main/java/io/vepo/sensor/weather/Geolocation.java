package io.vepo.sensor.weather;

import java.util.Objects;

public class Geolocation {
    private final double lat;
    private final double lon;

    public Geolocation(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lat, lon);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else 
        if (!(obj instanceof Geolocation)) {
            return false;
        } else {
            Geolocation other = (Geolocation) obj;
            return lat == other.lat && lon == other.lon;
        }
    }

    @Override
    public String toString() {
        return String.format("Geolocation[lat=%f, lon=%f]", lat, lon);
    }

}
