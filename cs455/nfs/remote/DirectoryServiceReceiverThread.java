package cs455.nfs.remote;

import java.io.*;
import java.net.*;

import cs455.nfs.tree.TreeNode;

/**
 * @author Theresa Wellington
 * April 2012
 * Waits for input from a connect ClientModule or another DirectoryService & responds to requests
 *
 */
public class DirectoryServiceReceiverThread extends Thread {

	private DirectoryService directoryService;
	private Socket socket;
	
	DirectoryServiceReceiverThread(DirectoryService ds, Socket s){
		socket = s;
		directoryService = ds;
	}


	public void start() {

		//wait for commands from socketInputStream
		while(true){
			InputStream socketInputStream = null;
			
			//try to get input from the socket
			try {
				socketInputStream = socket.getInputStream();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			//get the input stream
			DataInputStream din = new DataInputStream(socketInputStream);
			
			//determine size of message
			int size =0;
			try {
				size = din.available();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//create a buffer
			byte[] buffer = new byte[size];
			//read into the buffer
			try {
				din.read(buffer,0,size);
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			
			if(size>0){
				
				String request = new String(buffer);
				String response = new String();
				
				
				if(request.equals("peek")){
					response = directoryService.treeToString(directoryService.getFileStructure().getRoot(), directoryService.getRootPath(), true);
				}
				else if(request.contains("mount")){
					String[] requestArray = request.split(" ");
					
					if(requestArray[1].equals("/")){
						response = directoryService.treeToString(directoryService.getFileStructure().getRoot(), directoryService.getRootPath() , false);
					}
					else{
						TreeNode<String> startingNode = directoryService.getFileStructure().findNode(requestArray[1]);
						response = directoryService.treeToString(startingNode, directoryService.getRootPath() + "/"  + requestArray[1], false);
					}
				}
				else if(request.contains("mkdir")){
					String[] requestArray = request.split(" ");
					String newDirectory = requestArray[2];
					TreeNode<String> parent = directoryService.getFileStructure().findNode(requestArray[1]);
					TreeNode<String> newNode = new TreeNode<String>(newDirectory, parent);
					//System.out.println(requestArray[1] + " " + requestArray[2] + " parent: " + parent.getData());
					parent.addChild(newNode);
					String pathToNode = directoryService.getRootPath() + "/" + 
						requestArray[1] + "/" + newDirectory;
					new File(pathToNode).mkdir();
					response = "done";
				}
				else if(request.contains("mv")){
					String[] requestArray =request.split(" ");
					//mv d2/d3/../f3 ../d4 pea.cs.colostate.edu 3000
					String startLocation = requestArray[1];
					String endLocation = requestArray[2];
					String destHost = requestArray[3];
					int destPort = Integer.parseInt(requestArray[4]);
					TreeNode<String> movingNode = directoryService.getFileStructure().findNode(startLocation);
					if(movingNode!=null){
						String fullMovingFileName = directoryService.getRootPath()   + startLocation;
						File originalfile = new File(fullMovingFileName);
						//if the move occurs on this machine, just issue a command
						if(destHost.equals(directoryService.getHostName()) && destPort==directoryService.getPort()){
							TreeNode<String> endNode = directoryService.getFileStructure().findNode(endLocation);
							if(endNode!=null){
								String fullDestFileName = directoryService.getRootPath()  + endLocation;
								File destfile = new File(fullDestFileName + "/" + originalfile.getName());
								originalfile.renameTo(destfile);
								//remove from current node and add to new node
								directoryService.getFileStructure().moveChild(movingNode, endNode);
								response= "success";
							}
							else System.out.println("could not find destination");
						}
						//copy file to new node
						else{
							Socket remoteDS = null;
							//check if socket exists already
							if(directoryService.getConnection(destHost) == null){
								//connect to remote host
								
								try {
									remoteDS = new Socket(destHost, destPort);
								} catch (UnknownHostException e) {
									
									e.printStackTrace();
								} catch (IOException e) {
									
									e.printStackTrace();
								}
								directoryService.addDSConnection(destHost, remoteDS);
							}
							
							else{
								remoteDS = directoryService.getConnection(destHost);
							}
							
							
							String moveRequest = "moverequest ";
							moveRequest = moveRequest + endLocation  + "/" + originalfile.getName();
							
							//add file info
							
						      byte [] mybytearray  = new byte [(int)originalfile.length()];
						      FileInputStream fis = null;
							try {
								fis = new FileInputStream(originalfile);
							} catch (FileNotFoundException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						      BufferedInputStream bis = new BufferedInputStream(fis);
						      try {
								bis.read(mybytearray,0,mybytearray.length);
							} catch (IOException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						      
						      String fromFile = new String(mybytearray);
						      moveRequest = moveRequest + "\n" + fromFile;
							
							//generate a request to move a file
						
							OutputStream socketOutputStream = null;
							try {
								socketOutputStream = remoteDS.getOutputStream();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							try {
								byte[] mrByte = moveRequest.getBytes();
								socketOutputStream.write(mrByte);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							//remove file from this structure
							originalfile.delete();
							movingNode.getParent().removeChild(movingNode);
							
							response="done with directory service";
						}
					}
					else System.out.println("could not find node");
					
				}
				else if(request.contains("moverequest")){
					String[] requestArray =request.split("\n");
					//moverequest /dir1/file3.txt
					String requestString = requestArray[0];
					String[] requestStringArray = requestString.split(" ");
					String filePath = requestStringArray[1];
					//create the new file	
					FileWriter fstream = null;
					try {
						fstream = new FileWriter(directoryService.getRootPath() + "/" + filePath);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					  BufferedWriter out = new BufferedWriter(fstream);
					  for(int i=1; i<requestArray.length; i++){
						  try {
							  if(requestArray[i].length()==0){
								  out.newLine();
							  }
							  else{
								  out.write(requestArray[i]);
								  out.newLine();
							  }
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					  }
					  
					  //Close the output stream
					  try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					//need to clean up directory structure still
					String parentPath = filePath.substring(0, filePath.lastIndexOf("/"));
					String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
					TreeNode<String> parent = directoryService.getFileStructure().findNode(parentPath);
					TreeNode<String> newChild = new TreeNode<String>(fileName, parent);
					parent.addChild(newChild);
					
					response ="move successful";
					
				}
				else if(request.contains("remove")){
					String[] requestArray =request.split(" ");
					//moverequest /dir1/file3.txt
					String removePathString = requestArray[1];
					TreeNode<String> removalNode = directoryService.getFileStructure().findNode(removePathString);
					if(removalNode==null){
						System.out.println("error finding node");
					}
					else{
						File removalFile = new File(directoryService.getRootPath()+ "/" + removePathString);
						if(!removalFile.delete()){
							System.out.println("removal failed");
						}
						removalNode.getParent().removeChild(removalNode);
					}
					response = "remove successful";
				}
				else{
					System.out.println("invalid command");
					response = "error";
				}
				
				
				//generate a response for the client module
				OutputStream socketOutputStream = null;
				try {
					socketOutputStream = socket.getOutputStream();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				try {
					byte[] responseByte = response.getBytes();
					socketOutputStream.write(responseByte);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}
		
	}
	
	
}
