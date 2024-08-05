import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.StringTokenizer;

/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Manages file reception on the WorkerServer and handles storage server requests.
 */
public class FileTransferRequest implements Runnable {
    private Socket socket;

    // used to order received file
    private int taskID;

    // worker's ID for storing and sending file
    private int workerID;

    // file mapping to access file name's value for sending the file to the final
    // storage
    private static HashMap<Integer, String> fileMap = new HashMap();

    // array to indicate what file (specified by taskID) is available for sending to
    // the storage
    private static int[] fileArray = new int[10000];

    private static String statePath = "";

    // Constructor
    public FileTransferRequest(Socket socket, int taskID, int workerID, HashMap<Integer, String> fileMap,
            int[] fileArray, String statePath) throws Exception {
        this.socket = socket;
        this.taskID = taskID;
        this.workerID = workerID;
        this.fileMap = fileMap;
        this.fileArray = fileArray;
        this.statePath = statePath;
    }

    // Implement the run() method of the Runnable interface.
    public void run() {
        try {
            processRequest();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    // Method for processing file reception and handles storage server requests.
    private void processRequest() throws Exception {
        System.out.println("request thread: " + fileMap.get(412));

        // Get a reference to the socket's input and output streams.
        System.out.println("task id is: " + taskID);
        DataInputStream is = new DataInputStream(socket.getInputStream());
        DataOutputStream os = new DataOutputStream(socket.getOutputStream());

        // Set up input streams
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        // read the request line
        String requestLine = br.readLine();
        System.out.println();
        System.out.println(requestLine);

        // process the request based on the request line's value
        if (requestLine.equals("client transfer request")) {
            processClientRequest(requestLine, br, os, is, workerID, taskID);
        } else if (requestLine.equals("storage transfer request")) {
            processStorageRequest(requestLine, os, workerID, taskID);
        }

        os.close();
        br.close();
        socket.close();
        System.out.println("Socket supposed to be closed");
    }

    // method for receiving and saving file through TCP
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

    // Method for send the next available file
    private static String chooseFile() {
        for (int i = 1; i < 10000; i++) {
            if (fileArray[i] == 1) {
                fileArray[i] = 0;
                System.out.println((i) + ":" + fileMap.get(i));
                return fileMap.get(i);
            }
        }
        return "";
    }

    // Method for receiving transfer request from clients. It will receive a new
    // message of the format "username filename" from the client. It will then
    // extract
    // the message to receive the file. Then it will receive the file
    private static void processClientRequest(String requestLine, BufferedReader br, DataOutputStream os,
            DataInputStream is, int workerID, int taskID) throws Exception {
        // receive a new message of the format "username filename" from the client
        requestLine = br.readLine();

        // new tokenizer to extract relative file path of the incoming file for local
        // storing of the file
        StringTokenizer tokens = new StringTokenizer(requestLine);

        // extract username and filename
        String username = tokens.nextToken();
        String filename = tokens.nextToken();

        // create a relative filename to store the file
        String relPath = "/" + username + "/" + filename;

        // Notice what message it has received from the client
        System.out.println("Received from client: ");
        System.out.println(requestLine);

        // Notice the client that the receiving process has started
        os.writeBytes("receiving: " + relPath + ", task ID is: " + taskID + "\n");

        // Construct a relative filepath and directory path for storing
        String filePath = "./worker" + workerID + relPath;
        String dirPath = "./worker" + workerID + "/" + username;

        // receive and store the file
        receiveFile(filePath, is, dirPath);

        // Use the file map to record a new file that is available for sending to
        // storage
        fileMap.put(taskID, username + " " + filename);

        // Use the file array to record that a new file is available for sending to
        // storage
        fileArray[taskID] = 1;

        // Notice the client that the new file has been received and ready for final
        // storage forwarding
                // store the current state
        storeState(statePath, taskID);
        os.writeBytes("received: " + relPath + "\n");
    }

    private static void processStorageRequest(String requestLine, DataOutputStream os, int workerID, int taskID) throws Exception {
        // Notice what message it has received from the storage
        System.out.println("Received from client: ");
        System.out.println(requestLine);

        // choose the next available file to send to storage
        String fileValue = chooseFile();
        System.out.println("Chosed a file: " + fileValue);
        // if there is a no available file, send a message to tell the storage that.
        if (fileValue.equals("")) {
            os.writeBytes("sending: " + "none" + "\n");
            System.out.println("no file is sent");
            // if there is an available file, send the file to the storage
        } else {
            // notice the storage that the file is being sent
            os.writeBytes("sending: " + fileValue + "\n");
            System.out.println("sent file name ");

            // new tokenizer to extract the file value from the file map. Extract username
            // and filename (the format for file value is "username filename")
            StringTokenizer tokens = new StringTokenizer(fileValue);
            String username = tokens.nextToken();
            String filename = tokens.nextToken();

            // create a relative filename to send with the file
            String relPath = "/" + username + "/" + filename;

            // send the file
            sendFile(("./worker" + workerID + relPath), os);
            System.out.println("sent file ");

                    // store the current state

            // notice the storage that the file has been sent and store the state into local files
            storeState(statePath, taskID);
            os.writeBytes("sent: " + filename + "\n");
        }
    }

    // Store the state of the array and map by writing a big string into each file
    private static void storeState(String statePath, int taskID) throws Exception{
        // path for map file
        String stateMapPath = statePath + "Map";
        BufferedWriter writer = new BufferedWriter(new FileWriter(statePath));
        BufferedWriter mapWriter = new BufferedWriter(new FileWriter(stateMapPath));

        // initiate the big strings
        String stateString = "";
        String mapStateString = "";

        // since we dont use the first index to store the state of availability of a file, use it to record the highest taskID and store it
        if (fileArray[0] < taskID){
            fileArray[0] = taskID;
        }
        stateString = Integer.toString(fileArray[0]) +  " ";

        // this string is only for testing
        String stateString2= "";
        // use this loop to construct the big strings
        for (int i = 1; i< fileArray.length; i++){
            // export the file array into a string
            stateString += Integer.toString(fileArray[i]) + " ";
            // export the map content into a string (use file array values to get keys)
            if (fileArray[i] == 1) {
                mapStateString += Integer.toString(i) + ":" + fileMap.get(i) + "\t";
            }
            // this is for testing 
            if (i < 15) {
                stateString2 += Integer.toString(fileArray[i]) + " ";
            }
        }
        System.out.println(taskID + ": " + stateString2 + " || " + mapStateString);
        // write the strings into files and closes
        writer.write(stateString);
        mapWriter.write(mapStateString);
        writer.close();
        mapWriter.close();
    }

}