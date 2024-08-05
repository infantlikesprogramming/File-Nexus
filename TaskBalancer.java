import java.io.*;
import java.net.*;

/*
 * Authors: Dung Ha & Katie Le
 * COMP 352 Final Project - FileNexus
 * 
 * Server that balances file loads from multiple clients and directs them to the respective worker server.
 */
class TaskBalancer {

    public static void main(String argv[]) throws Exception {

        // this holds the request lines for all connections
        String clientSentence;

        // welcome socket
        ServerSocket welcomeSocket = new ServerSocket(6789);
        System.out.println("Waiting for incoming connection Request...");

        // This helps to decide which worker address to sent back to the file sending
        // client
        int count = 0;

        while (true) {
            Socket connectionSocket = welcomeSocket.accept();

            // reader and output streams for TCP connections
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            // get the client request
            clientSentence = inFromClient.readLine();

            System.out.println("RECEIVED FROM CLIENT: " + clientSentence);
            // if the request is from a client to send file to a worker, assign the file
            // sending requests to the workers evenly
            // and send the worker's addresses back.
            if (clientSentence.equals("file sending request")) {
                if (count % 2 == 0) {
                    outToClient.writeBytes("127.0.0.1 7001\n");
                    System.out.println("127.0.0.1 7001\n");
                } else {
                    outToClient.writeBytes("127.0.0.1 7002\n");
                    System.out.println("127.0.0.1 7002\n");
                }
                count++;
                connectionSocket.close();
            } else {
                outToClient.writeBytes("not a request\n");
            }
        }
    }
}
