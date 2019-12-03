import java.util.LinkedList;
import java.util.Queue;

public class BinaryTree{
	static Queue<BinaryTree> queue = new LinkedList<BinaryTree>();
	private String self;
	private BinaryTree[] children = new BinaryTree[2];//only two children at most
	public BinaryTree(String name) {
		self = name;
	}
	public void addLeft(BinaryTree tree) {
		children[0] = tree;
	}
	public void addRight(BinaryTree tree) {
		children[1] = tree;
	}
	public BinaryTree getLeft() {
		return children[0];
	}
	public BinaryTree getRight() {
		return children[1];
	}
	public String getName() {
		return self;
	}
	@Override
	public String toString() {
		return self + "[" + (children[0] != null?children[0].getName():"None") + ", "
				+ (children[1] != null?children[1].getName():"None")+"]";
	}
	public static void dfsPreOrder(BinaryTree tree) {
		System.out.println(tree);
		if(tree.getLeft() != null) {
			dfsPreOrder(tree.getLeft());
		}
		if(tree.getRight() != null) {
			dfsPreOrder(tree.getRight());
		}
	}
	//TODO change to true/false
	/*public static String dfsPreOrder_search(BinaryTree tree, String search) {
		if(tree == null) {
			return null;
		}
		System.out.println(tree);
		if(tree.getName().equals(search)) {
			System.out.println("Found: " + search);
			return tree.toString();
		}
		if(tree.getLeft() != null) {
			return dfsPreOrder_search(tree.getLeft(), search);
		}
		if(tree.getRight() != null) {
			return dfsPreOrder_search(tree.getRight(), search);
		}
		
	}*/
	public static void dfsInOrder(BinaryTree tree) {
		if(tree.getLeft() != null) {
			dfsInOrder(tree.getLeft());
		}
		System.out.println(tree);
		if(tree.getRight() != null) {
			dfsInOrder(tree.getRight());
		}
	}
	public static void dfsPostOrder(BinaryTree tree) {
		if(tree.getLeft() != null) {
			dfsPostOrder(tree.getLeft());
		}
		if(tree.getRight() != null) {
			dfsPostOrder(tree.getRight());
		}
		System.out.println(tree);
	}
	public static void bfs(BinaryTree tree) {
		queue.clear();//reset it if we ran it before
		queue.add(tree);
		while(queue.peek() != null) {
			BinaryTree current = queue.poll();
			System.out.println(current);
			if(current.getLeft() != null) {
				queue.add(current.getLeft());
			}
			if(current.getRight() != null) {
				queue.add(current.getRight());
			}
		}
	}
	public static void main(String[] args) {
		
		//level 0
		BinaryTree tree_a = new BinaryTree("A");
		tree_a.addLeft(new BinaryTree("B"));
		tree_a.addRight(new BinaryTree("C"));
		//level 1
		tree_a.getLeft().addRight(new BinaryTree("D"));
		tree_a.getRight().addLeft(new BinaryTree("E"));
		tree_a.getRight().addRight(new BinaryTree("F"));
		//Samples converted from Python from https://www.freecodecamp.org/news/all-you-need-to-know-about-tree-data-structures-bceacb85490c/
		/*
		 * DFS explores a path all the way to a leaf before backtracking and exploring another path.
		 */
		System.out.println("Pre Order");
		dfsPreOrder(tree_a);
		System.out.println("\nIn Order\n");
		dfsInOrder(tree_a);
		System.out.println("\nPost Order\n");
		dfsPostOrder(tree_a);
		System.out.println("\nBFS\n");
		/*
		 * BFS algorithm traverses the tree level by level and depth by depth.
		 */
		bfs(tree_a);
		
		System.out.println("\n Pre-Order Search");
		//System.out.println(dfsPreOrder_search(tree_a, "F"));
	}
}