package cs455.nfs.tree;


/**
 * Class to implement a generic tree
 * @author Theresa Wellington 
 * Spring 2012
 * @param <E> the type of data to store
 */
public class Tree<E>{
	/**
	 * the root of the tree
	 */
	private TreeNode<E> root;
	
	/**
	 * Constructor creates a new Tree Object with an initial root;
	 * @param root the initial root
	 */
	public Tree(TreeNode<E> root){
		this.root = root;
	}	
	
	/**
	 * returns the root
	 * @return the root
	 */
	public TreeNode<E> getRoot(){
		return root;
	}
	

	/**
	 * find a node given the path
	 */
	public TreeNode<E> findNode(String path, TreeNode<E> currNode){
		//split apart the path
		String[] pathArray = path.split("/");
		//walk through the path
		for(String pathPart:pathArray){
			//check if the pathPart indicates moving up a level in the tree
			if(pathPart.equals("..")){
				currNode = currNode.getParent();
			}
			else if(pathPart.equals("")){
				//do nothing with it
			}
			else{
				//check each child in each step to see if the directory matches
				boolean childFound = false;
				if(currNode!=null){
					for(TreeNode<E> child:currNode.getChildren()){
						childFound = false;
						//pathPart matched to tree node
						if(child.getData().toString().equals(pathPart)){
							currNode = child;
							childFound = true;
							//break out of this second for loop
							break;
						}
					}
				}
				//if no child is found for this pathPart, return null because the path is invalid
				if(!childFound) return null;
			}
		}
		return currNode;
	}
	
	/**
	 * returns the TreeNode matching the path
	 * @param path
	 * @return
	 */
	public TreeNode<E> findNode(String path){
		return this.findNode(path,root);
	}
	
	/**
	 * Move a child node to a newParent
	 */
	public void moveChild(TreeNode<E> child, TreeNode<E> newParent){
		TreeNode<E> originalParent = child.getParent();
		child.setParent(newParent);
		newParent.addChild(child);
		originalParent.removeChild(child);
	}

}
