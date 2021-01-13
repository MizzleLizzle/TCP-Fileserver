import java.util.Scanner;

public class Main { // the Main Class for the Client
    
    private static String IP = "127.0.0.1"; // loopback-IPv4.
    private static int PORT = 50001; // some port above 50000

    public static void main(String[] args) {   

        System.out.println("Enter HELP for list of possible commands");
        Client client = new Client(); // construct the client that the user will interact with

        client.startConnection(IP, PORT); //attempt the connection

        Scanner input = new Scanner(System.in); // console input
        String inputline;
        while ((inputline = input.nextLine()) != null) {
            if (inputline.equalsIgnoreCase("HELP")) { // catching some extra bits that the client doesn't need to know about (our little secret)
                System.out.println("\"QUIT\" to end programm\n" + "\"LIST\" to get list of available files\n" + "\"GET <filename>\" to transfer specified file");
            } else if (inputline.equalsIgnoreCase("QUIT")) { // in case this was all a bit too much
                client.stopConnection();
                System.out.println("Stopping the Connection..."); // letting the user know
                input.close(); // close the inputstream
                System.exit(1); // close the program
            } else {
                client.sendMessage(inputline); // this makes the client send a message. Documentation in Client.java
            }
        }
    }

}
