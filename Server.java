import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.*;
import java.util.*;
import java.sql.*;
import java.sql.Date;

public class Server {
    private static String dbUsername = "alien";
    private static String dbPassword = "alien123.com";
    private static String dbUrl = "jdbc:postgresql://localhost:5432/sacco";
    private static Map<String, Boolean> loggedInClients = new HashMap<>();

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java Server <dbUsername> <dbPassword>");
            return;
        }

        dbUsername = args[0];
        dbPassword = args[1];

        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Server is running and waiting for a client...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected!");

                Thread thread = new Thread(() -> {
                    try (
                            OutputStream outputStream = clientSocket.getOutputStream();
                            PrintWriter writer = new PrintWriter(outputStream, true);
                            InputStream inputStream = clientSocket.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                        String welcomeMessage = "Welcome to Uprise-sacco Management System\t"
                                + "Please register or login to continue.";

                        // Send the combined message to the client
                        writer.println(welcomeMessage);

                        boolean isLoggedIn = false;
                        String username = null;

                        while (true) {
                            String command = reader.readLine();
                            System.out.println("Client: " + command);

                            if (command == null) {
                                System.out.println("Client connection closed abruptly.");
                                return;
                            }
                            if (command.equals("logout")) {
                                writer.println("SessionTermination!");
                                break;
                            }
                            // if (isLoggedIn && !command.startsWith("register")) {
                            // // Prompt user to register or login before performing any other command
                            // writer.println("You need to register or login to access other commands.");
                            // continue;
                            // }

                            if (command.startsWith("login")) {
                                String[] parts = command.split(" ");
                                if (parts.length == 3) {
                                    username = parts[1];
                                    String password = parts[2];
                                    // Perform authentication by checking against a MySQL database
                                    String loginStatus = isValidCredentials(username, password);

                                    if (loginStatus.startsWith("valid")) {
                                        if (loginStatus.equals("valid")) {
                                            writer.println("Login successful! You are logged in as " + username + ".");
                                            isLoggedIn = true;
                                            loggedInClients.put(username, true);
                                        } else {
                                            writer.println("valid  admin login");
                                            isLoggedIn = true;
                                            loggedInClients.put(username, true);
                                        }
                                    } else if (loginStatus.equals("low loan progress")) {
                                        writer.println("Login successful!  logged in as " + username + " "
                                                + "low loan progress ");
                                        isLoggedIn = true;
                                        loggedInClients.put(username, true);

                                    } else {
                                        writer.println("Invalid credentials.");
                                    }

                                } else if (command.equalsIgnoreCase("login")) {
                                    writer.println("logincommand received");
                                }

                                else {
                                    writer.println("Invalid login command!");
                                }

                            } else if (command.equalsIgnoreCase("register")) {
                                writer.println("registercommand received");

                            } else if (command.equalsIgnoreCase("forgotpassword")) {
                                writer.println("reset password!");
                            } else if (command.startsWith("reset")) {
                                handleresetpasswordCommand(command, writer, username);

                            } else if (command.equalsIgnoreCase("deposit")) {
                                writer.println("depositcommand received");
                            } else if (command.equalsIgnoreCase("checkstatement")) {
                                writer.println("checkstatementcommand received");
                            } else if (command.equalsIgnoreCase("requestloan")) {
                                writer.println("loanrequest received");
                            } else if (command.equalsIgnoreCase("loanrequeststatus")) {
                                writer.println("loanrequeststatus received");
                            } else if (command.equalsIgnoreCase("loandeposit")) {
                                writer.println("loandeposit received");
                            } else if (command.equalsIgnoreCase("loanaccept")) {
                                writer.println("loanaccept received");
                            } else if (command.equalsIgnoreCase("approve")) {
                                writer.println("loanapprove received");
                            }

                            else if (command.equalsIgnoreCase("loanreject")) {
                                writer.println("loanreject received");
                            }

                            else if (command.startsWith("register")) {
                                handleRegisterCommand(command, writer, reader);
                            } else if (command.startsWith("view")) {
                                handleViewCommand(command, writer, reader);
                            } else if (!isLoggedIn) {
                                writer.println("You need to register or login to access other commands.");
                            } else {
                                String[] parts = command.split(" ");
                                String commandType = parts[0];

                                switch (commandType) {
                                    case "deposit":
                                        handleDepositCommand(command, writer, username);
                                        break;
                                    case "LoanDeposit":
                                        handleLoanDepositCommand(command, writer, username);
                                        break;
                                    case "CheckStatement":
                                        handleCheckStatementCommand(command, writer, username);
                                        break;
                                    case "requestLoan":
                                        handleRequestLoanCommand(command, writer, username);
                                        break;
                                    case "LoanRequestStatus":
                                        handleLoanRequestStatusCommand(command, writer, username);
                                        break;
                                    case "LoanAccept":
                                        handleLoanAcceptCommand(command, writer, username);
                                        break;
                                    case "loanApprove":
                                        handleLoanApproveCommand(command, writer, username);
                                        break;
                                    case "LoanReject":
                                        handleLoanRejectCommand(command, writer, username);
                                        break;
                                    default:
                                        writer.println("Invalid command.");
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String isValidCredentials(String username, String password) {
        String clientStatus = isValidClientCredentials(username, password);
        if (clientStatus.equals("valid") || clientStatus.equals("low loan progress")) {
            return clientStatus;
        } else {
            return isValidAdminCredentials(username, password);
        }
    }

    private static String isValidAdminCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            boolean isValid = resultSet.next();

            resultSet.close();
            statement.close();

            if (isValid) {
                // Status not found or not on track, indicate low loan progress
                return "valid  admin login";
            } else {
                return "invalid";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }

    }

    private static String isValidClientCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM clients WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            boolean isValid = resultSet.next();

            resultSet.close();
            statement.close();

            if (isValid) {
                String status = getLoanStatus(username);
                if (status == null) {
                    // Status not found or not on track, indicate low loan progress
                    return "valid";
                } else if (status.equals("pending")) {
                    return "low loan progress";
                } else {

                    // Valid credentials and loan progress is on track
                    return "valid";
                }
            }

            else {
                // Invalid credentials
                return "invalid";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    public static void handleViewCommand(String command, PrintWriter writer, BufferedReader reader) {
        String status = "pending"; // The status to filter for

        try {
            String result = handleCommandView(status);
            writer.println("view information" + result);
        } catch (SQLException e) {
            e.printStackTrace();
            writer.println("Error: Failed to retrieve loan applications.");
        }
    }

    private static void handleLoanApproveCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            String loanApplicationId = parts[1];
            String amount = parts[2];

            String resultMessage = loanApprove(loanApplicationId, amount);
            writer.println(resultMessage);
        } else {
            writer.println("Invalid loan approve command!");
        }
    }

    private static String loanApprove(String loanApplicationId, String amount) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "UPDATE loanrequests SET status = 'granted', amount = ? WHERE loanapplicationid = ?::uuid";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setBigDecimal(1, new BigDecimal(amount));
            statement.setString(2, loanApplicationId);
            int rowsAffected = statement.executeUpdate();

            if (rowsAffected > 0) {
                return "Loan application " + loanApplicationId + " has been approved.";
            } else {
                return "Failed to approve loan application " + loanApplicationId + ".";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error occurred while approving loan application.";
        }
    }

    public static String handleCommandView(String status) throws SQLException {
        StringBuilder result = new StringBuilder();
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT loanapplicationid, user_id,amount, paymentperiodinmonths, status FROM loanrequests WHERE status = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, status);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID loanApplicationId = (UUID) resultSet.getObject("loanapplicationid");
                String userId = resultSet.getString("user_id");
                int paymentInMonths = resultSet.getInt("paymentperiodinmonths");
                String amount = resultSet.getString("amount");
                String loanStatus = resultSet.getString("status");
                // Timestamp createdAt = resultSet.getTimestamp("created_at");

                result.append(String.format("%-38s", loanApplicationId))
                        .append(String.format("%-8s", userId))
                        .append(String.format("%-15s", amount))
                        .append(String.format("%-10s", paymentInMonths))
                        .append(String.format("%-12s", loanStatus)).append("\t");
                // .append(String.format("%-25s", createdAt)).append("\t");
            }
        }

        return result.toString();
    }

    private static void handleDepositCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 4) {
            String amount = parts[1];
            String dateDeposited = parts[2];
            String receiptNumber = parts[3];

            String resultMessage = deposit(amount, dateDeposited, receiptNumber, username);
            writer.println("Deposit successful." + resultMessage);
        } else {
            writer.println("Invalid deposit command!");
        }
    }

    private static void handleLoanDepositCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 4) {
            String amount = parts[1];
            String dateDeposited = parts[2];
            String receiptNumber = parts[3];

            String resultMessage = loandeposit(amount, dateDeposited, receiptNumber, username);
            writer.println("loanDeposit successfull!\t" + resultMessage);
        } else {
            writer.println("Invalid deposit command!");
        }
    }

    private static String deposit(String amount, String dateDeposited, String receiptNumber, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Check if the receiptNumber is available and not cleared in the BankReceipts
            // table
            String checkReceiptSql = "SELECT COUNT(*) AS count, status FROM BankReceipts WHERE receiptnumbers = ? GROUP BY status";
            PreparedStatement checkReceiptStatement = connection.prepareStatement(checkReceiptSql);
            checkReceiptStatement.setString(1, receiptNumber);
            ResultSet checkReceiptResult = checkReceiptStatement.executeQuery();

            if (checkReceiptResult.next()) {
                int count = checkReceiptResult.getInt("count");
                String receiptStatus = checkReceiptResult.getString("status");

                if (count > 0 && "pending".equals(receiptStatus)) {
                    // Insert the transaction into the transactions table with 'success' status
                    String insertSql = "INSERT INTO transactions (id, user_id, amount, date_deposited, receipt_number, status, created_at, updated_at) "
                            + "SELECT ?, c.membernumber, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, 'success', NOW(), NOW() " +
                            "FROM clients c " +
                            "WHERE c.username = ? " +
                            "RETURNING user_id";
                    PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                    UUID id = UUID.randomUUID(); // Generate a new UUID value
                    insertStatement.setObject(1, id);
                    insertStatement.setBigDecimal(2, new BigDecimal(amount));
                    insertStatement.setString(3, dateDeposited);
                    insertStatement.setString(4, receiptNumber);
                    insertStatement.setString(5, username);
                    ResultSet insertResult = insertStatement.executeQuery();

                    if (insertResult.next()) {
                        String userId = insertResult.getString("user_id");

                        // Update the status and set the user_id in the BankReceipts table
                        String updateReceiptSql = "UPDATE BankReceipts SET status = 'invalid', userid = ? WHERE receiptnumbers = ?";
                        PreparedStatement updateReceiptStatement = connection.prepareStatement(updateReceiptSql);
                        updateReceiptStatement.setString(1, userId); // Set the user_id in the BankReceipts table
                        updateReceiptStatement.setString(2, receiptNumber);
                        updateReceiptStatement.executeUpdate();

                        // Return the amount deposited
                        return " Amount deposited: " + amount;
                    } else {
                        return "Error: Failed to insert the transaction.";
                    }
                } else if ("invalid".equals(receiptStatus)) {
                    return " Receipt number is already used.";
                } else {
                    return " Receipt number is not available or already used.";
                }
            } else {
                return " Receipt number pending.Check again another day";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Error: Insertion error occurred.";
    }

    private static String loandeposit(String amount, String dateDeposited, String receiptNumber, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // First, check if the loan application exists and its status is 'accepted'
            String selectLoanSql = "SELECT loanid FROM loans l " +
                    "JOIN loanrequests q ON l.loanapplicationid = q.loanapplicationid " +
                    "JOIN clients c ON q.user_id = c.membernumber " +
                    "WHERE c.username = ? AND q.status = 'accepted'";

            try (PreparedStatement selectLoanStatement = connection.prepareStatement(selectLoanSql)) {
                selectLoanStatement.setString(1, username);
                try (ResultSet loanResultSet = selectLoanStatement.executeQuery()) {
                    if (loanResultSet.next()) {
                        UUID loanId = UUID.fromString(loanResultSet.getString("loanid"));

                        // Check receipt status in BankReceipts table
                        String checkReceiptSql = "SELECT COUNT(*) AS count, status FROM BankReceipts WHERE receiptnumbers = ? GROUP BY status";
                        try (PreparedStatement checkReceiptStatement = connection.prepareStatement(checkReceiptSql)) {
                            checkReceiptStatement.setString(1, receiptNumber);
                            try (ResultSet checkReceiptResult = checkReceiptStatement.executeQuery()) {
                                if (checkReceiptResult.next()) {
                                    int count = checkReceiptResult.getInt("count");
                                    String receiptStatus = checkReceiptResult.getString("status");

                                    if (count > 0 && "pending".equals(receiptStatus)) {
                                        // Insert the loan deposit into loandeposits table with 'pending' status
                                        String insertDepositSql = "INSERT INTO loandeposits (loanid, monthlyloandeposits, datedeposited, receipt_number, status) VALUES (?, ?, TO_DATE(?, 'YYYY-MM-DD'), ?, 'success') RETURNING depositid";
                                        try (PreparedStatement insertDepositStatement = connection
                                                .prepareStatement(insertDepositSql)) {
                                            insertDepositStatement.setObject(1, loanId);
                                            insertDepositStatement.setBigDecimal(2, new BigDecimal(amount));
                                            insertDepositStatement.setString(3, dateDeposited);
                                            insertDepositStatement.setString(4, receiptNumber);

                                            try (ResultSet insertResult = insertDepositStatement.executeQuery()) {
                                                if (insertResult.next()) {
                                                    int depositid = insertResult.getInt("depositid");

                                                    // Update the status and set the user_id in the BankReceipts table
                                                    String updateReceiptSql = "UPDATE BankReceipts SET status = 'invalid', userid = ? WHERE receiptnumbers = ?";
                                                    try (PreparedStatement updateReceiptStatement = connection
                                                            .prepareStatement(updateReceiptSql)) {
                                                        updateReceiptStatement.setObject(1, loanId);
                                                        updateReceiptStatement.setString(2, receiptNumber);
                                                        updateReceiptStatement.executeUpdate();

                                                        // Check and update status in loans table
                                                        String updateLoanStatusSql = "UPDATE loans SET status = ? WHERE loanid = ?";
                                                        try (PreparedStatement updateLoanStatusStatement = connection
                                                                .prepareStatement(updateLoanStatusSql)) {
                                                            BigDecimal totalDeposits = calculateTotalDeposits(
                                                                    connection, loanId);
                                                            BigDecimal payableAmount = calculatePayableLoanAmount(
                                                                    connection, loanId);

                                                            String newLoanStatus;
                                                            if (totalDeposits.equals(payableAmount)) {
                                                                newLoanStatus = "complete";
                                                            } else if (totalDeposits
                                                                    .equals(payableAmount.divide(new BigDecimal(2)))) {
                                                                newLoanStatus = "ontrack";
                                                            } else {
                                                                newLoanStatus = "accepted";
                                                            }

                                                            updateLoanStatusStatement.setString(1, newLoanStatus);
                                                            updateLoanStatusStatement.setObject(2, loanId);
                                                            updateLoanStatusStatement.executeUpdate();

                                                            // Return the amount deposited
                                                            return "Amount deposited: " + amount + " Deposit ID: "
                                                                    + depositid;
                                                        }
                                                    }
                                                } else {
                                                    return "Error: Failed to insert the transaction.";
                                                }
                                            }
                                        }
                                    } else if ("invalid".equals(receiptStatus)) {
                                        return "Receipt number is already used.";
                                    } else {
                                        return "Receipt number is not available or already used.";
                                    }
                                } else {
                                    return "Receipt number pending. Check again another day.";
                                }
                            }
                        }
                    } else {
                        return "No accepted loan application found for the user.";
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error on insertion";
        }
    }

    private static BigDecimal calculateTotalDeposits(Connection connection, UUID loanId) throws SQLException {
        String sumDepositsSql = "SELECT SUM(monthlyloandeposits) AS total_deposits FROM loandeposits WHERE loanid = ?";
        try (PreparedStatement sumDepositsStatement = connection.prepareStatement(sumDepositsSql)) {
            sumDepositsStatement.setObject(1, loanId);
            try (ResultSet sumDepositsResult = sumDepositsStatement.executeQuery()) {
                if (sumDepositsResult.next()) {
                    return sumDepositsResult.getBigDecimal("total_deposits");
                } else {
                    return BigDecimal.ZERO;
                }
            }
        }
    }

    private static BigDecimal calculatePayableLoanAmount(Connection connection, UUID loanId) throws SQLException {
        String selectPayableAmountSql = "SELECT payableloanamount FROM loans WHERE loanid = ?";
        try (PreparedStatement selectPayableAmountStatement = connection.prepareStatement(selectPayableAmountSql)) {
            selectPayableAmountStatement.setObject(1, loanId);
            try (ResultSet payableAmountResult = selectPayableAmountStatement.executeQuery()) {
                if (payableAmountResult.next()) {
                    return payableAmountResult.getBigDecimal("payableloanamount");
                } else {
                    return BigDecimal.ZERO;
                }
            }
        }
    }

    private static void handleCheckStatementCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            String dateFrom = parts[1];
            String dateTo = parts[2];
            // Process the check statement command and perform database operations
            String statements = checkStatement(dateFrom, dateTo, username);
            if (!statements.isEmpty()) {

                writer.println("Statement Information:" + statements);

            } else {
                writer.println("No transactions found for the given date range.");
            }
        } else {
            writer.println("Invalid CheckStatement command format. Please use 'CheckStatement date_from date_to'.");
        }
    }

    private static String checkStatement(String dateFrom, String dateTo, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String selectSql = "SELECT t.id, t.user_id, t.amount,t.date_deposited, t.receipt_number, t.status " +
                    "FROM transactions t " +
                    "JOIN clients c ON t.user_id = c.membernumber " +
                    "WHERE c.username = ? AND t.date_deposited BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') "
                    +
                    "GROUP BY GROUPING SETS ((t.user_id, t.date_deposited, t.amount, t.receipt_number,t.id, t.status), ()) "
                    +
                    "ORDER BY t.id;";

            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, username);
            selectStatement.setString(2, dateFrom);
            selectStatement.setString(3, dateTo);
            ResultSet resultSet = selectStatement.executeQuery();

            double accountBalance = 0.0;
            StringBuilder statementData = new StringBuilder(); // Use StringBuilder to construct the statementData
            boolean foundTransactions = false;

            while (resultSet.next()) {
                UUID transactionId = (UUID) resultSet.getObject("id");
                if (transactionId == null) {
                    // Skip null value rows
                    continue;
                }
                double amount = resultSet.getDouble("amount");
                Date dateDeposited = resultSet.getDate("date_deposited");
                String receiptNumber = resultSet.getString("receipt_number");

                // Append each value along with spaces to the statementData
                statementData.append("     ");
                statementData.append(transactionId).append(" ");
                statementData.append(amount).append("  ");
                statementData.append(dateDeposited).append("   ");
                statementData.append(receiptNumber).append("\t");

                // Update accountBalance
                accountBalance += amount;

                foundTransactions = true;
            }

            resultSet.close();
            selectStatement.close();

            if (foundTransactions) {
                // Calculate loan progress and contribution progress
                String totalLoanMonthsString = getTotalLoanMonths(username);
                int totalLoanMonths = Integer.parseInt(totalLoanMonthsString);
                int clearedLoanMonths = getClearedLoanMonths(username);
                double loanProgress = (double) clearedLoanMonths / totalLoanMonths * 100;

                int totalContributionMonths = getTotalContributionMonths();
                int clearedContributionMonths = getClearedContributionMonths(username);
                double contributionProgress = (double) clearedContributionMonths / totalContributionMonths * 100;

                // Append loan status, contribution status, and account balance at the end of
                // statementData
                statementData.append(String.format("     Account Balance: %-10.2f ", accountBalance)).append("\t");
                statementData.append(String.format("     Contribution Progress: %-10s ", contributionProgress))
                        .append("\t");
                statementData.append(String.format("     Loan Status: %-10s", loanProgress)).append("\t");
                statementData.append(String.format("loanprogress", loanProgress)).append("\t");
            } else {
                statementData.append("No transactions found for the given date range.");
            }

            return statementData.toString();
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error";
        }
    }

    private static int getTotalContributionMonths() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(DISTINCT EXTRACT(MONTH FROM date_deposited)) AS totalContributionMonths FROM transactions WHERE status = 'success'";
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            int totalContributionMonths = 24;
            if (resultSet.next()) {
                totalContributionMonths = resultSet.getInt("totalContributionMonths");
            }

            resultSet.close();
            statement.close();

            return totalContributionMonths;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getTotalLoanMonths(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String selectSql = "SELECT paymentPeriodInMonths " +
                    "FROM loanrequests q " +
                    "JOIN clients c ON q.user_id = c.membernumber " +
                    "WHERE c.username = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, username);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                int paymentPeriodInMonths = resultSet.getInt("paymentPeriodInMonths");
                resultSet.close();
                selectStatement.close();
                connection.close();
                return String.valueOf(paymentPeriodInMonths);
            }

            resultSet.close();
            selectStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "0"; // Return "0" if username is not found or there is an error
    }

    private static int getClearedLoanMonths(String username) {
        int clearedMonths = 0;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(*) AS clearedMonths " +
                    "FROM loandeposits q " +
                    "JOIN loans l ON q.loanid = l.loanid " +
                    "JOIN clients c ON l.userid = c.membernumber " +
                    "WHERE q.status = 'success' AND c.username = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
                preparedStatement.setString(1, username);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        clearedMonths = resultSet.getInt("clearedMonths");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return clearedMonths;

    }

    private static int getClearedContributionMonths(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(DISTINCT EXTRACT(MONTH FROM date_deposited)) AS clearedContributionMonths " +
                    "FROM transactions t " +
                    "JOIN clients c ON t.user_id = c.membernumber " +
                    "WHERE c.username = ? AND t.status = 'success'";

            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();

            int clearedContributionMonths = 0;
            if (resultSet.next()) {
                clearedContributionMonths = resultSet.getInt("clearedContributionMonths");
            }

            resultSet.close();
            statement.close();

            return clearedContributionMonths;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // private static double calculatePerformance(double loanProgress, double
    // contributionProgress) {
    // // Calculate and return the overall performance percentage
    // double overallPerformance = (loanProgress + contributionProgress) / 2;
    // return overallPerformance;
    // }

    private static void handleRequestLoanCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 3) {
            String amount = parts[1];
            String paymentPeriodInMonths = parts[2];
            // Process the loan request command and perform database operations
            String loanApplicationId = requestLoan(amount, paymentPeriodInMonths, username);
            if (!loanApplicationId.isEmpty()) {
                writer.println("Loan application submitted. " + loanApplicationId);
            } else {
                writer.println("Failed to submit loan application.");
            }
        } else {
            writer.println(
                    "Invalid requestLoan command format. Please use 'requestLoan amount paymentPeriodInMonths'.");
        }
    }

    private static double calculateTotalEquitableLoanAmount() {
        double totalEquitableAmount = 0.0;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Query to calculate the total equitable loan amount for all applications
            // (excluding the current application)
            String sql = "SELECT SUM(amount) as total_equitable_amount " +
                    "FROM loanrequests l " +
                    "JOIN clients c ON l.user_id = c.membernumber " +
                    "WHERE c.username = ? AND l.status = 'pending'";

            try (PreparedStatement statement = connection.prepareStatement(sql)) {

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        totalEquitableAmount = resultSet.getDouble("total_equitable_amount");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return totalEquitableAmount;
    }

    private static final double THRESHOLD_AMOUNT = 2000000.0;

    private static void updateStatusToGranted() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String updateSql = "UPDATE loanrequests SET status = 'granted' WHERE status = 'pending'";
            try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                updateStatement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static String requestLoan(String amount, String paymentPeriodInMonths, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String insertSql = "INSERT INTO loanrequests (user_id, amount, status, paymentPeriodInMonths) " +
                    "SELECT c.membernumber, ?, 'pending', ? " +
                    "FROM clients c " +
                    "WHERE c.username = ? " +
                    "RETURNING loanapplicationid"; // Use the RETURNING clause to retrieve the generated
                                                   // loanapplicationid
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setBigDecimal(1, new BigDecimal(amount));
            insertStatement.setInt(2, Integer.parseInt(paymentPeriodInMonths));
            insertStatement.setString(3, username);

            ResultSet resultSet = insertStatement.executeQuery();

            // Retrieve the generated loanapplicationid
            String loanapplicationid = null;
            if (resultSet.next()) {
                loanapplicationid = resultSet.getString("loanapplicationid");
            }

            insertStatement.close();
            resultSet.close();

            if (loanapplicationid != null) {
                // Check if the number of loan applicants is less than 10
                int numberOfApplicants = getNumberOfLoanApplicants();
                if (numberOfApplicants ==7 ) {
                    // Check availability of funds
                    double availableFunds = getAvailableFunds();
                    double remainingBalance = availableFunds - THRESHOLD_AMOUNT;
                    // Calculate total equitable loan amount for all applications (excluding the
                    // current application)
                    double totalEquitableAmount = calculateTotalEquitableLoanAmount();

                    if (remainingBalance >= totalEquitableAmount) {
                        // Update status for all pending loan applications to 'granted'
                        updateStatusToGranted();

                        // Return the loan application approval message with the loanapplicationid
                        return "Loan application approved. Your loan amount is: " + amount +
                                ". Loan application ID: " + loanapplicationid;
                    } else {
                        // Apply the default logic for approving or rejecting the loan application based
                        // on available funds
                        double equitableAmount = calculateEquitableLoanAmount(username);

                        // Check if the current application's equitable amount is less than or equal to
                        // the remaining available funds
                        if (remainingBalance >= equitableAmount) {
                            // Check loan performance of previous term
                            boolean lowPerformance = checkLowPerformance(username);
                            if (!lowPerformance) {
                                // Approve the loan application with the equitable amount
                                approveLoanApplication(loanapplicationid, equitableAmount);
                                double proportionalShare = remainingBalance / (totalEquitableAmount - equitableAmount);
                                distributeRemainingBalance(proportionalShare, username);
                                // Return the loan application approval message with the loanapplicationid
                                return "Loan application approved. Your loan amount is: " + equitableAmount +
                                        ". Loan application ID: " + loanapplicationid;
                            } else {
                                return "Loan application rejected due to low performance in previous term.";
                            }
                        } else {
                            // Reject the loan application due to insufficient funds
                            return "Loan application rejected due to insufficient funds in the Sacco.";
                        }
                    }
                } else {
                    return "Loan application received. Waiting for administrator approval. " +
                            "Loan application ID: " + loanapplicationid;
                }
            } else {
                return "Error: Loan application failed. Please try again.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Error: Unable to process loan application.";
    }

    private static void distributeRemainingBalance(double proportionalShare, String currentUsername) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Retrieve all pending loan applications (excluding the current one)
            String selectSql = "SELECT l.loanapplicationid, l.amount, c.username, c.membernumber " +
                    "FROM loanrequests l " +
                    "JOIN clients c ON l.user_id = c.membernumber " +
                    "WHERE l.status = 'pending' AND c.username != ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, currentUsername);
            ResultSet resultSet = selectStatement.executeQuery();

            double remainingShare = proportionalShare; // The remaining share to be distributed

            // Update the loan amount for the current user in the loanrequests table
            String updateSql = "UPDATE loanrequests SET amount = amount + ? " +
                    "WHERE loanapplicationid = ? AND user_id = " +
                    "(SELECT membernumber FROM clients WHERE username = ?)";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);

            // Iterate through each pending loan application and update their loan amount
            while (resultSet.next()) {
                String loanapplicationid = resultSet.getString("loanapplicationid");
                BigDecimal amount = resultSet.getBigDecimal("amount");

                // Calculate the amount to be added to the current application's loan amount
                BigDecimal additionalAmount = amount.multiply(new BigDecimal(proportionalShare));

                // Update the loan amount for the current application
                updateStatement.setBigDecimal(1, additionalAmount);
                updateStatement.setString(2, loanapplicationid);
                updateStatement.setString(3, currentUsername);
                updateStatement.executeUpdate();

                remainingShare -= proportionalShare;

                // Break the loop if the remaining share becomes zero (to avoid precision
                // errors)
                if (remainingShare <= 0) {
                    break;
                }
            }

            // Close the resources
            selectStatement.close();
            resultSet.close();
            updateStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void approveLoanApplication(String loanapplicationid, double equitableAmount) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Check if the loan application exists and is pending
            String checkLoanSql = "SELECT status FROM loanrequests WHERE id = ? AND status = 'pending'";
            PreparedStatement checkLoanStatement = connection.prepareStatement(checkLoanSql);
            checkLoanStatement.setObject(1, UUID.fromString(loanapplicationid));
            ResultSet checkLoanResult = checkLoanStatement.executeQuery();

            if (checkLoanResult.next()) {
                // Update the loan application with approved status and equitable amount
                String approveLoanSql = "UPDATE loanrequests SET status = 'granted', equitable_amount = ? WHERE id = ?";
                PreparedStatement approveLoanStatement = connection.prepareStatement(approveLoanSql);
                approveLoanStatement.setDouble(1, equitableAmount);
                approveLoanStatement.setObject(2, UUID.fromString(loanapplicationid));
                int rowsAffected = approveLoanStatement.executeUpdate();

                if (rowsAffected > 0) {
                    System.out.println("Loan application approved. Equitable amount: " + equitableAmount);
                } else {
                    System.out.println("Error: Loan application approval failed.");
                }
            } else {
                System.out.println("Error: Loan application not found or already approved.");
            }

            checkLoanResult.close();
            checkLoanStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static int getNumberOfLoanApplicants() {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT COUNT(DISTINCT user_id) AS numberOfLoanApplicants FROM loanrequests where status ='pending'";

            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();

            int numberOfLoanApplicants = 0;
            if (resultSet.next()) {
                numberOfLoanApplicants = resultSet.getInt("numberOfLoanApplicants");
            }

            resultSet.close();
            statement.close();

            return numberOfLoanApplicants;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static String getLoanStatus(String username) {
        String status = null;
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String query = "SELECT l.status FROM loans l " +
                    "JOIN loanrequests q ON l.loanapplicationid = q.loanapplicationid " +
                    "JOIN clients c ON q.user_id = c.membernumber " +
                    "WHERE c.username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {

                status = resultSet.getString("status");
                if (status == null) {
                    // Skip null value rows
                    continue;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    private static String getLoanProgress(String username) {
        String status = getLoanStatus(username);
        if (status == null) {
            // Status not found, assume 100% progress
            return "no loan record";
        } else if (status.equals("complete")) {
            // Status is 'complete', assign 100% progress
            return "100.0";
        } else if (status.equals("ontrack")) {
            // Status is 'ontrack', assign 60% progress
            return "60.0%";
        } else {
            // Status is 'pending' or unknown, assume 0% progress
            return "low progress";
        }
    }

    private static String getContributionStatus(double contributionProgress, String username) {

        if (contributionProgress >= 100) {
            return "Cleared";
        } else if (contributionProgress >= 50) {
            return "On Track";
        } else {
            return "Behind";
        }

    }

    private static double calculateEquitableLoanAmount(String username) {
        double totalContributions = 0.0;
        double maxLoanAmount = 0.0;

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Calculate total contributions
            String contributionsSql = "SELECT sum(amount) AS TotalContributions FROM transactions t JOIN clients c ON t.user_id = c.membernumber WHERE c.username = ?";
            PreparedStatement contributionsStatement = connection.prepareStatement(contributionsSql);
            contributionsStatement.setString(1, username);
            ResultSet contributionsResultSet = contributionsStatement.executeQuery();

            if (contributionsResultSet.next()) {
                totalContributions = contributionsResultSet.getDouble("TotalContributions");
            }

            contributionsResultSet.close();
            contributionsStatement.close();

            // Check for pending loan
            String pendingLoanSql = "SELECT sum(loanamount) AS PendingLoanAmount FROM loans l JOIN clients c ON l.user_id = c.membernumber WHERE c.username = ? AND l.status = 'pending'";
            PreparedStatement pendingLoanStatement = connection.prepareStatement(pendingLoanSql);
            pendingLoanStatement.setString(1, username);
            ResultSet pendingLoanResultSet = pendingLoanStatement.executeQuery();

            double pendingLoanAmount = 0.0;
            if (pendingLoanResultSet.next()) {
                pendingLoanAmount = pendingLoanResultSet.getDouble("PendingLoanAmount");
            }

            pendingLoanResultSet.close();
            pendingLoanStatement.close();

            // Calculate the maximum loan amount based on contribution rules
            double halfContributions = totalContributions * 0.5;
            double threeQuarterContributions = totalContributions * 0.75;
            maxLoanAmount = halfContributions > pendingLoanAmount ? halfContributions - pendingLoanAmount : 0.0;

            // If the requested loan amount is greater than half of the total contributions,
            // limit the loan amount to three-quarters of the total contributions
            if (maxLoanAmount > halfContributions) {
                maxLoanAmount = threeQuarterContributions - pendingLoanAmount;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return maxLoanAmount;
    }

    private static double getAvailableFunds() {
        double availableFunds = 0.0; // Assume there are 3 million available funds in the Sacco

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Get the total amount from transactions
            String transactionsSql = "SELECT SUM(amount) AS TotalAmount FROM transactions";
            PreparedStatement transactionsStatement = connection.prepareStatement(transactionsSql);
            ResultSet transactionsResultSet = transactionsStatement.executeQuery();

            if (transactionsResultSet.next()) {
                double totalAmount = transactionsResultSet.getDouble("TotalAmount");
                availableFunds += totalAmount; // Add the total amount to the initial available funds
            }

            transactionsResultSet.close();
            transactionsStatement.close();

            // Deduct total loan amount from loans table
            String loansSql = "SELECT SUM(loanamount) AS TotalLoans FROM loans";
            PreparedStatement loansStatement = connection.prepareStatement(loansSql);
            ResultSet loansResultSet = loansStatement.executeQuery();

            if (loansResultSet.next()) {
                double totalLoans = loansResultSet.getDouble("TotalLoans");
                availableFunds -= totalLoans; // Deduct the total loan amount from the available funds
            }

            loansResultSet.close();
            loansStatement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableFunds;
    }

    private static boolean checkLowPerformance(String username) {
        String loanProgressStr = getLoanProgress(username);
        double loanProgress;
        if (loanProgressStr == null) {
            // Handle the case where loan progress is null
            // For example, set some default value or return false
            return false;
        }

        if (loanProgressStr.equals("no loan record")) {
            // No loan record found, assume 100% progress
            loanProgress = 100.0;
        } else if (loanProgressStr.equals("100.0")) {
            // Loan is complete, assign 100% progress
            loanProgress = 100.0;
        } else if (loanProgressStr.equals("60.0%")) {
            // Loan is on track, assign 60% progress
            loanProgress = 60.0;
        } else {
            // Loan progress is either 'low progress' or unknown, assume 0% progress
            loanProgress = 0.0;
        }

        String contributionStatus = getContributionStatus(loanProgress, username);
        return contributionStatus.equals("Behind");
    }

    private static void handleLoanRequestStatusCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 2) {
            String loanApplicationId = parts[1];
            // Process the loan request status command and perform database operations
            String status = getLoanRequestStatus(loanApplicationId, username);
            writer.println("Loan request status: " + status);
        } else {
            writer.println(
                    "Invalid LoanRequestStatus command format. Please use 'LoanRequestStatus loanApplicationId'.");
        }
    }

    private static String getLoanRequestStatus(String loanApplicationId, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            UUID uuidLoanApplicationId = UUID.fromString(loanApplicationId); // Convert the loanApplicationId to UUID

            String selectSql = "SELECT status FROM loanrequests WHERE loanapplicationid = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setObject(1, uuidLoanApplicationId); // Set the UUID value in the PreparedStatement

            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                String status = resultSet.getString("status");
                resultSet.close();
                selectStatement.close();
                connection.close();
                return status;
            }

            resultSet.close();
            selectStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Loan request not found.";
    }

    private static int handleRegisterCommand(String command, PrintWriter writer, BufferedReader reader) {
        String[] parts = command.split(" ");
        if (parts.length == 5) {
            String username = parts[1];
            String password = parts[2];
            String email = parts[3];
            String telephone = parts[4];

            // Check if the username is already taken
            if (isUsernameTaken(username)) {
                writer.println("Username already exists. Please choose a different username.");
                return 0; // Return 0 to indicate that registration failed
            }

            int membernumber = registerUser(username, password, email, telephone);
            if (membernumber != 0) {
                writer.println("Registration successful! Your membernumber is: " + membernumber
                        + ". Please enter 'login' to login and continue.");
                return membernumber; // Return the membernumber if registration is successful
            } else {
                writer.println("Failed to register. Please try again.");
                return 0; // Return 0 to indicate that registration failed
            }
        } else {
            writer.println("Invalid registration command!");
            return 0; // Return 0 to indicate that registration failed
        }
    }

    private static boolean isUsernameTaken(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM clients WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
            boolean isTaken = resultSet.next();

            resultSet.close();
            statement.close();

            return isTaken;
        } catch (SQLException e) {
            e.printStackTrace();
            return true; // In case of an error, assume the username is taken to avoid potential
                         // registration issues.
        }
    }

    private static int registerUser(String username, String password, String email, String telephone) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String insertSql = "INSERT INTO clients (username, password, email, telephone,created_at, updated_at) VALUES (?, ?, ?, ?,NOW(),NOW()) RETURNING membernumber";
            PreparedStatement insertStatement = connection.prepareStatement(insertSql);
            insertStatement.setString(1, username);
            insertStatement.setString(2, password);
            insertStatement.setString(3, email);
            insertStatement.setString(4, telephone);

            ResultSet resultSet = insertStatement.executeQuery();

            int membernumber = 0;
            if (resultSet.next()) {
                membernumber = resultSet.getInt("membernumber");
            }

            resultSet.close();
            insertStatement.close();
            connection.close();

            return membernumber;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0; // Return 0 or a negative value to indicate an error or failure
        }
    }

    private static void handleLoanAcceptCommand(String command, PrintWriter writer, String username) {
        // Extract loan application ID from the command
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            writer.println("Invalid LoanAccept command format. Please use ' accept'.");
            return;
        }

        String message = acceptLoanApplication(username);
        writer.println("Loan Accept Info:" + message);
    }

    private static void handleLoanRejectCommand(String command, PrintWriter writer, String username) {
        // Extract loan application ID from the command
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            writer.println("Invalid LoanReject command format. Please use 'LoanReject loanapplicationid reject'.");
            return;
        }

        String message = rejectLoanApplication(username);

        // Notify the client that the loan application has been rejected
        writer.println("rejected loangrant \t" + message);
    }

    private static String rejectLoanApplication(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String updateSql = "SELECT l.loanapplicationid, l.amount FROM loanrequests l "
                    + "INNER JOIN clients c ON l.user_id = c.membernumber "
                    + "WHERE l.status = 'granted' AND c.username = ?";
            try (PreparedStatement selectStatement = connection.prepareStatement(updateSql)) {
                selectStatement.setString(1, username);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        return "No granted loan application found for the given username.";
                    }
                    UUID loanApplicationId = (UUID) resultSet.getObject("loanapplicationid");
                    // Update the loan application status in the loanrequests table
                    String updateSql2 = "UPDATE loanrequests SET status = 'rejected',created_at = NOW(),updated_at = NOW() WHERE loanapplicationid = ? AND status = 'granted'";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql2)) {
                        updateStatement.setObject(1, loanApplicationId);
                        int rowsAffected = updateStatement.executeUpdate();
                        if (rowsAffected > 0) {
                            return "Loan application " + loanApplicationId + " has been rejected.";
                        } else {
                            return "Failed to reject loan application " + loanApplicationId + ".";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL error";
        }
    }

    public static String acceptLoanApplication(String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Check if the loan application is granted for the given username
            String selectSql = "SELECT l.loanapplicationid, l.amount, l.paymentperiodinmonths FROM loanrequests l "
                    + "INNER JOIN clients c ON l.user_id = c.membernumber "
                    + "WHERE l.status = 'granted' AND c.username = ?";

            try (PreparedStatement selectStatement = connection.prepareStatement(selectSql)) {
                selectStatement.setString(1, username);
                try (ResultSet resultSet = selectStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        return "No granted loan application found for the given username.";
                    }
                    UUID loanApplicationId = (UUID) resultSet.getObject("loanapplicationid");
                    BigDecimal amount = resultSet.getBigDecimal("amount");
                    int paymentPeriodInMonths = resultSet.getInt("paymentperiodinmonths");

                    // Update the loan application status in the loanrequests table
                    String updateSql = "UPDATE loanrequests SET status = 'accepted', created_at = NOW(), updated_at = NOW() WHERE loanapplicationid = ? AND status = 'granted'";
                    try (PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {
                        updateStatement.setObject(1, loanApplicationId);
                        int rowsAffected = updateStatement.executeUpdate();

                        if (rowsAffected > 0) {
                            // Calculate the payableloanamount and monthlyloaninstallments
                            BigDecimal payableLoanAmount = amount.multiply(BigDecimal.ONE
                                    .add(BigDecimal.valueOf(0.02).multiply(BigDecimal.valueOf(paymentPeriodInMonths))));
                            BigDecimal monthlyLoanInstallments = payableLoanAmount
                                    .divide(BigDecimal.valueOf(paymentPeriodInMonths), 2, RoundingMode.HALF_UP);
                            double interestrate = 0.02;
                            // Insert the accepted loan information into the loans table
                            String insertSql = "INSERT INTO loans (loanid, loanapplicationid, loanamount, acceptdate, payableloanamount, monthlyloaninstallments,interestrate, created_at, updated_at) "
                                    +
                                    "VALUES (?, ?, ?, NOW(), ?, ?,?, NOW(), NOW())";
                            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql)) {
                                UUID loanId = UUID.randomUUID(); // Generate a new UUID for the loanID
                                insertStatement.setObject(1, loanId);
                                insertStatement.setObject(2, loanApplicationId);
                                insertStatement.setBigDecimal(3, amount);
                                insertStatement.setBigDecimal(4, payableLoanAmount);
                                insertStatement.setBigDecimal(5, monthlyLoanInstallments);
                                insertStatement.setDouble(6, interestrate);
                                insertStatement.executeUpdate();

                                return "Loan ID: " + loanId + "\t Monthly Loan Installments: "
                                        + monthlyLoanInstallments;
                            }
                        } else {
                            return "Failed to accept loan application " + loanApplicationId + ".";
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL error";
        }
    }

    private static void handleresetpasswordCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 4) {
            String email = parts[1];
            String telephone = parts[2];
            String newPassword = parts[3];

            String resultMessage = resetpassword(email, telephone, newPassword);
            writer.println("passwordresetted" + resultMessage);
        } else {
            writer.println("Invalid resetpassword command!");
        }
    }

    private static String resetpassword(String email, String telephone, String newPassword) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String updateSql = "UPDATE clients SET password = ? WHERE  email = ? AND telephone = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);

            updateStatement.setString(1, newPassword);
            updateStatement.setString(2, email);
            updateStatement.setString(3, telephone);

            int rowsAffected = updateStatement.executeUpdate();
            updateStatement.close();

            if (rowsAffected > 0) {
                return "Password reset successfully!";
            } else {
                return "No matching record found for the given details.";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error resetting password.";
        }
    }

}
