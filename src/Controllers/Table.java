package Controllers;

import Models.Model;
import Models.Record;
import javafx.beans.property.SimpleLongProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Table<T extends Record & Model<T>> extends Base implements Initializable {
    @FXML
    protected TableView<T> tableView;
    @FXML
    private Button deleteButton;

    protected abstract void addColumns();

    protected abstract void populateData();

    protected Form<T> formController;
    protected FormFactory formFactory;

    public Table(FormFactory formFactory) {
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
        List<Object> arguments = record.toValues();
        arguments.add(getUserId());
        arguments.add(getUserId());
        executeInsert(getInsertStatement(), arguments, (ex, newId) -> {
            if (ex != null) printSQLException(ex);
            if (newId != null) record.setId(newId);
        });
    }

    protected abstract String getInsertStatement();

    protected abstract T getNewRecord();

    @FXML
    private void addRecord() {
        if (formController == null) {
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
        if (selected != null && formController == null) {
            openForm(FormFactory.Type.Read, selected, Form.Mode.Read, (record) -> finalizeAction());
        }
    }

    protected void updateInDatabase(T record) {
        List<Object> arguments = record.toValues();
        arguments.add(getUserId());
        arguments.add(record.getId());
        executeUpdate(getUpdateStatement(), arguments, (ex, updateCount) -> {
            if (ex != null) printSQLException(ex);
            if (updateCount == 1) getSelectedRecord().applyChanges(record);
        });
    }

    protected abstract String getUpdateStatement();

    @FXML
    private void editRecord() {
        T selected = getSelectedRecord();
        if (selected != null && formController == null) {
            openForm(FormFactory.Type.Update, selected.copy(), Form.Mode.Update, (updatedRecord) -> {
                if (updatedRecord != null) updateInDatabase(updatedRecord);
                finalizeAction();
            });
        }
    }

    protected List<Object> toArray(Object... values) {
        List<Object> output = new ArrayList();
        if (values != null) {
            for (Object value : values) {
                output.add(value);
            }
        } else {
            output.add(null);
        }

        return output;
    }

    protected void deleteFromDatabase(T record) {

        System.out.println("Deleting record");
        if (deleteDependencies(record)) {
            System.out.println("Dependencies deleted");
            executeUpdate(getDeleteStatement(), toArray(record.getId()), (ex, updates) -> {
                if (ex != null) printSQLException(ex);
                if (updates == 1) record.setId(0);
            });
        }
    }

    protected abstract boolean deleteDependencies(T record);

    protected abstract String getDeleteStatement();

    @FXML
    private void deleteRecord() {
        T recordToDelete = getSelectedRecord();
        if (recordToDelete != null) {
            deleteButton.setDisable(true);
            deleteFromDatabase(recordToDelete);
            if (recordToDelete.getId() == 0) {
                tableView.getItems().remove(recordToDelete);
                tableView.refresh();
                displayAlert(bundle.getString("record.deleted.title"), bundle.getString("record.deleted.message"), Alert.AlertType.INFORMATION);
            }
            deleteButton.setDisable(false);
        }
    }
}
