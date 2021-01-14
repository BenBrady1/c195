package Controllers;

import Models.Model;
import Models.Record;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * an abstract class to hold records from the database of the given Record subclass
 *
 * @param <T> a subclass of the Record model that implements the Model interface
 */
public abstract class Table<T extends Record & Model<T>> extends Base implements Initializable {
    @FXML
    protected TableView<T> tableView;
    @FXML
    private Button deleteButton;
    @FXML
    protected Button filterButton;

    protected Form<T> formController;
    final protected FormFactory formFactory;
    final protected Main.EventEmitter eventEmitter;

    public Table(FormFactory formFactory, Main.EventEmitter eventEmitter) {
        this.formFactory = formFactory;
        this.eventEmitter = eventEmitter;
    }

    /**
     * adds columns to the table that match the shape of the generic T
     */
    protected abstract void addColumns();

    /**
     * uses introspection to get all the string fields for the given generic mode T
     *
     * @param tClass    the class for T
     * @param fieldName the name of the instance field
     * @return the TableColumn for the record
     */
    protected TableColumn<T, String> getStringColumn(Class<T> tClass, String fieldName) {
        try {
            final Field field = tClass.getDeclaredField(fieldName);
            field.setAccessible(true);
            final String key = String.format("%s.%s", tClass.getSimpleName().toLowerCase(), field.getName());
            final TableColumn<T, String> column = new TableColumn<>(bundle.getString(key));
            column.setCellValueFactory(param -> {
                try {
                    return new SimpleStringProperty((String) field.get(param.getValue()));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                return null;
            });
            return column;
        } catch (NoSuchFieldException ex) {
            System.out.println(ex);
        }

        return null;
    }

    /**
     * implemented by subclasses to populate the table with the data and build any maps might be necessary to properly
     * display foreign key columns
     */
    protected abstract void populateData();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filterButton.setDisable(true);
        filterButton.setVisible(false);
        final TableColumn<T, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    /**
     * opens a form in the proper mode with the given record
     *
     * @param mode
     * @param record
     * @param callback
     */
    private void openForm(FormFactory.Mode mode, T record, Consumer<T> callback) {
        formController = formFactory.getInstance(mode, record, callback);
        formController.open();
        System.out.println("here");
    }

    private void finalizeAction() {
        tableView.refresh();
        formController = null;
        System.out.println("here");
    }

    private void addToDatabase(T record) {
        if (canUpdate(record)) {
            final List<Object> arguments = record.toValues();
            arguments.add(userId);
            arguments.add(userId);
            executeInsert(getInsertStatement(), arguments, (ex, newId) -> {
                if (ex != null) printSQLException(ex);
                if (newId != null) record.setId(newId);
            });
        }
    }

    protected abstract String getInsertStatement();

    protected abstract T getNewRecord();

    @FXML
    private void addRecord() {
        if (formController == null) {
            openForm(FormFactory.Mode.Create, getNewRecord(), (newRecord) -> {
                if (newRecord != null) {
                    addToDatabase(newRecord);
                    if (newRecord.getId() != 0) {
                        tableView.getItems().add(newRecord);
                    }
                }
                finalizeAction();
            });
        }
    }

    private T getSelectedRecord() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void viewRecord() {
        final T selected = getSelectedRecord();
        if (selected != null && formController == null) {
            openForm(FormFactory.Mode.Read, selected, (record) -> finalizeAction());
        }
    }

    protected void updateInDatabase(T record) {
        if (canUpdate(record)) {
            final List<Object> arguments = record.toValues();
            arguments.add(userId);
            arguments.add(record.getId());
            executeUpdate(getUpdateStatement(), arguments, (ex, updateCount) -> {
                if (ex != null) printSQLException(ex);
                if (updateCount == 1) getSelectedRecord().applyChanges(record);
            });
        }
    }

    protected abstract boolean canUpdate(T record);

    protected abstract String getUpdateStatement();

    @FXML
    private void editRecord() {
        final T selected = getSelectedRecord();
        if (selected != null && formController == null) {
            openForm(FormFactory.Mode.Update, selected.copy(), (updatedRecord) -> {
                if (updatedRecord != null) updateInDatabase(updatedRecord);
                finalizeAction();
            });
        }
    }

    protected List<Object> toArray(Object... values) {
        final List<Object> output = new ArrayList<>();
        if (values != null) {
            Collections.addAll(output, values);
        } else {
            output.add(null);
        }

        return output;
    }

    protected void deleteFromDatabase(T record) {
        if (deleteDependencies(record)) {
            executeUpdate(getDeleteStatement(), toArray(record.getId()), (ex, updates) -> {
                if (ex != null) printSQLException(ex);
                if (updates == 1) record.setId(0);
            });
        }
    }

    protected abstract boolean deleteDependencies(T record);

    protected abstract String getDeleteStatement();

    @FXML
    protected void deleteRecord() {
        final T recordToDelete = getSelectedRecord();
        if (recordToDelete != null) {
            deleteButton.setDisable(true);
            deleteFromDatabase(recordToDelete);
            if (recordToDelete.getId() == 0) {
                tableView.getItems().remove(recordToDelete);
                tableView.refresh();
                displayAlert(bundle.getString("record.deleted.title"), getDeletedMessage(), Alert.AlertType.INFORMATION);
                emitEvent();
            }
            deleteButton.setDisable(false);
        }
    }

    protected void emitEvent() {
    }

    protected abstract String getDeletedMessage();

    public ObservableList<T> getData() {
        return tableView.getItems();
    }

    @FXML
    protected void addFilter() {
    }
}
