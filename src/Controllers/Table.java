package Controllers;

import Models.Item;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class Table extends Base implements Initializable {
    @FXML
    protected TableView<Item> tableView;

    protected abstract void addColumns();

    protected abstract void populateData();

    protected Form formController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        final TableColumn<Item, Integer> idColumn = new TableColumn("ID");
        idColumn.setCellValueFactory(param -> new SimpleIntegerProperty(param.getValue().getId()).asObject());
        tableView.getColumns().add(idColumn);
        addColumns();
        populateData();
        tableView.refresh();
    }

    private Item openForm(Item item, Form.Mode mode) {
        formController.open(item, mode);
        return item;
    }

    protected abstract void addToDatabase(Item item);

    protected abstract Item getNewItem();

    @FXML
    private void addItem() {
        System.out.println("add item called");
        Item newItem = openForm(getNewItem(), Form.Mode.Create);
        addToDatabase(newItem);
        tableView.refresh();
    }

    private Item getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    @FXML
    private void viewItem() {
        System.out.println("view item called");
        openForm(getSelectedItem(), Form.Mode.Read);
    }

    protected abstract void updateInDatabase(Item item);

    @FXML
    private void editItem() {
        System.out.println("edit item called");
        updateInDatabase(openForm(getSelectedItem(), Form.Mode.Update));
        tableView.refresh();
    }

    protected abstract void deleteFromDatabase(Item item);

    @FXML
    private void deleteItem() {
        System.out.println("delete item called");
        Item itemToDelete = getSelectedItem();
        deleteFromDatabase(itemToDelete);
        tableView.getItems().remove(itemToDelete);
        tableView.refresh();
    }
}
