import java.io.*;
import java.net.*;

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
                System.out.println(commandPrompt);

                if (commandPrompt.startsWith("Welcome to Uprise-sacco Management System")) {
                    if (!isLoggedIn) {
                        printCommandSyntax();
                        isLoggedIn = processLogin();
                    } else if (commandPrompt.startsWith("Registration successful!") & isLoggedIn) {
                        String command = readUserInput();
                        sendCommandToServer(command);

                    }

                    else {
                        String command = readUserInput();
                        sendCommandToServer(command);
                    }
                } else if (commandPrompt.equals("depositcommand received") & isLoggedIn) {
                        System.out.println("Please Enter  amount to deposit ? ! ");
                                 String amount = readUserInput();
                                 System.out.println(" Enter  Date of deposit(YY-MM_DD)  ");
                                 String date_deposited = readUserInput();
                                 System.out.println(" Enter  Receipt Number ");
                                 String receipt_number = readUserInput();
                                String loginCommand = "deposit" + " " + amount + " " + date_deposited +" "+ receipt_number;
                                 sendCommandToServer(loginCommand);
                   
                } else if(commandPrompt.equals("checkstatementcommand received")){
                            System.out.println(" Enter  StartDate of checkstatement(YY-MM_DD)  ");
                                 String dateFrom = readUserInput();
                                 System.out.println(" Enter EndDate of checkstatement(YY-MM_DD)  ");
                                 String dateTo = readUserInput();
                                 String loginCommand = "checkstatement" + " " + dateFrom + " " + dateTo;
                                 sendCommandToServer(loginCommand);

                }
                
                else if (commandPrompt.equals("SessionTermination!")) {
                    isLoggedIn = true;
                    System.out.println("Logging out. Exiting...");
                    break;
                } else {

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
        // System.out.println( serverResponse);

         if (serverResponse.equals("logincommand received")) {
            System.out.println("Please Enter  username ! ");
            String username = readUserInput();
            System.out.println(" Enter  password  ");
            String password = readUserInput();
            String loginCommand = "login" + " " + username + " " + password;
            sendCommandToServer(loginCommand);

            return true;
        } else if (serverResponse.equals("registercommand received")) {
            System.out.println("Please Enter username: ");
            String username = readUserInput().trim(); // Trim leading and trailing whitespaces
            if (username.isEmpty()) {
                System.out.println("Error: Username cannot be empty.");
                return false;
            }
        
            System.out.println("Enter password with a minimum of 6 characters, including letters and numbers: ");
            String password = readUserInput();
            if (!isValidPassword(password)) {
                System.out.println("Error: Invalid password format.");
                return false;
            }
        
            System.out.println("Enter a valid email address: ");
            String email = readUserInput().trim();
            if (!isValidEmail(email)) {
                System.out.println("Error: Invalid email address.");
                return false;
            }
        
            System.out.println("Enter a valid phone number: ");
            String telephone = readUserInput().trim();
            if (!isValidPhoneNumber(telephone)) {
                System.out.println("Error: Invalid phone number format.");
                return false;
            }
        
            String loginCommand = "register" + " " + username + " " + password + " " + email + " " + telephone;
            sendCommandToServer(loginCommand);
            return true;
        }
         else if (serverResponse.startsWith("Registration successful!")) {
            System.out.println(serverResponse + "\n");
            String commandAfterInvalidLogin = readUserInput();
            sendCommandToServer(commandAfterInvalidLogin);
            return true;
        }  
         else if (serverResponse.equals("SessionTermination!")) {                    
                    System.out.println("Logging out. Exiting...");                   
                    return false;
                }     
              
        else {
            System.out.println(serverResponse);
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
        System.out.println(" \n *** System Commands Available ***\n" +
                "** please press enter key after inputing **\n" +
                " ****                             *\n" +
                "*         ******* login *******    *\n" +
                "*           ****** deposit ******    *\n" +
                "*          ***** CheckStatement ***** *\n" +
                "*         **** LoanRequestStatus **** *\n" +
                "*             *****  register   *    *\n" +
                "*             ***  loanAccept   ***   *\n" +
                "*             **  loanReject   ******  *\n" +
                "*        ******* loanDeposit  ********** *\n" +
                "*      ********* logout      *************  *\n" +
                "***********************************************");
    }


    
    
    private static boolean isValidPassword(String password) {
        // Password should have a minimum of 6 characters and contain at least one letter and one number.
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
    
}
