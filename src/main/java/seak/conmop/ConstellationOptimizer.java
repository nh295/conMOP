/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.hipparchus.util.FastMath;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import org.moeaframework.util.Vector;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.Orbit;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import seak.conmop.deployment.ConstellationDeployment;
import seak.conmop.deployment.DeploymentStrategy;
import seak.conmop.deployment.Installment;
import seak.conmop.launch.DeltaV;
import seak.conmop.util.Bounds;
import seak.conmop.variable.BooleanSatelliteVariable;
import seak.conmop.variable.ConstellationMatrix;
import seak.conmop.variable.ConstellationVariable;
import seak.conmop.variable.SatelliteVariable;
import seak.orekit.coverage.access.TimeIntervalArray;
import seak.orekit.coverage.analysis.AnalysisMetric;
import seak.orekit.coverage.analysis.FastCoverageAnalysis;
import seak.orekit.coverage.analysis.GroundEventAnalyzer;
import seak.orekit.event.EventAnalysis;
import seak.orekit.event.GndStationEventAnalysis;
import seak.orekit.object.CommunicationBand;
import seak.orekit.object.Constellation;
import seak.orekit.object.CoverageDefinition;
import seak.orekit.object.GndStation;
import seak.orekit.object.Satellite;
import seak.orekit.object.communications.ReceiverAntenna;
import seak.orekit.object.communications.TransmitterAntenna;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.scenario.Scenario;
import seak.orekit.util.Orbits;

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
     * The time limit [s] for right ascensions to line up of any two orbital
     * planes
     */
    private final double raanTimeLimit;

    /**
     * The delta V [m/s] for deploying multiple satellites with one space tug
     */
    private final double tugDvLimit;

    /**
     * The latitude [rad] of the launch site
     */
    private final double launchLatitude;

    /**
     * The ground stations for downlink
     */
    private final Collection<GndStation> gndStations;

    /**
     * a dummy constructor for analyzing hypervolumes after the optimization
     */
    public ConstellationOptimizer() {
        this(null, null, null, null, null, 0.0, null, null, null, null, new Properties());
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
     * @param gndStations
     * @param properties
     */
    public ConstellationOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory,
            Set<GeodeticPoint> poi, double halfAngle, Bounds<Integer> nSatBound, Bounds<Double> sma,
            Bounds<Double> inc, Collection<GndStation> gndStations, Properties properties) {
        this(name, startDate, endDate, propagatorFactory, poi, halfAngle, nSatBound,
                sma, new Bounds(0.0, 0.0), inc,
                new Bounds(0.0, 2 * Math.PI), new Bounds(0.0, 0.0), new Bounds(0.0, 2 * Math.PI), gndStations, properties);
    }

    public ConstellationOptimizer(String name, AbsoluteDate startDate, AbsoluteDate endDate,
            PropagatorFactory propagatorFactory, Set<GeodeticPoint> poi, double halfAngle, Bounds<Integer> nSatBound,
            Bounds<Double> sma, Bounds<Double> ecc, Bounds<Double> inc,
            Bounds<Double> raan, Bounds<Double> ap, Bounds<Double> ta,
            Collection<GndStation> gndStations, Properties properties) {
        super(1, 4);

        try {
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
            this.gndStations = gndStations;

            this.raanTimeLimit = Double.parseDouble(properties.getProperty("raanTimeLimit", "604800"));
            this.tugDvLimit = Double.parseDouble(properties.getProperty("dvLimit", "2200"));
            this.launchLatitude = Double.parseDouble(properties.getProperty("launchLatitude", "0"));

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
        for (SatelliteVariable var : constel.getSatelliteVariables()) {
            if (var instanceof BooleanSatelliteVariable) {
                if (!((BooleanSatelliteVariable) var).getManifest()) {
                    continue;
                }
            }
            //avoid critically inclined orbit which causes propagation issues
            if (FastMath.abs(var.getInc() - 1.1074628348333333) < 0.05) {
                var.setInc(1.109208162611111);
            }
            Orbit orb = var.toOrbit(inertialFrame, startDate, earthMu);
            HashSet<CommunicationBand> comms = new HashSet<>();
            comms.add(CommunicationBand.UHF);
            satelliteList.add(new Satellite("sat", orb, null, new ArrayList(),
                    new ReceiverAntenna(1, comms), new TransmitterAntenna(1, comms), 100, 100));
        }

        constellations.add(new Constellation("constel", satelliteList));

        CoverageDefinition cdef = new CoverageDefinition("", poi, earthShape);
        cdef.assignConstellation(constellations);
        HashSet<CoverageDefinition> cdefSet = new HashSet<>();
        cdefSet.add(cdef);

        ArrayList<EventAnalysis> eventanalyses = new ArrayList<>();
        FastCoverageAnalysis fca = new FastCoverageAnalysis(startDate, endDate,
                inertialFrame, cdefSet, halfAngle);
        eventanalyses.add(fca);

        //declare ground access analysis
        //assign each satellite to each ground station
        Map<Satellite, Set<GndStation>> stationAssignment = new HashMap<>();
        for (Satellite sat : satelliteList) {
            stationAssignment.put(sat, new HashSet(gndStations));
        }
        GndStationEventAnalysis gndStaEA = new GndStationEventAnalysis(startDate, endDate, inertialFrame, stationAssignment, propagatorFactory);
        eventanalyses.add(gndStaEA);

        Scenario scen = new Scenario("", startDate, endDate, timeScale,
                inertialFrame, propagatorFactory, cdefSet, eventanalyses, new ArrayList<>(), properties);
        try {
            scen.call();
        } catch (Exception ex) {
            Logger.getLogger(ConstellationOptimizer.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalStateException("Evaluation failed");
        }

        GroundEventAnalyzer gea = new GroundEventAnalyzer(fca.getEvents(cdef));
        Properties properties = new Properties();
        properties.setProperty("threshold", "7200.0");

        DescriptiveStatistics respStats = gea.getStatistics(AnalysisMetric.MEAN_TIME_TO_T, false, properties);
        DescriptiveStatistics gapStats = gea.getStatistics(AnalysisMetric.DURATION, false, properties);
        solution.setObjective(0, respStats.getMean());
        solution.setObjective(1, satelliteList.size());
//        solution.setObjective(1, gapStats.getPercentile(90));

//        //compute average semi-major axis
//        DescriptiveStatistics stats = new DescriptiveStatistics();
//        for (Satellite sat : satelliteList) {
//            stats.addValue(sat.getOrbit().getA());
//        }
//        solution.setObjective(2, stats.getMean());
        DeploymentStrategy deployment = deploymentStrategy(constel.getSatelliteVariables());
        constel.setDeploymentStrategy(deployment);
        solution.setObjective(2, deployment.getTotalDV());

        //check ground sataion gap for each satellite
        double maxGndGap = Double.NEGATIVE_INFINITY;
        for (Satellite sat : satelliteList) {
            GroundEventAnalyzer gndStaAnalyzer = new GroundEventAnalyzer(gndStaEA.getEvents(sat));
            DescriptiveStatistics gndGapStats = gndStaAnalyzer.getStatistics(AnalysisMetric.DURATION, false, properties);
            maxGndGap = FastMath.max(maxGndGap, gndGapStats.getMax());
        }

        solution.setObjective(3, maxGndGap);
    }

    @Override
    public Solution newSolution() {
        Solution soln = new Solution(numberOfVariables, numberOfObjectives);
        soln.setVariable(0, new ConstellationVariable(nSatBound, smaBound, eccBound, incBound, apBound, raanBound, taBound));
//        soln.setVariable(0, new ConstellationMatrix(nSatBound, smaBound, eccBound, incBound, apBound, raanBound, taBound));
        return soln;
    }

    /**
     * Computes the delta v required to deploy the entire constellation by
     * checking to see if there are large groups that can be launched together
     *
     * @param satellites
     * @return
     */
    private DeploymentStrategy deploymentStrategy(Collection<SatelliteVariable> satellites) {
        //check inclinations and raan first
        Map<Double, Map<Double, List<SatelliteVariable>>> map = new HashMap();
        for (SatelliteVariable sat : satellites) {
            if (!map.containsKey(sat.getInc())) {
                map.put(sat.getInc(), new HashMap<>());
            }
            if (!map.get(sat.getInc()).containsKey(sat.getRaan())) {
                map.get(sat.getInc()).put(sat.getRaan(), new ArrayList<>());
            }
            map.get(sat.getInc()).get(sat.getRaan()).add(sat);
        }
        
        Collection<SatelliteVariable> unassignedSats = new ArrayList<>();

        //check for large groups
        Collection<Collection<SatelliteVariable>> largeGroups = new ArrayList();
        for (Double inc : map.keySet()) {
            for (Double raan : map.get(inc).keySet()) {
                if (map.get(inc).get(raan).size() > 5) {
                    largeGroups.add(map.get(inc).get(raan));
                }else{
                    unassignedSats.addAll(map.get(inc).get(raan));
                }
            }
        }

        //Check if the groups can be launched together
        Collection<List<SatelliteVariable>> feasibleLargeDeployments = new ArrayList();
        for (Collection<SatelliteVariable> satGroup : largeGroups) {
            List<SatelliteVariable> satList = new ArrayList(satGroup);
            boolean meetsConstraints = true;
            for (int i = 1; i < satList.size(); i++) {
                //check RAAN constraint with newly added satellite
                if (!ConstellationDeployment.raanCompatitble(satList.get(i - 1), satList.get(i), raanTimeLimit)) {
                    meetsConstraints = false;
                    break;
                }
            }
            if (ConstellationDeployment.deltaVCompatible(satGroup, tugDvLimit).isEmpty()) {
                meetsConstraints = false;
            }
            if(meetsConstraints){
                feasibleLargeDeployments.add(satList);
            }else{
                unassignedSats.addAll(satGroup);
            }
        }
        
        //find assignments for the other satellites
        Collection<Collection<List<SatelliteVariable>>> feasibleDeployments = new ArrayList();
        if(unassignedSats.isEmpty()){
            feasibleDeployments.add(new ArrayList());
        }else{
            feasibleDeployments = enumeratePartitions(unassignedSats);
        }
        ArrayList<Installment> minLaunchDeployment = new ArrayList<>();
        double minDV = Double.POSITIVE_INFINITY;
        for (Collection<List<SatelliteVariable>> deployment : feasibleDeployments) {
            //add the large groups
            deployment.addAll(feasibleLargeDeployments);
            
            ArrayList<Installment> bestDeployment = new ArrayList<>();
            double dv = 0.0;
            int nSatAssinged = 0;

            for (List<SatelliteVariable> launchGroup : deployment) {
                List<SatelliteVariable> bestOrder = ConstellationDeployment.deltaVCompatible(launchGroup, tugDvLimit);
                if (bestOrder.isEmpty()) {
                    bestDeployment.clear();
                    break;
                }

                double tugDV = ConstellationDeployment.deploymentDV(bestOrder);

                //add the dv required to get to first satellite in deployment order
                double[][] v = DeltaV.launch(bestOrder.get(0).getInc(),
                        launchLatitude,
                        Orbits.circularOrbitVelocity(bestOrder.get(0).getSma()),
                        0.0);
                double launchDV = FastMath.min(Vector.magnitude(v[0]), Vector.magnitude(v[1]));

                bestDeployment.add(new Installment(bestOrder, launchDV, tugDV));
                nSatAssinged += bestOrder.size();

                dv += tugDV + launchDV;
            }

            if (nSatAssinged != satellites.size()) {
                throw new IllegalStateException("All satellites not included in deployment strategy");
            }

            if (minLaunchDeployment.isEmpty() || bestDeployment.size() < minLaunchDeployment.size()) {
                minLaunchDeployment = bestDeployment;
                minDV = dv;
            } else if (bestDeployment.size() == minLaunchDeployment.size() && dv < minDV) {
                minLaunchDeployment = bestDeployment;
            }

        }
        if (minLaunchDeployment.isEmpty()) {
            throw new IllegalStateException("No deployment strategy found!");
        }
        return new DeploymentStrategy(minLaunchDeployment);
    }

    private Collection<Collection<List<SatelliteVariable>>> enumeratePartitions(Collection<SatelliteVariable> satellites) {
        ArrayList<SatelliteVariable> sats = new ArrayList<>(satellites);

        //in implementation, we maintain the maximum partition number of the array in the 0th index
        Collection<int[]> prev = new ArrayList();
        prev.add(new int[]{0, 0});
        Collection<Collection<List<SatelliteVariable>>> out;
        ArrayList<int[]> curr = new ArrayList();;
        boolean trivial = false;

        //handle trivial cases
        if (sats.size() == 1) {
            curr = new ArrayList(prev);
            trivial = true;
        }

        while (!trivial) {
            curr = new ArrayList();
            for (int[] subPart : prev) {
                for (int partNum = 0; partNum <= subPart[0] + 1; partNum++) {
                    int[] extended = Arrays.copyOf(subPart, subPart.length + 1);
                    extended[subPart.length] = partNum;

                    //check to see if new member belongs to a new partition
                    if (subPart[0] < partNum) {
                        extended[0] = partNum;
                        curr.add(extended);
                    } else {
                        boolean meetsConstraints = true;

                        for (int i = 1; i < extended.length - 1; i++) {
                            //check if newly added satellite belongs in the same partition as other satellite
                            if (extended[extended.length - 1] == extended[i]) {

                                //check RAAN constraint with newly added satellite (minus 2 because first index is max partition number)
                                SatelliteVariable newSat = sats.get(extended.length - 2);
                                if (!ConstellationDeployment.raanCompatitble(newSat, sats.get(i - 1), raanTimeLimit)) {
                                    meetsConstraints = false;
                                    break;
                                }

                                //check to see if all satellites in same parition as newly added satellite can be launched together
                                ArrayList<SatelliteVariable> group = new ArrayList<>();
                                group.add(newSat);
                                for (int j = 1; j < extended.length - 1; j++) {
                                    if (extended[j] == extended[extended.length - 1]) {
                                        group.add(sats.get(j - 1));
                                    }
                                }

                                if (ConstellationDeployment.deltaVCompatible(group, tugDvLimit).isEmpty()) {
                                    meetsConstraints = false;
                                    break;
                                }
                            }
                        }
                        if (meetsConstraints) {
                            //add deployment strategy is constraints are met
                            curr.add(extended);
                        }
                    }
                }
            }

            //quite loop when all paritions are complete
            if (curr.get(0).length == sats.size() + 1) {
                break;
            }

            prev = curr;
        }

        out = new ArrayList(curr.size());
        for (int[] partition : curr) {
            //map contains the launch groups (keys are partition numbers)
            HashMap<Integer, ArrayList<SatelliteVariable>> map = new HashMap<>();
            for (int i = 1; i < partition.length; i++) {
                if (!map.containsKey(partition[i])) {
                    map.put(partition[i], new ArrayList<>());
                }
                map.get(partition[i]).add(sats.get(i - 1));
            }
            ArrayList<List<SatelliteVariable>> deployment = new ArrayList<>();
            for (Integer i : map.keySet()) {
                deployment.add(map.get(i));
            }
            out.add(deployment);
        }
        return out;
    }

}
