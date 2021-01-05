package Controllers;

import Models.Item;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public abstract class Table<T extends Item> extends Base implements Initializable {
    @FXML
    protected TableView<T> tableView;

    protected abstract void addColumns();

    protected abstract void populateData();

    protected Form<T> formController;
    protected Optional<Integer> userId;
    private FormFactory formFactory;

    public Table(Optional<Integer> userId, FormFactory formFactory) {
        this.userId = userId;
        this.formFactory = formFactory;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final TableColumn<T, Integer> idColumn = new TableColumn("ID");
        idColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    private void openForm(FormFactory.Type type, T item, Form.Mode mode, Consumer<T> callback) {
        formController = formFactory.getInstance(type);
        formController.open(item, mode, callback);
    }

    private void finalizeAction() {
        tableView.refresh();
        formController = null;
    }

    protected abstract void addToDatabase(T item);

    protected abstract T getNewItem();

    @FXML
    private void addItem() {
        if (formController == null) {
            System.out.println("add item called");
            openForm(FormFactory.Type.Create, getNewItem(), Form.Mode.Create, (newItem) -> {
                if (newItem != null) addToDatabase(newItem);
                finalizeAction();
            });
        }
    }

    private T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void viewItem() {
        T selected = getSelectedItem();
        System.out.println(selected);
        if (selected != null && formController == null) {
            System.out.println("view item called");
            openForm(FormFactory.Type.Read, selected, Form.Mode.Read, (item) -> finalizeAction());
        }
    }

    protected abstract void updateInDatabase(T item);

    @FXML
    private void editItem() {
        T selected = getSelectedItem();
        if (selected != null && formController == null) {
            System.out.println("edit item called");
            openForm(FormFactory.Type.Update, selected, Form.Mode.Update, (updatedItem) -> {
                if (updatedItem != null) updateInDatabase(updatedItem);
                finalizeAction();
            });
        }
    }

    protected abstract void deleteFromDatabase(T item);

    @FXML
    private void deleteItem() {
        System.out.println("delete item called");
        T itemToDelete = getSelectedItem();
        if (itemToDelete != null) {
            deleteFromDatabase(itemToDelete);
            tableView.getItems().remove(itemToDelete);
            tableView.refresh();
        }
    }
}
