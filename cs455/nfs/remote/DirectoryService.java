package cs455.nfs.remote;

import java.io.*;
import java.net.*;
import java.util.*;
import cs455.nfs.tree.*;

/**
 * @author Theresa Wellington
 * April, 2012
 * CS455 HW3 - Networked File System
 */

/**
 * Exactly one DS running on a given machine, but multiple DS in the system
 * Responsible for managing directory structure and assorted files contained in directories and sub-directories
 * DS will take /tmp/wellington/HW3 directory as root of the FS; create subdirectories in root directory
 * Responsible for responding to requests from clients. Requests include
 * 		1) retrievals of directory structure
 * 		2) transfer of files to a file system that is managed by a DS on a different machine
 * 
 * Interaction between components - commands executed on client module
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
 */
public class DirectoryService {
	
	private int portNumber;
	private String hostName;
	private final String rootPath = "/tmp/wellington/HW3";
	//The string stored is the filename
	private Tree<String> fileStructure;
	//connections to other directory services
	private HashMap<String, Socket> dsSockets;
	
	DirectoryService(int port){
		portNumber = port;
		//get hostName
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dsSockets = new HashMap<String, Socket>();
	}
	
	//creates the fileStructure tree and adds files to it
	private void setFileStructure(){
		//create a file for root directory
		File rootFile = new File(rootPath);
		//initialize the tree
		TreeNode<String> rootNode = new TreeNode<String>(rootFile.getName());
		fileStructure = new Tree<String>(rootNode);
		//add files
		addFile(rootNode, rootFile);
	}
	
	//recursively adds a file (or directory) to the tree
	private void addFile(TreeNode<String> parent, File parentFile){
		String[] fileList = parentFile.list();
		//for each file in the list
		if(fileList!=null){
			for(String fileName:fileList){
				//create a file based on parent pathname/filename
				File file = new File(parentFile.getPath() + "/" + fileName);
				//create a new TreeNode for the file
				TreeNode<String> node = new TreeNode<String>(fileName, parent);
				//add the new TreeNode as a child of the parent node
				parent.addChild(node);
				//if the file is a directory, make a recursive call to add its subdirectories/files
				if(file.isDirectory()){
					addFile(node, file);
				}
			}
		}
	}
	
	//creates a string representation of the string that can be printed or sent to the client module
	//boolean peek - if set to true, addFileToString formats the string differently
	public String treeToString(TreeNode<String> startNode, String startPath, boolean peek){
		if(startNode==null) return "invalid path";
		String result = new String();
		Boolean first = true;
		//start with children startNode
		for(TreeNode<String> child:startNode.getChildren()){
			String temp = new String();
			//add output from addFileToString method to toString result
			String addFileResult = new String();
			//if else just for display purposes - stops the first thing printed from having a new line
			if(first){
				addFileResult = addFileToString(temp,0,child,startPath, false, peek);
				first = false;
			}
			else
				addFileResult = addFileToString(temp,0,child,startPath, true, peek);
			result = result + addFileResult;
		}
		
		return result;
	}
	


	//adds a file to the end of the output string
	//if peek=false, counts of the subdirectores/files are included for each directory  to assist in traversal on the CM side
	private String addFileToString(String currentOut, int depth, TreeNode<String> node, String currentPath, boolean newLinePrint, boolean peek){
		
		String newLine = System.getProperty("line.separator");
		
		String tab = "   ";

		//add a new line character
		if(newLinePrint){
			currentOut = currentOut + newLine;
		}
		//add tabs for each level of depth
		for(int i=0; i<depth; i++){
			currentOut = currentOut + tab;
		}
		//add file name
		currentOut = currentOut + node.getData();
		//update currentpath
		currentPath = currentPath + "/" + node.getData();
		//if the current file being added is a directory
		if(new File(currentPath).isDirectory()){
			//add a '/' to the output
			currentOut = currentOut + "/";
			if(!peek) currentOut = currentOut + node.startSubCount();
			//recursive call to add subdirectories/files
			for(TreeNode<String> child:node.getChildren()){
				//add the results from the subdirectory/file
				currentOut = addFileToString(currentOut, depth+1, child, currentPath, true, peek);
			}
		}

		//return the result of tree traversal
		return currentOut;
	}
	
	public void addDSConnection(String remoteIP, Socket socket){
		dsSockets.put(remoteIP, socket);
	}
	
	public Socket getConnection(String remoteIP){
		return dsSockets.get(remoteIP);
	}
	
	public Tree<String> getFileStructure(){
		return fileStructure;
	}
	
	public int getPort(){
		return portNumber;
	}
	
	public String getHostName(){
		return hostName;
	}
	
	public String getRootPath(){
		return rootPath;
	}
	
	//command line call java cs455.nfs.remote.DirectoryService portnum
	public static void main(String[] args){
		if(args.length < 1){
			System.out.println("Correct usage: cs455.nfs.remote.DirectoryService portnum");
			System.exit(0);
		}
		DirectoryService directoryService = new DirectoryService(Integer.parseInt(args[0]));
		directoryService.setFileStructure();
		System.out.println("Directory Structure: ");
		System.out.println(directoryService.treeToString(directoryService.fileStructure.getRoot(),directoryService.rootPath, true));

		
		//create ServerSocket to wait for a connection from the cm
		ServerSocket clientModuleSocket = null;
		try {
			clientModuleSocket = new ServerSocket(directoryService.portNumber);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		//create link receiver thread
		Thread linkReceiverThread = new Thread(new LinkReceiverThread(directoryService, clientModuleSocket));
		linkReceiverThread.start();
		
			

		
	}
	
}
