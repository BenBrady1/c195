package Controllers;

import Models.Appointment;
import Models.Contact;
import Models.Customer;
import Models.Division;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.util.Pair;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class Report extends Base implements Initializable {
    @FXML
    private TextArea textArea;

    private final List<Pair<String, Integer>> monthsCounts = new ArrayList<>();
    private final List<Pair<String, Integer>> typesCounts = new ArrayList<>();

    private final Map<Long, List<Appointment>> contactAppointmentMap = new HashMap<>();
    private final List<Contact> contacts = new ArrayList<>();

    private final Map<Long, List<Customer>> divisionCustomerMap = new HashMap<>();
    private final List<Division> divisions = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        textArea.setEditable(false);
    }

    @FXML
    private void runReport(ActionEvent event) {
        textArea.clear();
        final String button = ((Button) event.getSource()).getId().replace("button", "");
        String report;
        switch (button) {
            case "1":
                report = report1();
                break;
            case "2":
                report = report2();
                break;
            case "3":
                report = report3();
                break;
            default:
                System.out.println("unreachable unhandled report button");
                report = "";
        }
        textArea.setText(report);
    }

    private String report1() {
        executeQuery("SELECT MONTH(`Start`) as `Month`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY MONTH(`Start`) " +
                "ORDER BY MONTH(`Start`)", this::parseMonthsCount);
        executeQuery("SELECT `Type`, COUNT(*) as `Count` " +
                "FROM appointments GROUP BY `Type` " +
                "ORDER BY `Type`", this::parseTypesCount);

        String display = bundle.getString("report.byMonth") + ":\n";
        for (Pair<String, Integer> monthsCount : monthsCounts) {
            display += formatPair(monthsCount);
        }
        display += "\n" + bundle.getString("report.byType") + ":\n";
        for (Pair<String, Integer> typesCount : typesCounts) {
            display += formatPair(typesCount);
        }

        return display;
    }

    private String formatPair(Pair<String, Integer> pair) {
        return String.format("\t%s:\t%d\n", pair.getKey(), pair.getValue());
    }

    private void parseMonthsCount(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        monthsCounts.clear();
        try {
            while (rs.next()) {
                final String month;
                month = bundle.getString(String.format("month.%d", rs.getInt(1)));
                monthsCounts.add(new Pair<>(month, rs.getInt(2)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private void parseTypesCount(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        typesCounts.clear();
        try {
            while (rs.next()) {
                typesCounts.add(new Pair<>(rs.getString(1), rs.getInt(2)));
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    private String report2() {
        executeQuery("SELECT Appointment_ID, Title, Description, `Location`, `Type`, `Start`, `End`, " +
                "Customer_ID, User_ID, c.Contact_ID, c.Contact_Name, c.Email " +
                "FROM appointments a " +
                "JOIN contacts c ON c.Contact_ID = a.Contact_ID " +
                "ORDER BY Contact_ID, `Start`", this::parseContactsAndAppointments);

        String display = bundle.getString("report.byContact") + ":\n";
        for (Contact contact : contacts) {
            display += "\n";
            display += contact.toReportString();
            final List<Appointment> appointments = contactAppointmentMap.get(contact.getId());
            for (Appointment appointment : appointments) {
                display += appointment.toReportString();
            }
        }

        return display;
    }

    private void parseContactsAndAppointments(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        try {
            while (rs.next()) {
                final Appointment appointment = new Appointment(rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getTimestamp(6).toLocalDateTime(),
                        rs.getTimestamp(7).toLocalDateTime(),
                        rs.getLong(8),
                        rs.getLong(9),
                        rs.getLong(10));
                List<Appointment> appointments = contactAppointmentMap.get(appointment.getContactId());
                if (appointments == null) {
                    final Contact contact = new Contact(rs.getLong(10), rs.getString(11), rs.getString(12));
                    contacts.add(contact);
                    appointments = new ArrayList<>();
                    contactAppointmentMap.put(contact.getId(), appointments);
                }
                appointments.add(appointment);
            }
        } catch (SQLException exception) {
            printSQLException(exception);
        }
    }

    private String report3() {
        executeQuery("SELECT Customer_ID, Customer_Name, Address, Postal_Code, Phone, d.Division_ID, d.Country_ID, d.Division " +
                "FROM customers c " +
                "JOIN first_level_divisions d ON d.Division_ID = c.Division_ID " +
                "ORDER BY d.Division, c.Customer_ID", this::parseCustomersAndDivisions);

        String display = bundle.getString("report.three") + ":\n";
        for (Division division : divisions) {
            display += "\n";
            display += division.toReportString();
            for (Customer customer : divisionCustomerMap.get(division.getId())) {
                display += customer.toReportString();
            }
        }
        return display;
    }

    private void parseCustomersAndDivisions(SQLException ex, ResultSet rs) {
        if (ex != null) return;
        try {

            while (rs.next()) {
                final Customer customer = new Customer(
                        rs.getLong(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5),
                        rs.getLong(6)
                );
                List<Customer> customers = divisionCustomerMap.get(customer.getDivisionId());
                if (customers == null) {
                    customers = new ArrayList<>();
                    final Division division = new Division(rs.getLong(6), rs.getString(8), rs.getLong(7));
                    divisions.add(division);
                    divisionCustomerMap.put(division.getId(), customers);
                }
                customers.add(customer);
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
