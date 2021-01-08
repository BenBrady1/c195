package Models;

import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Appointment extends Record implements Model<Appointment> {
    private String title;
    private String description;
    private String location;
    private String type;
    private LocalDateTime start;
    private LocalDateTime end;
    private long customerId;
    private long userId;
    private long contactId;

    public Appointment(long id,
                       String title,
                       String description,
                       String location,
                       String type,
                       LocalDateTime start,
                       LocalDateTime end,
                       long customerId,
                       long userId,
                       long contactId) {
        super(id);
        this.title = title;
        this.description = description;
        this.location = location;
        this.type = type;
        this.start = start;
        this.end = end;
        this.customerId = customerId;
        this.userId = userId;
        this.contactId = contactId;
    }

    @Override
    public Appointment copy() {
        return null;
    }

    @Override
    public List<Object> toValues() {
        return new ArrayList(List.of(title, description, location, type, start, end, customerId, userId, contactId));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title.trim();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description.trim();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location.trim();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type.trim();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(long customerId) {
        this.customerId = customerId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getFormattedStart() {
        return formatDate(start);
    }

    public String getFormattedEnd() {
        return formatDate(end);
    }

    private String formatDate(LocalDateTime date) {
//        final DateFormat formatter = DateTimeFormatter.
//        System.out.println(date);
//        return formatter.format(date);
        return date.toString();
    }
}
