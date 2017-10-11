/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.propulsion;

/**
 * Propellant class holds information pertaining to the performance of a given propellant
 * @author nozomihitomi
 */
public class Propellant {
    /**
     * The specific impulse [1/s] of the propellant.
     */
    private final double isp;
    
    /**
     * The name of the propellant
     */
    private final String name;

    /**
     * 
     * @param isp The specific impulse [1/s] of the propellant.
     * @param name The name of the propellant
     */
    public Propellant(double isp, String name) {
        this.isp = isp;
        this.name = name;
    }

    /**
     * Gets the specific impulse [1/s] of this propellant
     * @return the specific impulse [1/s] of this propellant
     */
    public double getIsp() {
        return isp;
    }

    /**
     * Gets the name of this propellant
     * @return the name of this propellant
     */
    public String getName() {
        return name;
    }
    
}
