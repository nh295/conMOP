/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.launch;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author nozomihitomi
 */
public class LaunchVehicle implements Iterable<LaunchStage>{

    private final ArrayList<LaunchStage> stages;

    public LaunchVehicle(ArrayList<LaunchStage> stages) {
        this.stages = new ArrayList(stages);
    }

    public double getDeltaV() {
        return DeltaV.stagedRocketEquation(this);
    }
    
    public int getNumberOfStages(){
        return stages.size();
    }

    @Override
    public Iterator<LaunchStage> iterator() {
        return stages.iterator();
    }
    
    
}
