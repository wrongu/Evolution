package utils;

import java.util.ArrayList;

public class Tree<T> {
	private T data;
	private Tree<T> parent;
	private ArrayList<Tree<T>> children;
	
	public Tree(T data){
		this(data, null);
	}
	
	public Tree(T data, Tree<T> parent){
		this.data = data;
		this.parent = parent;
		this.children = new ArrayList<Tree<T>>();
	}
	
	public void setParent(Tree<T> newParent){
		parent = newParent;
	}
	
	public void addChild(Tree<T> child){
		children.add(child);
	}
	
	public T value(){
		return data;
	}
	
	public boolean isRoot(){
		return (parent == null);
	}
	
	public boolean isLeaf(){
		return (children.size() == 0);
	}
	
	/**
	 * returns the parent of the given position, or 'null' if position is the root.
	 */
	public Tree<T> getParent(){
		return parent;
	}
	
	public Tree<T> search(T obj){
		// move to root
		Tree<T> root = this;
		while(!root.isRoot()) root = root.getParent();
		// start search
		return dfs(root, obj);
	}
	
	private Tree<T> dfs(Tree<T> node, T obj){
		if(node.value().equals(obj)) return node;
		else{
			for(Tree<T> child : node.children){
				Tree<T> searchresult = dfs(child, obj);
				if(searchresult != null) return searchresult;
			}
		}
		return null;
	}
	
	public Tree<T> getChild(int child){
		if(child >= 0 && child < children.size()) return children.get(child);
		else return null;
	}
}
