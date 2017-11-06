/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.variable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import org.orekit.utils.Constants;
import seak.conmop.deployment.DeploymentStrategy;
import seak.conmop.deployment.Installment;
import seak.conmop.util.Bounds;

/**
 * A variable containing the information for a constellation. All satellites
 * within this variable have the same bounds on their orbital parameters
 *
 * @author nhitomi
 */
public class ConstellationVariable implements Variable {

    private static final long serialVersionUID = -6029490728272163698L;

    /**
     * The bounds on the number of satellites allowed in this constellation
     */
    private final Bounds<Integer> satelliteBound;

    /**
     * The bounds on the semi major axis [m]
     */
    private final Bounds<Double> smaBound;

    /**
     * The bounds on the eccentricity in the range [0,1]
     */
    private final Bounds<Double> eccBound;

    /**
     * The bounds on the inclination [rad]
     */
    private final Bounds<Double> incBound;

    /**
     * The bounds on the argument of perigee [rad]
     */
    private final Bounds<Double> argPerBound;

    /**
     * The bounds on the right ascension of the ascending node [rad]
     */
    private final Bounds<Double> raanBound;

    /**
     * The bounds on the true anomaly [rad]
     */
    private final Bounds<Double> anomBound;

    /**
     * Satellite variables
     */
    private final List<SatelliteVariable> satelliteVars;

    /**
     * Deployment strategy. Auxiliary information that is not a decision.
     */
    private DeploymentStrategy deploymentStrategy;

    /**
     * Creates a new variable for a satellite. Assumes all valid values are
     * allowed for right ascension of the ascending node 
     * and true anomaly. Argument of perigee assumed to be 0.0
     *
     * @param satelliteBound The bounds on the number of satellites allowed in
     * this constellation
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     */
    public ConstellationVariable(
            Bounds<Integer> satelliteBound,
            Bounds<Double> smaBound, Bounds<Double> eccBound,
            Bounds<Double> incBound) {
        this(satelliteBound, smaBound, eccBound, incBound,
                new Bounds<>(0.0, 0.0),
                new Bounds<>(0.0, 2. * Math.PI),
                new Bounds<>(0.0, 2. * Math.PI));
    }

