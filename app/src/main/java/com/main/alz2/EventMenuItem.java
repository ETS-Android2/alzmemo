package com.main.alz2;

/**
 * Created by Jeff on 12/13/2015.
 */
public class EventMenuItem {

    private int eventId;
    private String detail;
    private String to;
    private String from;
    private String eventDate;
    private String location;
    private String time;
    private int reminder;
    private int color;


    public EventMenuItem(int eventId, String detail, String to, String from, String eventDate, String location, int reminder, int color) {
        this.eventId = eventId;
        this.detail = detail;
        this.to = to;
        this.from = from;
        this.eventDate = eventDate;
        this.location = location;
        this.reminder = reminder;
        this.color = color;

        String[] tmp = this.eventDate.split(" ");
        this.time = tmp[1].split(":")[0] + ":" +  tmp[1].split(":")[1];

    }

    public EventMenuItem(){

        this.detail = "";
        this.to = "";
        this.from = "";
        this.eventDate = "";
        this.location = "";
    }



    public int getEventId() {
        return eventId;
    }
    public int getReminder() {
        return reminder;
    }

    public int getColor()
    {
        return color;
    }

    public void setColor(int color)
    {
        this.color=color;
    }
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }
    public void setReminder(int reminder) {
        this.reminder = reminder;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getEventDate() {
        return eventDate;
    }

    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getItemDesc() {
        return this.time + " - " + this.getTo();
    }
}
