// Red-Black Tree Node
class Node {
    int data;
    Node parent;
    Node left;
    Node right;
    int color;
}

// Red-Black Tree
public class RbTree {
    private Node root;
    private Node nullLeaf;
    private int colorFlips = 0;

    // Balance the tree after deletion
    private void fixDelete(Node x) {
        System.out.println("fix delete start for: "+x.data + " - " + x.color);
        Node n;
        while (x != root && x.color == 0) {
            if (x == x.parent.left) {
                n = x.parent.right;
                if (n.color == 1) {
                    n.color = 0;
                    x.parent.color = 1;
                    leftRotate(x.parent);
                    n = x.parent.right;
                }

                if (n.left.color == 0 && n.right.color == 0) {
                    n.color = 1;
                    x = x.parent;
                } else {
                    if (n.right.color == 0) {
                        n.left.color = 0;
                        n.color = 1;
                        rightRotate(n);
                        n = x.parent.right;
                    }

                    n.color = x.parent.color;
                    x.parent.color = 0;
                    n.right.color = 0;
                    leftRotate(x.parent);
                    x = root;
                }
            } else {
                n = x.parent.left;
                if (n.color == 1) {
                    n.color = 0;
                    x.parent.color = 1;
                    rightRotate(x.parent);
                    n = x.parent.left;
                }

                if (n.left.color == 0 && n.right.color == 0) {
                    n.color = 1;
                    colorFlips++;
                    x = x.parent;
                } else {
                    if (n.left.color == 0) {
                        n.right.color = 0;
                        n.color = 1;
                        leftRotate(n);
                        n = x.parent.left;
                    }
                    if (n.color != x.parent.color){
                        colorFlips++;
                    }
                    n.color = x.parent.color;

                    if (x.parent.color != 0){
                        x.parent.color = 0;
                        colorFlips++;
                    }
                    n.left.color = 0;
                    rightRotate(x.parent);
                    x = root;
                }
            }
        }
        x.color = 0;
        System.out.println("fix delete end for: "+x.data + " - " + x.color);
    }

    private void rbTransplant(Node u, Node v) {
        if (u.parent == null) {
            root = v;
        } else if (u == u.parent.left) {
            u.parent.left = v;
        } else {
            u.parent.right = v;
        }
        v.parent = u.parent;
    }

    private void deleteNodeHelper(Node node, int key) {
        Node temp = nullLeaf;
        Node x, y;
        while (node != nullLeaf) {
            if (node.data == key) {
                temp = node;
            }

            if (node.data <= key) {
                node = node.right;
            } else {
                node = node.left;
            }
        }

        if (temp == nullLeaf) {
            System.out.println("Couldn't find key in the tree");
            return;
        }

        y = temp;
        int yOriginalColor = y.color;
        if (temp.left == nullLeaf) {
            x = temp.right;
            rbTransplant(temp, temp.right);
        } else if (temp.right == nullLeaf) {
            x = temp.left;
            rbTransplant(temp, temp.left);
        } else {
            y = maximum(temp.left);
            yOriginalColor = y.color;
            x = y.right;
            if (y.parent == temp) {
                x.parent = y;
            } else {
                rbTransplant(y, y.right);
                y.right = temp.right;
                y.right.parent = y;
            }

            rbTransplant(temp, y);
            y.right = temp.right;
            y.right.parent = y;
            y.color = temp.color;
            colorFlips++;
        }
        if (yOriginalColor == 0) {
            fixDelete(x);
            colorFlips++;  // Increment colorFlips after fixDelete
        }
    }

