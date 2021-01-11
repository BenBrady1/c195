package Models;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

public class Appointment extends Record implements Model<Appointment> {
    private String title;
    private String description;
    private String location;
    private String type;
    private OffsetDateTime start;
    private OffsetDateTime end;
    private long customerId;
    private long userId;
    private long contactId;

    public Appointment(long id,
                       String title,
                       String description,
                       String location,
                       String type,
                       OffsetDateTime start,
                       OffsetDateTime end,
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
        return new Appointment(id, title, description, location, type, start, end, customerId, userId, contactId);
    }

    @Override
    public List<Object> toValues() {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new ArrayList(List.of(title,
                description,
                location,
                type,
                formatter.format(start.atZoneSameInstant(ZoneOffset.UTC)),
                formatter.format(end.atZoneSameInstant(ZoneOffset.UTC)),
                customerId,
                userId,
                contactId));
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

    public OffsetDateTime getStart() {
        return start;
    }

    public void setStart(OffsetDateTime start) {
        this.start = start;
    }

    public OffsetDateTime getEnd() {
        return end;
    }

    public void setEnd(OffsetDateTime end) {
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

    private String formatDate(OffsetDateTime date) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).withLocale(locale);
        return date.toLocalDateTime().format(formatter);
    }

    @Override
    protected void customValidate() throws ValidationError {
        checkDateRange(start.atZoneSameInstant(ZoneId.of("US/Eastern")), bundle.getString("appointment.start"));
        checkDateRange(end.atZoneSameInstant(ZoneId.of("US/Eastern")), bundle.getString("appointment.end"));
        if (start.compareTo(end) > 0) {
            // FIXME: translate
            throw new ValidationError("start comes after end");
        }
    }

    private void checkDateRange(ZonedDateTime date, String name) throws ValidationError {
        if (date.getHour() < 8 || date.getHour() > 22 || (date.getHour() == 22 && date.getMinute() != 0)) {
            // FIXME: translate
            throw new ValidationError(String.format("%s outside range", name));
        }
    }
}
