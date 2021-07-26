/**
 * ActorAndNumber class
 * Holds an actor and a number -> useful for priority queues
 *
 * @name -> Ethan Chen
 * @date -> October 29, 2020
 * @class -> CS 10, Fall 2020, Pierson
 */

public class ActorAndNumber implements Comparable<ActorAndNumber> {

    private double number; // the number
    private String name; // the name

    public ActorAndNumber(String name, double number) { // constructor
        this.number = number;
        this.name = name;

    }

    public String getName() {
        return this.name;
    }

    @Override
    public int compareTo(ActorAndNumber o) { // compares based on the number
        if(number < o.number) {
            return -1;
        } else if(number > o.number) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public String toString() {
        return (name + ": " + number);
    }
}