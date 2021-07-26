/**
 * StringCompressorBST
 * The same as StringCompressor, except with a BST (done accidentally the first time I attempted this problem set)
 *
 * @name -> Ethan Chen
 * @date -> October 19, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.util.*;

public class StringCompressorBST {

    /**
     * INSTANCE VARIABLES
     * --------------------
     */

    private BufferedBitReader bitReader;
    private BufferedBitWriter bitWriter;
    private BufferedReader charReader;
    private BufferedWriter charWriter;

    private HashMap<Character, Integer> charFrequencies;
    private PriorityQueue<BSTEdited<Integer, Character>> charPriorityQueue;
    private BSTEdited<Integer, Character> headCharSearchTree;
    private ArrayList<Character> allChars;
    private HashMap<Character, String> bitMap;

    String compressedFilePath;
    String decompressedFilePath;

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public StringCompressorBST(String pathname) throws IOException {

        compressedFilePath = pathname.substring(0, pathname.length() - 4) + "_compressed.txt";
        decompressedFilePath = pathname.substring(0, pathname.length() - 4) + "_decompressed.txt";

        charReader = new BufferedReader(new FileReader(pathname));
        charWriter = new BufferedWriter(new FileWriter(new File(decompressedFilePath)));
        bitWriter = new BufferedBitWriter(compressedFilePath);

        charFrequencies = new HashMap<>();
        Comparator<BSTEdited> treeComparator = new TreeComparator2();
        charPriorityQueue = new PriorityQueue<>(treeComparator);
        bitMap = new HashMap<>();
        allChars = new ArrayList<>();
    }

    /**
     * METHODS
     * --------------------
     */

    public void compressString() {

        try {
            createCharMap();
            mapToPriorityQueue();
            priorityQueueToTree();
            createBitMap();
            writeBitFile();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    private void createCharMap() throws Exception {
        String line;
        boolean firstLine = true;

        while ((line = charReader.readLine()) != null) {

            if (firstLine) {
                firstLine = false;
            } else {
                putCharInMap('\n');
            }

            char[] charArray = line.toCharArray();

            for (char c : charArray) {
                putCharInMap(c);
            }
        }
        if (firstLine) {
            throw new Exception("Invalid File - Empty");
        }
    }

    private void putCharInMap(char c) {
        if (charFrequencies.containsKey(c)) {
            charFrequencies.put(c, charFrequencies.get(c) + 1);
        } else {
            charFrequencies.put(c, 1);
        }
        allChars.add(c);
    }

    private void mapToPriorityQueue() {
        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            BSTEdited<Integer, Character> characterTree = new BSTEdited<>(entry.getValue(), entry.getKey());
            charPriorityQueue.add(characterTree);
        }
    }

    private void priorityQueueToTree() {
        if(charPriorityQueue.size() == 1) {
            headCharSearchTree = new BSTEdited(0, '+', null, charPriorityQueue.remove());

        } else {
            while (charPriorityQueue.size() > 1) {
                BSTEdited<Integer, Character> t1 = charPriorityQueue.remove();
                BSTEdited<Integer, Character> t2 = charPriorityQueue.remove();
                headCharSearchTree = new BSTEdited(t1.getKey() + t2.getKey(), '+', t1, t2);
                charPriorityQueue.add(headCharSearchTree);
            }
            headCharSearchTree = charPriorityQueue.remove();
        }
    }

    private void createBitMap() {
        BSTEdited<Integer, Character> traversalTree = headCharSearchTree;
        createBitMap(traversalTree, "");
    }

    private void createBitMap(BSTEdited<Integer, Character> traversalTree, String bitOutput) {
        if (traversalTree.isLeaf()) {
                bitMap.put(traversalTree.getValue(), bitOutput);
        } else {
            if (traversalTree.hasLeft()) {
                bitOutput += "0";
                createBitMap(traversalTree.getLeft(), bitOutput);
                bitOutput = bitOutput.substring(0, bitOutput.length() - 1);
            }
            if (traversalTree.hasRight()) {
                bitOutput += "1";
                createBitMap(traversalTree.getRight(), bitOutput);
            }
        }
    }

    private void writeBitFile() throws IOException {
        for (char c : allChars) {
            String bitString = bitMap.get(c);
            for (char bit : bitString.toCharArray()) {
                if (bit == '0') {
                    bitWriter.writeBit(false);
                } else {
                    bitWriter.writeBit(true);
                }
            }
        }
        bitWriter.close();
    }

    public void decompress() throws IOException {
        bitReader = new BufferedBitReader(compressedFilePath);
        BSTEdited<Integer, Character> traversalTree = headCharSearchTree;
        while (bitReader.hasNext()) {
            boolean bit = bitReader.readBit();
            if (bit) {
                traversalTree = traversalTree.getRight();
            } else {
                traversalTree = traversalTree.getLeft();
            }

            if (traversalTree.isLeaf()) {
                charWriter.write(traversalTree.getValue());
                traversalTree = headCharSearchTree;
            }
        }
        charWriter.close();
    }

    /**
     * TESTER
     * --------------------
     */

    public static void main(String[] args) {
        try {
            StringCompressorBST c = new StringCompressorBST("inputs/USConstitution.txt");
            c.compressString();
            System.out.println(c.charFrequencies);
            System.out.println(c.headCharSearchTree);
            System.out.println(c.bitMap);
            c.decompress();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
