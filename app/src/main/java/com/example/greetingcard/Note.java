package com.example.greetingcard;

import java.io.Serializable;

public class Note implements Serializable {
    private String id;
    private String title;
    private String content;
    private String date;
    private int backgroundColor;
    private long timestamp;

    public Note() {}

    public Note(String title, String content, String date) {
        this.title = title;
        this.content = content;
        this.date = date;
        this.backgroundColor = 0xFFFFFFFF; // default white
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public int getBackgroundColor() { return backgroundColor; }
    public void setBackgroundColor(int backgroundColor) { this.backgroundColor = backgroundColor; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}