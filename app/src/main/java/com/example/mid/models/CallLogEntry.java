package com.example.mid.models;

public class CallLogEntry {
    private String name;
    private String number;
    private int duration;
    private String type;
    private String date;

    public CallLogEntry(String name, String number, int duration, String type, String date) {
        this.name = name;
        this.number = number;
        this.duration = duration;
        this.type = type;
        this.date = date;
    }

    public String getName() { return name; }
    public String getNumber() { return number; }
    public int getDuration() { return duration; }
    public String getType() { return type; }
    public String getDate() { return date; }
}
