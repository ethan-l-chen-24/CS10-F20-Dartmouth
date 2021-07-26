/**
 * StringCompressor
 * Takes a file, compresses it using Huffman Encoding, and decompresses the encoded file
 *
 * @name -> Ethan Chen
 * @date -> October 19, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.util.*;

public class StringCompressor {

    /**
     * INSTANCE VARIABLES
     * --------------------
     */

    private BufferedBitReader bitReader; // reads and interprets bits from compressed file
    private BufferedBitWriter bitWriter; // writes bits to compressed file
    private BufferedReader charReader; // reads String input from txt file
    private BufferedWriter charWriter; // writes String output to decompressed file

    private HashMap<Character, Integer> charFrequencies; // map of characters and their frequencies
    private PriorityQueue<BinaryTree<CharFreq>> charPriorityQueue; // priority queue of trees w/ characters and frequencies
    private BinaryTree<CharFreq> headCharSearchTree; // the top of the Huffman code tree
    private ArrayList<Character> allChars; // a list of all chars in the file
    private HashMap<Character, String> bitMap; // the mapping from a character to its corresponding bit representation

    private String compressedFilePath; // file path names for created files
    private String decompressedFilePath;

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public StringCompressor(String pathname) {

        compressedFilePath = pathname.substring(0, pathname.length() - 4) + "_compressed.txt"; // e.g. USConstitution_compressed.txt
        decompressedFilePath = pathname.substring(0, pathname.length() - 4) + "_decompressed.txt"; // e.g. USConstitution_decompressed.txt

        try {
            charReader = new BufferedReader(new FileReader(pathname)); // initializes
        } catch (Exception e) {
            System.out.println(e);
        }

        charFrequencies = new HashMap<>(); // initializing necessary objects/data structures
        Comparator<BinaryTree> treeComparator = new TreeComparator(); // NOTE ***
        charPriorityQueue = new PriorityQueue<>(treeComparator);
        bitMap = new HashMap<>();
        allChars = new ArrayList<>();

        // *** In order to use this comparator, BinaryTree must use generic <E extends Comparable<E>> instead of <E>
    }

    /**
     * METHODS
     * --------------------
     */

    /**
     * GOAL: compress a file's text using Huffman Encoding into a new, smaller compressed file
     */
    public void compress() {
        try { // in case of exceptions (caused by empty file)
            createCharMap(); // all of the methods for huffman encoding
            mapToPriorityQueue();
            priorityQueueToTree();
            createBitMap();
            writeBitFile();
        } catch (Exception e) {
            System.out.println(e); // print the exception
        }
    }

    /**
     * HELPER: creates the map of each character's frequency in the text
     */
    private void createCharMap() throws Exception {
        String line;
        boolean firstLine = true; // checks if reading the first line

        while ((line = charReader.readLine()) != null) { // read each line from the txt file

            // this is so that a line break is not placed before the first line of the file, but all new lines after
            if (firstLine) {
                firstLine = false;
            } else {
                putCharInMap('\n');
            }

            char[] charArray = line.toCharArray(); // put each character from line in an array

            for (char c : charArray) {
                putCharInMap(c); // put the character into the map ** see method
            }
        }
        charReader.close(); // should never throw error if it has made it this far

        if (firstLine) { // if firstLine is still true, means there was no text, empty, throw exception
            throw new Exception("Invalid File - Empty");
        }
    }

    /**
     * HELPER: puts the char in map w/ freq 1 if it doesn't exist, if it does increase freq by 1
     */
    private void putCharInMap(char c) {
        if (charFrequencies.containsKey(c)) { // if character already in map
            charFrequencies.put(c, charFrequencies.get(c) + 1); // increase its frequency in map by 1
        } else {
            charFrequencies.put(c, 1); // put new character into map with frequency 1
        }
        allChars.add(c); // add all characters to cumulative list of chars in file
    }

    /**
     * HELPER: takes entries in the map, turn them into trees, and puts them into a priority queue (based on the frequency)
     */
    private void mapToPriorityQueue() {

        // for every entry in the map, make a binary tree with CharFreqs (character and frequency class with accessors)
        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            BinaryTree<CharFreq> characterTree = new BinaryTree<>(new CharFreq(entry.getKey(), entry.getValue()));

            // add each tree to the priority queue, based on the frequency in CharFreq
            charPriorityQueue.add(characterTree);
        }
    }

    /**
     * HELPER: makes the huffman tree
     */
    private void priorityQueueToTree() {
        if (charPriorityQueue.size() == 1) { // if only one item (1 character in file), put on right of arbitrary tree node
            headCharSearchTree = new BinaryTree<>(new CharFreq('+', 0), null, charPriorityQueue.remove());
        } else {
            while (charPriorityQueue.size() > 1) {
                BinaryTree<CharFreq> t1 = charPriorityQueue.remove(); // lowest frequency
                BinaryTree<CharFreq> t2 = charPriorityQueue.remove(); // second lowest frequency

                // put the two smallest trees as the left and right nodes of a tree with character '+'
                // note that with this method, only leaves are characters, so no character binary code is a
                // prefix to another's binary code
                CharFreq combinedCharFreq = new CharFreq('+', t1.getData().getFrequency() + t2.getData().getFrequency());
                headCharSearchTree = new BinaryTree<>(combinedCharFreq, t1, t2);
                charPriorityQueue.add(headCharSearchTree); // put the tree back onto the priority queue
            }
            headCharSearchTree = charPriorityQueue.remove(); // at the very end, the last tree is the complete huffman tree
        }
    }

    /**
     * HELPER: creates a map with each character mapped to its huffman bit value
     */
    private void createBitMap() {
        BinaryTree<CharFreq> traversalTree = headCharSearchTree; // creates a pointer to the head of the tree
        createBitMap(traversalTree, ""); // creates the map
    }

    /**
     * HELPER: recursively carries out createBitMap(), traversing the tree and writing bit mappings to bitMap
     */
    private void createBitMap(BinaryTree<CharFreq> traversalTree, String bitOutput) {
        if (traversalTree.isLeaf()) { // if it is a character, write it and its path ("0101" for example) to the map
            bitMap.put(traversalTree.getData().getChar(), bitOutput);
        } else {
            if (traversalTree.hasLeft()) { // traverse left
                bitOutput += "0"; // coded as 0
                createBitMap(traversalTree.getLeft(), bitOutput); // recursive call, lower down on tree

                // to remove the 0 from the string output if a tree has a right and a left
                // e.g. if the very top node has a right and a left, then without removing the left string
                // left = 0, right = 01, when right should just be = 1
                bitOutput = bitOutput.substring(0, bitOutput.length() - 1);
            }
            if (traversalTree.hasRight()) { // traverse right
                bitOutput += "1"; // coded as 1
                createBitMap(traversalTree.getRight(), bitOutput); // recursive call, lower down on tree
            }
        }
    }

    /**
     * HELPER: writes the bits to the compressed file
     */
    private void writeBitFile() throws IOException {
        bitWriter = new BufferedBitWriter(compressedFilePath); // should never throw exception
        for (char c : allChars) { // for every char in file, map to binary using the bitMap
            String bitString = bitMap.get(c);
            for (char bit : bitString.toCharArray()) { // for every bit
                if (bit == '0') { // if 0, write false, if 1, write true using bitWriter
                    bitWriter.writeBit(false);
                } else {
                    bitWriter.writeBit(true);
                }
            }
        }
        bitWriter.close(); // close the writer
    }

    /**
     * GOAL: take the compressed file, decode it using the huffman tree, and write it to another text file
     */
    public void decompress() throws IOException { // throws error if run before compress or file is empty

        bitReader = new BufferedBitReader(compressedFilePath);
        charWriter = new BufferedWriter(new FileWriter(new File(decompressedFilePath)));

        BinaryTree<CharFreq> traversalTree = headCharSearchTree;

        // read every bit - for every 0, move left down tree, for every 1, move right
        // when it reaches a leaf, write the corresponding character to the file
        // reset back to the top of the file and continue reading bits - this works because no character's bit value
        // is a prefix to another's
        while (bitReader.hasNext()) {
            boolean bit = bitReader.readBit(); // read a bit
            if (bit) { // 1 - travel right
                traversalTree = traversalTree.getRight();
            } else { // 0 - travel left
                traversalTree = traversalTree.getLeft();
            }

            if (traversalTree.isLeaf()) { // if at a leaf
                charWriter.write(traversalTree.getData().getChar()); // write the character
                traversalTree = headCharSearchTree; // return to the top of the tree
            }
        }
        bitReader.close();
        charWriter.close();
    }


    /**
     * PRIVATE CLASS BITFREQ
     * Contains a character and its frequency, as well as accessors and comparing ability based on the frequency
     * --------------------
     */

    private class CharFreq implements Comparable<CharFreq> {

        private char character;
        private int frequency;

        public CharFreq(char character, int frequency) {
            this.character = character;
            this.frequency = frequency;
        }

        // accessors for instance variables
        public char getChar() {
            return this.character;
        }

        public int getFrequency() {
            return this.frequency;
        }

        @Override
        public String toString() {
            return "Character: " + character + "   Frequency: " + frequency;
        } // toString method

        @Override
        public int compareTo(CharFreq o) { // compares the frequencies (for the priority queue)
            if (frequency < o.frequency) {
                return -1;
            } else if (frequency == o.frequency) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * TESTER
     * --------------------
     */

    public static void main(String[] args) {

        StringCompressor c = new StringCompressor("inputs/USConstitution.txt");
        c.compress();
        System.out.println(c.charFrequencies); // prints initial character:frequency map
        System.out.println(c.headCharSearchTree); // prints out the search tree
        System.out.println(c.bitMap); // prints out the character:bit map

        // look for computer output in ***_compressed.txt and ***_decompressed.txt

        try {
            c.decompress();
        } catch (Exception e) {
            System.out.println(e);
        }

    }


}
