/**
 * ProximityComparator class
 * A comparator class that compares the Kevin-Bacon distances of different actors
 *
 * @name -> Ethan Chen
 * @date -> October 29, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.util.Comparator;
import java.util.Set;

public class ProximityComparator implements Comparator<String> { // comparator class to be passed to PriorityQueue

    Graph<String, Set<String>> g; // the pathGraph

    public ProximityComparator(Graph<String, Set<String>> g) {
        this.g = g;
    } // pass along the graph that contains the vertices' path

    @Override
    public int compare(String o1, String o2) { // compares based on the length of the path/Kevin Bacon number
        if (GraphLib.getPath(g, o1).size() > GraphLib.getPath(g, o2).size()) {
            return 1;
        } else if (GraphLib.getPath(g, o1).size() < GraphLib.getPath(g, o2).size()) {
            return -1;
        } else {
            return 0;
        }
    }
}