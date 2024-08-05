/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Initiates file uploads and interacts with the Task Balancer to determine the appropriate worker server.
 */
public class MultiThreadedClient2 {
    public static void main(String argv[]) throws Exception {
        // Task balancer server port
        int TBport = 6789;
        // File name to send file
        String fileName = "";
        // Username
        String username = "Client_2";
        String[] files = { "red.pdf", "grass.jpeg" , "yellow.txt", "green.txt" };
        // Create 5 threads to send 5 files at a time
        for (int i = 0; i < files.length; i++) {
            fileName = files[i];
            // Construct an object to process the outgoing request
            FileSend request = new FileSend(fileName, username, TBport);

            // Create a new thread to process the request.
            Thread thread = new Thread(request);

            // Start the thread slowly so that we can test the effects.
            Thread.sleep(2000);
            thread.start();
        }
    }
}
