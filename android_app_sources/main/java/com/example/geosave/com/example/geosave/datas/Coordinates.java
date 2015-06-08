package com.example.geosave.com.example.geosave.datas;

/**
 * Coordinates Class
 * v 1.0
 *
 * @author Thibaut Vercueil
 */


public class Coordinates {
    private double lat;
    private double lng;

    /**
     * Constructor
     *
     * @param lat
     * @param lng
     */
    public Coordinates(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        double longDiff = lon2 - lon1;
        double y = Math.sin(longDiff) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(longDiff);

        return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360;
    }

    /**
     * Getter
     *
     * @return current latitude
     */
    public double getLat() {
        return this.lat;
    }

    /**
     * Setter
     *
     * @param l latitude
     */
    public void setLat(double l) {
        this.lat = l;
    }

    /**
     * Getter
     *
     * @return current longitude
     */
    public double getLng() {
        return this.lng;
    }

    /**
     * Setter
     *
     * @param l longitude
     */
    public void setLng(double l) {
        this.lng = l;
    }

    public double getDistance(Coordinates C) {
        //Converting into Radian
        double phi1 = (this.lat * Math.PI) / 180;
        double lambda1 = (this.lng * Math.PI) / 180;

        double phi2 = (C.getLat() * Math.PI) / 180;
        double lambda2 = (C.getLng() * Math.PI) / 180;

        //Applying the magic formula
        return Math.acos(Math.sin(phi1) * Math.sin(phi2) + Math.cos(phi1) * Math.cos(phi2) * Math.cos(lambda2 - lambda1)) * 6371000;

    }
}