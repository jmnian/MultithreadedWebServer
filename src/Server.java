import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    public static void main(String[] args) {
        //Make sure command line arguments are correct
        if (args.length == 0) {
            System.out.println("Please specify file root and port number");
        }
        String root = "";
        if (args[0].equals("-document_root")) {
            root = args[1];
        } else {
            System.out.println("Please use '-document_root' to specify your file root");
            return;
        }
        if (!args[2].equals("-port")) {
            System.out.println("Please use '-port' to specify your port");
            return;
        }
        int portNo = Integer.parseInt(args[3]);
        if (portNo < 8000 || portNo > 9999) {
            System.out.println("Please use some port number that's >= 8000 and <= 9999");
            return;
        }
        ServerSocket server = null;
        //Constructing server and keep it up and running
        try {
            server = new ServerSocket(portNo);
            server.setReuseAddress(true);
            System.out.println("Server is up and running, listening to port " + portNo);
            while (true) {
                Socket client = server.accept();
                System.out.println("New Client connected! Client IP: " + client.getInetAddress().getHostAddress());
                // Create thread object for each client
                ClientHandler clientHandler = new ClientHandler(client, root);
                new Thread(clientHandler).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //clean up
            if (server != null) {
                try {
                    server.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
