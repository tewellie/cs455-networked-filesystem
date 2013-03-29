package cs455.nfs.client;

/**
 * 
 * @author Theresa Wellington
 * April 2012
 * Class to store information about a Directory Service
 *
 */
public class DirectoryServiceInfo {
	private String host;
	private int portNumber;
	
	DirectoryServiceInfo(String host, int port){
		this.host = host;
		portNumber = port;
	}
	
	public Boolean equals(DirectoryServiceInfo other){
		if(host.equals(other.host)){
			return true;
		}
		else return false;
	}
	
	public String getHost(){
		return host;
	}
	
	public int getPort(){
		return portNumber;
	}
}
