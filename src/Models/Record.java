package Models;

import java.lang.reflect.Field;
import java.util.Locale;
import java.util.ResourceBundle;

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
        for (Field declaredField : getClass().getDeclaredFields()) {
            try {
                declaredField.setAccessible(true);
                Object value = declaredField.get(this);
                if (value instanceof String) {
                    if (((String) value).length() == 0) {
                        throw new ValidationError(getErrorMessage(declaredField.getName(), "empty"));
                    }
                } else if (value instanceof Long) {
                    if ((Long) value == 0) {
                        throw new ValidationError(getErrorMessage(declaredField.getName(), "empty"));
                    }
                } else {
                    throw new RuntimeException("unreachable");
                }
            } catch (IllegalAccessException ex) {
                System.out.println(ex);
            }
        }
    }
}
