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
import org.hipparchus.util.FastMath;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;
import org.moeaframework.problem.AbstractProblem;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import seak.conmop.util.Bounds;
import seak.conmop.variable.WalkerVariable;
import seak.orekit.constellations.Walker;
import seak.orekit.coverage.analysis.AnalysisMetric;
import seak.orekit.coverage.analysis.GroundEventAnalyzer;
import seak.orekit.event.EventAnalysis;
import seak.orekit.event.EventAnalysisEnum;
import seak.orekit.event.EventAnalysisFactory;
import seak.orekit.event.FieldOfViewEventAnalysis;
import seak.orekit.object.Constellation;
import seak.orekit.object.CoverageDefinition;
import seak.orekit.object.Instrument;
import seak.orekit.object.Satellite;
import seak.orekit.object.fieldofview.NadirSimpleConicalFOV;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.scenario.Scenario;
import seak.orekit.util.OrekitConfig;

/**
 * Optimizer for a simple 1 coverage definition problem
 *
 * @author nhitomi
 */
public class WalkerOptimizer extends AbstractProblem {

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
     * The bounds allowable on the total number of satellites
     */
    private final Bounds<Integer> tBound;

    /**
     * The bounds allowable on the number of planes
     */
    private final Bounds<Integer> pBound;

    /**
     * The bounds allowable on the phasing value
     */
    private final Bounds<Integer> fBound;

    /**
     * The bounds allowable on the semi-major axis [m]
     */
    private final Bounds<Double> smaBound;

    /**
     * The bounds allowable on the inclination [rad]
     */
    private final Bounds<Double> incBound;

    /**
     * The instrument to use for each satellite in the constellation
     */
    private final Instrument view;

    /**
     * The shape of the Earth
     */
    private final BodyShape earthShape;

    /**
     * The standard gravitational constant for Earth
     */
    private final double earthMu;

    /**
     * Constructor for that allows all feasible values for p and f.
     *
     * @param name
     * @param startDate
     * @param endDate
     * @param propagatorFactory
     * @param poi
     * @param tBound
     * @param sma
     * @param inc
     * @param properties
     */
    public WalkerOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory,
            Set<GeodeticPoint> poi, Bounds<Integer> tBound,
            Bounds<Double> sma,
            Bounds<Double> inc, Properties properties) {
        this(name, startDate, endDate, propagatorFactory, poi, tBound, tBound,
                new Bounds(0, tBound.getUpperBound() - 1), sma, inc, properties);
    }

    public WalkerOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory,
            Set<GeodeticPoint> poi, Bounds<Integer> tBound,
            Bounds<Integer> pBound, Bounds<Integer> fBound, Bounds<Double> sma,
            Bounds<Double> inc, Properties properties) {
        super(1, 2);

        try {
            OrekitConfig.init();
            this.startDate = startDate;
            this.endDate = endDate;
            this.timeScale = TimeScalesFactory.getUTC();
            this.inertialFrame = FramesFactory.getEME2000();
            this.propagatorFactory = propagatorFactory;
            this.poi = poi;
            this.properties = properties;
            this.tBound = tBound;
            this.pBound = pBound;
            this.fBound = fBound;
            this.smaBound = sma;
            this.incBound = inc;

            //must use IERS_2003 and EME2000 frames to be consistent with STK
            Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2003, true);

            this.earthShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                    Constants.WGS84_EARTH_FLATTENING, earthFrame);
            this.earthMu = Constants.WGS84_EARTH_MU;
            
            NadirSimpleConicalFOV fov = new NadirSimpleConicalFOV(FastMath.toRadians(45), earthShape);
            this.view = new Instrument("view", fov, 100, 100);

        } catch (OrekitException ex) {
            Logger.getLogger(WalkerOptimizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Failed to create a new problem");
        }
    }

    @Override
    public void evaluate(Solution solution) {
        
        ArrayList<Constellation> constellations = new ArrayList();
        
        RealVariable sma = (RealVariable) solution.getVariable(0);
        RealVariable inc = (RealVariable) solution.getVariable(1);
        WalkerVariable wv = (WalkerVariable) solution.getVariable(2);
        ArrayList<Instrument> payload = new ArrayList<>();
        payload.add(view);
        Walker walker = new Walker("", payload, sma.getValue(), inc.getValue(), 
                wv.getT(), wv.getP(), wv.getF(), inertialFrame, startDate, earthMu);
        constellations.add(walker);

        CoverageDefinition cdef = new CoverageDefinition("", poi, earthShape);
        cdef.assignConstellation(constellations);
        HashSet<CoverageDefinition> cdefSet = new HashSet<>();
        cdefSet.add(cdef);

        EventAnalysisFactory eaf = new EventAnalysisFactory(startDate, endDate, inertialFrame, propagatorFactory);
        ArrayList<EventAnalysis> eventanalyses = new ArrayList<>();
        FieldOfViewEventAnalysis fovEvent = (FieldOfViewEventAnalysis) eaf.createGroundPointAnalysis(EventAnalysisEnum.FOV, cdefSet, properties);
        eventanalyses.add(fovEvent);

        Scenario scen = new Scenario("", startDate, endDate, timeScale,
                inertialFrame, propagatorFactory, cdefSet, eventanalyses, null, properties);
        try {
            scen.call();
        } catch (Exception ex) {
            Logger.getLogger(WalkerOptimizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation failed");
        }

        GroundEventAnalyzer gea = new GroundEventAnalyzer(fovEvent.getEvents(cdef));
        DescriptiveStatistics gapStats = gea.getStatistics(AnalysisMetric.DURATION, false, new Properties());
        solution.setObjective(0, gapStats.getMean());
        solution.setObjective(1, walker.getSatellites().size());
    }

    @Override
    public Solution newSolution() {
        Solution soln = new Solution(numberOfVariables, numberOfObjectives);
        soln.setVariable(0, new WalkerVariable(smaBound, incBound, tBound, pBound, fBound));
        return soln;
    }

}
