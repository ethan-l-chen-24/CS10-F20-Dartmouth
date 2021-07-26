/**
 * TreeComparator
 * Passed to a priority queue, enables comparisons of a tree based on its data
 *
 * @name -> Ethan Chen
 * @date -> October 19, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.util.Comparator;

public class TreeComparator implements Comparator<BinaryTree> {

    @Override
    public int compare(BinaryTree o1, BinaryTree o2) {
        return o1.getData().compareTo(o2.getData()); // returns compare values of their data
    }
}