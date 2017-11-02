/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.deployment;

import java.io.Serializable;
import java.util.Collection;

/**
 * The deployment strategy contains information on how the constellation will be
 * deployed. This includes information on how many launches are necessary, which
 * satellites are packaged together on the same launch vehicle, and the delta V [m/s]
 * required for each launch
 *
 * @author nozomihitomi
 */
public class DeploymentStrategy implements Serializable{

    private static final long serialVersionUID = 4921026633423580472L;

    /**
     * The deployment strategy
     */
    private final Collection<Installment> deploymentStrategy;

    /**
     * The deployment strategy
     * @param deploymentStrategy the grouped deployments in this strategy
     */
    public DeploymentStrategy(Collection<Installment> deploymentStrategy) {
        this.deploymentStrategy = deploymentStrategy;
    }
    
    /**
     * Gets the total delta V [m/s] required for this deployment strategy
     * @return the total delta V [m/s] required for this deployment strategy
     */
    public double getTotalDV(){
        double dv = 0.0;
        for(Installment group : deploymentStrategy){
            dv += group.getLaunchDV();
            dv += group.getOtherDV();
        }
        return dv;
    }

    /**
     * Gets the grouped deployments in this strategy
     * @return the grouped deployments in this strategy
     */
    public Collection<Installment> getInstallments() {
        return deploymentStrategy;
    }
}
