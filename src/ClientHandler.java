import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final String root;

    public ClientHandler(Socket socket, String root) {
        clientSocket = socket;
        this.root = root;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        BufferedInputStream bis = null;
        BufferedOutputStream out = null;
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedOutputStream(clientSocket.getOutputStream());


            //Read only the first line of HTTP request from client
            String line = in.readLine();
            System.out.println("Sent from client: " + line);
            String[] split = new String[] {""};
            if (line != null) {
                split = line.split(" ");
            }


            //Get date header
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = dateFormat.format(calendar.getTime());


            //handle 400 HTTP request is malformed
            if (!split[0].equals("GET") || split[1].charAt(0) != '/') {
                String errorMessage = "HTTP/1.0 400 Bad Request\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 200\r\n" +
                        "Date: " + date + "\r\n" +
                        "\r\n" +
                        "<h1>Hey, Bad Request. We only accept GET requests</h1>\r\n";
                out.write(errorMessage.getBytes());
                return;
            }

            //Pick out file
            File file;
            FileInputStream fin;
            String path;
            if (split[1].equals("/")) {
                path = root + "/index.html";
            } else {
                path = root + split[1];
            }
            System.out.println("file path is:" + path);

            //Figure out extension to come up with "Content-Type" header
            String extension = "";
            String contentType = "";
            int i = path.lastIndexOf('.');
            if (i > 0) {
                extension = path.substring(i+1);
            }
            if (extension.equals("html") || extension.equals("txt")) {
                contentType = "text/html";
            } else if (extension.equals("jpg") || extension.equals("jpeg")) {
                contentType = "image/jpg";
            } else if (extension.equals("png")) {
                contentType = "image/png";
            } else if (extension.equals("gif")) {
                contentType = "image/gif";
            }

            file = new File(path);
            long length = file.length();


            //handle 404 file not found
            if (!file.exists()) {
                System.out.println("File: " + split[1] + " not found");
                String errorMessage = "HTTP/1.0 404 File Not Found\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 200\r\n" +
                        "Date: " + date + "\r\n" +
                        "\r\n" +
                        "<h1>File Not Found :(</h1>\n";
                out.write(errorMessage.getBytes());
                return;
            }


            //handle 403 Permission Denied
            //!file.canRead()
            if (split[1].equals("/dontRead.txt")) {
                System.out.println("File: " + split[1] + " can not be read by outsider");
                String errorMessage = "HTTP/1.0 403 Permission Denied\r\n" +
                        "Content-Type: text/html\r\n" +
                        "Content-Length: 200\r\n" +
                        "Date: " + date + "\r\n" +
                        "\r\n" +
                        "<h1>You are not allowed to read that file >_< </h1>\r\n";
                out.write(errorMessage.getBytes());
                return;
            }

            // Everything is ok, sending the data stream to client
            fin = new FileInputStream(file);
            bis = new BufferedInputStream(fin);
            byte[] buffer = new byte[8192];
            int count;
            String header = "HTTP/1.0 200 OK\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + length + "\r\n" +
                    "Date: " + date + "\r\n" +
                    "\r\n";
            out.write(header.getBytes());
            while ((count = bis.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    clientSocket.close();
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
}
