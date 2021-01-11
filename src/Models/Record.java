package Models;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public abstract class Record {
    public static ResourceBundle bundle;
    public static Locale locale;

    public class ValidationError extends Exception {
        public ValidationError(String message) {
            super(message);
        }
    }

    protected long id;

    public Record(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String getErrorMessage(String field, String issueName) {
        final String key = String.format("%s.%s", getClass().getSimpleName().toLowerCase(), field);
        final String title = bundle.getString(key);
        final String issue = bundle.getString(String.format("issue.%s", issueName));
        final String message = bundle.getString("error.empty")
                .replace("%{field}", title)
                .replace("%{issue}", issue);
        return String.format(message, field, issue);
    }

    public void validate() throws ValidationError {
        for (final Field declaredField : getRequiredFields()) {
            try {
                declaredField.setAccessible(true);
                final Object value = declaredField.get(this);
                if (value instanceof String) {
                    if (((String) value).length() == 0) {
                        throw new ValidationError(getErrorMessage(declaredField.getName(), "empty"));
                    }
                } else if (value instanceof Long) {
                    System.out.println(declaredField.getName());
                    if ((Long) value == 0) {
                        throw new ValidationError(getErrorMessage(declaredField.getName(), "empty"));
                    }
                } else if (value instanceof OffsetDateTime) {
                } else {
                    throw new ValidationError("unreachable");
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
        }
        
        customValidate();
    }

    protected void customValidate() throws ValidationError {};

    protected List<Field> getRequiredFields() {
        return Arrays.stream(getClass().getDeclaredFields()).collect(Collectors.toList());
    }
}
