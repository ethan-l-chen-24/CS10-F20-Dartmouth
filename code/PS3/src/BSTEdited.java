import java.security.InvalidKeyException;
import java.util.*;

/**
 * Generic binary search tree
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, Fall 2016, min
 */

public class BSTEdited<K extends Comparable<K>,V> {
    private K key;
    private V value;
    private BSTEdited<K,V> left, right;

    /**
     * Constructs leaf node -- left and right are null
     */
    public BSTEdited(K key, V value) {
        this.key = key; this.value = value;
    }

    /**
     * Constructs inner node
     */
    public BSTEdited(K key, V value, BSTEdited<K,V> left, BSTEdited<K,V> right) {
        this.key = key; this.value = value;
        this.left = left; this.right = right;
    }

    /**
     * GETTERS AND SETTERS
     */

    public K getKey() {
        return key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    public BSTEdited<K, V> getLeft() {
        return left;
    }

    public void setLeft(BSTEdited<K, V> left) {
        this.left = left;
    }

    public BSTEdited<K, V> getRight() {
        return right;
    }

    public void setRight(BSTEdited<K, V> right) {
        this.right = right;
    }

    /**
     * Is it a leaf node?
     */
    public boolean isLeaf() {
        return left == null && right == null;
    }

    /**
     * Does it have a left child?
     */
    public boolean hasLeft() {
        return left != null;
    }

    /**
     * Does it have a right child?
     */
    public boolean hasRight() {
        return right != null;
    }

    /**
     * Returns the value associated with the search key, or throws an exception if not in BST
     */
    public V find(K search) throws InvalidKeyException {
        System.out.println(key); // to illustrate
        int compare = search.compareTo(key);
        if (compare == 0) return value;
        if (compare < 0 && hasLeft()) return left.find(search);
        if (compare > 0 && hasRight()) return right.find(search);
        throw new InvalidKeyException(search.toString());
    }

    /**
     * Smallest key in the tree, recursive version
     */
    public K min() {
        if (left != null) return left.min();
        return key;
    }

    /**
     * Smallest key in the tree, iterative version
     */
    public K minIter() {
        BSTEdited<K,V> curr = this;
        while (curr.left != null) curr = curr.left;
        return curr.key;
    }

    /**
     * Inserts the key & value into the tree (replacing old key if equal)
     */
    public void insert(K key, V value) {
        int compare = key.compareTo(this.key);
        if (compare == 0) {
            // replace
            this.value = value;
        }
        else if (compare < 0) {
            // insert on left (new leaf if no left)
            if (hasLeft()) left.insert(key, value);
            else left = new BSTEdited<K,V>(key, value);
        }
        else if (compare > 0) {
            // insert on right (new leaf if no right)
            if (hasRight()) right.insert(key, value);
            else right = new BSTEdited<K,V>(key, value);
        }
    }

    /**
     * Deletes the key and returns the modified tree, which might not be the same object as the original tree
     * Thus must afterwards just use the returned one
     */
    public BSTEdited<K,V> delete(K search) throws InvalidKeyException {
        int compare = search.compareTo(key);
        if (compare == 0) {
            // Easy cases: 0 or 1 child -- return other
            if (!hasLeft()) return right;
            if (!hasRight()) return left;
            // If both children are there, delete and substitute the successor (smallest on right)
            // Find it
            BSTEdited<K,V> successor = right;
            while (successor.hasLeft()) successor = successor.left;
            // Delete it
            right = right.delete(successor.key);
            // And take its key & value
            this.key = successor.key;
            this.value = successor.value;
            return this;
        }
        else if (compare < 0 && hasLeft()) {
            left = left.delete(search);
            return this;
        }
        else if (compare > 0 && hasRight()) {
            right = right.delete(search);
            return this;
        }
        throw new InvalidKeyException(search.toString());
    }

    /**
     * Parenthesized representation:
     * <tree> = "(" <tree> ["," <tree>] ")" <key> ":" <value>
     *        | <key> ":" <value>
     */
  /*  public String toString() {
        if (isLeaf()) return key+":"+value;
        String s = "(";
        if (hasLeft()) s += left;
        else s += "_";
        s += ",";
        if (hasRight()) s += right;
        else s += "_";
        return s + ")" + key+":"+value;
    } */

    @Override
    public String toString() {
        String string = getStringOut(0); // calls recursive helper method
        return string;
    }

    /** helper method for toString(), actually creates string */
    public String getStringOut(int level) { // numIndents - number of indents/levels down in tree
        String string = "";
        for(int x = 0; x<level; x++) {
            string += "   "; // indents (level) times
        }
        string += (key + ":" + value + "\n");
        if(hasLeft()) {
            string += getLeft().getStringOut(level + 1);
        } if(hasRight()) {
            string += getRight().getStringOut(level + 1);
        }
        return string;
    }

    /**
     * Very simplistic BST parser in a parenthesized representation
     * <tree> = "(" <tree> ["," <tree>] ")" <key> ":" <value>
     *        | <key> ":" <value>
     * Assumes that the tree actually has the BST property!!!
     * No effort at all to handle malformed trees
     */
    public static BSTEdited<String,String> parseBST(String s) {
        return parseBST(new StringTokenizer(s, "(,)", true));
    }

    /**
     * Does the real work of parsing, now given a tokenizer for the string
     */
    public static BSTEdited<String,String> parseBST(StringTokenizer st) {
        String token = st.nextToken();
        if (token.equals("(")) {
            // Inner node
            BSTEdited<String,String> left = parseBST(st);
            BSTEdited<String,String> right = null;
            String comma = st.nextToken();
            if (comma.equals(",")) {
                right = parseBST(st);
                String close = st.nextToken();
            }
            String label = st.nextToken();
            String[] pieces = label.split(":");
            return new BSTEdited<String,String>(pieces[0], pieces[1], left, right);
        }
        else {
            // Leaf
            String[] pieces = token.split(":");
            return new BSTEdited<String,String>(pieces[0], pieces[1]);
        }
    }

    /**
     * Some tree testing
     */
    public static void main(String[] args) throws Exception {
        BSTEdited<String,String> t = parseBST("((a:1,c:3)b:2,(e:5,g:8)f:6)d:4");
        System.out.println("initial: " + t);
        System.out.println("min: " + t.min() + " == " + t.minIter());

        System.out.println("finding a");
        System.out.println("found a = "+t.find("a"));

        System.out.println("finding h");
        try {
            System.out.println("found h = "+t.find("h"));
        }
        catch (InvalidKeyException e) {
            System.err.println(e);
        }

        t.insert("i", "10");
        t.insert("j", "11");
        t.insert("h", "9");
        System.out.println("inserted i,j,j: " + t);
        System.out.println("finding h");
        System.out.println("found h = "+t.find("h"));

        t = t.delete("a");
        System.out.println("deleted a: " + t);
        t = t.delete("c");
        System.out.println("deleted c: " + t);
        t = t.delete("g");
        System.out.println("deleted g: " + t);
        t = t.delete("f");
        System.out.println("deleted f: " + t);
    }
}