import java.io.*;
import java.math.BigDecimal;
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
                                    if (isValidCredentials(username, password)) {
                                        writer.println("Login successful!" + "You are logged in as " + username + ".");
                                        isLoggedIn = true;
                                        loggedInClients.put(username, true);
                                    } else {
                                        writer.println("Invalid username or password!");
                                    }
                                } else if (command.equalsIgnoreCase("login")) {
                                    writer.println("logincommand received");
                                }

                                else {
                                    writer.println("Invalid login command!");
                                }

                            } else if (command.equalsIgnoreCase("register")) {
                                writer.println("registercommand received");
                            
                            } else if (command.equalsIgnoreCase("deposit")) {
                                writer.println("depositcommand received");
                            }
                            else if (command.equalsIgnoreCase("checkstatement")) {
                                writer.println("checkstatementcommand received");
                            }

                            else if (command.startsWith("register")) {
                                handleRegisterCommand(command, writer, reader);
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

    private static boolean isValidCredentials(String username, String password) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String sql = "SELECT * FROM clients WHERE username = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            boolean isValid = resultSet.next();

            resultSet.close();
            statement.close();

            return isValid;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void handleDepositCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 4) {
            String amount = parts[1];
            String dateDeposited = parts[2];
            String receiptNumber = parts[3];
            String resultMessage = deposit(amount, dateDeposited, receiptNumber, username);
            writer.println(resultMessage);
        } else {
            writer.println("Invalid deposit command!");
        }
    }

    private static void handleLoanDepositCommand(String command, PrintWriter writer, String username) {
        String[] parts = command.split(" ");
        if (parts.length == 2) {
            String amount = parts[1];

            String resultMessage = loandeposit(amount, username);
            writer.println(resultMessage);
        } else {
            writer.println("Invalid deposit command!");
        }
    }
    
    private static String deposit(String amount, String dateDeposited, String receiptNumber, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // Check if the receiptNumber is available and not cleared in the BankReceipts table
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
                        return "Deposit successful. Amount deposited: " + amount;
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
    

    
    private static String loandeposit(String amount, String username) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // First, check if the loan application exists and its status is 'accepted'
            String selectLoanSql = "SELECT status, loanid FROM loans WHERE userid = (SELECT membernumber FROM clients  WHERE username = ?) AND status = 'accepted'";
            PreparedStatement selectLoanStatement = connection.prepareStatement(selectLoanSql);
            selectLoanStatement.setString(1, username);
            ResultSet loanResultSet = selectLoanStatement.executeQuery();

            if (loanResultSet.next()) {
                UUID loanId = UUID.fromString(loanResultSet.getString("loanid"));

                // Next, insert the loan deposit amount in the loandeposits table
                String insertDepositSql = "INSERT INTO loandeposits (loanapplicationid, monthlyLoanDeposits, status) VALUES (?, ?, 'pending')";
                PreparedStatement insertDepositStatement = connection.prepareStatement(insertDepositSql);
                insertDepositStatement.setObject(1, loanId);
                insertDepositStatement.setDouble(2, Double.parseDouble(amount));
                int rowsAffected = insertDepositStatement.executeUpdate();
                insertDepositStatement.close();

                connection.close();

                if (rowsAffected > 0) {
                    return "Loan deposit has been successfully inserted.";
                } else {
                    return "Failed to insert the loan deposit.";
                }
            } else {
                loanResultSet.close();
                selectLoanStatement.close();
                connection.close();
                return "No accepted loan application found for the user.";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error on insertion";
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
            String selectSql = "SELECT t.*, sum(amount) AS AccountBalance FROM transactions t JOIN clients c ON t.user_id = c.membernumber WHERE c.username = ? AND t.date_deposited BETWEEN TO_DATE(?, 'YYYY-MM-DD') AND TO_DATE(?, 'YYYY-MM-DD') GROUP BY t.id, t.date_deposited";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, username);
            selectStatement.setString(2, dateFrom);
            selectStatement.setString(3, dateTo);
            ResultSet resultSet = selectStatement.executeQuery();

            double loanProgress = 0.0;
            double contributionProgress = 0.0;
            int totalLoanMonths = 0;
            double accountBalance = 0.0; // Declare and initialize accountBalance here
            StringBuilder statementData = new StringBuilder();
            boolean foundTransactions = false;

            while (resultSet.next()) {
                if (accountBalance == 0.0) {
                    accountBalance = resultSet.getDouble("AccountBalance");
                }

                UUID transactionId = (UUID) resultSet.getObject("id");
                double amount = resultSet.getDouble("amount");
                Date dateDeposited = resultSet.getDate("date_deposited");
                String receiptNumber = resultSet.getString("receipt_number");

                // Format each row of the table and append to the statementData
                statementData.append(String.format(
                        "Transaction ID: %s\tAmount: %-10.2f\tDate Deposited: %-15s\tReceipt Number: %-20s\t",
                        transactionId, amount, dateDeposited, receiptNumber));
                foundTransactions = true;
            }

            resultSet.close();
            selectStatement.close();

            if (foundTransactions) {
                // Calculate loan progress and contribution progress
                String totalLoanMonthsString = getTotalLoanMonths(username);
                totalLoanMonths = Integer.parseInt(totalLoanMonthsString);
                int clearedLoanMonths = getClearedLoanMonths(username);
                loanProgress = (double) clearedLoanMonths / totalLoanMonths * 100;

                int totalContributionMonths = getTotalContributionMonths();
                int clearedContributionMonths = getClearedContributionMonths(username);
                contributionProgress = (double) clearedContributionMonths / totalContributionMonths * 100;
                // Insert loan status, contribution status, and account balance at the end of
                // statementData
                int lastIndex = statementData.length();
                statementData.insert(lastIndex, String.format("\tAccount Balance: %-10.2f", accountBalance));
                statementData.insert(lastIndex, String.format("\tContribution Status: %-10s",
                        getContributionStatus(contributionProgress, totalLoanMonthsString)));
                statementData.insert(lastIndex, String.format("\tLoan Status: %-10s\t", getLoanStatus(loanProgress)));
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
            String sql = "SELECT COUNT(DISTINCT EXTRACT(MONTH FROM date_deposited)) AS totalContributionMonths FROM transactions WHERE status = 'cleared'";
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
                    "FROM loanrequests lr " +
                    "JOIN clients c ON lr.user_id = c.membernumber " +
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

        return 0;
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
                writer.println("Loan application submitted. Your loanapplicationid is: " + loanApplicationId);
            } else {
                writer.println("Failed to submit loan application.");
            }
        } else {
            writer.println(
                    "Invalid requestLoan command format. Please use 'requestLoan amount paymentPeriodInMonths'.");
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
            insertStatement.setDouble(1, Double.parseDouble(amount));
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
                if (numberOfApplicants < 10) {
                    // Check availability of funds
                    double availableFunds = getAvailableFunds();
                    if (availableFunds >= 2000000) {
                        // Check loan performance of previous term
                        boolean lowPerformance = checkLowPerformance(username);
                        if (!lowPerformance) {
                            // Calculate equitable loan amount
                            double equitableAmount = calculateEquitableLoanAmount(username);

                            // Approve the loan application
                            approveLoanApplication(loanapplicationid, equitableAmount);

                            // Return the loan application approval message with the loanapplicationid
                            return "Loan application approved. Your loan amount is: " + equitableAmount +
                                    ". Loan application ID: " + loanapplicationid;
                        } else {
                            return "Loan application rejected due to low performance in previous term.";
                        }
                    } else {
                        return "Loan application rejected due to insufficient funds in the Sacco.";
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
            String sql = "SELECT COUNT(DISTINCT user_id) AS numberOfLoanApplicants FROM loanrequests";

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

    private static String getLoanStatus(double loanProgress) {
        // Replace this implementation with the logic to determine the loan status based
        // on the loan progress
        if (loanProgress >= 100) {
            return "Cleared";
        } else if (loanProgress >= 50) {
            return "On Track";
        } else {
            return "Behind";
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

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String selectSql = "SELECT sum(amount) AS TotalContributions FROM transactions t JOIN clients c ON t.user_id = c.membernumber WHERE c.username = ?";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            selectStatement.setString(1, username);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                totalContributions = resultSet.getDouble("TotalContributions");
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();

        }

        return totalContributions * 0.5; // Return 50% of the total contributions
    }

    private static double getAvailableFunds() {
        double availableFunds = 3000000.0; // Assume there are 3 million available funds in the Sacco

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String selectSql = "SELECT SUM(amount) AS TotalAmount FROM transactions";
            PreparedStatement selectStatement = connection.prepareStatement(selectSql);
            ResultSet resultSet = selectStatement.executeQuery();

            if (resultSet.next()) {
                double totalAmount = resultSet.getDouble("TotalAmount");
                availableFunds += totalAmount; // Add the total amount to the initial available funds
            }

            resultSet.close();
            selectStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return availableFunds;
    }

    private static boolean checkLowPerformance(String username) {
        // assume the member's loan performance is below 50%
        double loanProgress = 40.0;
        return loanProgress < 50;
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

    private static int handleRegisterCommand(String command, PrintWriter writer, BufferedReader reader)
            throws IOException {
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
        if (parts.length != 3) {
            writer.println("Invalid LoanAccept command format. Please use 'LoanAccept loanapplicationid accept'.");
            return;
        }

        String loanApplicationId = parts[1];
        // Assuming you have the method to approve the loan application by loan
        // application ID and equitable amount
        double equitableAmount = 0.0;
        acceptLoanApplication(loanApplicationId, equitableAmount);

        // Notify the client that the loan application has been accepted
        writer.println("Loan application " + loanApplicationId + " has been accepted.");
    }

    private static void handleLoanRejectCommand(String command, PrintWriter writer, String username) {
        // Extract loan application ID from the command
        String[] parts = command.split(" ");
        if (parts.length != 3) {
            writer.println("Invalid LoanReject command format. Please use 'LoanReject loanapplicationid reject'.");
            return;
        }

        String loanApplicationId = parts[1];
        // Assuming you have the method to reject the loan application by loan
        // application ID
        rejectLoanApplication(loanApplicationId);

        // Notify the client that the loan application has been rejected
        writer.println("Loan application " + loanApplicationId + " has been rejected.");
    }

    private static String rejectLoanApplication(String loanApplicationId) {
        // Implement the logic to update the loan application status to 'rejected' in
        // the loanrequests table
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            String updateSql = "UPDATE loanrequests SET status = 'rejected' WHERE id = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setObject(1, UUID.fromString(loanApplicationId));
            int rowsAffected = updateStatement.executeUpdate();
            if (rowsAffected > 0) {
                return ("Loan application " + loanApplicationId + " has been rejected.");
            } else {
                return ("Failed to reject loan application " + loanApplicationId + ".");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "error";
        }
    }

    private static String acceptLoanApplication(String loanApplicationId, double equitableAmount) {
        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword)) {
            // First, update the loan application status and equitable amount in
            // loanrequests table
            String updateSql = "UPDATE loanrequests SET status = 'accepted', equitable_amount = ? WHERE loanapplicationid = ?";
            PreparedStatement updateStatement = connection.prepareStatement(updateSql);
            updateStatement.setDouble(1, equitableAmount);
            updateStatement.setObject(2, UUID.fromString(loanApplicationId));
            int rowsAffected = updateStatement.executeUpdate();
            updateStatement.close();

            if (rowsAffected > 0) {
                // Second, insert the accepted loan information into the loans table
                String insertSql = "INSERT INTO loans (loanid, loanamount) VALUES (?, ?)";
                PreparedStatement insertStatement = connection.prepareStatement(insertSql);
                UUID loanid = UUID.randomUUID(); // Generate a new UUID for the loanID
                insertStatement.setObject(1, loanid);
                insertStatement.setDouble(2, equitableAmount);
                insertStatement.executeUpdate();
                insertStatement.close();

                return "Loan ID " + loanid + " has been inserted into the loans table.";
            } else {
                return "Failed to accept loan application " + loanApplicationId + ".";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL error";
        }
    }

}
