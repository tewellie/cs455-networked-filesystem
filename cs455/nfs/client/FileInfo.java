package cs455.nfs.client;

/**
 * 
 * @author Theresa Wellington
 * April 2012
 * Class to store information about files (or directories)
 */
public class FileInfo {
	
	private DirectoryServiceInfo directoryService;
	private String name;
	private String type; //can be 'f' or 'd'
	private String physicalPath;

	//creates a new fileInfo object
	FileInfo(DirectoryServiceInfo ds, String filename, String type){
		directoryService = ds;
		name = filename;
		this.type = type;
		physicalPath ="";
	}
	
	public DirectoryServiceInfo getDSInfo(){
		return directoryService;
	}
	
	public void setDSInfo(DirectoryServiceInfo ds){
		directoryService = ds;
	}
	
	public String getName(){
		return name;
	}
	
	public String getType(){
		return type;
	}
	
	public void setPhysicalPath(String path){
		physicalPath = path;
	}
	
	public String getPhysicalPath(){
		return physicalPath;
	}
	
	//compares based on fileName
	public boolean equals(FileInfo other){
		if(other.name.equals(this.name)) return true;
		else return false;
	}
	
	//compares fileName to string
	public boolean equals(String other){
		if(other.equals(this.name)) return true;
		else return false;
	}
	
	public String toString(){
		return name;
	}
}
