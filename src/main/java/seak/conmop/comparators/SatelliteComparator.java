/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.comparators;

import java.util.Comparator;
import seak.conmop.util.OrbitalElementEnum;
import seak.conmop.variable.SatelliteVariable;

/**
 * Comparator for satellite variables that uses the orbital elements
 * @author nozomihitomi
 */
public class SatelliteComparator implements Comparator<SatelliteVariable>{
    
    private final OrbitalElementEnum elementType;

    /**
     * Creates a new comparator for the orbital elements
     * @param elementType the orbital element to sort by
     */
    public SatelliteComparator(OrbitalElementEnum elementType) {
        this.elementType = elementType;
    }

    @Override
    public int compare(SatelliteVariable o1, SatelliteVariable o2) {
        switch(elementType){
            case SMA:
                return o1.getSma().compareTo(o2.getSma());
            case ECC:
                return o1.getEcc().compareTo(o2.getEcc());
            case INC:
                return o1.getInc().compareTo(o2.getInc());
            case RAAN:
                return o1.getRaan().compareTo(o2.getRaan());
            case AP:
                return o1.getArgPer().compareTo(o2.getArgPer());
            case TA:
                return o1.getTrueAnomaly().compareTo(o2.getTrueAnomaly());
            default:
                    throw new UnsupportedOperationException("Orbital element type unknown");
        }
    }
    
}
