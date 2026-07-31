package com.implantiq;

public class Patient {
    private String id;
    private String name;
    private String lastPredictionDate;
    private String grade;

    public Patient(String id, String name, String lastPredictionDate, String grade) {
        this.id = id;
        this.name = name;
        this.lastPredictionDate = lastPredictionDate;
        this.grade = grade;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getLastPredictionDate() { return lastPredictionDate; }
    public String getGrade() { return grade; }
    
    public String getInitials() {
        if (name == null || name.isEmpty()) return "??";
        String[] parts = name.split(" ");
        if (parts.length >= 2) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}