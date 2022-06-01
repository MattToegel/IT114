package Module3;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import Module3.TreePrinter.PrintableNode;

/*
From: https://www.baeldung.com/java-binary-tree and https://www.edureka.co/blog/java-binary-tree 
*/
public class BinaryTree {
    static class Node implements PrintableNode {
        int value;
        Node left, right;

        Node(int value) {
            this.value = value;
            left = null;
            right = null;
        }

        @Override
        public PrintableNode getLeft() {
            // TODO Auto-generated method stub
            return left;
        }

        @Override
        public PrintableNode getRight() {
            // TODO Auto-generated method stub
            return right;
        }

        @Override
        public String getText() {
            // TODO Auto-generated method stub
            return value + "";
        }
    }

    public void insert(Node node, int value) {
        if (value < node.value) {
            if (node.left != null) {
                insert(node.left, value);
            } else {
                System.out.println(" Inserted " + value + " to left of " + node.value);
                node.left = new Node(value);
            }
        } else if (value > node.value) {
            if (node.right != null) {
                insert(node.right, value);
            } else {
                System.out.println("  Inserted " + value + " to right of "
                        + node.value);
                node.right = new Node(value);
            }
        }
    }

    public void traverseInOrder(Node node) {
        if (node != null) {
            traverseInOrder(node.left);
            System.out.print(" " + node.value);
            traverseInOrder(node.right);
        }
    }

    public void traversePreOrder(Node node) {
        if (node != null) {
            System.out.print(" " + node.value);
            traversePreOrder(node.left);
            traversePreOrder(node.right);
        }
    }

    public void traversePostOrder(Node node) {
        if (node != null) {
            traversePostOrder(node.left);
            traversePostOrder(node.right);
            System.out.print(" " + node.value);
        }
    }

    public void traverseLevelOrder(Node root) {
        if (root == null) {
            return;
        }

        Queue<Node> nodes = new LinkedList<>();
        nodes.add(root);

        while (!nodes.isEmpty()) {

            Node node = nodes.remove();

            System.out.print(" " + node.value);

            if (node.left != null) {
                nodes.add(node.left);
            }

            if (node.right != null) {
                nodes.add(node.right);
            }
        }
    }

    public static void main(String args[]) {
        BinaryTree tree = new BinaryTree();

        System.out.println("Binary Tree Example");
        Node root = new Node(0);//init for now, but it'll be overwritten, this is to get rid of compiler errors
        String example = args.length >= 1 ? args[0] : "baeldung";
        if (example.equalsIgnoreCase("edureka")) {
            // data from edureka article
            root = new Node(5);
            System.out.println("Building tree with root value " + root.value);
            tree.insert(root, 2);
            tree.insert(root, 4);
            tree.insert(root, 8);
            tree.insert(root, 6);
            tree.insert(root, 7);
            tree.insert(root, 3);
            tree.insert(root, 9);
        } else if (example.equalsIgnoreCase("baeldung")) {
            root = new Node(6);
            System.out.println("Building tree with root value " + root.value);
            // data from baeldung article
            tree.insert(root, 4);
            tree.insert(root, 8);
            tree.insert(root, 3);
            tree.insert(root, 5);
            tree.insert(root, 7);
            tree.insert(root, 9);

        } else {
            // custom
            if (example.contains(",")) {
                String[] data = example.split(",");
                for (int i = 0; i < data.length; i++) {
                    try {
                        int t = Integer.parseInt(data[i].trim());
                        if (i == 0) {
                            root = new Node(t);
                        } else {
                            tree.insert(root, t);
                        }
                    } catch (Exception e1) {
                        //ignoring NaN
                    }
                }
            }
            else{
                System.out.println("Unsupported custom value. Make sure it's a comma separated list without space.");
                System.out.println("If you plan to use spaces wrap the entire list in quotes");
            }
        }
        TreePrinter.print(root);
        System.out.println("DFS Traversing tree in order");
        tree.traverseInOrder(root);
        System.out.println("");
        System.out.println("DFS Traversing tree pre order");
        tree.traversePreOrder(root);
        System.out.println("");
        System.out.println("DFS: Traversing tree post order");
        tree.traversePostOrder(root);
        System.out.println("");
        System.out.println("BFS: Traversing tree level order");
        tree.traverseLevelOrder(root);
        System.out.println("");

    }
}

// From: https://stackoverflow.com/a/29704252
class TreePrinter {
    /** Node that can be printed */
    public interface PrintableNode {
        /** Get left child */
        PrintableNode getLeft();

        /** Get right child */
        PrintableNode getRight();

        /** Get text to be printed */
        String getText();
    }

    /**
     * Print a tree
     * 
     * @param root
     *             tree root node
     */
    public static void print(PrintableNode root) {
        List<List<String>> lines = new ArrayList<List<String>>();

        List<PrintableNode> level = new ArrayList<PrintableNode>();
        List<PrintableNode> next = new ArrayList<PrintableNode>();

        level.add(root);
        int nn = 1;

        int widest = 0;

        while (nn != 0) {
            List<String> line = new ArrayList<String>();

            nn = 0;

            for (PrintableNode n : level) {
                if (n == null) {
                    line.add(null);

                    next.add(null);
                    next.add(null);
                } else {
                    String aa = n.getText();
                    line.add(aa);
                    if (aa.length() > widest)
                        widest = aa.length();

                    next.add(n.getLeft());
                    next.add(n.getRight());

                    if (n.getLeft() != null)
                        nn++;
                    if (n.getRight() != null)
                        nn++;
                }
            }

            if (widest % 2 == 1)
                widest++;

            lines.add(line);

            List<PrintableNode> tmp = level;
            level = next;
            next = tmp;
            next.clear();
        }

        int perpiece = lines.get(lines.size() - 1).size() * (widest + 4);
        for (int i = 0; i < lines.size(); i++) {
            List<String> line = lines.get(i);
            int hpw = (int) Math.floor(perpiece / 2f) - 1;

            if (i > 0) {
                for (int j = 0; j < line.size(); j++) {

                    // split node
                    char c = ' ';
                    if (j % 2 == 1) {
                        if (line.get(j - 1) != null) {
                            c = (line.get(j) != null) ? '┴' : '┘';
                        } else {
                            if (j < line.size() && line.get(j) != null)
                                c = '└';
                        }
                    }
                    System.out.print(c);

                    // lines and spaces
                    if (line.get(j) == null) {
                        for (int k = 0; k < perpiece - 1; k++) {
                            System.out.print(" ");
                        }
                    } else {

                        for (int k = 0; k < hpw; k++) {
                            System.out.print(j % 2 == 0 ? " " : "─");
                        }
                        System.out.print(j % 2 == 0 ? "┌" : "┐");
                        for (int k = 0; k < hpw; k++) {
                            System.out.print(j % 2 == 0 ? "─" : " ");
                        }
                    }
                }
                System.out.println();
            }

            // print line of numbers
            for (int j = 0; j < line.size(); j++) {

                String f = line.get(j);
                if (f == null)
                    f = "";
                int gap1 = (int) Math.ceil(perpiece / 2f - f.length() / 2f);
                int gap2 = (int) Math.floor(perpiece / 2f - f.length() / 2f);

                // a number
                for (int k = 0; k < gap1; k++) {
                    System.out.print(" ");
                }
                System.out.print(f);
                for (int k = 0; k < gap2; k++) {
                    System.out.print(" ");
                }
            }
            System.out.println();

            perpiece /= 2;
        }
    }
}