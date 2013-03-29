package cs455.nfs.tree;

import java.util.ArrayList;

/**
 * Class to implement a generic tree node
 * Originally written as a binary treenode for cs200 with Andy Waterman and modified for this assignment
 * @author Theresa Wellington & Andy Waterman - Fall 2010
 * updated by Theresa Wellington Spring 2012
 * @param <T> the type of data to hold
 * 
 */
public class TreeNode<T> {
	/**
	 * the data to store in this tree-node
	 */
	private T data;
	/**
	 * this tree-node's parent node
	 */
	private TreeNode<T> parent;
	/**
	 * this tree-node's children
	 */
	private ArrayList<TreeNode<T>> children;
	
	private static int totalNumberSub;
	
	/**
	 * Constructor creates a new TreeNode Object, initializing the given parameters
	 * @param data the data to store
	 * @param parent the node's parent
	 * @param left the node's left child
	 * @param right the node's right child
	 */
	public TreeNode(T data, TreeNode<T> parent){
		this.setData(data);
		this.setParent(parent);
		children = new ArrayList<TreeNode<T>>();
	}

	/**
	 * Constructor creates a new TreeNode Object, initializing the given parameters
	 * @param data the data to store
	 */
	public TreeNode(T data){
		this(data,null);
	}
	
	/**
	 * sets this node's data to newData
	 * @param newData the new data
	 */
	private void setData(T newData){
		data = newData;	
	}
	/**
	 * returns the data stored in this node
	 * @return the data
	 */
	public T getData(){
		return data;
	}
	/**
	 * returns this node's parent node
	 * @return the parent
	 */
	public TreeNode<T> getParent(){
		return parent;
	}
	
	/**
	 * sets this node's parent to newParent
	 * @param newParent the new parent
	 */
	public void setParent(TreeNode<T> newParent){
		parent = newParent;
	}
	
	/**
	 * returns this node's children
	 */
	public ArrayList<TreeNode<T>> getChildren(){
		return children;
	}
	

	/**
	 * returns the number of children that this node has
	 * @return the number of children this node has
	 */
	public int numberOfChildren(){
		return children.size();
	}
	
	/**
	 * adds a child to the children list
	 * @param child to add
	 */
	public void addChild(TreeNode<T> child){
		children.add(child);
	}

	
	/**
	 * removes a child from this node if it exists
	 * @param removeNode the node to remove
	 */
	public void removeChild(TreeNode<T> removeNode){
		children.remove(removeNode);
	}
	
	/**
	 * tests if the node is the tree root
	 * @return true if parent==null
	 */
	public boolean isRoot(){
		if (parent == null) return true;
		else return false;
	}
	
	/**
	 * start a recursive call to determine the number of subdirectories/files a node has
	 */
	public int startSubCount(){
		totalNumberSub = 0;
		totalNumberSubNodes(this);
		return totalNumberSub;
	}
	
	/**
	 * recursively calculates number of subdirectories/files
	 */
	private void totalNumberSubNodes(TreeNode<T> currNode){
		for(TreeNode<T> node:currNode.children){
			totalNumberSub++;
			totalNumberSubNodes(node);
		}

	}

	
}