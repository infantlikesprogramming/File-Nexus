/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Multi-threaded final storage server for storing client's files
 */
public class StorageServer {
    public static void main(String argv[]) throws Exception {
        int workerID = 1;
        int count = 0;
        // keep polling for data from Work Server 1 and Work Server 2 in Round Robin
        // manner
        while (true) {
            if (count % 2 == 0) {
                workerID = 1;
            } else {
                workerID = 2;
            }
            for (int i = 0; i < 5; i++) {
                // Construct an object to poll for files
                FileReceive request = new FileReceive(workerID);

                // Create a new thread to process the request.
                Thread thread = new Thread(request);

                // Start the thread.
                thread.start();
                // Join the thread. The main thread will refrain from continuing unless started
                // threads are finished.
                thread.join();
            }
            count++;
            System.out.println("Finish iter: " + count);
            // wait for 5 seconds before polling from the next worker
            Thread.sleep(5000);
        }
    }
}
