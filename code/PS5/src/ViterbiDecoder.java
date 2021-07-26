/**
 * ViterbiDecoder Class
 * Contains all of the code to train a Viterbi graph (Hidden Markov Model) and make inferences on an input of
 * observations based on its HMM
 *
 * @name -> Ethan Chen
 * @date -> November 3, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.util.*;

public class ViterbiDecoder {

    /**
     * VARIABLES
     * --------------------
     */

    private BufferedReader trainTagsReader; // buffered reader for the tag trainer file
    private BufferedReader trainSentencesReader; // buffered reader for the sentence trainer file

    private Map<String, Map<String, Double>> stateTransitionsGraph; // holds the transitions & probabilities between states
    private Map<String, Map<String, Double>> stateObservationsGraph; // holds the probabilities between each state and the potential observations

    private double u;


    /**
     * CONSTRUCTORS
     * --------------------
     */

    /**
     * This constructor takes two training files and builds the Viterbi graph
     *
     * @param trainTagsFileName - the file that has the tags to train the Viterbi graph
     * @param trainSentencesFileName - the file with the words/observations to train the Viterbi graph
     * @throws IOException
     */

    public ViterbiDecoder(String trainTagsFileName, String trainSentencesFileName) throws IOException {
        // initialize variables
        this.trainTagsReader = new BufferedReader(new FileReader(new File(trainTagsFileName)));
        this.trainSentencesReader = new BufferedReader(new FileReader(new File(trainSentencesFileName)));

        stateTransitionsGraph = new HashMap<>();
        stateObservationsGraph = new HashMap<>();

        trainViterbiGraph(); // build the Viterbi graph from the given readers
    }

    /**
     * This constructor takes no parameters, and instead prompts the user to input sets of lines and tags to train the
     * Viterbi graph
     */

    public ViterbiDecoder() {
        // initialize variables
        stateTransitionsGraph = new HashMap<>();
        stateObservationsGraph = new HashMap<>();

        inputTrainManually(); // build the Viterbi graph from user input
    }


    /**
     * DECODING METHODS
     * --------------------
     */

    /**
     * Takes filename testSentencesFileName with sentences to test on the Viterbi graph, runs the algorithm, and
     * writes the associated tags in the file given by writeTagsToFileName
     */

    public void decodeAndWrite(String testSentencesFileName, String writeTagsToFileName) throws IOException {
        // initialize reader and writer
        BufferedReader testSentencesReader = new BufferedReader(new FileReader(new File(testSentencesFileName)));
        BufferedWriter testTagWriter = new BufferedWriter(new FileWriter(new File(writeTagsToFileName)));
        ArrayList<List<String>> linesOfTags = new ArrayList<>(); // lines of tags, where each list is another line

        String line;
        while((line = testSentencesReader.readLine()) != null) { // gets a line of words from the testSentences
            String[] words = line.split(" "); // splits it into an array of Strings
            linesOfTags.add(decodeObservations(words)); // adds the tags based on the decoding algorithm for those words
        }
        testSentencesReader.close();
        printObservations(linesOfTags, testTagWriter); // prints out all of the tags into the output file
    }

    /**
     * Runs Viterbi Algorithm on sentences in testSentencesFileName's file, and compares that to the expected tags given
     * in comopareToTestTagsFileName's file. The amount of matches and mismatches, as well as a percent accuracy is
     * then given.
     */

    public void decodeAndCompareResults(String testSentencesFileName, String compareToTestTagsFileName) throws IOException {
        // initializes readers
        BufferedReader testSentencesReader = new BufferedReader(new FileReader(new File(testSentencesFileName)));
        BufferedReader compareToSentencesReader = new BufferedReader(new FileReader(new File(compareToTestTagsFileName)));
        ArrayList<List<String>> linesOfTags = new ArrayList<>(); // lines of tags, where each list is another line

        String line;
        while((line = testSentencesReader.readLine()) != null) { // gets a line of words from the testSentences
            String[] words = line.split(" "); // splits it into an array of Strings
            linesOfTags.add(decodeObservations(words)); // adds the tags based on the decoding algorithm for those words
        }

        testSentencesReader.close();

        int matches = 0; // number of correct tags based on the algorithm
        int mismatches = 0; // number of incorrect tags
        for(List<String> lines : linesOfTags) { // for each line
            String lineToCompareTo = compareToSentencesReader.readLine();
            String[] wordsToCompareTo = lineToCompareTo.split(" ");
            for(int i = 0; i<wordsToCompareTo.length; i++) {
                if(lines.get(i).equals(wordsToCompareTo[i])) { // if the expected tags are equal to the deducted tags
                    matches++; // it is a match
                } else {
                    mismatches++; // otherwise it is a mismatch
                }
            }
        }

        compareToSentencesReader.close();

        // prints out the matches, mismatches, and the success rate
        System.out.println("There were " + matches + " tag matches and " + mismatches + " tag mismatches, " +
                "leading to a total Viterbi Decoding success rate of " + ((double) matches/(matches + mismatches)));
        System.out.println();

    }

    /**
     * The main decoding algorithm, returns the tags as a list of strings, where index 0 is the first tag and index
     * length - 1 is the last
     *
     * Note: this is my implementation of the pseudocode given for the Viterbi Algorithm from the CS webpage at:
     * https://www.cs.dartmouth.edu/~cs10/notes20.html
     */

    public List<String> decodeObservations(String[] observations) {
        ArrayList<String> currStates = new ArrayList<>(); // current states being observed
        currStates.add("#"); // # means start
        Map<String, Double> currScores = new HashMap<>(); // the current scores being observed
        currScores.put("#", 0.0);
        ArrayList<Map<String, String>> traceBackMap = new ArrayList<>(); // a map that will be used to find the exact path
        // Note: traceBackMap works by, for each index, holding which state each state came from

        for(int i = 0; i<observations.length; i++) { // for every observation
            String observation = observations[i].toLowerCase();
            ArrayList<String> nextStates = new ArrayList<>(); // the next possible states from the states currently being observed
            Map<String, Double> nextScores = new HashMap<>(); // the next scores for those next possible states (only holds onto the best)
            for(String state : currStates) { // for every current state
                for(String nextState : stateTransitionsGraph.get(state).keySet()) { // for each of its next states
                    if(!nextStates.contains(nextState)) { // add it to next states if not already there
                        nextStates.add(nextState);
                    }
                    double nextScore;
                    if(stateObservationsGraph.get(nextState).containsKey(observation)) { // if the score (log probability) exists
                        // currScore + transitionScore + observationScore (works because of logs)
                        nextScore = currScores.get(state)
                                + stateTransitionsGraph.get(state).get(nextState)
                                + stateObservationsGraph.get(nextState).get(observation);
                    } else {
                        // currScore + transitionScore + u
                        nextScore = currScores.get(state)
                                + stateTransitionsGraph.get(state).get(nextState)
                                + u;

                        // Note: u is used for observations that were not present during the training
                        // It should, therefore, be a larger number magnitude-wise
                        // For my algorithm, I decided to use the value of u that was 1.5x lower than the lowest
                        // transition score. This optimizes the number of correct tag matches (at least for the brown
                        // corpus)
                    }

                    // if the score for that state hasn't been put in or is larger (better) than what already exists
                    if(!nextScores.containsKey(nextState) || nextScore > nextScores.get(nextState)) {
                        nextScores.put(nextState, nextScore); // put in the score
                        if(traceBackMap.size() != (i+1)) { // if traceBackMap has not put in its next state
                            HashMap<String, String> backPath = new HashMap<>();
                            traceBackMap.add(backPath);
                        }
                        traceBackMap.get(i).put(nextState, state); // put in the best possible path
                    }
                }
            }

            currStates = nextStates; // progress states and scores
            currScores = nextScores;
        }

        // finds the best state by finding the highest score from the potential list of final states
        double highestScore = -Double.MAX_VALUE; // lowest possible value to start
        String highestState = "";
        for(String state : currScores.keySet()) {
            if(currScores.get(state) > highestScore) { // if it is smaller than the current smallest
                 highestScore = currScores.get(state);
                 highestState = state; // best state
            }
        }
        return getViterbiPath(traceBackMap, highestState); // return the path

    }

    /**
     * A helper method that gets the path (the proper list of tags) from the map that traces the path of states
     *
     * TraceBackMap works, because if we have some final state with the best score, we start at the last index of
     * traceBackMap and iterate backwards, finding where each before it came from until we hit the beginning
     * Because we have only inserted the best possible paths probability-wise, this is the best path based on the
     * algorithm
     */

    private List<String> getViterbiPath(ArrayList<Map<String, String>> traceBackMap, String lastState) {
        LinkedList<String> pathString = new LinkedList<>();
        pathString.add(lastState); // adds the last state (best final state) to the list
        for(int i = traceBackMap.size()-1; i>=1; i--) { // iterates backwards
            String nextString = traceBackMap.get(i).get(lastState); // finds where it came from
            pathString.add(0, nextString); // adds to beginning of list because iterating through backwards
            lastState = nextString; // progress iteration
        }

        return pathString;
    }

    /**
     * A helper method that prints out a list of lists of strings into a file, where each list in the list of lists
     * is its own line in the file
     */

    private void printObservations(List<List<String>> linesOfTags, BufferedWriter testTagWriter) throws IOException {
        for(List<String> l : linesOfTags) {
            for(String s : l) {
                testTagWriter.write(s + " "); // print it out using the bufferedWriter
            }
            testTagWriter.newLine();
        }
        testTagWriter.close();
    }

    /**
     * takes a line from the user and spits out the corresponding tags
     */

    public void inputLines() {
        boolean cont = true;
        while(cont) { // as long as the user decides to continue
            // get sentence
            Scanner sc = new Scanner(System.in);
            System.out.println("What would like your sentence to be?");
            String line = sc.nextLine().toLowerCase();
            String[] observations = line.split(" ");

            // print out corresponding tags
            String output = "";
            for(String tag : decodeObservations(observations)) { // formatting the string to print out the tags
                output += (tag + " ");
            }
            System.out.println("Here are the corresponding tags: ");
            System.out.println(output);

            // asks if you would like to continue
            System.out.println();
            System.out.println("Would you like to do another sentence? 0 = yes, anything else = no");
            Scanner sc3 = new Scanner(System.in);
            String answer = sc3.nextLine();
            if(!answer.equals("0")) {
                cont = false;
            }
        }
    }


    /**
     * TRAINING METHODS
     * --------------------
     */

    /**
     * A helper method that trains the Viterbi graph from the given text files (note that this only runs if
     * those parameters are given in the constructor)
     *
     * This particular method just uses the buffered readers to attain lists of the tags and words, before
     * passing them to trainGraph()
     */

    private void trainViterbiGraph() throws IOException {
        String tagsLine;
        String sentenceLine;

        // gets tags and words from readers simultaneously
        while((tagsLine = trainTagsReader.readLine()) != null && (sentenceLine = trainSentencesReader.readLine()) != null) {
            String[] tags = tagsLine.split(" ");
            String[] words = sentenceLine.split(" ");
            trainGraph(tags, words); // trains the graph using them, for every line in the files
        }

        logGraph(stateTransitionsGraph); // converts to probabilities and logs each graph
        logGraph(stateObservationsGraph);
    }

    /**
     * A helper method that takes a list of tags and words and creates the Viterbi graph
     */

    private void trainGraph(String[] tags, String[] words) {
        if(!stateTransitionsGraph.containsKey("#")) { // put in the # (start) as a state
            stateTransitionsGraph.put("#", new HashMap<>());
        }

        String lastTag = "#"; // consider this the first state, so first tag is a transition from $ to that tag
        for(int i = 0; i<words.length; i++) {
            String currTag = tags[i]; // gets the corresponding tag and word
            String currObs = words[i].toLowerCase();

            if(!stateTransitionsGraph.containsKey(currTag)) { // if the vertex doesn't exist, put it in
                stateTransitionsGraph.put(currTag, new HashMap<>());
            }

            if(!stateTransitionsGraph.get(lastTag).containsKey(currTag)) { // create transition between the last and this state/tag
                stateTransitionsGraph.get(lastTag).put(currTag, 1.0); // new transition, occurrences = 1
            } else {
                // repeating occurrence of this transition, increment by 1
                stateTransitionsGraph.get(lastTag).put(currTag, stateTransitionsGraph.get(lastTag).get(currTag)+1);
            }

            if(!stateObservationsGraph.containsKey(currTag)) { // if the vertex doesn't exist, put it in
                stateObservationsGraph.put(currTag, new HashMap<>());
            }

            if(!stateObservationsGraph.get(currTag).containsKey(currObs)) { // create pointer from state to the observation
                stateObservationsGraph.get(currTag).put(currObs, 1.0); // new pointer, occurrences = 1
            } else {
                // repeating occurrence of this pointer, increment by 1
                stateObservationsGraph.get(currTag).put(currObs, stateObservationsGraph.get(currTag).get(currObs)+1);
            }

            lastTag = currTag; // progress the state
        }
    }

    /**
     * A helper method that takes a Map<String, Map<String, Double>> (effectively a graph), finds the proportions
     * for each state's transitions, and then logs them by base e
     */

    private void logGraph(Map<String, Map<String, Double>> graph) {
        double min = 0; // finds the lowest logged probability value
        for(String state : graph.keySet()) {
            int total = 0; // the total amount of occurrences from a certain state
            for(String transition : graph.get(state).keySet()) { // getting that total
                total += graph.get(state).get(transition);
            }

            // divide each states' transitions' probabilities by that total and then log it
            for(String transition : graph.get(state).keySet()) {
                double probability = graph.get(state).get(transition)/total;
                double log = Math.log(probability);
                if(log < min) { // check if a min
                    min = log;
                }
                graph.get(state).put(transition, log); // put in that new value
            }
        }

        u = min * 1.5; // 1.5 time multiplier for optimization
    }

    /**
     * A helper method that takes user input to get lists of observations and tags to train the Viterbi graph on
     * Note that this is only run if no parameters (filepaths) are given in the constructor
     */

    private void inputTrainManually() {
        System.out.println("Welcome to the Viterbi Decoder!");
        boolean cont = true;
        while(cont) { // continues as long as user wants to continue
            // prompts for sentence
            Scanner sc = new Scanner(System.in);
            System.out.println("What would like your sentence to be?");
            String line = sc.nextLine();
            System.out.println("Your line is:"); // prints out sentence
            System.out.println(line);
            System.out.println();
            String[] observations = line.split(" ");

            // prompts for the corresponding tags
            Scanner sc2 = new Scanner(System.in);
            System.out.println("What are the corresponding tags?");
            String tagLine = sc2.nextLine();
            System.out.println();
            String[] tags = tagLine.split(" ");

            // only trains if the lengths of each are the same
            if(observations.length == tags.length) {
                trainGraph(tags, observations); // trains

                // asks for continuation
                System.out.println("Would you like to continue adding lines? 0 = yes, anything else = no");
                Scanner sc3 = new Scanner(System.in);
                String answer = sc3.nextLine();
                if(!answer.equals("0")) {
                    cont = false;
                }
            } else {
                System.out.println("Try again"); // otherwise asks to try again
            }
        }

        logGraph(stateTransitionsGraph); // probabilifies and logs each graph
        logGraph(stateObservationsGraph);
    }

}