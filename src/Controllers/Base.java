package Controllers;

import javafx.scene.control.Alert;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * an abstract base controller class with helper methods that can be used across controllers
 */
abstract public class Base {
    private static Locale coercedLocale = coerceLocale();
    protected static ResourceBundle bundle;
    protected static Connection conn;
    protected static Optional<Long> userId = Optional.ofNullable(null);

    /**
     * Sets the locale to be used for the duration of the program
     *
     * @return the locale for the program to use
     */
    private static Locale coerceLocale() {
        Locale locale = Locale.getDefault();
        switch (locale.getLanguage()) {
            case "en":
            case "fr":
                break;
            default:
                locale = new Locale("en", "US");
        }
//        locale = new Locale("fr", "CA");
        return locale;
    }

    /**
     * @return the statically-set locale for the runtime of the program
     */
    public static Locale getLocale() {
        return coercedLocale;
    }

    public static ResourceBundle getBundle() {
        return bundle;
    }

    public static void setLocaleAndBundle() {
        coercedLocale = coerceLocale();
        bundle = ResourceBundle.getBundle("App", getLocale());
    }

    protected static long getUserId() {
        return userId.orElse(null);
    }

    protected View viewController;

    {
        if (conn == null) {
            try {
                createDatabaseConnection();
            } catch (SQLException ex) {
                printSQLException(ex);
                conn = null;
            }
        }
    }

    /**
     * parses the git ignored database.xml file in the root of the repo to build a connection string and keep the
     * connection details private
     *
     * @return the database connection string as parsed from the database.xml file
     * @throws Exception any IO or parsing exception
     */
    private String getConnectionString() throws Exception {
        final File inputFile = new File("database.xml");
        final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        final Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        final String serverName = doc.getElementsByTagName("server").item(0).getTextContent();
        final int port = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent());
        final String name = doc.getElementsByTagName("name").item(0).getTextContent();
        final String user = doc.getElementsByTagName("user").item(0).getTextContent();
        final String password = doc.getElementsByTagName("password").item(0).getTextContent();
        final String query = String.format("user=%s&password=%s", user, password);

        return new URI("jdbc:mysql", null, serverName, port, "/" + name, query, null).toString();
    }

    /**
     * establishes a connection to the database
     *
     * @return the connection object
     * @throws SQLException any exception that occurs when trying to connect to the DB
     */
    private Connection createDatabaseConnection() throws SQLException {
        if (conn != null && !conn.isClosed()) return conn;
        conn = null;
        try {
            conn = DriverManager.getConnection(getConnectionString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return conn;
    }

    /**
     * A wrapper around Base#executeQuery(String, Object[], BiFunction) for when there are no arguments and no value is
     * needed from the callback function
     *
     * @param query   the query to execute
     * @param handler a function to handle any errors or result sets from the query
     * @see Base#executeQuery(String, List, BiFunction)
     */
    protected void executeQuery(String query, BiConsumer<SQLException, ResultSet> handler) {
        executeQuery(query, null, (ex, rs) -> {
            handler.accept(ex, rs);
            return null;
        });
    }

    /**
     * A wrapper around Base#executeQuery(String, Object[], BiFunction) for when no value is needed from the callback
     * function
     *
     * @param query     the query to execute
     * @param arguments an array of arguments
     * @param handler   a function to handle any errors or result sets from the query
     * @see Base#executeQuery(String, List, BiFunction)
     */
    protected void executeQuery(String query, List<Object> arguments, BiConsumer<SQLException, ResultSet> handler) {
        executeQuery(query, arguments, (ex, rs) -> {
            handler.accept(ex, rs);
            return null;
        });
    }

    /**
     * A wrapper around the SQL query that allows for a lambda function to be passed as an argument
     * for a Node-esque error-first callback style. This allows for the caller to consume the result
     * set or error and for the resources to be cleaned up in a DRY manner
     *
     * @param query     the query to execute
     * @param arguments an array of arguments
     * @param handler   a function to handle any errors or result sets from the query, its return value will be returned
     *                  from this function
     */
    protected <T> T executeQuery(String query, List<Object> arguments, BiFunction<SQLException, ResultSet, T> handler) {
        try (var stmt = createDatabaseConnection().prepareStatement(query)) {
            setArguments(stmt, arguments);

            try (var rs = stmt.executeQuery()) {
                return handler.apply(null, rs);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
            return handler.apply(ex, null);
        }
    }

    protected void executeInsert(String query, List<Object> arguments, BiConsumer<SQLException, Long> handler) {
        try (
                Connection connection = createDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            setArguments(stmt, arguments);
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    handler.accept(null, generatedKeys.getLong(1));
                } else {
                    throw new SQLException("Creating user failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            printSQLException(ex);
            handler.accept(ex, null);
        }
    }

    protected void executeUpdate(String query, List<Object> arguments, BiConsumer<SQLException, Integer> handler) {
        executeUpdate(query, arguments, ((BiFunction<SQLException, Integer, Void>) (ex, updates) -> {
            handler.accept(ex, updates);
            return null;
        }));
    }

    protected <T> T executeUpdate(String query, List<Object> arguments, BiFunction<SQLException, Integer, T> handler) {
        try (
                Connection connection = createDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            setArguments(stmt, arguments);

            int affectedRows = stmt.executeUpdate();
            return handler.apply(null, affectedRows);
        } catch (SQLException ex) {
            printSQLException(ex);
            return handler.apply(ex, null);
        }
    }

    private void setArguments(PreparedStatement stmt, List<Object> arguments) throws SQLException {
        if (arguments != null) {
            for (int i = 0; i < arguments.size(); i++) {
                stmt.setObject(i + 1, arguments.get(i));
            }
        }
    }

    /**
     * reusable method to print any sql exceptions during development
     *
     * @param ex the exception to print
     */
    protected void printSQLException(SQLException ex) {
        System.out.println("SQLException: " + ex.getMessage());
        System.out.println("SQLState: " + ex.getSQLState());
        System.out.println("VendorError: " + ex.getErrorCode());
    }

    protected void setViewController(View viewController) {
        this.viewController = viewController;
    }

    /**
     * A wrapper around Base#displayError(String, String) with a default title
     *
     * @param ex the error holding the message to display
     * @see Base#displayError(String, String)
     */
    protected void displayError(Exception ex) {
        displayError(bundle.getString("error.defaultTitle"), ex.getMessage());
    }


    /**
     * A wrapper around Base#displayError(String, String) with a default title
     *
     * @param message the string holding the message to display
     * @see Base#displayError(String, String)
     */
    protected void displayError(String message) {
        displayError(bundle.getString("error.defaultTitle"), message);
    }


    /**
     * A wrapper around Base#displayAlert(String, String, Alert.AlertType) to display errors to the end user.
     *
     * @param title   the title for the error message alert
     * @param message the error message to be displayed
     * @see Base#displayAlert(String, String, Alert.AlertType)
     */
    protected void displayError(String title, String message) {
        displayAlert(title, message, Alert.AlertType.ERROR);
    }

    /**
     * A function to display an alert that blocks the rest of the program.
     *
     * @param title   the title for the error message alert
     * @param message the error message to be displayed
     * @param type    the type of alert to display
     */
    protected void displayAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
