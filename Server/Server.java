import java.io.*;
import java.net.*;
import java.nio.file.Files;


public class Server {
    private ServerSocket serverSocket;

    public static void main(String[] args) { // the main function just constructs and starts the server at port 50001
        Server server = new Server();
        server.start(50001);
        
    }

    public void start(int port) {
        try {
            serverSocket = new ServerSocket(port); // initializing a new Serversocket.
            System.out.println("Server accepting clients at port " + port);
            while (true) {
                new ClientHandler(serverSocket.accept()).start(); // it will wait at .accept() until a client connects, then start a new thread with a clientsocket for that client.
            } // the .start() at the end here is inherited from the java Thread class and sets up the thread + calls the run() function.
        } catch (Exception e) {
            e.printStackTrace();
        }   
    }

    public void stop() { // this never actually gets called. just here for good measure
        try {
            serverSocket.close(); // if it were called, it would close the server program.
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread { // this class does the talky bit, kind of like a PR manager
        private Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket) { // construct the PR manager
            this.clientSocket = socket;
        }

        public void run() {
            try {
                System.out.println("Accepted new Connection");
                out = new PrintWriter(clientSocket.getOutputStream(), true); // set up datastreams for input and output to the client
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            
                String inputLine;
                while (!(inputLine = in.readLine()).equalsIgnoreCase("QUIT")) { // read the inputstream again and again, until it is "QUIT" (which never happens)
                    if (inputLine.equalsIgnoreCase("LIST")) {
                        out.println(listOfFiles()); // list the Files in the Files directory
                        System.out.println("Listed files."); // letting the user know
                    } else if (inputLine.substring(0,3).equalsIgnoreCase("GET")) { // we only want to know the command here, not the filename
                        String filename = inputLine.split(" ")[1];
                        sendFile(filename); // here wo only need the filenam itself
                    }
                    else {
                        out.println("Unknown command"); // default case, bc theoretically most commands the server doesn't know. 
                        System.out.println("Unknown command"); // like "Bake me cake" for example. The server doesn't know how to do that. what a shame.
                    }
                }

            in.close();
            out.close();
            clientSocket.close();
            } catch (Exception e) { // this happens when the client program is closed.
                e.printStackTrace();
                System.out.println("^ that probably means that the client was closed."); // it does!
            }
        }

        public String listOfFiles() { // list all available files in Files directory
            File folder = new File("./Files");
            File[] listOfFiles = folder.listFiles(); // put all found files into an array
            String files = new String();
            for (int i = 0; i < listOfFiles.length; i++) {
                files += listOfFiles[i].getName() + ","; // attach file + a regex to later add CR-marks
            }
            return files.substring(0,files.length()-1); // we don't want the last comma as that would look stupid in the output.
        }

        public void sendFile(String filename) { // the main function for sending a file
            if (listOfFiles().contains(filename)) {
                try {
                    out.println("TRUE"); // letting the client know if the file they want actually exists
                    System.out.println("sending File: " + filename); // letting the user know
                    File file = new File("./Files/"+filename);
                    byte[] fileContent = Files.readAllBytes(file.toPath()); // converting the fileContent to a byte array
                    DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
                    output.writeInt((int)file.length()); // telling the client how long the file is
                    output.write(fileContent); // and finally the content itself
                    System.out.println("File " + filename + " sent!"); // letting the user know
                } catch (Exception e) {
                    e.printStackTrace();
                } 
            } else {
                out.println("FALSE"); // the file doesn't exist.
                System.out.println("File " + filename + " doesn't exist!");
            }

        }
    }
}