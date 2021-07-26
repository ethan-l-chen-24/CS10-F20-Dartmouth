/**
 * ViterbiTester Class
 * Tests my ViterbiDecoder
 *
 * @name -> Ethan Chen
 * @date -> November 3, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.IOException;

public class ViterbiTester {

    public static void main(String[] args) throws IOException {

        // ETHAN TEST CASE
        ViterbiDecoder testDecode = new ViterbiDecoder("inputs/chase-train-tags.txt", "inputs/chase-train-sentences.txt");
        testDecode.decodeAndWrite("inputs/chase-test-sentences.txt", "inputs/chase-output.txt");


        // SIMPLE
        System.out.println("Results from the decoder based on the few sentences: ");
        System.out.println();

        ViterbiDecoder stupidDecode = new ViterbiDecoder("inputs/simple-train-tags.txt", "inputs/simple-train-sentences.txt");
        stupidDecode.decodeAndCompareResults("inputs/simple-test-sentences.txt", "inputs/simple-test-tags.txt");
        stupidDecode.decodeAndCompareResults("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt");


        // BROWN
        System.out.println();
        System.out.println("Results from the decoder based on the full Brown Corpus: ");
        System.out.println();

        ViterbiDecoder smartDecode = new ViterbiDecoder("inputs/brown-train-tags.txt", "inputs/brown-train-sentences.txt");
        smartDecode.decodeAndWrite("inputs/simple-test-sentences.txt", "inputs/simple-output.txt");
        smartDecode.decodeAndWrite("inputs/brown-test-sentences.txt", "inputs/brown-output.txt");
        smartDecode.decodeAndWrite("inputs/random-sentences.txt", "inputs/random-output.txt");
        smartDecode.decodeAndCompareResults("inputs/simple-test-sentences.txt", "inputs/simple-test-tags.txt");
        smartDecode.decodeAndCompareResults("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt");
        smartDecode.inputLines(); // lines given by user


        // USER INPUTTED

        System.out.println();
        System.out.println("And here is a decoder created by the user: ");
        System.out.println();

        ViterbiDecoder userDecode = new ViterbiDecoder();
        userDecode.decodeAndCompareResults("inputs/simple-test-sentences.txt", "inputs/simple-test-tags.txt");
        userDecode.decodeAndCompareResults("inputs/brown-test-sentences.txt", "inputs/brown-test-tags.txt");
    }

}
