package Controllers;

import Models.Appointment;
import javafx.scene.control.TableColumn;

public class AppointmentTable extends Table<Appointment> {
    public AppointmentTable() {
        super(null);
    }

    @Override
    protected void addColumns() {
        final TableColumn<Appointment, String> titleColumn = getStringColumn(Appointment.class, "title");
        final TableColumn<Appointment, String> descriptionColumn = getStringColumn(Appointment.class, "description");
        final TableColumn<Appointment, String> locationColumn = getStringColumn(Appointment.class, "location");
        final TableColumn<Appointment, String> typeColumn = getStringColumn(Appointment.class, "type");
        tableView.getColumns().addAll(titleColumn, descriptionColumn, locationColumn, typeColumn);
    }

    @Override
    protected void populateData() {

    }

    @Override
    protected String getInsertStatement() {
        return null;
    }

    @Override
    protected Appointment getNewRecord() {
        return null;
    }

    @Override
    protected String getUpdateStatement() {
        return null;
    }

    @Override
    protected boolean deleteDependencies(Appointment record) {
        return false;
    }

    @Override
    protected String getDeleteStatement() {
        return null;
    }

    @Override
    protected String getDeletedMessage() {
        return null;
    }
}
