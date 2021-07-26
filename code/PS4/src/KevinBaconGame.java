/**
 * Kevin Bacon
 * Plays the Kevin Bacon Game, finding the degrees of separation between actors that have starred with one another
 *
 * @name -> Ethan Chen
 * @date -> October 29, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.io.*;
import java.util.*;

public class KevinBaconGame {

    /**
     * VARIABLES
     * --------------------
     */

    private BufferedReader actorsBuf; // reads from actors file
    private BufferedReader moviesBuf; // reads from movies file
    private BufferedReader moviesStarringBuf; // reads from movie-actors file

    private HashMap<Integer, String> actorIDs; // maps actor IDs to their names
    private HashMap<Integer, String> movieIDs; // maps movie IDs to their names
    private HashMap<String, ArrayList<String>> movieStars; // maps movies to all of their actors

    AdjacencyMapGraph<String, Set<String>> actorGraph; // the graph that connects actors by their movies (do bfs on this)
    AdjacencyMapGraph<String, Set<String>> pathGraph; // the graph that holds the path to the center of the universe

    /**
     * CONSTRUCTOR
     * --------------------
     */

    public KevinBaconGame(String actorsFilePath, String moviesFilePath, String moviesStarringActorsFilePath) throws Exception {
        actorsBuf = new BufferedReader(new FileReader(new File(actorsFilePath))); // initialize readers
        moviesBuf = new BufferedReader(new FileReader(new File(moviesFilePath)));
        moviesStarringBuf = new BufferedReader(new FileReader(new File(moviesStarringActorsFilePath)));

        actorIDs = new HashMap<>(); // initialize variables
        movieIDs = new HashMap<>();
        movieStars = new HashMap<>();
        actorGraph = new AdjacencyMapGraph<>();

        // load from the files and fill the necessary data structures to play the game
        loadActorIDs();
        loadMovieIDs();
        loadMovieStars();
        createGraph();

        // uncomment to print out the maps
        printOutMaps();

        startGame(); // *** NOTE: the game is run on construction of an object
    }

    /**
     * METHODS
     * --------------------
     */

    /** prints out the maps */
    private void printOutMaps() {
        System.out.println("All maps and the actorGraph");
        System.out.println();
        System.out.println("Actor IDs:");
        System.out.println(actorIDs);
        System.out.println();
        System.out.println("Movie IDs:");
        System.out.println(movieIDs);
        System.out.println();
        System.out.println("Actors in Movies IDs:");
        System.out.println(movieStars);
        System.out.println();
        System.out.println("Actor IDs:");
        System.out.println(actorGraph);
        System.out.println();
    }

    /** reads the actors file and loads IDs and names into the actorIDs map */
    private void loadActorIDs() throws Exception {
        String line;
        boolean empty = true;
        while((line = actorsBuf.readLine()) != null) { // go line by line through file
            empty = false;
            String[] parsed = line.split("\\|"); // split by the |

            Integer actorID;
            try { // catching invalid formats
                actorID = Integer.valueOf(parsed[0]);
            } catch (Exception e) {
                throw new Exception("File not of valid format");
            }

            if(parsed.length < 2) { // catching invalid formats
                throw new Exception("File not of valid format");
            }


            String actorName = parsed[1];
            if(!actorIDs.containsKey(actorID) && !actorIDs.containsValue(actorName)) { // in case of duplicates
                actorIDs.put(actorID, actorName); // put the ID and name into the map
            }
        }
        if(empty) {
            throw new Exception("Empty file");
        }
        actorsBuf.close();
    }

    /** reads the movies file and loads IDs and movies into the movieIDs map */
    private void loadMovieIDs() throws Exception {
        String line;
        boolean empty = true;
        while((line = moviesBuf.readLine()) != null) { // go line by line through file
            empty = false;
            String[] parsed = line.split("\\|");  // split by the |

            Integer movieID;
            try { // catching invalid formats
                 movieID = Integer.valueOf(parsed[0]);
            } catch (Exception e) {
                throw new Exception("File not of valid format");
            }

            if(parsed.length < 2) { // catching invalid formats
                throw new Exception("File not of valid format");
            }

            String movieName = parsed[1];
            if(!movieIDs.containsKey(movieID) && !movieIDs.containsValue(movieName)) { // in case of duplicates
                movieIDs.put(movieID, movieName); // put the ID and name into the map
            }
        }
        if(empty) {
            throw new Exception("Empty file");
        }
        moviesBuf.close();
    }

    /** reads the movie-actors file and reads keys from each of the other maps to create movieStars map */
    private void loadMovieStars() throws Exception {
        String line;
        boolean empty = true;
        while((line = moviesStarringBuf.readLine()) != null) { // go line by line through file
            empty = false;
            String[] parsed = line.split("\\|"); // split by the |

            Integer movieID;
            Integer actorID;
            try { // catching invalid formats
                 movieID = Integer.valueOf(parsed[0]);
                 actorID = Integer.valueOf(parsed[1]);
            } catch (Exception e) {
                throw new Exception("File not of valid format");
            }

            if(parsed.length < 2) { // catching invalid formats
                throw new Exception("File not of valid format");
            }


            String movie = movieIDs.get(movieID); // gets the names by the ID in the corresponding maps
            String actor = actorIDs.get(actorID);

            if(!movieStars.containsKey(movie)) { // if the movie hasn't already put in, add as new key in the map
                movieStars.put(movie, new ArrayList<String>());
            }
            movieStars.get(movie).add(actor); // add the actors to that movie
        }
        if(empty) {
            throw new Exception("Empty file");
        }
        moviesStarringBuf.close();
    }

    /** uses movieStars to create the actorGraph graph */
    private void createGraph() {

        for(String actor : actorIDs.values()) {
            if(!actorGraph.hasVertex(actor)) { // in case of duplicate names
                actorGraph.insertVertex(actor); // put all of the actors into the actorGraph
            }
        }

        for(String movie : movieStars.keySet()) { // for every movie

            // this associates every actor with every actor in the same movie
            // the for loop variables are set so that actors do not repeat comparisons,
            // and do not compare with each other more than once
            for(int i = 0; i<movieStars.get(movie).size()-1; i++) {
                for(int j = i+1; j<movieStars.get(movie).size(); j++) {
                    String actor1 = movieStars.get(movie).get(i);
                    String actor2 = movieStars.get(movie).get(j);
                    if(!actor1.equals(actor2)) { // in case actor added to movie twice (faulty movie-actors file)
                        if(!actorGraph.hasEdge(actor1, actor2)) {
                            actorGraph.insertUndirected(actor1, actor2, new HashSet<>()); // if no edge exists yet
                        }
                        actorGraph.getLabel(actor1, actor2).add(movie); // add the movie to the edge's label
                    }
                }
            }
        }
    }

    /** UI for the Kevin Bacon Game - run through console */
    private void startGame() throws Exception {

        String centerOfUniverse = findCenter(); // prompt for center of universe
        pathGraph = (AdjacencyMapGraph<String, Set<String>>) GraphLib.bfs(actorGraph, centerOfUniverse);
        // run breadth first search on actors to create path graph

        int state = 0; // 0 = prompt for what to do state
        while(state != 1) { // 1 = quit state
            System.out.println();

            if (state == 0) { // present choices
                System.out.println("What action would you like to take?");
                System.out.println("1: quit");
                System.out.println("2: find the Kevin-Bacon number of an actor to " + centerOfUniverse);
                System.out.println("3: print average Kevin-Bacon number");
                System.out.println("4: find number of and names of all people not " +
                        "connected to " + centerOfUniverse);
                System.out.println("5: find number of and names of all people " +
                        "connected to " + centerOfUniverse);
                System.out.println("6: change the center of the universe");
                System.out.println("7: rank all actors by proximity to " + centerOfUniverse);
                System.out.println("8: find the betweenness centrality of another actor");
                System.out.println("9: HELP - Give me good Kevin Bacons!");

                Scanner sc = new Scanner(System.in);
                String stringState = sc.nextLine();
                try { // prompt input
                    state = Integer.parseInt(stringState);
                } catch (Exception e) {
                    System.out.println("Invalid Choice");
                }

            } else if (state == 2) { // 2 = find kevin bacon number of actor to center of universe
                System.out.println("Who would you like to search for?");
                String person = getPerson();
                printPath(GraphLib.getPath(pathGraph, person)); // print the path from the pathGraph
                state = 0; // always return to base state when done

            } else if (state == 3) { // 3 = find the center of universe's average separation
                System.out.println("The average Kevin-Bacon number with " + centerOfUniverse + " is: ");
                System.out.println(GraphLib.averageSeparation(pathGraph, centerOfUniverse)); // print the average sep
                state = 0;

            } else if (state == 4) { // 4 = find the number of and all actors not associated with the center of the universe
                HashSet<String> notAssociated = (HashSet<String>) GraphLib.missingVertices(actorGraph, pathGraph); // get a list of all not associated
                System.out.println("There are " + notAssociated.size() + " actors not associated with " + centerOfUniverse);
                System.out.println("The actors not associated with " + centerOfUniverse + " are: ");
                System.out.println(notAssociated); // print it out
                state = 0;

            } else if(state == 5) { // 5 = find the number of and all actors associated with the center of the universe, including themselves
                int countAssociation = countGraphVertices(pathGraph);
                System.out.println("There are " + countAssociation + " actors associated with " + centerOfUniverse);
                System.out.println("The actors associated with " + centerOfUniverse + " are: ");
                System.out.println(pathGraph.vertices());
                state = 0;

            } else if(state == 6) { // 6 = choose a new center of the universe
                System.out.println("Who would you like as the center of the universe?");
                centerOfUniverse = getPerson(); // get a new name
                // run another bfs and create a new pathGraph based on new person
                pathGraph = (AdjacencyMapGraph<String, Set<String>>) GraphLib.bfs(actorGraph, centerOfUniverse);
                System.out.println("You have changed the center of the universe to " + centerOfUniverse + ".");
                state = 0;

            } else if(state == 7) { // 7 = rank order all actors by their Kevin Bacon number and print them out
                System.out.println("The actors and their associated Kevin Bacon numbers are: ");
                printCloseActors(); // print them out
                state = 0;

            } else if(state == 8) { // 8 = betweenness centrality of a given person (EXTENSION)
                System.out.println("Who would you like to check the betweenness centrality of?");
                String person = getPerson();
                printBetweennessCentrality(person);
                state = 0;

            } else if(state == 9) { // 9 = find good Kevin Bacons
                findGoodKevinBacons();
                state = 0;
            }

            else { // invalid number
                System.out.println("That was not a valid choice. Please try again.");
                state = 0;
            }
        }
        System.out.println();
        System.out.println("Thank you for playing the Kevin Bacon Game!"); // after quit

    }

    /** prompts user to find the center of the universe at the start of the game */
    private String findCenter() {
        System.out.println("Welcome to the Kevin-Bacon-Game");
        System.out.println("To begin, please type in the name of the person you would like as the center of the universe:");
        String centerOfUniverse = getPerson();
        System.out.println(centerOfUniverse + " is the center of the universe");

        return centerOfUniverse; // return a name of a selected person

    }

    /** prompts user to put in name of an actor, if it doesn't exist, asks again */
    private String getPerson() {
        boolean invalid = true; // makes sure it gets a valid actor
        String person = null;
        while(invalid) { // continue until valid
            Scanner sc = new Scanner(System.in);
            person = sc.nextLine(); // prompt for name

            if(actorGraph.hasVertex(person)) { // only set invalid to false if it exists in actorGraph
                invalid = false;
            } else {
                System.out.println("That actor does not exist. Try again.");
            }

        }
        System.out.println();
        return person;
    }

    /** prints out the path from one actor to the center of the universe, given the path graph */
    private void printPath(List<String> path) {
        if(path == null) {
            return;
        }

        // formats printing out the path with the actors and the movies they starred in
        for(int i = 0; i<path.size()-1; i++) {
            String output = (path.get(i) + " starred with " + path.get(i+1) + " in: "); // printing out actors
            for(String s : pathGraph.getLabel(path.get(i), path.get(i+1))) { // printing out movies
                output += s + ", ";
            }
            output = output.substring(0, output.length()-2); // removing last comma
            System.out.println(output);
        }
        System.out.println(path.get(0) + "'s Kevin-Bacon number to " + path.get(path.size()-1) + " is " + (path.size()-1));
        // printing out kevin bacon number
    }

    /** prints out actors in order of how close they are to the center of the universe */
    private void printCloseActors() {
        String output = "";
        PriorityQueue<String> minActorDistanceQueue = new PriorityQueue<>(new ProximityComparator(pathGraph));
        for(String vertex : pathGraph.vertices()) {
            minActorDistanceQueue.add(vertex); // adds all of the vertices to the queue based on the proximityComparator (see file)
        }

        while(!minActorDistanceQueue.isEmpty()) {
            String actor = minActorDistanceQueue.remove(); // pops off queue, always getting smallest first
            output += (actor + ": " + (GraphLib.getPath(pathGraph, actor).size()-1) + ", "); // prints name and kevin bacon number
        }
        output = output.substring(0, output.length()-2); // removes comma
        System.out.println(output);
    }

    /** counts the number of vertices in a graph */
    private int countGraphVertices(AdjacencyMapGraph<String, Set<String>> pathGraph) {
        int x = 0;
        for(String s : pathGraph.vertices()) { // increments for every item
            x++;
        }
        return x;
    }

    /** given an actor, calculates the percentage of paths they are present on */
    private void printBetweennessCentrality(String s) { // EXTENSION

        int totalPaths = 0;
        int containedPaths = 0;
        for(String actor : pathGraph.vertices()) { // for all people connected to the center of the universe
            ArrayList<String> path = (ArrayList<String>) GraphLib.getPath(pathGraph, actor);
            if(path.contains(s)) { // if it is in the path of one of the people
                containedPaths++;
            }
            totalPaths++;
        }

        System.out.println(s + " is present in " + containedPaths + " out of " + totalPaths + " total paths.");
        System.out.println("Their betweenness centrality is " + containedPaths + "/" + totalPaths + " = " + ((double) containedPaths/totalPaths));

    }

    /** Finds good candidates to use as Kevin Bacons based on their degree and average separation */
    private void findGoodKevinBacons() throws Exception {

        // Part 1: prints out all actors by the number of actors they starred with
        ArrayList<String> sortedActorsByInDegree = (ArrayList<String>) GraphLib.verticesByInDegree(actorGraph);
        System.out.println("All actors within Bacon universe ranked by how many other actors they have starred with: ");
        String inDegreeOutput = "";
        for(String actor : sortedActorsByInDegree) {
            inDegreeOutput += actor + ": " + actorGraph.inDegree(actor) + ", ";
        }
        inDegreeOutput = inDegreeOutput.substring(0, inDegreeOutput.length()-2); // removing comma
        System.out.println(inDegreeOutput); // print out all the sorted actors
        System.out.println();

        // comparison to Kevin Bacon
        System.out.println("For reference, Kevin Bacon has starred with: " + actorGraph.inDegree("Kevin Bacon") + " other actors");
        System.out.println();

        // Part 2: create a smaller list of the actors with the most connections
        // if there are more than 100 actors, only take the top 10%, otherwise take the entire list

        // NOTE: I would have used the entire list, but my computer could not compute all of them and
        // said it ran out of heap space. In any case, narrowing it down to a smaller list will not likely
        // change the outcome of who is chosen, so it is fine to reduce the list size
        ArrayList<String> sortedActorsSubList = new ArrayList<>();
        if(sortedActorsByInDegree.size() <= 100) { // if less than 100
            for(int x = 0; x<sortedActorsByInDegree.size(); x++) {
                sortedActorsSubList.add(sortedActorsByInDegree.get(x)); // add the entire list
            }
        } else { // if more than 100
            for(int x = 0; x<sortedActorsByInDegree.size()/10; x++) {
                sortedActorsSubList.add(sortedActorsByInDegree.get(x)); // only add the top 10%
            }
        }


        // Part 3: using the sublist, sort by the average Kevin Bacon number
        // NOTE: for sorting by both average Kevin Bacon number and the final scores, I decided to use a priority queue
        // although I could have also used a list and passed a comparator into the sort method
        PriorityQueue<ActorAndNumber> bestByAverageKBNumber = new PriorityQueue<>();
        PriorityQueue<ActorAndNumber> bestByAverageKBNumber2 = new PriorityQueue<>();
        for(String actorInBaconUniverse : sortedActorsSubList) {
            AdjacencyMapGraph<String, Set<String>> actorBFS = (AdjacencyMapGraph<String, Set<String>>) GraphLib.bfs(actorGraph, actorInBaconUniverse);
            ActorAndNumber newActor = new ActorAndNumber(actorInBaconUniverse, GraphLib.averageSeparation(actorBFS, actorInBaconUniverse));
            bestByAverageKBNumber.add(newActor); // puts into PriorityQueue based on averageSeparation
            bestByAverageKBNumber2.add(newActor); // the second one is just for printing, ensures the same priority queue
        }

        System.out.println("Top 10% of those actors ranked by their average separation:");
        System.out.println("(or if this is a list with less than 100 actors, the entire list)");
        String averageKBoutput = "";
        while(!bestByAverageKBNumber2.isEmpty()) {
            averageKBoutput += (bestByAverageKBNumber2.remove() + ", ");
        }
        averageKBoutput = averageKBoutput.substring(0, averageKBoutput.length()-2);
        System.out.println(averageKBoutput); // print out ranked list of actors by their average number
        System.out.println();

        // comparison to Kevin Bacon
        AdjacencyMapGraph<String, Set<String>> kbBFS = (AdjacencyMapGraph<String, Set<String>>) GraphLib.bfs(actorGraph, "Kevin Bacon");
        System.out.println("For reference, Kevin Bacon's average separation is: " + GraphLib.averageSeparation(kbBFS, "Kevin Bacon"));

        System.out.println();

        // Part 4: compute scores
        HashMap<String, Integer> actorScores = new HashMap<>(); // create map that maps actors to their scores
        for(int i = 0; i<sortedActorsSubList.size(); i++) {
            // put all the actors in with their index as their score form part 1
            actorScores.put(sortedActorsSubList.get(i), i);
        }

        int indexCounter = 0;
        while(!bestByAverageKBNumber.isEmpty()) {
            ActorAndNumber s = bestByAverageKBNumber.remove();
            String actorName = s.getName();
            // add their rank/index from the priority queue on top of their existing score
            actorScores.put(actorName, actorScores.get(actorName) + indexCounter);
            indexCounter++;
        }

        // Part 5: sort by final scores (same methods as before)
        PriorityQueue<ActorAndNumber> finalScoreQueue = new PriorityQueue<>();
        for(String s : actorScores.keySet()) {
            finalScoreQueue.add(new ActorAndNumber(s, actorScores.get(s))); // adding the names and scores to be sorted
        }

        System.out.println("The candidates ranks for each category have been summed to create a final score");
        System.out.println("The top 5 candidates for Kevin Bacons are: ");
        for(int x = 0; x<5; x++) {
            System.out.println(finalScoreQueue.remove()); // prints out the top 5 candidates
        }

    }


    /** RUNNER */
    public static void main(String[] args) { // runs the game by constructing an object of KevinBaconGame
        try {
            KevinBaconGame game = new KevinBaconGame("testFiles/actors.txt", "testFiles/movies.txt", "testFiles/movie-actors.txt");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}