    private void fixInsert(Node newNode){
        System.out.println("fix insert start for: "+newNode.data + " - " + newNode.color);
        Node uncleNode;
        while (newNode.parent.color == 1) {
            if (newNode.parent == newNode.parent.parent.right) {
                uncleNode = newNode.parent.parent.left; // uncle
                if (uncleNode.color == 1) {
                    uncleNode.color = 0;
                    colorFlips++;
                    newNode.parent.color = 0;
                    colorFlips++;
                    newNode.parent.parent.color = 1;
                    colorFlips++;
                    newNode = newNode.parent.parent;
                } else {
                    if (newNode == newNode.parent.left) {
                        newNode = newNode.parent;
                        rightRotate(newNode);
                    }
                    newNode.parent.color = 0;
                    newNode.parent.parent.color = 1;
                    leftRotate(newNode.parent.parent);
                }
            } else {
                uncleNode = newNode.parent.parent.right; // uncle

                if (uncleNode.color == 1) {
                    uncleNode.color = 0;
                    colorFlips++;
                    newNode.parent.color = 0;
                    colorFlips++;
                    newNode.parent.parent.color = 1;
                    colorFlips++;
                    newNode = newNode.parent.parent; // Move k up
                } else {
                    if (newNode == newNode.parent.right) {
                        newNode = newNode.parent;
                        leftRotate(newNode);
                    }
                    newNode.parent.color = 0;
                    colorFlips++;
                    newNode.parent.parent.color = 1;
                    colorFlips++;
                    rightRotate(newNode.parent.parent);
                }
            }
            if (newNode == root) {
                break;
            }
        }
        if (root.color == 1){
            root.color = 0;
            colorFlips--; // since the color root node changes back to black, reducing the colorFipCount
        }
        System.out.println("fix insert start for: "+newNode.data + " - " + newNode.color);
    }

    private void leftRotate(Node x) {
        System.out.println("rotating left at node: " + x.data + " - " + x.color);
        Node y = x.right;
        x.right = y.left;
        if (y.left != nullLeaf) {
            y.left.parent = x;
        }
        y.parent = x.parent;
        if (x.parent == null) {
            this.root = y;
        } else if (x == x.parent.left) {
            x.parent.left = y;
        } else {
            x.parent.right = y;
        }
        y.left = x;
        x.parent = y;
    }

    private void rightRotate(Node grandParentNode) {
        System.out.println("rotating right at node: " + grandParentNode.data + " - " + grandParentNode.color);
        Node parentNode = grandParentNode.left;
        grandParentNode.left = parentNode.right;
        if (parentNode.right != nullLeaf) {
            parentNode.right.parent = grandParentNode;
        }
        parentNode.parent = grandParentNode.parent;
        if (grandParentNode.parent == null) {
            this.root = parentNode;
        } else if (grandParentNode == grandParentNode.parent.right) {
            grandParentNode.parent.right = parentNode;
        } else {
            grandParentNode.parent.left = parentNode;
        }
        parentNode.right = grandParentNode;
        grandParentNode.parent = parentNode;
    }

    private void insert(int key) {
        Node node = new Node();
        node.parent = null;
        node.data = key;
        node.left = nullLeaf;
        node.right = nullLeaf;
        node.color = 1; // new node must be red

        Node y = null;
        Node x = this.root;

        while (x != nullLeaf) {
            y = x;
            if (node.data < x.data) {
                x = x.left;
            } else {
                x = x.right;
            }
        }

        node.parent = y;
        if (y == null) {
            root = node;
        } else if (node.data < y.data) {
            y.left = node;
        } else {
            y.right = node;
        }

        if (node.parent == null) {
            node.color = 0;
            return;
        }

        if (node.parent.parent == null) {
            return;
        }

        fixInsert(node);
    }

    // Find the maximum node
    public Node maximum(Node node) {
        while (node.right != nullLeaf) {
            node = node.right;
        }
        return node;
    }

    // Constructor
    public RbTree() {
        nullLeaf = new Node();
        nullLeaf.color = 0;
        nullLeaf.left = null;
        nullLeaf.right = null;
        root = nullLeaf;
    }

    // Insert a node
    public void insertKey(int key) {
        insert(key);
    }

    // Delete a node
    public void deleteNode(int data) {
        deleteNodeHelper(this.root, data);
    }

    public int getColorFlips() {
        return colorFlips;
    }
}
