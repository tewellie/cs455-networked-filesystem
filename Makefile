# Makefile for NFS

JFLAGS       = -g




default: 
	javac $(JFLAGS) cs455/nfs/client/ClientModule.java
	javac $(JFLAGS) cs455/nfs/client/DirectoryServiceInfo.java
	javac $(JFLAGS) cs455/nfs/client/FileInfo.java
	javac $(JFLAGS) cs455/nfs/remote/DirectoryService.java
	javac $(JFLAGS) cs455/nfs/remote/DirectoryServiceReceiverThread.java
	javac $(JFLAGS) cs455/nfs/remote/LinkReceiverThread.java
	javac $(JFLAGS) cs455/nfs/tree/Tree.java
	javac $(JFLAGS) cs455/nfs/tree/TreeNode.java
	
	
	
all: 
	javac $(JFLAGS) cs455/nfs/client/ClientModule.java
	javac $(JFLAGS) cs455/nfs/client/DirectoryServiceInfo.java
	javac $(JFLAGS) cs455/nfs/client/FileInfo.java
	javac $(JFLAGS) cs455/nfs/remote/DirectoryService.java
	javac $(JFLAGS) cs455/nfs/remote/DirectoryServiceReceiverThread.java
	javac $(JFLAGS) cs455/nfs/remote/LinkReceiverThread.java
	javac $(JFLAGS) cs455/nfs/tree/Tree.java
	javac $(JFLAGS) cs455/nfs/tree/Tree.java
	
clean: 
	rm -f *.class *~


