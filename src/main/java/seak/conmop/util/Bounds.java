/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.util;

import java.io.Serializable;

/**
 * An object to store the upper and lower bounds of an interval
 *
 * @author nhitomi
 * @param <T>
 */
public class Bounds<T extends Comparable<T>> implements Serializable{

    private static final long serialVersionUID = -6427497168771357058L;

    private final T upperBound;

    private final T lowerBound;

    public Bounds(T lowerBound, T upperBound) {

        //check that the lower bound is lower than the upper bound using natural ordering
        if (upperBound.compareTo(lowerBound) < 0) {
            throw new IllegalArgumentException("Upperbound is less than the lowerbound");
        }

        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public boolean inBounds(T input) {
        if (input.compareTo(lowerBound) < 0) {
            return false;
        } else if (input.compareTo(upperBound) > 0) {
            return false;
        } else {
            return true;
        }
    }

}
