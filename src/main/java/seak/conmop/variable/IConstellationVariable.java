/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import org.moeaframework.core.Variable;
import seak.orekit.object.Constellation;

/**
 * Interface for a constellation variable
 *
 * @author nhitomi
 */
public interface IConstellationVariable extends Variable {

    /**
     * Converts the variable into a constellation containing Satellites within
     * assigned orbital parameters
     *
     * @return
     */
    public Constellation getConstellation();

}
