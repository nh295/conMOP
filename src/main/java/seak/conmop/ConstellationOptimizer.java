/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import seak.conmop.util.Bounds;
import seak.conmop.variable.BooleanSatelliteVariable;
import seak.conmop.variable.ConstellationMatrix;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;
import seak.orekit.coverage.analysis.AnalysisMetric;
import seak.orekit.coverage.analysis.FastCoverageAnalysis;
import seak.orekit.coverage.analysis.GroundEventAnalyzer;
import seak.orekit.event.EventAnalysis;
import seak.orekit.object.Constellation;
import seak.orekit.object.CoverageDefinition;
import seak.orekit.object.Satellite;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.scenario.Scenario;
import seak.orekit.util.OrekitConfig;

/**
 * Problem to optimize the number of satellites in a constellation and their
 * orbital parameters
 *
 * @author nhitomi
 */
public class ConstellationOptimizer extends AbstractProblem {

    private final Properties properties;

    /**
     * The points of interest for the coverage study
     */
    private final Set<GeodeticPoint> poi;

    /**
     * The propagator factory to create the propagators for each satellite
     */
    private final PropagatorFactory propagatorFactory;

    /**
     * The inertial frame to use for the study
     */
    private final Frame inertialFrame;

    /**
     * The time scale to use for the study (e.g. UTC).
     */
    private final TimeScale timeScale;

    /**
     * The end date of the study
     */
    private final AbsoluteDate endDate;

    /**
     * The start date of the study
     */
    private final AbsoluteDate startDate;
    
    /**
     * Half angle for a simple conical field of view sensor [rad]
     */
    private final double halfAngle;

    /**
     * The bounds allowable on the number of satellites
     */
    private final Bounds<Integer> nSatBound;

    /**
     * The bounds allowable on the semi-major axis [m]
     */
    private final Bounds<Double> smaBound;

    /**
     * The bounds allowable on the eccentricity
     */
    private final Bounds<Double> eccBound;

    /**
     * The bounds allowable on the inclination [rad]
     */
    private final Bounds<Double> incBound;

    /**
     * The bounds allowable on the right ascension of the ascending node [rad]
     */
    private final Bounds<Double> raanBound;

    /**
     * The bounds allowable on the argument of perigee [rad]
     */
    private final Bounds<Double> apBound;

    /**
     * The bounds allowable on the true anomaly [rad]
     */
    private final Bounds<Double> taBound;
    
    /**
     * The shape of the Earth
     */
    private final BodyShape earthShape;

    /**
     * The standard gravitational constant for Earth
     */
    private final double earthMu;
    
    /**
     * a dummy constructor for analyzing hypervolumes after the optimization 
     */
    public ConstellationOptimizer() {
        this(null, null, null, null, null, 0.0, null, null, null, null);
    }

    /**
     * Constructor for that allows all feasible values for right ascension of
     * the ascending node, argument of perigee and true anomaly. Only circular
     * or near-circular orbits are allowed
     *
     * @param name
     * @param startDate
     * @param endDate
     * @param propagatorFactory
     * @param poi
     * @param halfAngle
     * @param nSatBound
     * @param sma
     * @param inc
     * @param properties
     */
    public ConstellationOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory,
            Set<GeodeticPoint> poi, double halfAngle, Bounds<Integer> nSatBound, Bounds<Double> sma,
            Bounds<Double> inc, Properties properties) {
        this(name, startDate, endDate, propagatorFactory, poi, halfAngle, nSatBound,
                sma, new Bounds(0.0, 0.0000000001), inc, 
                new Bounds(0.0, 2 * Math.PI), new Bounds(0.0, 2 * Math.PI), new Bounds(0.0, 2 * Math.PI), properties);
    }

    public ConstellationOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory, Set<GeodeticPoint> poi, double halfAngle, Bounds<Integer> nSatBound, 
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> raan, Bounds<Double> ap, Bounds<Double> ta, Properties properties) {
        super(1, 3);

        try {
            OrekitConfig.init();
            this.startDate = startDate;
            this.endDate = endDate;
            this.timeScale = TimeScalesFactory.getUTC();
            this.inertialFrame = FramesFactory.getEME2000();
            this.propagatorFactory = propagatorFactory;
            this.halfAngle = halfAngle;
            this.poi = poi;
            this.properties = properties;
            this.nSatBound = nSatBound;
            this.smaBound = sma;
            this.eccBound = ecc;
            this.incBound = inc;
            this.raanBound = raan;
            this.apBound = ap;
            this.taBound = ta;

            //must use IERS_2003 and EME2000 frames to be consistent with STK
            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2003, true);

            this.earthShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING, earthFrame);
            this.earthMu = Constants.WGS84_EARTH_MU;

        } catch (OrekitException ex) {
            Logger.getLogger(ConstellationOptimizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Failed to create a new problem");
        }
    }

    @Override
    public void evaluate(Solution solution) {

        ArrayList<Constellation> constellations = new ArrayList();
        ConstellationVariable constel = (ConstellationVariable) solution.getVariable(0);
        
        ArrayList<Satellite> satelliteList = new ArrayList<>();
        for(SatelliteVariable var : constel.getSatelliteVariables()){
            if(var instanceof BooleanSatelliteVariable){
                if(!((BooleanSatelliteVariable)var).getManifest()){
                    continue;
                }
            }
            
            Orbit orb = new KeplerianOrbit(
                    var.getSma(), var.getEcc(), var.getInc(), 
                    var.getArgPer(), var.getRaan(), var.getTrueAnomaly(),
                    PositionAngle.TRUE, inertialFrame, startDate, earthMu);
            satelliteList.add(new Satellite("sat", orb, null, new ArrayList()));
        }
        
        constellations.add(new Constellation("constel", satelliteList));

        CoverageDefinition cdef = new CoverageDefinition("", poi, earthShape);
        cdef.assignConstellation(constellations);
        HashSet<CoverageDefinition> cdefSet = new HashSet<>();
        cdefSet.add(cdef);

        ArrayList<EventAnalysis> eventanalyses = new ArrayList<>();
        FastCoverageAnalysis fca = new FastCoverageAnalysis(startDate, endDate, 
                inertialFrame, cdefSet, halfAngle, 
                Integer.parseInt(properties.getProperty("numThreads", "1")));
        eventanalyses.add(fca);

        Scenario scen = new Scenario("", startDate, endDate, timeScale,
                inertialFrame, propagatorFactory, cdefSet, eventanalyses, new ArrayList<>(), properties);
        try {
            scen.call();
        } catch (Exception ex) {
            Logger.getLogger(ConstellationOptimizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation failed");
        }

        GroundEventAnalyzer gea = new GroundEventAnalyzer(fca.getEvents(cdef));
        DescriptiveStatistics gapStats = gea.getStatistics(AnalysisMetric.DURATION, false);
        solution.setObjective(0, gapStats.getMean());
        solution.setObjective(1, satelliteList.size());
        
        //compute average semi-major axis
        DescriptiveStatistics stats = new DescriptiveStatistics();
        for(Satellite sat : satelliteList){
            stats.addValue(sat.getOrbit().getA());
        }
        solution.setObjective(2, stats.getMean());
    }

    @Override
    public Solution newSolution() {
        Solution soln = new Solution(numberOfVariables, numberOfObjectives);
//        soln.setVariable(0, new ConstellationVariable(nSatBound, smaBound, eccBound, incBound, apBound, raanBound, taBound));
        soln.setVariable(0, new ConstellationMatrix(nSatBound, smaBound, eccBound, incBound, apBound, raanBound, taBound));
        return soln;
    }

}
