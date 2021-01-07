package Controllers;

import Models.Model;
import Models.Record;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Table<T extends Record & Model<T>> extends Base implements Initializable {
    @FXML
    protected TableView<T> tableView;

    protected abstract void addColumns();

    protected abstract void populateData();

    protected Form<T> formController;
    protected Optional<Integer> userId;
    protected FormFactory formFactory;

    public Table(Optional<Integer> userId, FormFactory formFactory) {
        this.userId = userId;
        this.formFactory = formFactory;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final TableColumn<T, Long> idColumn = new TableColumn("ID");
        idColumn.setCellValueFactory(param -> new SimpleLongProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    private void openForm(FormFactory.Type type, T record, Form.Mode mode, Consumer<T> callback) {
        formController = formFactory.getInstance(type);
        formController.open(record, mode, callback);
    }

    private void finalizeAction() {
        tableView.refresh();
        formController = null;
    }

    private void addToDatabase(T record) {
        executeInsert(getInsertStatement(), record.toValues(), (ex, newId) -> {
            if (ex != null) printSQLException(ex);
            if (newId != null) record.setId(newId);
        });
    }

    protected abstract String getInsertStatement();

    protected abstract T getNewRecord();

    @FXML
    private void addRecord() {
        if (formController == null) {
            System.out.println("add record called");
            openForm(FormFactory.Type.Create, getNewRecord(), Form.Mode.Create, (newRecord) -> {
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
        T selected = getSelectedRecord();
        System.out.println(selected);
        if (selected != null && formController == null) {
            System.out.println("view record called");
            openForm(FormFactory.Type.Read, selected, Form.Mode.Read, (record) -> finalizeAction());
        }
    }

    protected void updateInDatabase(T record) {
        executeUpdate(getUpdateStatement(), record.toValuesWithID(), (ex, updateCount) -> {
            if (ex != null) printSQLException(ex);
            if (updateCount == 1) getSelectedRecord().applyChanges(record);
        });
    }

    protected abstract String getUpdateStatement();

    @FXML
    private void editRecord() {
        T selected = getSelectedRecord().copy();
        if (selected != null && formController == null) {
            System.out.println("edit record called");
            openForm(FormFactory.Type.Update, selected, Form.Mode.Update, (updatedRecord) -> {
                if (updatedRecord != null) updateInDatabase(updatedRecord);
                finalizeAction();
            });
        }
    }

    protected void deleteFromDatabase(T record) {
        executeUpdate(getDeleteStatement(), new Object[]{record.getId()}, (ex, updates) -> {
            if (ex != null) printSQLException(ex);
            if (updates == 1) record.setId(0);
        });
    }

    protected abstract String getDeleteStatement();

    @FXML
    private void deleteRecord() {
        System.out.println("delete record called");
        T recordToDelete = getSelectedRecord();
        if (recordToDelete != null) {
            deleteFromDatabase(recordToDelete);
            if (recordToDelete.getId() == 0) {
                tableView.getItems().remove(recordToDelete);
                tableView.refresh();
            }
        }
    }
}
