package Controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.ResourceBundle;

public class Login implements Initializable {
    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    /**
     *
     * @param event JavaFX button press event
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        final String username = usernameField.getText();
        final String password = passwordField.getText();
        if (username.length() != 0 && password.length() != 0) {
            System.out.println(username);
            System.out.println(hashPassword());
        }
    }

    /**
     * Logs user in when the "Enter" key is pressed
     * @param event JavaFX key event
     */
    @FXML
    private void handleEnter(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLogin(null);
        }
    }

    /**
     * Obviously horribly unsafe but better than storing passwords in the clear.
     * @return the hashed password
     */
    private String hashPassword() {
        try {
            byte[] messageDigest = MessageDigest.getInstance("SHA-512").digest(passwordField.getText().getBytes());
            return Base64.getEncoder().encodeToString(messageDigest);
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }
    }
}
