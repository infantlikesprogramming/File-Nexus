import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.StringTokenizer;

/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Handles file reception and storage on the StorageServer
 */

public class FileReceive implements Runnable {

    // this field helps us send request to the right worker server
    int workerID;

    // Constructor
    public FileReceive(int workerID) throws Exception {
        this.workerID = workerID;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Method for processing file reception and storage
    private void processRequest() throws Exception {

        // TCP to receive the file
        Socket storageSocket = new Socket("127.0.0.1", 7000 + workerID);
        DataInputStream is = new DataInputStream(storageSocket.getInputStream());
        DataOutputStream os = new DataOutputStream(storageSocket.getOutputStream());

        // Set up response reader
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // construct the request line to send to the worker server
        String WSLine = "storage transfer request";
        // send the request line to the worker server
        os.writeBytes(WSLine + "\n");

        // read the status from the worker server
        String sendStatus = br.readLine();
        System.out.println("RECEIVED FROM WORKER SERVER: " + sendStatus);

        // String tokenizer to extract file name and file path. The send status should
        // have the format "sending: username filename"
        StringTokenizer tokens = new StringTokenizer(sendStatus);
        tokens.nextToken();

        // Extract the username
        String username = tokens.nextToken();

        // if the username is none, the worker has no file to sent
        if (username.equals("none")) {
            System.out.println("No file to receive");
            // otherwise, we had the username, continue to extract the file name
        } else {
            String filename = tokens.nextToken();

            // create a relative file path to store the file
            String relPath = "/" + username + "/" + filename;
            System.out.println("Receiving file ...: " + relPath);

            // create a file path to receive the file
            String filePath = "./storage" + relPath;
            String dirPath = "./storage" + "/" + username;

            // receive the file
            receiveFile(filePath, is, dirPath);

            // receive a confirmation that the file has been sent
            sendStatus = br.readLine();
            System.out.println("RECEIVED FROM WORKER SERVER: " + sendStatus);
            System.out.println("-----------------------------------------");
        }

        // close connection
        storageSocket.close();

    }

    // method for receive and saving file through TCP
    private static void receiveFile(String fileName, DataInputStream dataInputStream, String dirPath) throws Exception {
        int bytes = 0;

        // create a directory if there has not been one for the fileName
        File theDir = new File(dirPath);
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        System.err.println(fileName);

        // creating stream to store file
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = dataInputStream.readLong(); // read file size
        System.err.println("file size: " + size);

        // read from input stream using input stream and write to file using file stream
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes; // read upto file size
        }
        fileOutputStream.close();
    }
}
