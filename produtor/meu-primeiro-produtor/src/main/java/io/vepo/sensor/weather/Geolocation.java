package io.vepo.sensor.weather;

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
    public String toString() {
        return String.format("Geolocation[lat=%f, lon=%f]", lat, lon);
    }

}
