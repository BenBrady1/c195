package Models;

import java.lang.reflect.Field;

public abstract class Record {
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

    private String getErrorMessage(String field, String issue) {
        return String.format("'%s' should not be %s.", field, issue);
    }

    private String titleize(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1).replaceAll("([a-z])([A-Z])", "$1 $2");
    }

    public void validate() throws ValidationError {
        for (Field declaredField : getClass().getDeclaredFields()) {
            try {
                String prettyName = titleize(declaredField.getName());
                declaredField.setAccessible(true);
                Object value = declaredField.get(this);
                if (value instanceof String) {
                    if (((String) value).length() == 0) {
                        throw new ValidationError(getErrorMessage(prettyName, "empty"));
                    }
                } else if (value instanceof Long) {
                    if ((Long) value == 0) {
                        throw new ValidationError(getErrorMessage(prettyName, "empty"));
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
