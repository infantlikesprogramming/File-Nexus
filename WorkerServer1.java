import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.*;
import java.util.*;
/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Intermediate multi-threaded server for processing file uploads from clients (server) 
 * and transfer requests from the storage server (client).
 */
public class WorkerServer1 {

    public static void main(String argv[]) throws Exception {
        int workerID = 1;
        // port number for worker.
        int port = 7000 + workerID;
        HashMap<Integer, String> fileMap = new HashMap();
        int[] fileArray = new int[10000];

        // path for array state file. Also initialize file array from the array state file
        String statePath = initFileArray(workerID, fileArray);
        //  Initialize file map from the map state file
        fileMap = initFileMap(workerID);
        // Initialize task ID with file array
        int taskID = fileArray[0];

        // blockingly assigned taskID to organize tasks to be sent to the primary
        // storage server

        // Establish the listen socket.
        ServerSocket socket = new ServerSocket(port);
        // Process incoming requests in an infinite loop.
        while (true) {
            // new task ID
            taskID++;
            // Listen for a TCP connection request.
            Socket connection = socket.accept();
            // Construct an object to process the incoming request
            FileTransferRequest request = new FileTransferRequest(connection, taskID, workerID, fileMap, fileArray, statePath);
            // Create a new thread to process the request.
            Thread thread = new Thread(request);
            // Start the thread.
            thread.start();
        }
    }

    /* This function creates a map state file from scratch if it does not exist. If it does, 
    it puts the value stored from the file into the file map */
    private static HashMap<Integer, String> initFileMap(int workerID) throws Exception{
        HashMap<Integer, String> fileMap = new HashMap();
        // Paths for map state file
        String filePath = "./worker" + workerID +"_state/worker" + workerID + "state";
        String fileMapPath = filePath + "Map";
        String dirPath = "./worker" + workerID + "_state";
        try {
            // Potentially create files and dir
            File theDir = new File(dirPath);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }

            File stateMapFile = new File(fileMapPath);
            if (!stateMapFile.exists()) {
                stateMapFile.createNewFile();
                // initialize the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(fileMapPath));
                //writer.newLine();
                writer.close();
            }

            // Input the content from the file to  the map. Use one array to break the string in the map state file into strings
            // Use another array to break the strings just splitted into value and key pairs
            String[] fileMapArray;
            String[] keyValue;
            Scanner myReaderMap = new Scanner(stateMapFile);
            if (myReaderMap.hasNextLine()) {
                String data = myReaderMap.nextLine();
                // convert data to array
                System.out.println(data);
                fileMapArray = data.split("\t");
                // from in each index, convert the value into key value pair and put them in to a map
                for (int i = 0; i < fileMapArray.length; i++){
                    keyValue = fileMapArray[i].split(":");
                    fileMap.put(Integer.parseInt(keyValue[0]),keyValue[1]);
                }
            }
            myReaderMap.close();
            // return the map
            return fileMap;
        } catch (FileNotFoundException e) {
            System.out.println("A reader error occurred.");
            e.printStackTrace();
        }
        return fileMap;
    }
    /* This function creates an array state file from scratch if it does not exist. If it does, 
    it puts the value stored from the file into the file array */
    private static String initFileArray(int workerID, int[] fileArray) throws Exception{
        // Path to array state file and dir
        String filePath = "./worker" + workerID +"_state/worker" + workerID + "state";
        String dirPath = "./worker" + workerID + "_state";
        try {
            // create dir and file if not exist
            File theDir = new File(dirPath);
            if (!theDir.exists()) {
                theDir.mkdirs();
            }
            
            File stateFile = new File(filePath);
            if (!stateFile.exists()) {
                stateFile.createNewFile();
                // initialize the file
                BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
                writer.write("0");
                writer.newLine();
                writer.close();
            }

            // split the data stored in the array state file into an array
            String[] fileStringArray;
            Scanner myReader = new Scanner(stateFile);
            if (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                // convert data to array
                fileStringArray = data.split(" ");
                // convert the values of indexes into the file array
                for (int i = 0; i < fileStringArray.length; i++){
                    fileArray[i] = Integer.parseInt(fileStringArray[i]);
                }
            }
            myReader.close();

            System.out.println(fileArray[15]);
        } catch (FileNotFoundException e) {
            System.out.println("A reader error occurred.");
            e.printStackTrace();
        }
        return filePath;
    }
}
