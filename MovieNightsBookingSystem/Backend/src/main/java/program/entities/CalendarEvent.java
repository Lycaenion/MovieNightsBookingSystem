package program.entities;

import com.google.api.client.util.DateTime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class CalendarEvent {
    private String summary;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;

    public CalendarEvent(String summary, DateTime startDateTime, DateTime endDateTime) {
        this.summary = summary;
        this.startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateTime.getValue()), ZoneId.systemDefault());
        this.endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDateTime.getValue()), ZoneId.systemDefault());
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(DateTime startDateTime) {
        this.startDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(startDateTime.getValue()), ZoneId.systemDefault());
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(DateTime endDateTime) {
        this.endDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(endDateTime.getValue()), ZoneId.systemDefault());
    }
}
