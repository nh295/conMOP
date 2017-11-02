/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.deployment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import seak.conmop.variable.SatelliteVariable;

/**
 * A group of satellites that are deployed at the same time from the same launch
 * vehicle or space tug.
 *
 * @author nozomihitomi
 */
public class Installment implements Serializable{

    private static final long serialVersionUID = 732360808393249326L;
    
    /**
     * The satellites that are deployed in this installment
     */
    private final List<SatelliteVariable> satellites;
    
    /**
     * The launch deltaV [m/s] required to deploy this installment
     */
    private final double launchDV;
    
    /**
     * Other deltaV [m/s] (e.g. from a space tug required to deploy this installment
     */
    private final double otherDV;

    /**
     * 
     * @param satellites The satellites that are deployed in this installment
     * @param launchDV The launch deltaV [m/s] required to deploy this installment
     * @param otherDV Other deltaV [m/s] (e.g. from a space tug required to deploy this installment
     */
    public Installment(List<SatelliteVariable> satellites, double launchDV, double otherDV) {
        this.satellites = new ArrayList(satellites);
        Collections.unmodifiableList(this.satellites);
        this.launchDV = launchDV;
        this.otherDV = otherDV;
    }

    /**
     * Gets the satellites that are deployed in this installment
     * @return the satellites that are deployed in this installment
     */
    public List<SatelliteVariable> getSatellites() {
        return satellites;
    }

    /**
     * Gets the launch deltaV [m/s] required to deploy this installment
     * @return the launch deltaV [m/s] required to deploy this installment
     */
    public double getLaunchDV() {
        return launchDV;
    }

    /**
     * Gets the other deltaV [m/s] (e.g. from a space tug required to deploy this installment
     * @return the other deltaV [m/s] (e.g. from a space tug required to deploy this installment
     */
    public double getOtherDV() {
        return otherDV;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.satellites);
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.launchDV) ^ (Double.doubleToLongBits(this.launchDV) >>> 32));
        hash = 29 * hash + (int) (Double.doubleToLongBits(this.otherDV) ^ (Double.doubleToLongBits(this.otherDV) >>> 32));
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Installment other = (Installment) obj;
        if (Double.doubleToLongBits(this.launchDV) != Double.doubleToLongBits(other.launchDV)) {
            return false;
        }
        if (Double.doubleToLongBits(this.otherDV) != Double.doubleToLongBits(other.otherDV)) {
            return false;
        }
        if (!Objects.equals(this.satellites, other.satellites)) {
            return false;
        }
        return true;
    }
    
    
    
}
