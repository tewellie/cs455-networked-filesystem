Theresa Wellington
CS 455 - networked file system

Classes:
	cs455/nfs/client/ClientModule.java - constructs a VFS, interacting with multiple DS
	cs455/nfs/client/DirectoryServiceInfo.java - maintains information about di
	cs455/nfs/client/FileInfo.java - stores information about files (or directories)
	cs455/nfs/remote/DirectoryService.java - responsible for managing directory structure and assorted files
	cs455/nfs/remote/DirectoryServiceReceiverThread.java  -  waits for input from a connect ClientModule or another DirectoryService & responds to requests
	cs455/nfs/remote/LinkReceiverThread.java -  accepts incoming connections, starting a DirectoryServiceReceiverThread for each connection
	cs455/nfs/tree/Tree.java - creates a generic tree; used to keep track of directory structure on DS and VFS on CM
	cs455/nfs/tree/TreeNode.java - creates a generic TreeNode - modified code from a TreeNode.java class written with Andy Waterman for a CS 200

Other Information:
	vmount - limited to mounting a remote DS into a virtual folder (cannot mount into a folder that is part of another mounted DS)
	vrm  - can delete physical files as well as virtual directories - physical directories must be empty to be removed
	vls - pink indicates folders, white (default color) normal files	
	peek/vmount - there is no error handling for incorrect port numbers, so if the command is issued incorrectly the program will fail to make a connection and crash
	most of the commands do only minimal error checking, so extra spaces in command input or incorrect parameters will result in the command not being recognized, the program won't crash (it will just print an error message) except in the case of incorrect IP/portNumb information