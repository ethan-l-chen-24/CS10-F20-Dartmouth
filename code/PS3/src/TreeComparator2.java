/**
 * TreeComparator2
 * Same as TreeComparator except for BST
 *
 * @name -> Ethan Chen
 * @date -> October 19, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

import java.util.Comparator;

public class TreeComparator2 implements Comparator<BSTEdited> {

    @Override
    public int compare(BSTEdited o1, BSTEdited o2) {
        return o1.getKey().compareTo(o2.getKey());
    }

}
