package Models;

public class Contact extends Record implements Reportable {
    final String name;
    final String email;

    public Contact(long id, String name, String email) {
        super(id);
        this.name = name;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String toString() {
        return name;
    }

    public String toReportString() {
        return String.format("%d\t%s\t%s\n", id, name, email);
    }
}
