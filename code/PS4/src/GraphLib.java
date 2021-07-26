/**
 * GraphLib Class
 * Contains a set of methods that can be run on graphs that are relevant to the Kevin Bacon Game
 *
 * @name -> Ethan Chen
 * @date -> October 29, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.util.*;

public class GraphLib {

    /**
     * STATIC METHODS
     * --------------------
     */

    /** generic breadth first search algorithm that returns a graph of the paths */
    public static <V,E> Graph<V,E> bfs(Graph<V,E> g, V source) throws Exception {
        AdjacencyMapGraph<V, E> pathGraph = new AdjacencyMapGraph<>();
        SLLQueue<V> searchQueue = new SLLQueue<>();
        HashSet<V> visited = new HashSet<>();

        searchQueue.enqueue(source); // enqueue start vertex onto queue
        visited.add(source); // set start vertex as visited
        pathGraph.insertVertex(source); // add it as a vertex onto the pathGraph (destination of all paths)

        while(!searchQueue.isEmpty()) { // repeat until queue is empty
            V currVertex = searchQueue.dequeue(); // remove the first item
            for(V v : g.outNeighbors(currVertex)) { // for each of the vertex's neighbors
                if(!visited.contains(v)) { // if it hasn't already been visited
                    visited.add(v); // set it as visited
                    pathGraph.insertVertex(v); // put it into the pathGraph as a vertex
                    searchQueue.enqueue(v); // put it on the queue
                    pathGraph.insertDirected(v, currVertex, g.getLabel(v, currVertex));
                    // create an edge between the neighbor and its source - will create path to ultimate source which
                    // is the target vertex/center of the universe
                }
            }
        }

        return pathGraph;

    }

    /** given a path graph and a starting vertex, returns the path as a list back to the top */
    public static <V,E> List<V> getPath(Graph<V,E> tree, V v) {
       ArrayList<V> pathList = new ArrayList<>();
       V currentVertex = v;
       if(!tree.hasVertex(v)) { //
           System.out.println("This path does not exist");
           return null;
       }

       // only the top node/certer of universe doesn't point to another node
       while(tree.outDegree(currentVertex) != 0) { // while the vertex still points to another vertex
           pathList.add(currentVertex); // add it onto the path
           for(V parent : tree.outNeighbors(currentVertex)) { // should only be one outNeighbor for pathGraph
               currentVertex = parent; // set currentVertex as the last vertex's "parent", until at top
           }
       }
       pathList.add(currentVertex); // add the top to the list
       return pathList; // return
    }

    /** returns a set of all the items in graph but not in subgraph */
    public static <V,E> Set<V> missingVertices(Graph<V,E> graph, Graph<V,E> subgraph) {
        HashSet<V> missingSet = new HashSet<>();
        for(V v : graph.vertices()) { // for all the vertices in graph
            if(!subgraph.hasVertex(v)) { // if graph doesn't have them
                missingSet.add(v); // add it to the set
            }
        }
        return missingSet;
    }

    /** returns the average distance of all nodes from the root */
    public static <V,E> double averageSeparation(Graph<V,E> tree, V root) {
        int totalDistance = totalDistance(tree, root, 0); // find the total distance
        return (double) totalDistance / (double) (tree.numVertices()-1); // have to subtract 1 to remove counting the root
    }

    /** recursive helper method that carries out finding the average separation */
    private static <V,E> int totalDistance(Graph<V,E> tree, V currVertex, int distanceFromRoot) {
        int totalDistance = distanceFromRoot; // set distance as the distance from the root
        if(tree.inDegree(currVertex) == 0) { // effectively a leaf, nothing pointing to it/furthest away along path
            return distanceFromRoot;
        } else {
            for(V vertex : tree.inNeighbors(currVertex)) { // go through all the neighbors
                totalDistance += totalDistance(tree, vertex, distanceFromRoot+1); // add subtrees distances
            }
            return totalDistance;
        }
    }

    /** returns a sorted list of all vertices by their indegree */
    public static <V,E> List<V> verticesByInDegree(Graph<V,E> g) {
        ArrayList<V> verticesSortedByInDegree = new ArrayList<>();

        for(V vertex : g.vertices()) { // add each vertex to an ArrayList
            verticesSortedByInDegree.add(vertex);
        }

        verticesSortedByInDegree.sort(new InDegreeComparator(g)); // call sort with Comparator that measures by indegree
        return verticesSortedByInDegree;
    }

    /** private class that acts as a comparator, to compare indegrees between vertices in a graph */
    private static class InDegreeComparator<V, E> implements Comparator<V> {

        Graph<V, E> g; // the graph (not the path graph)

        public InDegreeComparator(Graph<V, E> g) {
            this.g = g;
        } // pass along the graph that contains the vertices

        @Override
        public int compare(V o1, V o2) {
            if (g.inDegree(o1) > g.inDegree(o2)) { // compare the indegrees
                return -1;
            } else if (g.inDegree(o1) < g.inDegree(o2)) {
                return 1;
            } else {
                return 0;
            }
        }
    }
}

