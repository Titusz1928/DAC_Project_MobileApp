package com.example.dac_project.models;

public class Coordinate {
    public double lat;
    public double lng;
    public String name;
    public String description;
    public int nr;

    public Coordinate(double lat, double lng, String name, String description, int nr) {
        this.lat = lat;
        this.lng = lng;
        this.name = name;
        this.description = description;
        this.nr = nr;
    }
}
