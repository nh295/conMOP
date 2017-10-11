/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.launch;

import seak.conmop.propulsion.Propellant;

/**
 * A launch stage of a rocket
 * @author nozomihitomi
 */
public class LaunchStage {
    
    /**
     * The mass [kg] of the stage without propellant
     */
    private final double dryMass;
    
    /**
     * The mass [kg] of the stage including the propellant
     */
    private final double wetMass;
    
    /**
     * the propellant used in the stage
     */
    private final Propellant propellant;

    /**
     * 
     * @param dryMass The mass [kg] of the stage without propellant
     * @param wetMass The mass [kg] of the stage including the propellant
     * @param propellant the propellant used in the stage
     */
    public LaunchStage(double dryMass, double wetMass, Propellant propellant) {
        this.dryMass = dryMass;
        this.wetMass = wetMass;
        this.propellant = propellant;
    }

    /**
     * Gets the mass [kg] of the stage without propellant
     * @return the mass [kg] of the stage without propellant
     */
    public double getDryMass() {
        return dryMass;
    }

    /**
     * Gets the mass [kg] of the stage including the propellant
     * @return the mass [kg] of the stage including the propellant
     */
    public double getWetMass() {
        return wetMass;
    }

    /**
     * Gets the propellant used in the stage
     * @return the propellant used in the stage
     */
    public Propellant getPropellant() {
        return propellant;
    }
    
}
