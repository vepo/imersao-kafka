package io.vepo.sensor.weather;

import java.util.Objects;

public class WeatherAggregate {

    private final double maxTemperature;
    private final double minTemperature;
    private final double maxWind;
    private final double minWind;

    public WeatherAggregate(double maxTemperature,
                            double minTemperature,
                            double maxWind,
                            double minWind) {
        this.maxTemperature = maxTemperature;
        this.minTemperature = minTemperature;
        this.maxWind = maxWind;
        this.minWind = minWind;
    }

    public double getMaxTemperature() {
        return maxTemperature;
    }

    public double getMaxWind() {
        return maxWind;
    }

    public double getMinTemperature() {
        return minTemperature;
    }

    public double getMinWind() {
        return minWind;
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxTemperature, minTemperature, maxWind, minWind);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else 
        if (!(obj instanceof WeatherAggregate)) {
            return false;
        } else {
            WeatherAggregate other = (WeatherAggregate) obj;
            return maxTemperature == other.maxTemperature && 
                   minTemperature == other.minTemperature &&
                   maxWind == other.maxWind &&
                   minWind == other.minWind;
        }
    }

    @Override
    public String toString() {
        return String.format("WeatherAggregate[maxTemperature=%f, minTemperature=%f, maxWind=%f, minWind=%f]", 
                             maxTemperature, minTemperature, maxWind, minWind);
    }
}
