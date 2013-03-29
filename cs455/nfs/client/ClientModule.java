package cs455.nfs.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.*;
import java.util.*;

import cs455.nfs.tree.*;

/**
 * @author Theresa Wellington
 * April, 2012
 * CS455 HW3 - Networked File System
 */


/**
 * Client module interacts with multiple directory services
 * Constructs a virtual file system on the local machine
 *  by mounting file systems from different remote directory services
 *  The files physically reside on remote machines where the DS manages access to them
 *  Client module must support 3 distinct functions:
 *  	1) mount directory/file structures
 *  	2) traverse these directory structures
 *  	3) initiate the physical relocation of files between two different directory structures
 *  Client module has its own virtual file  system 
 *     configured and managed under the /tmp/wellington/HW3 directory
 * 	   This VFS is managed and interpreted by the client module
 * 	   Potential methods for managing VS:
 * 		 1) in-memory data structures that do not have any footprints on the file system at the client
 * 		 2) a single text file where you record structure of your virtual file system
 * 		 3) replicate DS on your local system at client module (but not maintain copies of files on client side)
 * 		 4) come up with your own scheme
 * 		 Strategy should be simple and effective
 * Metadata associated with individual directories and files within the VFS should include information
 * about the remote directory service that holds the physical directory/file
 * 
 * Support for commands: all commands will be prefaced with the letter v
 * 		vmkdir 	to create directories
 * 		vrm		to remove directories
 * 		vcd		traverse directory structure - should also support vcd .. to move to parent directory
 * 				support for basic vcd <directory>, not commands that span multiple levels
 * 		vls		provides a listing of files and subdirectories that are present in the directory listing (idea - code by colors?)
 * 		vpwd	provides information about cfurrent working directory within file system on client side
 * 		
 * 
 * Interaction between components: 
 * 		peek DirService_host_IP DirService_portnum
 * 			result in retrieval request being sent to targeted DS
 * 			DS should send a response back with a listing of the directory structure 
 * 				and the files that it manages. Print directory structure that was just received
 * 		vcd alpha (navigate to sub-directory alpha(
 * 		vmount DirService_host_IP DirService_portnum A/B
 * 			allows you to load directory structure managed by remote directory service within the VFS
 * 			command issued within a virtual directory, all sub-directories and files of the specified remote-directory 
 * 		vmv directory/.../ file.txt directory/X
 * 			while client specifies relocation of files, actual relocation performed by DS that holds physical file
 * 			relocation request sent to source DS that manages 'physical' file being transferred
 * 			request should include information about:
 * 				1) the source directory service
 * 				2) the directory location within the source directory service
 * 				3) the target directory service
 * 				4) the directory location within the target directory service
 * 			system should account for cases where these transfers involve one or two directory services
 * 			if transfer relates to a relocation within the same directory service, 
 * 				then the DS simply uses the local UNIX command to move files within its directory structure
 * 			if transfer relates to moving file to another DS, source DS will set up a TCP connection to that target DS 
 * 				and request that service to transfer the the file to the right location
 * 			use information on CM about directories in VFS to determine where the file is held
 * 			
 * 			
 */
public class ClientModule {
	//sockets for sending requests
	HashMap<DirectoryServiceInfo, Socket> dsConnections;
	//tree for virtual file system
	Tree<FileInfo> virtualFileSystem;
	TreeNode<FileInfo> currentLocation;
	DirectoryServiceInfo clientInfo;

