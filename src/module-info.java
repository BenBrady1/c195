module SchedulingApp {
    requires javafx.fxml;
    requires javafx.controls;
    requires java.sql;

    opens Controllers to javafx.fxml;
    opens main;
}