    /**
     * Constructs a new constellation variable
     *
     * @param satelliteBound The bounds on the number of satellites allowed in
     * this constellation
     * @param smaBound The bounds on the semi major axis [m]
     * @param eccBound The bounds on the eccentricity
     * @param incBound The bounds on the inclination [rad]
     * @param argPerBound The bounds on the argument of perigee [rad]
     * @param raanBound The bounds on the right ascension of the ascending node
     * [rad]
     * @param anomBound The bounds on the true anomaly [rad]
     */
    public ConstellationVariable(
            Bounds<Integer> satelliteBound,
            Bounds<Double> smaBound, Bounds<Double> eccBound, Bounds<Double> incBound,
            Bounds<Double> argPerBound, Bounds<Double> raanBound, Bounds<Double> anomBound) {
        checkBounds(satelliteBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
        this.satelliteBound = satelliteBound;
        this.smaBound = smaBound;
        this.eccBound = eccBound;
        this.incBound = incBound;
        this.argPerBound = argPerBound;
        this.raanBound = raanBound;
        this.anomBound = anomBound;
        this.satelliteVars = new ArrayList<>();
    }

    /**
     * Constructs a new constellation variable from a collection of satellite
     * variables. The satellites must all have the same bounds on semi-major
     * axis, eccentricity, inclination, argument of perigee, right ascension of
     * the ascending node, and the true anomaly.
     *
     * @param satelliteBound The bounds on the number of satellites allowed in
     * this constellation
     * @param satellites the satellites to assign to this constellation.
     */
    public ConstellationVariable(Bounds<Integer> satelliteBound, Collection<SatelliteVariable> satellites) {
        //obtain representative satellite variable
        SatelliteVariable rep = satellites.iterator().next();
        this.smaBound = rep.getSmaBound();
        this.eccBound = rep.getEccBound();
        this.incBound = rep.getIncBound();
        this.argPerBound = rep.getArgPerBound();
        this.raanBound = rep.getRaanBound();
        this.anomBound = rep.getAnomBound();
        checkBounds(satelliteBound, smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
        this.satelliteBound = satelliteBound;
        this.satelliteVars = new ArrayList<>();

        this.setSatelliteVariables(satellites);
    }

    /**
     * Copies the fields of the given constellation variable and creates a new
     * instance of a constellation.
     *
     * @param var the constellation variable to copy
     */
    protected ConstellationVariable(ConstellationVariable var) {
        this(var.satelliteBound,
                var.smaBound, var.eccBound, var.incBound,
                var.argPerBound, var.raanBound, var.anomBound);
        for (SatelliteVariable sat : var.getSatelliteVariables()) {
            satelliteVars.add((SatelliteVariable) sat.copy());
        }
        if (var.getDeploymentStrategy() != null) {
            this.setDeploymentStrategy(var.getDeploymentStrategy());
        }
    }

    private void checkBounds(Bounds<Integer> satelliteBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> argPer, Bounds<Double> raan, Bounds<Double> anom) {
        if (satelliteBound.getLowerBound() < 0.) {
            throw new IllegalArgumentException("Expected lower bound on the number of total satellites to be non-negative");
        }
        if (sma.getLowerBound() < Constants.WGS84_EARTH_EQUATORIAL_RADIUS) {
            throw new IllegalArgumentException("Expected semi-major axis to be larger than earth radius (6378137.0 m)");
        }
        if (ecc.getLowerBound() < 0. || ecc.getUpperBound() > 1.) {
            throw new IllegalArgumentException(
                    String.format("Expected eccentriciy bounds to be in range [0,1]."
                            + " Found [%f,%f]", ecc.getLowerBound(), ecc.getUpperBound()));
        }
        if (inc.getLowerBound() < 0. || inc.getUpperBound() > 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected inclination bounds to be in range [0,2pi]."
                            + " Found [%f,%f]", inc.getLowerBound(), inc.getUpperBound()));
        }
        if (argPer.getLowerBound() < 0. || argPer.getUpperBound() > 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected argument of perigee bounds to be in range [0,2pi]."
                            + " Found [%f,%f]", argPer.getLowerBound(), argPer.getUpperBound()));
        }
        if (raan.getLowerBound() < 0. || raan.getUpperBound() > 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected right ascension of the ascending node bounds to be in range [0,2pi]."
                            + " Found [%f,%f]", raan.getLowerBound(), raan.getUpperBound()));
        }
        if (anom.getLowerBound() < 0. || anom.getUpperBound() > 2. * Math.PI) {
            throw new IllegalArgumentException(
                    String.format("Expected true anomaly bounds to be in range [0,2pi]."
                            + " Found [%f,%f]", raan.getLowerBound(), raan.getUpperBound()));
        }
    }

    /**
     * Creates a template satellite variable with the same bounds given to this
     * constellation variable
     *
     * @return
     */
    public SatelliteVariable createSatelliteVariable() {
        return new SatelliteVariable(smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
    }

    @Override
    public Variable copy() {
        return new ConstellationVariable(this);
    }

    @Override
    public void randomize() {
        satelliteVars.clear();
        int n = PRNG.nextInt(satelliteBound.getLowerBound(), satelliteBound.getUpperBound());
        for (int i = 0; i < n; i++) {
            SatelliteVariable var = new SatelliteVariable(smaBound, eccBound, incBound, argPerBound, raanBound, anomBound);
            var.randomize();
            satelliteVars.add(var);
        }
    }

    /**
     * Gets the number of satellites that make up this constellation
     *
     * @return
     */
    public int getNumberOfSatellites() {
        return satelliteVars.size();
    }

    /**
     * Gets the satellite variables stored within this constellation
     *
     * @return the satellite variables stored within this constellation
     */
    public Collection<SatelliteVariable> getSatelliteVariables() {
        return satelliteVars;
    }

    /**
     * Sets the satellite variables within this constellation. All satellite
     * variables must have the same bounds as this constellation
     *
     * @param satellites the satellite variables to assign to this
     * constellation. Any satellite variables that existed previously to this
     * call are cleared out.
     */
    public final void setSatelliteVariables(Collection<SatelliteVariable> satellites) {
        //check that all the bounds are still the same and 
        for (SatelliteVariable var : satellites) {
            if (!(var.getSmaBound().equals(this.smaBound)
                    && var.getEccBound().equals(this.eccBound)
                    && var.getIncBound().equals(this.incBound)
                    && var.getArgPerBound().equals(this.argPerBound)
                    && var.getRaanBound().equals(this.raanBound)
                    && var.getAnomBound().equals(this.anomBound))) {
                throw new IllegalArgumentException(
                        "Given satellites and this constellation have different"
                        + " bounds on the allowable orbital parameters."
                        + " Expected the same bounds.");
            }
        }
        satelliteVars.clear();
        satelliteVars.addAll(satellites);
    }

    /**
     * Gets the bounds on the allowable number of satellites
     *
     * @return the bounds on the allowable number of satellites
     */
    public Bounds<Integer> getSatelliteBound() {
        return satelliteBound;
    }

    /**
     * Gets the bounds for an allowable semi-major axis [m]
     *
     * @return the bounds for an allowable semi-major axis [m]
     */
    public Bounds<Double> getSmaBound() {
        return smaBound;
    }

    /**
     * Gets the bounds for an allowable eccentricity
     *
     * @return the bounds for an allowable eccentricity
     */
    public Bounds<Double> getEccBound() {
        return eccBound;
    }

    /**
     * Gets the bounds for an allowable inclination [rad]
     *
     * @return the bounds for an allowable inclination [rad]
     */
    public Bounds<Double> getIncBound() {
        return incBound;
    }

    /**
     * Gets the bounds for an allowable argument of perigee [rad]
     *
     * @return the bounds for an allowable argument of perigee [rad]
     */
    public Bounds<Double> getArgPerBound() {
        return argPerBound;
    }

    /**
     * Gets the bounds for an allowable right ascension of the ascending node
     * [rad]
     *
     * @return the bounds for an allowable right ascension of the ascending node
     * [rad]
     */
    public Bounds<Double> getRaanBound() {
        return raanBound;
    }

    /**
     * Gets the bounds for an allowable true anomaly [rad]
     *
     * @return the bounds for an allowable true anomaly [rad]
     */
    public Bounds<Double> getAnomBound() {
        return anomBound;
    }

    /**
     * Gets this constellation's deployment strategy
     *
     * @return this constellation's deployment strategy
     */
    public DeploymentStrategy getDeploymentStrategy() {
        return deploymentStrategy;
    }

    /**
     * Sets this constellation's deployment strategy. This method will save the
     * deployment strategy using the satellite variables stored within this
     * instance. Deployment strategy must assign every satellite in this
     * constellation and must not include other satellites other than the ones
     * in this constellation.
     *
     * @param deploymentStrategy this constellation's deployment strategy
     * @return true if the deployment strategy was successfully set. else false.
     */
    public final boolean setDeploymentStrategy(DeploymentStrategy deploymentStrategy) {
        int nSatsInDeployment = 0;
        int nSatsAssigned = 0;

        ArrayList<Installment> newDeployment = new ArrayList<>();
        for (Installment installment : deploymentStrategy.getInstallments()) {
            nSatsInDeployment += installment.getSatellites().size();
            ArrayList<SatelliteVariable> constelSatellites = new ArrayList<>(this.getSatelliteVariables());
            List<SatelliteVariable> installSatellites = installment.getSatellites();
            ArrayList<SatelliteVariable> sats = new ArrayList<>();
            for (SatelliteVariable sat : installSatellites) {
                for (int i = 0; i < constelSatellites.size(); i++) {
                    if (sat.equals(constelSatellites.get(i))) {
                        sats.add(sat);
                        nSatsAssigned++;
                        constelSatellites.remove(i);
                        break;
                    }
                }
            }
            newDeployment.add(new Installment(sats, installment.getLaunchDV(), installment.getOtherDV()));
        }
        if (nSatsInDeployment > getNumberOfSatellites()
                || nSatsAssigned < getNumberOfSatellites()) {
            this.deploymentStrategy = null;
            return false;
        }
        this.deploymentStrategy = new DeploymentStrategy(newDeployment);
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.satelliteBound);
        hash = 41 * hash + Objects.hashCode(this.smaBound);
        hash = 41 * hash + Objects.hashCode(this.eccBound);
        hash = 41 * hash + Objects.hashCode(this.incBound);
        hash = 41 * hash + Objects.hashCode(this.argPerBound);
        hash = 41 * hash + Objects.hashCode(this.raanBound);
        hash = 41 * hash + Objects.hashCode(this.anomBound);
        hash = 41 * hash + Objects.hashCode(this.satelliteVars);
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
        final ConstellationVariable other = (ConstellationVariable) obj;
        if (!Objects.equals(this.satelliteBound, other.satelliteBound)) {
            return false;
        }
        if (!Objects.equals(this.smaBound, other.smaBound)) {
            return false;
        }
        if (!Objects.equals(this.eccBound, other.eccBound)) {
            return false;
        }
        if (!Objects.equals(this.incBound, other.incBound)) {
            return false;
        }
        if (!Objects.equals(this.argPerBound, other.argPerBound)) {
            return false;
        }
        if (!Objects.equals(this.raanBound, other.raanBound)) {
            return false;
        }
        if (!Objects.equals(this.anomBound, other.anomBound)) {
            return false;
        }
        if (!Objects.equals(this.satelliteVars, other.satelliteVars)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (SatelliteVariable var : getSatelliteVariables()) {
            sb.append(String.format("a: %.5f; e: %.5f; i: %.5f; ap: %.5f; raan: %.5f; v: %.5f;\n",
                    var.getSma(), var.getEcc(), var.getInc(), var.getArgPer(), var.getRaan(), var.getTrueAnomaly()));
        }

        return sb.toString();
    }

}
