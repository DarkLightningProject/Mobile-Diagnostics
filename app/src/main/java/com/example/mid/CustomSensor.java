package com.example.mid;

public class CustomSensor {
    private String name;
    private int type;

    public CustomSensor(String name, int type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public int getType() { return type; }
}