/**
 * StringCompressorExtension
 * A StringCompressor that saves a frequency list in a file with suffix _freq so the tree can be built without first
 * compressing. Also, all methods and variables have been made static.
 *
 * @name -> Ethan Chen
 * @date -> October 20, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.util.*;

public class StringCompressorExtension {

    /**
     * ALGORITHM
     * --------------------
     *
     * If I could somehow put the entire frequency map into a file in the same order it was originally found in,
     * then regardless of duplicate frequencies, the tree created through huffman's method should be the same
     * Therefore, I just wrote the character and its frequency to the file ending in _freq, and when I loaded, I
     * put the characters and frequencies into the map
     */

    /**
     * STATIC METHODS
     * --------------------
     */

    /**
     * GOAL: compress a file's text using Huffman Encoding into a new, smaller compressed file
     */
    public static void compress(String pathname) {
        ArrayList<Character> allChars = new ArrayList<>();
        try { // in case of exceptions (caused by empty file)
            HashMap<Character, Integer> charFrequencies = createCharMap(pathname, allChars); // all of the methods for huffman encoding
            PriorityQueue<BinaryTree<CharFreq>> charPriorityQueue = mapToPriorityQueue(charFrequencies);
            BinaryTree<CharFreq> headCharSearchTree = priorityQueueToTree(charPriorityQueue);
            HashMap<Character, String> bitMap = createBitMap(headCharSearchTree);
            writeBitFile(bitMap, pathname, allChars);
            writeFrequencyToFile(pathname, charFrequencies);
        } catch (Exception e) {
            System.out.println(e); // print the exception
        }
    }

    /**
     * HELPER: creates the map of each character's frequency in the text
     */
    private static HashMap<Character, Integer> createCharMap(String pathname, ArrayList<Character> allChars) throws Exception {
        BufferedReader charReader = new BufferedReader(new FileReader(new File(pathname)));
        String line;
        boolean firstLine = true; // checks if reading the first line

        HashMap<Character, Integer> map = new HashMap<>();

        while ((line = charReader.readLine()) != null) { // read each line from the txt file

            // this is so that a line break is not placed before the first line of the file, but all new lines after
            if (firstLine) {
                firstLine = false;
            } else {
                putCharInMap('\n', map, allChars);
            }
            char[] charArray = line.toCharArray(); // put each character from line in an array

            for (char c : charArray) {
                map = putCharInMap(c, map, allChars); // put the character into the map ** see method
            }
        }
        charReader.close(); // should never throw error if it has made it this far

        if (firstLine) { // if firstLine is still true, means there was no text, empty, throw exception
            throw new Exception("Invalid File - Empty");
        }
        return map;
    }

    /**
     * HELPER: puts the char in map w/ freq 1 if it doesn't exist, if it does increase freq by 1
     */
    private static HashMap<Character, Integer> putCharInMap(char c, HashMap<Character, Integer> charFrequencies, ArrayList<Character> allChars) {
        if (charFrequencies.containsKey(c)) { // if character already in map
            charFrequencies.put(c, charFrequencies.get(c) + 1); // increase its frequency in map by 1
        } else {
            charFrequencies.put(c, 1); // put new character into map with frequency 1
        }
        allChars.add(c); // add all characters to cumulative list of chars in file
        return charFrequencies;

    }

    /**
     * HELPER: takes entries in the map, turn them into trees, and puts them into a priority queue (based on the frequency)
     */
    private static PriorityQueue<BinaryTree<CharFreq>> mapToPriorityQueue(HashMap<Character, Integer> charFrequencies) {

        PriorityQueue<BinaryTree<CharFreq>> charPriorityQueue = new PriorityQueue<>(new TreeComparator());

        // for every entry in the map, make a binary tree with CharFreqs (character and frequency class with accessors)
        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            BinaryTree<CharFreq> characterTree = new BinaryTree<>(new CharFreq(entry.getKey(), entry.getValue()));

            // add each tree to the priority queue, based on the frequency in CharFreq
            charPriorityQueue.add(characterTree);
        }
        return charPriorityQueue;
    }

    /**
     * HELPER: makes the huffman tree
     */
    private static BinaryTree<CharFreq> priorityQueueToTree(PriorityQueue<BinaryTree<CharFreq>> charPriorityQueue) {

        BinaryTree<CharFreq> headCharSearchTree = new BinaryTree<>(null);

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
        return headCharSearchTree;
    }

    /**
     * HELPER: creates a map with each character mapped to its huffman bit value
     * @return
     */
    private static HashMap<Character, String> createBitMap(BinaryTree<CharFreq> headCharSearchTree) {
        BinaryTree<CharFreq> traversalTree = headCharSearchTree; // creates a pointer to the head of the tree
        HashMap<Character, String> bitMap = new HashMap<>();
        return createBitMap(traversalTree, bitMap, ""); // creates the map
    }

    /**
     * HELPER: recursively carries out createBitMap(), traversing the tree and writing bit mappings to bitMap
     */
    private static HashMap<Character, String> createBitMap(BinaryTree<CharFreq> traversalTree, HashMap<Character, String> bitMap, String bitOutput) {

        if (traversalTree.isLeaf()) { // if it is a character, write it and its path ("0101" for example) to the map
            bitMap.put(traversalTree.getData().getChar(), bitOutput);
        } else {
            if (traversalTree.hasLeft()) { // traverse left
                bitOutput += "0"; // coded as 0
                createBitMap(traversalTree.getLeft(), bitMap, bitOutput); // recursive call, lower down on tree

                // to remove the 0 from the string output if a tree has a right and a left
                // e.g. if the very top node has a right and a left, then without removing the left string
                // left = 0, right = 01, when right should just be = 1
                bitOutput = bitOutput.substring(0, bitOutput.length() - 1);
            }
            if (traversalTree.hasRight()) { // traverse right
                bitOutput += "1"; // coded as 1
                createBitMap(traversalTree.getRight(), bitMap, bitOutput); // recursive call, lower down on tree
            }
        }
        return bitMap;
    }

    /**
     * HELPER: writes the bits to the compressed file
     */
    private static void writeBitFile(HashMap<Character, String> bitMap, String pathname, ArrayList<Character> allChars) throws IOException {
        String compressedFilePath = pathname.substring(0, pathname.length() - 4) + "_compressed.txt"; // e.g. USConstitution_compressed.txt
        BufferedBitWriter bitWriter = new BufferedBitWriter(compressedFilePath); // should never throw exception
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
     * HELPER: writes the tree to a file in a way that can easily be read and transformed back to tree
     * IDEA: if you can recreate the exact same map as before (in same order, regardless of frequencies), you can
     * create the same tree and decode the same way
     */
    private static void writeFrequencyToFile(String pathname, Map<Character, Integer> charFrequencies) throws IOException {
        String freqEncodeFilePath = pathname.substring(0, pathname.length() - 4) + "_freq.txt";
        BufferedWriter frequencyBuf = new BufferedWriter(new FileWriter(new File(freqEncodeFilePath)));

        boolean firstLine = true;

        for (Map.Entry<Character, Integer> entry : charFrequencies.entrySet()) {
            if(firstLine) { // don't want line break at beginning
                firstLine = false;
            } else {
                frequencyBuf.write("\n");
            }
            if(entry.getKey() == '\n') { // if line break character, print lb in place
                frequencyBuf.write("lb" + " " + entry.getValue());
            } else {
                frequencyBuf.write(entry.getKey() + " " + entry.getValue()); // print "character frequency"
            }
        }
        frequencyBuf.close();
    }


    /**
     * GOAL: take the compressed file, decode it using the huffman tree, and write it to another text file
     */
    public static void decompress(String pathname, String treePathName) throws IOException { // throws error if run before compress or file is empty

        String decompressedFilePath = pathname.substring(0, pathname.length() - 15) + "_decompressed.txt"; // e.g. USConstitution_decompressed.txt
        BufferedBitReader bitReader = new BufferedBitReader(pathname);
        BufferedWriter charWriter = new BufferedWriter(new FileWriter(new File(decompressedFilePath)));

        BufferedReader buf = new BufferedReader(new FileReader(new File(treePathName)));

        BinaryTree<CharFreq> inputTree = loadTree(buf);;

        BinaryTree<CharFreq> traversalTree = inputTree;

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
                traversalTree = inputTree; // return to the top of the tree
            }
        }
        bitReader.close();
        charWriter.close();
    }

    /**
     * HELPER: loads the tree
     */
    private static BinaryTree<CharFreq> loadTree(BufferedReader buf) throws IOException {
        String line;

        HashMap<Character, Integer> loadMap = new HashMap<Character, Integer>();
        while((line = buf.readLine()) != null) {
            char character = line.charAt(0); // the character
            String frequencyInt = line.substring(2); // the rest of the line (which is the frequency)
            if(line.startsWith("lb")) { // if line break, special case
                character = '\n';
                frequencyInt = line.substring(3);
            }
            int frequency = Integer.valueOf(frequencyInt);
            loadMap.put(character, frequency); // put into the map
        }

        PriorityQueue<BinaryTree<CharFreq>> loadQueue = mapToPriorityQueue(loadMap); // load the queue
        buf.close();
        return priorityQueueToTree(loadQueue); // load the tree

    }


    /**
     * PRIVATE CLASS BITFREQ
     * Contains a character and its frequency, as well as accessors and comparing ability based on the frequency
     * --------------------
     */

    private static class CharFreq implements Comparable<CharFreq> {

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


        // to test, create a new file, comment out the decompress, run, comment out the compress and
        // uncomment the decompress, run again
        StringCompressorExtension.compress("inputs/USConstitution.txt");

        try {
           // StringCompressorExtension.decompress("inputs/alphabet_compressed.txt", "inputs/alphabet_freq.txt");
        } catch (Exception e) {
            System.out.println(e);
        }

    }


}