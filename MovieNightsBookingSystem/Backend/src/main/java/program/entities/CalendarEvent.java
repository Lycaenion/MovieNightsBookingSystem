package program.entities;

import com.google.api.client.util.DateTime;

public class CalendarEvent {
    private String summary;
    private DateTime startDate;
    private DateTime endDate;

    public CalendarEvent(String summary, DateTime startDate, DateTime endDate) {
        this.summary = summary;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }
}
