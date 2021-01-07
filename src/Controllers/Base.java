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
    private static final Locale coercedLocale = coerceLocale();
    protected final static ResourceBundle bundle = ResourceBundle.getBundle("App", getLocale());
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
        locale = new Locale("fr", "CA");
        return locale;
    }

    /**
     * @return the statically-set locale for the runtime of the program
     */
    public static Locale getLocale() {
        return coercedLocale;
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
     * @see Base#executeQuery(String, Object[], BiFunction)
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
     * @see Base#executeQuery(String, Object[], BiFunction)
     */
    protected void executeQuery(String query, Object[] arguments, BiConsumer<SQLException, ResultSet> handler) {
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
    protected <T> T executeQuery(String query,
                                 Object[] arguments,
                                 BiFunction<SQLException, ResultSet, T> handler) {
        try (var stmt = createDatabaseConnection().prepareStatement(query)) {
            if (arguments != null) {
                for (int i = 0; i < arguments.length; i++) {
                    stmt.setObject(i + 1, arguments[i]);
                }
            }

            try (var rs = stmt.executeQuery()) {
                return handler.apply(null, rs);
            }
        } catch (SQLException ex) {
            printSQLException(ex);
            return handler.apply(ex, null);
        }
    }

    protected void executeInsert(String query,
                                 List<Object> arguments,
                                 BiConsumer<SQLException, Long> handler) {
        try (
                Connection connection = createDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            if (arguments != null) {
                for (int i = 0; i < arguments.size(); i++) {
                    stmt.setObject(i + 1, arguments.get(i));
                }
            }

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

    protected void executeUpdate(String query,
                                 List<Object> arguments,
                                 BiConsumer<SQLException, Integer> handler) {
        try (
                Connection connection = createDatabaseConnection();
                PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            if (arguments != null) {
                for (int i = 0; i < arguments.size(); i++) {
                    stmt.setObject(i + 1, arguments.get(i));
                }
            }

            int affectedRows = stmt.executeUpdate();
            handler.accept(null, affectedRows);
        } catch (SQLException ex) {
            printSQLException(ex);
            handler.accept(ex, null);
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
     */
    protected void displayError(Exception ex) {
        displayError("Error!", ex.getMessage());
    }

    /**
     * Used to display validation errors to the end user.
     *
     * @param title   the title for the error message alert
     * @param message the error message to be displayed
     */
    protected void displayError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
