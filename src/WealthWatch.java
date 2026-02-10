import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.chart.PieChart;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.Font;
import javafx.util.converter.DoubleStringConverter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class WealthWatch extends Application {

    private TableView<Transaction> tableView;
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private TextField descriptionField, amountField, categoryField, budgetField;
    private RadioButton incomeRadio, expenseRadio;
    private PieChart pieChart;
    private double budgetAmount = 0;
    private Label budgetLabel;
    private Label remainingLabel;
    private double totalExpenses = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        DatabaseManager.createTransactionTable(); // Ensure the table is created on startup
        primaryStage.setTitle("WealthWatch - Personal Finance Manager");

        // Load custom font (optional)
        try {
            Font.loadFont(new FileInputStream("resources/Nexa Bold.otf"), 14);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // Input fields
        descriptionField = new TextField();
        descriptionField.setPromptText("Description");

        amountField = new TextField();
        amountField.setPromptText("Amount");

        categoryField = new TextField();
        categoryField.setPromptText("Category");

        budgetField = new TextField();
        budgetField.setPromptText("Set Budget");

        // Radio buttons for transaction type
        incomeRadio = new RadioButton("Income");
        expenseRadio = new RadioButton("Expense");
        ToggleGroup group = new ToggleGroup();
        incomeRadio.setToggleGroup(group);
        expenseRadio.setToggleGroup(group);

        // Add transaction button
        Button addButton = new Button("Add Transaction");
        addButton.setOnAction(e -> addTransaction());

        // Set budget button
        Button setBudgetButton = new Button("Set Budget");
        setBudgetButton.setOnAction(e -> setBudget());

        // Budget display
        budgetLabel = new Label("No budget set");
        remainingLabel = new Label("Remaining Amount: $0.00");

        // Table view setup
        tableView = new TableView<>();
        setupTable();

        // Pie chart setup
        pieChart = new PieChart();
        pieChart.setTitle("Spending by Category");

        // DatePicker for selecting month
        DatePicker monthPicker = new DatePicker();
        monthPicker.setPromptText("Select Month");
        monthPicker.setOnAction(e -> {
            LocalDate date = monthPicker.getValue();
            if (date != null) {
                loadTransactionsByMonth(YearMonth.from(date));
            }
        });

        // Layout setup for main UI
        VBox layout = new VBox(10, descriptionField, amountField, categoryField, incomeRadio, expenseRadio,
                addButton, setBudgetButton, budgetField, budgetLabel, remainingLabel, tableView, pieChart, monthPicker);
        layout.setPadding(new Insets(10));

        // Apply the external CSS
        Scene scene = new Scene(layout, 800, 600);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm()); // Link CSS file

        primaryStage.setScene(scene);
        primaryStage.show();
        loadTransactionsByMonth(YearMonth.now());
    }

    private void setupTable() {
        tableView.setEditable(true);

        TableColumn<Transaction, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descriptionCol.setCellFactory(TextFieldTableCell.forTableColumn());
        descriptionCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setDescription(event.getNewValue());
            updateTransactionInDB(t);
        });

        TableColumn<Transaction, Double> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(cellData -> cellData.getValue().amountProperty().asObject());
        amountCol.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        amountCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setAmount(event.getNewValue());
            updateRemainingAmount();
            updatePieChart();
            updateTransactionInDB(t);
        });

        TableColumn<Transaction, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(cellData -> cellData.getValue().categoryProperty());
        categoryCol.setCellFactory(TextFieldTableCell.forTableColumn());
        categoryCol.setOnEditCommit(event -> {
            Transaction t = event.getRowValue();
            t.setCategory(event.getNewValue());
            updatePieChart();
            updateTransactionInDB(t);
        });

        tableView.getColumns().addAll(descriptionCol, amountCol, categoryCol);
        tableView.setItems(transactions);
    }

    private void addTransaction() {
        try {
            if (descriptionField.getText().isEmpty() || amountField.getText().isEmpty()
                    || categoryField.getText().isEmpty()) {
                showAlert("Input Error", "Please fill in all fields.");
                return;
            }
            String description = descriptionField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String category = categoryField.getText();
            boolean isExpense = expenseRadio.isSelected();
            LocalDate date = LocalDate.now();

            // Save transaction to database
            String sql = "INSERT INTO transactions(description, amount, category, isExpense, date) VALUES(?, ?, ?, ?, ?)";
            int generatedId = -1;
            try (Connection conn = DatabaseManager.connect();
                    PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, description);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, category);
                pstmt.setBoolean(4, isExpense);
                pstmt.setString(5, date.toString());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        generatedId = rs.getInt(1);
                    }
                }
            }

            // Add to ObservableList for UI and update expenses
            if (generatedId != -1) {
                transactions.add(new Transaction(generatedId, description, amount, category, isExpense));
                if (isExpense)
                    totalExpenses += amount;
                updatePieChart();
                updateRemainingAmount();
            }

            descriptionField.clear();
            amountField.clear();
            categoryField.clear();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for the amount.");
        } catch (SQLException e) {
            showAlert("Database Error", "Could not save transaction: " + e.getMessage());
        }
    }

    private void setBudget() {
        try {
            budgetAmount = Double.parseDouble(budgetField.getText());
            budgetLabel.setText("Budget: $" + budgetAmount);
            updateRemainingAmount();
            budgetField.clear();
        } catch (NumberFormatException e) {
            showAlert("Input Error", "Please enter a valid number for the budget.");
        }
    }

    private void updateRemainingAmount() {
        double remaining = budgetAmount - totalExpenses;
        remainingLabel.setText("Remaining Amount: $" + remaining);
        if (remaining < 0)
            remainingLabel.setStyle("-fx-text-fill: red;");
        else
            remainingLabel.setStyle("-fx-text-fill: black;");
    }

    private void updatePieChart() {
        pieChart.getData().clear();
        for (Transaction transaction : transactions) {
            PieChart.Data slice = new PieChart.Data(transaction.getCategory(), transaction.getAmount());
            pieChart.getData().add(slice);
        }
    }

    private void loadTransactionsByMonth(YearMonth month) {
        transactions.clear(); // Clear current data
        totalExpenses = 0;

        String sql = "SELECT id, description, amount, category, isExpense FROM transactions WHERE strftime('%Y-%m', date) = ?";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, month.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String description = rs.getString("description");
                double amount = rs.getDouble("amount");
                String category = rs.getString("category");
                boolean isExpense = rs.getBoolean("isExpense");

                Transaction transaction = new Transaction(id, description, amount, category, isExpense);
                transactions.add(transaction);
                if (isExpense)
                    totalExpenses += amount;
            }
            updateRemainingAmount();
            updatePieChart();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not load transactions: " + e.getMessage());
        }
    }

    private void updateTransactionInDB(Transaction t) {
        String sql = "UPDATE transactions SET description = ?, amount = ?, category = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getDescription());
            pstmt.setDouble(2, t.getAmount());
            pstmt.setString(3, t.getCategory());
            pstmt.setInt(4, t.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            showAlert("Database Error", "Could not update transaction: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
