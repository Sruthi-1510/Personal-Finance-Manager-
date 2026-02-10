import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    // Define the path to the database
    private static final String DATABASE_URL = "jdbc:sqlite:personal_finance.db";

    // Connect to the database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
        return conn;
    }

    // Create the transactions table if it does not exist
    public static void createTransactionTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    description TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT,
                    isExpense INTEGER NOT NULL,
                    date TEXT NOT NULL
                );
                """;
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("Transactions table created or already exists.");
        } catch (SQLException e) {
            System.out.println("Failed to create transactions table: " + e.getMessage());
        }
    }
}

