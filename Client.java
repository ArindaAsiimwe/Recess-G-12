import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {
    private static BufferedReader reader;
    private static PrintWriter writer;

    /**
     * @param args
     */
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            // Initialize reader and writer
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            boolean isLoggedIn = false;

            while (true) {
                String commandPrompt = readServerResponse();
                // System.out.println(commandPrompt);

                if (commandPrompt.startsWith("Welcome to Uprise-sacco Management System")) {
                    if (!isLoggedIn) {
                        printCommandSyntax();
                        isLoggedIn = processLogin();
                    }

                    else {
                        String command = readUserInput();
                        sendCommandToServer(command);
                    }
                } else if (commandPrompt.startsWith("Login successful!")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Deposit successful.")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Registration successful!")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("valid  admin login")) {
                    displayAdminMenu();
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Loan Accept Info:")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.equals("Invalid command.")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Username already exists.")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Loan request status: ")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("loanDeposit successfull!")) {
                    System.out.println(commandPrompt);
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("loanrequeststatus received")) {
                    String loanappid;
                    while (true) {
                        System.out.println("Please Enter the loan application id to check loan status.");
                        loanappid = readUserInput().trim();
                        // Validate loanappid as UUID
                        if (isValidUUID(loanappid)) {
                            break; // Break the loop if the loanappid is a valid UUID.
                        }
                        System.out.println("Invalid loan application id. Please try again.");
                    }

                    String command = "LoanRequestStatus" + " " + loanappid;
                    sendCommandToServer(command);
                } else if (commandPrompt.equals("logincommand received")) {
                    String username;
                    while (true) {
                        System.out.println("Please Enter username: ");
                        username = readUserInput().trim();
                        if (!username.isEmpty()) {
                            break; // Break the loop if the username is not empty.
                        }
                        System.out.println("Username cannot be empty. Please try again.");
                    }

                    String password;
                    while (true) {
                        System.out.println("Enter  password ");
                        password = readUserInput();
                        if (isValidPassword(password)) {
                            break; // Break the loop if the password is valid.
                        }
                        System.out.println("Invalid password format. Please try again.");
                    }

                    String loginCommand = "login" + " " + username + " " + password;
                    sendCommandToServer(loginCommand);

                } else if (commandPrompt.startsWith("loandeposit received")) {
                    double amount;
                    while (true) {
                        System.out.println("Please enter the amount to deposit on loan:");
                        String amountValue = readUserInput().trim();

                        try {
                            amount = Double.parseDouble(amountValue);

                            if (amount < 0) {
                                System.out.println("Please enter a positive value.");
                            } else {
                                break; // Break the loop if the amount is valid.
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid amount. Please try again.");
                        }
                    }

                    String date_deposited;
                    while (true) {
                        System.out.println("Enter the Date of deposit (YY-MM-DD): ");
                        date_deposited = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid date
                        // format).
                        if (!date_deposited.isEmpty()) {
                            break; // Break the loop if the date is not empty.
                        }
                        System.out.println("Invalid date format. Please try again.");
                        // if (isValidDateFormat(date_deposited)) {
                        // break; // Break the loop if the date is in valid format.
                        // }
                        // System.out.println("Invalid date format. Please try again.");
                    }

                    String receipt_number;
                    while (true) {
                        System.out.println("Enter the Receipt Number: ");
                        receipt_number =

                                readUserInput().trim();
                        // You can add additional validation if needed.
                        if (!receipt_number.isEmpty()) {
                            break; // Break the loop if the receipt number is not empty.
                        }
                        System.out.println("Invalid receipt number. Please try again.");
                    }

                    String Command = "LoanDeposit" + " " + amount + " " + date_deposited + " " + receipt_number;

                    sendCommandToServer(Command);
                }

                else if (commandPrompt.startsWith("loanaccept received"))

                {
                    System.out.println("enter **accept** to accept the loan");
                    String accept = readUserInput();

                    String command = "LoanAccept" + " " + accept;
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("loanreject received")) {
                    System.out.println("enter **reject** to reject  the loan");
                    String reject = readUserInput();

                    String command = "LoanReject" + " " + reject;
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("loanapprove received")) {
                    System.out.println("enter loan application id to be approved");
                    String appid = readUserInput();
                    System.out.println("enter Amount to be approved");
                    String amount = readUserInput();

                    String command = "loanApprove" + " " + appid + " " + amount;
                    sendCommandToServer(command);

                }

                else if (commandPrompt.equals("depositcommand received") && isLoggedIn) {
                    String amount;
                    while (true) {
                        System.out.println("Please Enter the amount to deposit: ");
                        amount = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid
                        // numeric value).
                        if (!amount.isEmpty()) {
                            break; // Break the loop if the amount is not empty.
                        }
                        System.out.println("Invalid amount. Please try again.");
                    }

                    String date_deposited;
                    while (true) {
                        System.out.println("Enter the Date of deposit (YY-MM-DD): ");
                        date_deposited = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid date
                        // format).
                        if (!date_deposited.isEmpty()) {
                            break; // Break the loop if the date is not empty.
                        }
                        System.out.println("Invalid date format. Please try again.");
                    }

                    String receipt_number;
                    while (true) {
                        System.out.println("Enter the Receipt Number: ");
                        receipt_number = readUserInput().trim();
                        // You can add additional validation if needed.
                        if (!receipt_number.isEmpty()) {
                            break; // Break the loop if the receipt number is not empty.
                        }
                        System.out.println("Invalid receipt number. Please try again.");
                    }

                    String loginCommand = "deposit" + " " + amount + " " + date_deposited + " " + receipt_number;
                    sendCommandToServer(loginCommand);

                } else if (commandPrompt.startsWith("passwordresetted")) {

                    System.out.println("password resetted successfully, You are signed in now");
                    String commandAfterInvalidLogin = readUserInput();
                    sendCommandToServer(commandAfterInvalidLogin);

                } else if (commandPrompt.startsWith("Loan application ")) {

                    System.out.println(commandPrompt);
                    String commandAfterInvalidLogin = readUserInput();
                    sendCommandToServer(commandAfterInvalidLogin);

                } else if (commandPrompt.startsWith("rejected loangrant")) {

                    System.out.println(commandPrompt);
                    String commandAfterInvalidLogin = readUserInput();
                    sendCommandToServer(commandAfterInvalidLogin);

                } else if (commandPrompt.startsWith("loanrequest received")) {
                    String amount;
                    while (true) {
                        System.out.println("Please Enter the loan amount you want : ");
                        amount = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid
                        // numeric value).
                        if (!amount.isEmpty()) {
                            break; // Break the loop if the amount is not empty.
                        }
                        System.out.println("Invalid amount. Please try again.");
                    }

                    String paymentPeriod;
                    while (true) {
                        System.out.println("Enter the payment period in months not exceeding 36 months: ");
                        paymentPeriod = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid
                        // integer and not greater than 36).
                        if (!paymentPeriod.isEmpty()) {
                            try {
                                int period = Integer.parseInt(paymentPeriod);
                                if (period > 0 && period <= 36) {
                                    break; // Break the loop if the payment period is valid (between 1 and 36).
                                } else {
                                    System.out
                                            .println("Invalid payment period. Please enter a value between 1 and 36.");
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Invalid payment period. Please enter a valid integer.");
                            }
                        } else {
                            System.out.println("Invalid payment period. Please try again.");
                        }
                    }

                    String command = "requestLoan" + " " + amount + " " + paymentPeriod;
                    sendCommandToServer(command);
                }

                else if (commandPrompt.equals("checkstatementcommand received")) {
                    String dateFrom;
                    while (true) {
                        System.out.println("Enter the StartDate of check statement (YY-MM-DD): ");
                        dateFrom = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid date
                        // format).
                        if (!dateFrom.isEmpty()) {
                            break; // Break the loop if the start date is not empty.
                        }
                        System.out.println("Invalid date format. Please try again.");
                    }

                    String dateTo;
                    while (true) {
                        System.out.println("Enter the EndDate of check statement (YY-MM-DD): ");
                        dateTo = readUserInput().trim();
                        // You can add additional validation if needed (e.g., check if it's a valid date
                        // format).
                        if (!dateTo.isEmpty()) {
                            break; // Break the loop if the end date is not empty.
                        }
                        System.out.println("Invalid date format. Please try again.");
                    }

                    String loginCommand = "CheckStatement" + " " + dateFrom + " " + dateTo;
                    sendCommandToServer(loginCommand);
                } else if (commandPrompt.startsWith("Statement Information:")) {
                    // Assuming 'statementData' contains the data received from the server-side
                    String statements = commandPrompt;
                    String statementData = statements.substring("Statement Information:".length());
                    String[] rows = statementData.toString().split("\t");

                    // Print table headers
                    System.out.println("                                                       Bank Statement \n");
                    System.out.println(
                            "               Transaction ID                         Amount        DepositDate        Receipt Number");

                    for (String row : rows) {
                        // Split each row into individual columns
                        String[] columns = row.split(" ");

                        // Print each column separated by spaces
                        for (String column : columns) {
                            System.out.print(column + "   ");
                        }
                        System.out.println(); // Move to the next row
                    }
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("view information")) {
                    // Assuming 'statementData' contains the data received from the server-side
                    String statements = commandPrompt;
                    String statementData = statements.substring("view information".length());
                    String[] rows = statementData.toString().split("\t");

                    // Print table headers
                    System.out
                            .println("                            Pending Loan Requests Waiting for Admin Approval \n");
                    System.out.println(
                            "          LoanApplicationId            UserID       Amount      PaymentPeriodInMonths     LoanStatus     ");

                    for (String row : rows) {
                        // Split each row into individual columns
                        String[] columns = row.split(" ");

                        // Print each column separated by spaces
                        for (String column : columns) {
                            System.out.print(column + "  ");
                        }
                        System.out.println(); // Move to the next row
                    }
                    String command = readUserInput();
                    sendCommandToServer(command);

                } else if (commandPrompt.startsWith("Loan application submitted")) {
                    System.out.println(commandPrompt);
                    String commandAfterInvalidLogin = readUserInput();
                    sendCommandToServer(commandAfterInvalidLogin);

                } else if (commandPrompt.equals("SessionTermination!")) {
                    isLoggedIn = true;
                    System.out.println("Logging out. Exiting...");
                    break;
                } else {

                    isLoggedIn = true;
                    String command = readUserInput();
                    sendCommandToServer(command);
                }
            }
        } catch (

        IOException e) {
            e.printStackTrace();
        }

    }

    private static boolean processLogin() throws IOException {
        String command = readUserInput();
        sendCommandToServer(command);
        String serverResponse = readServerResponse();

        if (serverResponse.equals("logincommand received")) {
            String username;
            while (true) {
                System.out.println("Please Enter username: ");
                username = readUserInput().trim();
                if (!username.isEmpty()) {
                    break; // Break the loop if the username is not empty.
                }
                System.out.println("Username cannot be empty. Please try again.");
            }

            String password;
            while (true) {
                System.out.println("Enter  password ");
                password = readUserInput();
                if (isValidPassword(password)) {
                    break; // Break the loop if the password is valid.
                }
                System.out.println("Invalid password format. Please try again.");
            }

            String loginCommand = "login" + " " + username + " " + password;
            sendCommandToServer(loginCommand);

            return true;
        } else if (serverResponse.equals("registercommand received")) {
            System.out.println("Please Enter username: ");
            String username = readUserInput().trim(); // Trim leading and trailing whitespaces
            while (username.isEmpty()) {
                System.out.println("Username cannot be empty. Please try again.");
                username = readUserInput().trim();
            }

            System.out.println("Enter password with a minimum of 6 characters, including letters and numbers: ");
            String password = readUserInput();
            while (!isValidPassword(password)) {
                System.out.println("Invalid password format. Please try again.");
                password = readUserInput();
            }

            System.out.println("Enter a valid email address: ");
            String email = readUserInput().trim();
            while (!isValidEmail(email)) {
                System.out.println("Invalid email address. Please try again.");
                email = readUserInput().trim();
            }

            String telephone;
            while (true) {
                System.out.println("Enter a valid phone number: ");
                telephone = readUserInput().trim();
                if (isValidPhoneNumber(telephone)) {
                    break; // Break the loop if the phone number is valid.
                }
                System.out.println("Invalid phone number format. Please try again.");
            }

            String loginCommand = "register" + " " + username + " " + password + " " + email + " " + telephone;
            sendCommandToServer(loginCommand);
            return true;
        } else if (serverResponse.startsWith("Registration successful!")) {
            System.out.println(serverResponse + "\n");
            String commandAfterInvalidLogin = readUserInput();
            sendCommandToServer(commandAfterInvalidLogin);
            return true;
        } else if (serverResponse.startsWith("reset password!")) {
            String email;
            String telephone;

            // Prompt the user for their registered email
            do {
                System.out.println("Please enter your registered email address:");
                email = readUserInput().trim();
            } while (!isValidEmail(email));

            // Prompt the user for their registered telephone number
            do {
                System.out.println("Please enter your registered telephone number:");
                telephone = readUserInput().trim();
            } while (!isValidPhoneNumber(telephone));

            // Send the password reset request to the server along with the new password
            String newPassword;
            String confirmPassword;
            do {
                System.out.println(
                        "Please enter your new password (minimum 6 characters, including letters and numbers):");
                newPassword = readUserInput().trim();

                System.out.println("Please confirm your new password:");
                confirmPassword = readUserInput().trim();

                if (!newPassword.equals(confirmPassword)) {
                    System.out.println("Passwords do not match. Please try again.");
                } else if (!isValidPassword(newPassword)) {
                    System.out.println("Invalid password format. Please try again.");
                    confirmPassword = ""; // Reset confirmPassword to re-enter the new password.
                }
            } while (!newPassword.equals(confirmPassword));

            String resetCommand = "reset " + email + " " + telephone + " " + newPassword;
            sendCommandToServer(resetCommand);

            return true;
        }

        else if (serverResponse.equals("SessionTermination!")) {

            System.out.println("Logging out. Exiting...");
            return false;
        }

        else {
            System.out.println("4" + serverResponse);
            String commandAfterInvalidLogin = readUserInput();
            sendCommandToServer(commandAfterInvalidLogin);
            return false;
        }
    }

    private static String readServerResponse() throws IOException {
        return reader.readLine();
    }

    private static String readUserInput() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Uprise Client~$:");
            return bufferedReader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return "Error occured";
        }
    }

    private static void sendCommandToServer(String command) {
        writer.println(command);
    }

    private static void printCommandSyntax() {
        System.out.println(
                "********** System Commands Available *********\n" +
                        "******* please press enter key after inputing **\n" +
                        " *****************************************\n" +
                        "*         ******* login *******    *\n" +
                        "*           ****** deposit ******    *\n" +
                        "*          ***** CheckStatement ***** *\n" +
                        "*         **** LoanRequestStatus **** *\n" +
                        "*             *****  register    *    *\n" +
                        "*             ***  requestLoan   **   *\n" +
                        "*             ***  loanAccept   ***   *\n" +
                        "*             **  loanReject   ******  *\n" +
                        "*        ******* loanDeposit  ********** *\n" +
                        "*      ********* logout      *************  *\n" +
                        "*    ********* forgotpassword   *************  *\n" +
                        "***********************************************");
    }

    private static boolean isValidPassword(String password) {
        // Password should have a minimum of 6 characters and contain at least one
        // letter and one number.
        return password.length() >= 6 && password.matches(".*[a-zA-Z].*") && password.matches(".*\\d.*");
    }

    private static boolean isValidEmail(String email) {
        // Basic email format validation using a regular expression.
        return email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    private static boolean isValidPhoneNumber(String phoneNumber) {
        // Basic phone number format validation using a regular expression.
        return phoneNumber.matches("^\\d{10}$"); // Assuming phone number should be 10 digits.
    }

    // private static boolean isValidDateFormat(String inputDate) {
    // leDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd");
    // Format.setLenient(false);

    // {
    // e = dateFormat.parse(inputDate);
    // rue;
    // tch (ParseException e) {
    // alse;
    //
    // }

    // private static String UserInput() {

    // ou can implement your input reading mechanism here
    // rn "21-08-09"; // For example, hardcoded input for testing
    // }

    private static boolean isValidUUID(String uuidString) {
        try {
            // Regular expression pattern for UUID
            String uuidPattern = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
            Pattern pattern = Pattern.compile(uuidPattern);
            Matcher matcher = pattern.matcher(uuidString.toLowerCase()); // Convert to lowercase for case-insensitive
                                                                         // matching

            return matcher.matches();
        } catch (Exception e) {
            return false;
        }
    }

    private static void displayAdminMenu() {
        System.out.println("********Admin Menu**********************");
        System.out.println("******Available admin commands**************");
        System.out.println("*****view***** View Pending Loan Applications***");
        System.out.println("****approve***** Approve LoanApplicationId************");
        System.out.println("**Logout***********************************************");

    }

}