	//initalize clientModule
	ClientModule(){
		dsConnections = new HashMap<DirectoryServiceInfo, Socket>();
		try {
			clientInfo = new DirectoryServiceInfo(InetAddress.getLocalHost().getHostName(), 5000);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FileInfo rootInfo = new FileInfo(clientInfo, "rootFolder", "d");
		TreeNode<FileInfo> root = new TreeNode<FileInfo>(rootInfo);
		virtualFileSystem = new Tree<FileInfo>(root);
		currentLocation = root;
	}
	
	//create a new connection to a remote directory service
	//or return the DirectoryServiceInfo the previously created service
	private DirectoryServiceInfo createDSInfo(String host, int port){
		DirectoryServiceInfo dsInfo = 
			new DirectoryServiceInfo(host, port);
		
		//make sure it's not already in the system
		for(DirectoryServiceInfo currDS:dsConnections.keySet()){
			if(currDS.equals(dsInfo)) return currDS;
		}
		
		//create connection remote ds
		Socket remoteDS = null;
		try {
			remoteDS = new Socket(host, port);
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//enter ds into hashmap
		dsConnections.put(dsInfo, remoteDS);
		return dsInfo;
	}
	
	//begin adding a remote file structure the VFS
	private void startAddRemoteDirectories(DirectoryServiceInfo remoteInfo, 
			String directories, String remoteRoot){
		//was [\n] - change back if broken
		String[] splitRemote = directories.split("[\n]");
		addRemoteDirectories(remoteInfo, splitRemote, 0, 
				splitRemote.length, currentLocation, remoteRoot);		
	}
	
	//recursively add remote directories
	private void addRemoteDirectories(DirectoryServiceInfo remoteInfo, String[] splitRemote, 
			int start, int end, TreeNode<FileInfo> parent, String remoteRoot){	
		for(int i=start; i<end; i++){
			String type = "f";
			String name = splitRemote[i].replaceAll("\\s","");
			int numbSubFiles = 0;
			if(name.contains("/")){
				type = "d";
				String[] dirSplit = name.split("/");
				name = dirSplit[0];
				numbSubFiles = Integer.parseInt(dirSplit[1]);
			}
			//use spacing to determine parents, etc.
			FileInfo newFile = new FileInfo(remoteInfo, name, type);
			newFile.setPhysicalPath(remoteRoot);
			TreeNode<FileInfo> newNode = new TreeNode<FileInfo>(newFile, parent);
			//add the newFile as a child of the currParent
			parent.addChild(newNode);
			//call recursive bit if it's a directory
			if(newFile.getType().equals("d")){
				addRemoteDirectories(remoteInfo, splitRemote, i+1, i+numbSubFiles+1, newNode, remoteRoot);
				//skip over x number of subs
				i = i+numbSubFiles;
			}
			
		}
		
	}
	
	//if the path is being retrieved for directory creation on a remote machine, format differently
	private String getPath(boolean forDirectoryCreation, TreeNode<FileInfo> currNode){
		TreeNode<FileInfo> parentNode = currNode.getParent();
		String printResults = "";
		//don't want root folder name to show up if in root
		if(!currNode.equals(virtualFileSystem.getRoot()))
				 printResults = currNode.getData().getName();
		while(parentNode!=null){
			//if not root folder at the name and the /
			if( !parentNode.equals(virtualFileSystem.getRoot()) && 
					(!parentNode.getData().getDSInfo().equals(clientInfo) || !forDirectoryCreation)  ){
				printResults = parentNode.getData().getName() + "/" + printResults;
			}
			parentNode = parentNode.getParent();
		}
		//add final slash for root folder (if not for directory creation)
		if(!forDirectoryCreation){
			printResults = "/" + printResults;
		}
		if(forDirectoryCreation){
			
			String physicalPath = currNode.getData().getPhysicalPath();
			printResults =  physicalPath + "/" + printResults;
		}
		return printResults;
	}
	
	private String getPath(boolean forDirectoryCreation){
		return this.getPath(forDirectoryCreation, currentLocation);
	}
	
	

	//sends requests to the specified directory service
	private String requestHandler(DirectoryServiceInfo dsInfo, String requestString){
		Socket remoteDS = dsConnections.get(dsInfo);
		
		//send peek request
		//create an output stream for the socket
		OutputStream socketOutputStream = null;
		try {
			socketOutputStream = remoteDS.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//write the message to the output stream for the socket
		//format for output stream: message type, message size, message
		try {
			byte[] requestBytes = requestString.getBytes();
			socketOutputStream.write(requestBytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//System.out.println("waiting for response " + requestString);
		
		while(true){
			//wait for a response
			InputStream socketInputStream = null;
			
			//try to get input from the socket
			try {
				socketInputStream = remoteDS.getInputStream();
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			
			//get the input stream
			DataInputStream din = new DataInputStream(socketInputStream);
			
			//determine the type of the message
			int size =0;
			try {
				size = din.available();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//create a buffer
			byte[] readbuffer = new byte[size];
			//read into the buffer
			try {
				din.read(readbuffer,0,size);
			} catch (IOException e3) {
				// TODO Auto-generated catch block
				e3.printStackTrace();
			}
			
			if(size>0){
				//System.out.println("response received");
				return new String(readbuffer);
			}
		}
		

	}

	//command line call java cs455.nfs.client.ClientModule
	public static void main(String[] args){
		
		ClientModule clientModule = new ClientModule();
		
		//wait for commands
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		while(true){
			String command = "";
			try {
				command = buffer.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(command.contains("peek")){
				String[] peekContents = command.split(" ");
				if(peekContents.length!=3){
					System.out.println("correct usuage: peek DirService_host_IP DirService_portnum");
				}
				else{
					DirectoryServiceInfo dsInfo = 
						clientModule.createDSInfo(peekContents[1], Integer.parseInt(peekContents[2]));
					String response = clientModule.requestHandler(dsInfo, "peek");
					System.out.println(response);
				}
			}
			else if(command.contains("vmount")){
				String[] mountContents = command.split(" ");
				if(mountContents.length!=3 && mountContents.length!=4){
					System.out.println("correct usuage: peek DirService_host_IP DirService_portnum loadPath");
				}
				else if(!clientModule.currentLocation.getData().getDSInfo().equals(clientModule.clientInfo)){
					System.out.println("Cannot mount in this location. Please renavigate to a virtual folder");
				}
				else{
					String loadPath = "";
					if(mountContents.length == 3){
						loadPath = "/";
					}
					else if(mountContents.length == 4){
						loadPath = mountContents[3];
					}
					//will crash if the portnumber is forgotten because of support for loading entire remote ds
					DirectoryServiceInfo dsInfo = 
						clientModule.createDSInfo(mountContents[1], Integer.parseInt(mountContents[2]));
					
					String response = clientModule.requestHandler(dsInfo, "mount " + loadPath);
					if(response.equals("invalid path")){
						System.out.println(response);
					}
					else {
						if(loadPath.equals("/")) loadPath = "";
						clientModule.startAddRemoteDirectories(dsInfo, response, loadPath);
					}
				}
			}
			//creates mount-point at clientModule or a new directory on remote DS
			else if(command.contains("vmkdir")){
				String[] mkdirContents = command.split(" ");
				if(mkdirContents.length!=2) {
					System.out.println("correct usage: vmkdir directoryName");
				}
				else{
					
					String directoryName = mkdirContents[1];
					FileInfo newDirectory = new FileInfo(clientModule.clientInfo, directoryName, "d");
					//add this directory as a child in the current location
					clientModule.currentLocation.addChild(new TreeNode<FileInfo>(newDirectory, clientModule.currentLocation));
					if(clientModule.currentLocation.getData().getDSInfo() != clientModule.clientInfo){
						newDirectory.setDSInfo(clientModule.currentLocation.getData().getDSInfo());
						 clientModule.requestHandler(clientModule.currentLocation.getData().getDSInfo(), 
								 "mkdir " + clientModule.getPath(true) + " " + directoryName);
					}
				}
			}
			//support for traversing file structure
			else if(command.contains("vcd")){
				String[] vcdContents = command.split(" ");
				if(vcdContents.length!=2) {
					System.out.println("correct usage: vcd directoryName   OR    vcd ..");
				}
				else{
					String directoryName = vcdContents[1];
					//go to parent directory
					if(directoryName.equals("..")){
						TreeNode<FileInfo> parentLocation = clientModule.currentLocation.getParent();
						//if not in root node...
						if(parentLocation!=null){
							clientModule.currentLocation = parentLocation;
						}
						else System.out.println("No such file or directory.");
					}
					else{
						//try to identify correct node to cd to
						Boolean nodeIdentified = false;
						for(TreeNode<FileInfo> currNode:clientModule.currentLocation.getChildren()){
							//if this is the correct node
							if(currNode.getData().getName().equals(directoryName)){
								clientModule.currentLocation = currNode;
								nodeIdentified = true;							
							}
						}
						if(!nodeIdentified){
							System.out.println(directoryName + ": No such file or directory.");
						}
					}
				}
				
			}
			//print subdirectories and files in currentlocation
			else if(command.equals("vls")){
				String printResults = "";
				for(TreeNode<FileInfo> currNode:clientModule.currentLocation.getChildren()){
					if(currNode.getData().getType().equals("d")){
						//prints directories in pink
						printResults = printResults +  "\033[35m" + currNode.getData().getName() + "\033[0m" + "    ";
					}
					else printResults = printResults + currNode.getData().getName() + "    " ;
				}
				if(printResults.length() > 0) System.out.println(printResults);
			}
			//print current path
			else if(command.equals("vpwd")){	
				String printResults = clientModule.getPath(false);
				System.out.println(printResults);
			}
			else if(command.contains("vmv")){
				String[] vmvContents = command.split(" ");
				if(vmvContents.length!=3) {
					System.out.println("correct usage: vmv directory/.../file.txt directory/X");
				}
				else{
					String startPath = vmvContents[1];
					String endPath = vmvContents[2];
					
					TreeNode<FileInfo> movingNode = clientModule.virtualFileSystem.findNode(startPath, clientModule.currentLocation);
					//send request to remote file
					TreeNode<FileInfo> destinationNode = clientModule.virtualFileSystem.findNode(endPath, clientModule.currentLocation);
					
					
	
					if(movingNode == null){
						System.out.println(startPath + ": not a valid location");
					}
					else if(destinationNode == null){
						System.out.println(endPath + ": not a valid location");
					}
					else{
						String remoteStartPath = clientModule.getPath(true,movingNode);
						
						String remoteEndPath = clientModule.getPath(true,destinationNode);
						String[] startPathArray = remoteStartPath.split("/");
						if(clientModule.virtualFileSystem.findNode(remoteEndPath 
								+ "/" + startPathArray[startPathArray.length-1]) != null){
							System.out.println("Error: target file already exists. Choose a different destination");
							continue;
						}
						//requeststring needs remotePath, endPath, othermachine IP, othermachine port
						DirectoryServiceInfo destination = destinationNode.getData().getDSInfo();
						String requestString ="mv " + remoteStartPath + " " + 
							remoteEndPath + " " + destination.getHost() + " " + destination.getPort();
						//System.out.println("entering requestHandler");
						clientModule.requestHandler(movingNode.getData().getDSInfo(), requestString);

						clientModule.virtualFileSystem.moveChild(movingNode, destinationNode);
					}
				}
			}
			else if(command.contains("vrm")){
				String[] vrmContents = command.split(" ");
				if(vrmContents.length!=2) {
					System.out.println("correct usage: vr m directoryName");
				}
				else{
					String dirName = vrmContents[1];
					//get directory
					TreeNode<FileInfo> rmDir = null;
					for(TreeNode<FileInfo>child:clientModule.currentLocation.getChildren()){
						if(child.getData().getName().equals(dirName)){
							rmDir = child;
						}
					}
					if(rmDir == null){
						System.out.println(dirName + ": not a valid location");
					}
					else{
						//if it's just a part of virtual system, just orphan it
						if(rmDir.getData().getDSInfo().equals(clientModule.clientInfo)){
							clientModule.currentLocation.removeChild(rmDir);
						}
						else{
							if(rmDir.getChildren().size() > 0){
								System.out.println("You may only remove empty directories. Please delete other files first");
							}
							else{
								String removalPath = clientModule.getPath(true) + "/"+rmDir.getData().getName();
								String requestString ="remove " + removalPath;
								clientModule.requestHandler(rmDir.getData().getDSInfo(), requestString);
								//still remove it from VFS
								clientModule.currentLocation.removeChild(rmDir);
							}
						}
					}
				}
			}
			else{
				System.out.println("Command not recognized");
			}
		}
	}
	
}
