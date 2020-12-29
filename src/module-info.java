module SchedulingApp {
    requires javafx.fxml;
    requires javafx.controls;

    opens Controllers to javafx.fxml;
    opens main;
}