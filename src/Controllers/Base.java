package Controllers;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.net.URI;
import java.sql.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * an abstract base controller class with helper methods that can be used across controllers
 */
abstract public class Base {
    private static final Locale coercedLocale = coerceLocale();
    protected static Connection conn;

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
        if (conn != null) return conn;
        try {
            conn = DriverManager.getConnection(getConnectionString());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return conn;
    }

    /**
     * A wrapper around the SQL query that allows for a lambda function to be passed as an argument
     * for a Node-esque error-first callback style. This allows for the caller to consume the result
     * set or error and for the resources to be cleaned up in a DRY manner
     *
     * @param query     the query to execute
     * @param arguments a hashmap
     * @param handler   a function to handle any errors or result sets from the query
     */
    protected <T> T executeQuery(String query,
                                 HashMap<Integer, Object> arguments,
                                 BiFunction<SQLException, ResultSet, T> handler) {
        try (var stmt = createDatabaseConnection().prepareStatement(query)) {
            if (arguments != null) {
                for (Map.Entry<Integer, Object> entry : arguments.entrySet()) {
                    stmt.setObject(entry.getKey(), entry.getValue());
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
}
