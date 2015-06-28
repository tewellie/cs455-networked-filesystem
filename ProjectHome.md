A network file system written for an upper division computer science course in April 2012. There are two main classes, ClientModule.java and DirectoryService.java. The client module interacts with multiple directory services to create a virtual file system on the local machine. The client module supports three distinct functions: mounting directory/file structures, traversing these directory structures, and initiating the physical relocation of files between two different directory structures. Supported commands while running ClientModule.java:
vmkdir
vrm
vcd
vls
vpwd
peek DirService\_host\_IP DirService\_portnum
vmount DirService\_host\_IP DirService\_portnum
vmv directory/.../file.txt directory/X

The files physically reside on remote machines running DirectoryService. Exactly one DS runs on a given machine, but multiple DS's exist in the system. The DirectoryService is responsible for retrieval of the directory structure and transfer of files to a file system that is managed by a DS on a different machine.

Running from the command line:
java cs455.nfs.client.ClientModule
java cs455.nfs.remote.DirectoryService portnum