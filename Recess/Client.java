import java.io.*;
import java.net.*;


public class Client {
    private static BufferedReader reader;
    private static PrintWriter writer;
   

    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 8080)) {
            // Initialize reader and writer
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            boolean isLoggedIn = false;

            while (true) {
                String commandPrompt = readServerResponse();
                System.out.println(commandPrompt);

                if (commandPrompt.startsWith("Welcome to Uprise-sacco Management System")) {
                    if (!isLoggedIn) {
                        printCommandSyntax();
                        isLoggedIn = processLogin();
                    } else if (commandPrompt.startsWith("StatementData:")) {
                        isLoggedIn = true;
                        // Process and print statement table
                        String statementData = commandPrompt.substring("StatementData: ".length());
                        printStatementTable(statementData);
                       
                        }
                        else if (commandPrompt.startsWith("Registration successful!") & isLoggedIn){
                            String command = readUserInput();
                        sendCommandToServer(command);
                   
                     }               
                    
                    
                    else {
                        String command = readUserInput();
                        sendCommandToServer(command);
                    }
                } 
                else if (commandPrompt.startsWith("Registration successful!") & isLoggedIn){

                     String command = readUserInput();
                        sendCommandToServer(command);
                }
                 else if (commandPrompt.equals("SessionTermination!")) {
                    isLoggedIn=true;
                        System.out.println("Logging out. Exiting...");
                        break;
                    }                
                else {
                    isLoggedIn = true;
                    String command = readUserInput();
                    sendCommandToServer(command);                   
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean processLogin() throws IOException {
        String command = readUserInput();
        sendCommandToServer(command);
        String serverResponse = readServerResponse();
         System.out.println( serverResponse);

        if (serverResponse.startsWith("Login successful!") || serverResponse.startsWith("Deposit successful.")
                || serverResponse.startsWith("Loan application submitted.")) {
            String commandAfterLogin = readUserInput();
            sendCommandToServer(commandAfterLogin);
            return true;
        } else if (serverResponse.startsWith("StatementData: ")) {
            String statementData = serverResponse.substring("StatementData: ".length());
            printStatementTable(statementData);
            return true;
        }
        else if (serverResponse.startsWith("Registration successful!")){
            String commandAfterInvalidLogin = readUserInput();
            sendCommandToServer(commandAfterInvalidLogin);
            return true;       
        }
         else {
            String commandAfterInvalidLogin = readUserInput();
            sendCommandToServer(commandAfterInvalidLogin);
            return false;
        }
    }

    private static void printStatementTable(String statementData) {
        String[] rows = statementData.split("\t");
        System.out.println("Transaction Statement");
        System.out.println("----------------------------------------------");
        System.out.printf("%-15s %-10s %-15s %-20s%n", "Transaction ID", "Amount", "Date Deposited", "Receipt Number");
        System.out.println("----------------------------------------------");
    
        for (int i = 0; i < rows.length; i += 4) {
            String transactionId = rows[i];
            double amount = Double.parseDouble(rows[i + 1]);
            String dateDeposited = rows[i + 2];
            String receiptNumber = rows[i + 3];
    
            System.out.printf("%-15s %-10.2f %-15s %-20s%n", transactionId, amount, dateDeposited, receiptNumber);
        }
    
        System.out.println("----------------------------------------------");
    }
    

    private static String readServerResponse() throws IOException {
        return reader.readLine();
    }

    private static String readUserInput() {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.print("Enter Command~$:");
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
        System.out.println("***System Commands Available \n" +
                "***********login ********   \n" +
                "**********deposit ********* \n" +
                "*********CheckStatement ****** \n" +
                "********LoanRequestStatus *******\n" +
                "*******register  *******************   \n"+
                 "*****loanAccept **********************  \n"+
                "*****loanReject **********************  \n"+
                "****loanDeposit ************************ \n");
    }
}
