package utils;

public class Sexagesimal {
    public int degrees;
    public double minutes;

    public Sexagesimal(int degrees, double minutes) {
        //if (Math.abs(degrees) > 180 || minutes >= 60) {
        this.degrees = degrees;
        this.minutes = minutes;
    }

    public double toCoordinate() {
        double coordinate = Math.abs(degrees) + (minutes / 60.0);
        if (degrees < 0) coordinate = -coordinate;
        return coordinate;
    }

    public int toCoordinateE6() {
        return (int) (toCoordinate() * 1E6);
    }
}