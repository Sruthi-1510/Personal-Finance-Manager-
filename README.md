# WealthWatch - Personal Finance Manager

WealthWatch is a simple yet powerful personal finance application built with JavaFX and SQLite. It helps you track your income and expenses, visualize your spending with charts, and manage your budget effectively.

## Features

- **Transaction Tracking**: Add income and expense transactions with descriptions, amounts, and categories.
- **Budget Management**: Set a monthly budget and track your remaining balance in real-time.
- **Visual Analytics**: View a pie chart breakdown of your expenses by category.
- **Data Persistence**: All data is stored locally in an SQLite database (`personal_finance.db`), ensuring your financial records are safe and persistent.
- **Monthly Filtering**: Filter transactions by month to review past spending.

## Prerequisites

Before running the project, ensure you have the following installed:

1.  **Java Development Kit (JDK) 21 or higher**: This project uses JavaFX, which requires a modern JDK.
    - [Download JDK 21+](https://adoptium.net/)
    - **Important**: Ensure `java` and `javac` are added to your system's PATH during installation.

## Installation & Setup

1.  **Clone the repository**:

    ```bash
    git clone https://github.com/yourusername/WealthWatch.git
    ```

2.  **Verify Java Installation**:
    Open your terminal (Command Prompt or PowerShell) and run:
    ```bash
    java -version
    ```
    Ensure the output shows version 21 or higher.

## How to Run

### Windows (Recommended)

This project includes a PowerShell helper script to automatically compile and run the application.

1.  Open PowerShell in the cloned repository directory.
2.  Run the script:
    ```powershell
    .\run.ps1
    ```

### Manual Run (Command Line)

If you prefer to run it manually without the script:

**1. Create output directory:**

```bash
mkdir bin
```

**2. Compile:**

```bash
javac --module-path "lib/javafx-sdk-23/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "lib/sqlite-jdbc-3.46.1.3.jar;src" -d bin src/*.java
```

**3. Copy resources:**

```bash
copy src/style.css bin/
```

**4. Run:**

```bash
java --module-path "lib/javafx-sdk-23/lib" --add-modules javafx.controls,javafx.fxml,javafx.graphics -cp "lib/sqlite-jdbc-3.46.1.3.jar;bin;." WealthWatch
```

## Project Structure

- `src/`: Source code files (`.java`)
- `lib/`: External libraries (JavaFX SDK, SQLite JDBC driver)
- `resources/`: Assets like fonts
- `bin/`: Compiled bytecode (`.class` files) - _Generated on build_

## Troubleshooting

- **"javac is not recognized"**: Make sure you installed the JDK and checked "Add to PATH" during installation. You may need to restart your terminal.
- **"Error: JavaFX runtime components are missing"**: Ensure you are using the provided `run.ps1` script or the manual commands above which include the correct `--module-path`.

## License

This project is open-source and available under the MIT License.
