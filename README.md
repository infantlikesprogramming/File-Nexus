# FileNexus -  COMP 352 Project 

Authors: Dung Ha & Katie Le

## Overview: 

FileNexus is a client-to-server file upload system that employs a task balancer using the TCP protocol. This architecture allows for efficient handling of multiple file uploads simultaneously.

## Project Description:

Client 1, Client 2  -->   Task Balancer   -->   WorkerServer 1, WorkerServer 2   -->   Storage Server

In this system, a multi-threaded client uploads multiple files to a storage server. Before the files are transferred, a task balancer (non-multi-threaded) analyzes the server's load and directs the file to the appropriate worker server, ensuring load balancing and minimizing queuing time. The worker server stores a copy of each uploaded file in the local system based on the order of reception. The storage server, which is multi-threaded, sends requests to the worker server to upload files in a Round Robin manner.

This project supports multiple clients (at least 2) sending files simultaneously.

## Architecture:

### Main classes:

- MultiThreadedClient: Initiates file uploads and interacts with the Task Balancer to determine the appropriate worker server.
- TaskBalancer (Port 6789): Balances file loads from multiple clients and directs them to the respective worker server.
- WorkerServer1 & 2 (Ports 7001 and 7002): Intermediate multi-threaded servers for processing file uploads from clients (server) and transfer requests from the storage server (client).
- StorageServer (Port 7000): Multi-threaded server responsible for handling file transfer requests (accept 5 files per request) and storing files locally.

### Helper classes:

- FileSend: Handles file uploads for MultiThreadedClient.
- FileTransferRequest: Manages file reception on the WorkerServer and handles storage server requests.
- FileReceive: Handles file reception and storage on the StorageServer.

## How to run the project demo

Open the source code folder `/fileNexus` in a code editor (we use VS Code editor for this project). Download the Java Extension for the VS Code. Then the “Run” button will appear to run the classes. Otherwise compile each class by entering the bash command `javac <SourceFileName>.java`, and run each class by using the bash command `java <SourceFileName>.java`. 

Follow these steps:

1. Run the StorageServer.
2. Launch both WorkerServer1 and WorkerServer2.
3. Start the TaskBalancer.
4. Run MultiThreadedClient instances, each configured to send multiple files automatically.
5. Monitor the terminal and newly created folders for client and server activity.

## Extra feature (Updated May 3, 2024)

We've introduced a new feature to enhance the resilience of Worker Servers in managing file transmissions. This feature enables the Worker Servers to maintain the status of file transfers even in the face of network disruptions or server disconnects.

For context, a hash map is currently used by the Worker Servers to contain task IDs and file names of files to be transfered to the Storage Server. This new feature extracts information from this existing hash map to allow the servers to preserve the state of file transmission status. This state information is stored in two text files: 

1. `worker<no>stateMap.txt`: The state map file containing the task ID and file name
2. `worker<no>state.txt`: a transfer status file for the transfer status (1 for pending, 0 for resolved).

Each time the Worker Server receives a new file from a client, it updates the transfer status file and flags whether the file has been successfully transferred. Consequently, the corresponding entry is removed from the state map file once it has been transfered to the Storage Server.

Should a Worker Server experience downtime followed by recovery, it consults these text files to resume transferring any remaining files with a transfer status of 1. This process eliminates the need to restart file transmissions from the beginning, hence saving server's resources.

### How to run this extra feature

Follow these steps:

1. Initiate the TaskBalancer.
2. Start WorkerServer1 and WorkerServer2.
3. Run MultiThreadedClient instances
4. Monitor state files in the /worker1_state and /worker2_state directoriesonce both Worker Servers finish processing all tasks from clients
5. Launch the Storage Server
6. Wait 6 seconds, then halt both Worker Servers to simulate disconnection
7. Review the state files to identify pending tasks that weren't transferred due to disconnection.
8. Restart the Worker Servers and quickly check the state files, where all pending tasks should have been cleared.

