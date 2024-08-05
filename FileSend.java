import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Handles file uploads for MultiThreadedClient.
 */
public class FileSend implements Runnable {
    String fileName;
    String username;
    int port;

    // Constructor
    public FileSend(String fileName, String username, int port) throws Exception {
        this.fileName = fileName;
        this.username = username;
        this.port = port;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Method for processing client's file upload requests
    private void processRequest() throws Exception {

        // This line will be sent to the task balancer to signify that this is a client
        String tbLine;
        // This line will hold the address for file transfer returned by the task
        // balancer
        String storageAddress;

        // create TCP connection to task balancer, an output stream to send to the task
        // balancer, an input stream to read from the
        // task balancer
        Socket taskBalancerSocket = new Socket("127.0.0.1", port);
        DataOutputStream outToServer = new DataOutputStream(taskBalancerSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(taskBalancerSocket.getInputStream()));

        // Sending this will tell that the request is sent by a client and is waiting
        // for a worker's address
        tbLine = "file sending request";
        outToServer.writeBytes(tbLine + "\n");

        // Read the reponse of the task balancer, store it, print it, and close the
        // connection with the task balancer
        storageAddress = inFromServer.readLine();
        System.out.println("RECEIVED FROM TASK BALANCER: " + storageAddress);
        taskBalancerSocket.close();

        // tokenizer to get values from the response by the task balancer
        StringTokenizer tokens = new StringTokenizer(storageAddress);

        // extract IP and port number of the storage server
        String storageIP = tokens.nextToken();
        String storagePort = tokens.nextToken();

        // print out extracted values and the fileName to be sent
        System.out.println("IP: " + storageIP);
        System.out.println("Port: " + storagePort);
        System.out.println("File name: " + fileName);
        System.out.println("-----------------------------------------");

        // TCP to send the file
        Socket storageSocket = new Socket(storageIP, Integer.parseInt(storagePort));
        outToServer = new DataOutputStream(storageSocket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(storageSocket.getInputStream()));

        String WSLine = "client transfer request";
        outToServer.writeBytes(WSLine + "\n");

        outToServer.writeBytes(username + " " + fileName + "\n");
        String sendStatus = inFromServer.readLine();
        System.out.println("RECEIVED FROM STORAGE SERVER: " + sendStatus);

        sendFile(("./clientFiles/" + fileName), outToServer);

        sendStatus = inFromServer.readLine();
        System.out.println("RECEIVED FROM STORAGE SERVER: " + sendStatus);
        System.out.println("-----------------------------------------");

        storageSocket.close();

    }
    // Method for sending files from client to storage server
    private static void sendFile(String path, DataOutputStream dataOutputStream) throws Exception {
        int bytes = 0;
        File file = new File(path);
        FileInputStream fileInputStream = new FileInputStream(file);

        // send file size
        dataOutputStream.writeLong(file.length());
        // break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer)) != -1) {
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        fileInputStream.close();
    }
}
