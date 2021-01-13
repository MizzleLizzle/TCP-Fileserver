import java.net.*;
import java.io.*;


public class Client {
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) { // this function attempts to connect to the server.
        try {
            this.clientSocket = new Socket(ip, port);
            this.out = new PrintWriter(clientSocket.getOutputStream(), true); // connect an outputstream so we can send messages
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // connect an inputstream so we can receive them
            System.out.println("Successfully connected to " + ip + " at port: " + port); // just a message for the user.
        } catch (Exception e) {
            System.out.println(e);
            System.out.println("You probably forgot to start the server");
            System.exit(1);
        }
    }

    public void sendMessage(String message) { // the main function for sending and receiving messages
        if (message.equalsIgnoreCase("QUIT")) { // this doesn't actually send anything, as we don't want to close the server, just the client 
            stopConnection();
        } else if(message.equalsIgnoreCase("LIST")) { // the LIST command
            try {
                String response = "";
                out.println(message); // pass on the command to the server
                String[] files = in.readLine().split(","); // got some reformatting to do bc readline stops at the CR-mark and we don't want that (CR= Carriage return, means line break)
                for (String name : files) {
                    response += name + "\n"; // basically just replacing our regex with a CR mark
                }
                System.out.print(response); // telling the user
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if(message.substring(0,3).equalsIgnoreCase("GET")) { // the main function for receiving actual files
            if(message.contains(" ")) { // checking if the user typed the command correctly
                getFile(message); // it was so big I put it into its own function for better readability
            } else {
                System.out.println("Please add the name of the File you want!");
            }
        } else { // default case
            System.out.println("Unknown Command");
        }
    }

    public void getFile(String message) {
        String fileName = message.split(" ")[1]; // split the input into command ([0]) and argument or file name ([1])
        System.out.println("Getting file " + fileName); // letting the user know
        try {
            out.println(message); // passing the message on to the server
            if (in.readLine().equals("TRUE")) { // the server first tells us if the file exists
                DataInputStream input = new DataInputStream(clientSocket.getInputStream()); // we're sending the file via a bytearray, so we need a special kind of input
                int length = input.readInt(); // the server first tells us the length of the file which we need to write it later
                if(length > 0) {
                    byte[] fileContent = new byte[length]; 
                    input.readFully(fileContent, 0, length); // read the content into new file
                    System.out.println("File received! Saving File to current Directory");
                    try (FileOutputStream fileOutput = new FileOutputStream(new File(fileName))) {
                        fileOutput.write(fileContent); // saving the file
                        System.out.println("File " + fileName + " saved."); // letting the user know
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("This File doesn't exist!"); // this gets called in case the server told us that the file doesn't exist (or anything else, really)
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopConnection() { // the function to stop the client. It just closes all the datastreams.
        try {
            in.close();
            out.close();
            clientSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
