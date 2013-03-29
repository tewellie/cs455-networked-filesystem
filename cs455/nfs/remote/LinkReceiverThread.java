package cs455.nfs.remote;

import java.io.IOException;
import java.net.*;



public class LinkReceiverThread implements Runnable {
	
	private Socket socket;
	private DirectoryService directoryService;
	private ServerSocket server;
	
	LinkReceiverThread(DirectoryService service, ServerSocket server){
		this.server = server;
		directoryService = service;
	}

	@Override
	public void run() {

		//create a socket to listen on the server socket
		socket = null;
		try {
			socket = server.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		//if a socket has been created it means a Router is connected on the socket 
		//spawn a new thread to create another socket to listen for more connection requests
		Thread linkReceiverThread = new Thread(new LinkReceiverThread(directoryService, server));
		linkReceiverThread.start();
		
		//start waiting for messages on this socket
		//thread for listening for communication with discovery node
		DirectoryServiceReceiverThread dsReceiverThread = new DirectoryServiceReceiverThread(directoryService, socket);
		dsReceiverThread.start();
		
	}
	
	

}
