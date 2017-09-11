/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop;

import aos.IO.IOCreditHistory;
import aos.IO.IOSelectionHistory;
import aos.aos.AOSMOEA;
import aos.aos.AOSStrategy;
import aos.creditassigment.ICreditAssignment;
import aos.creditassignment.setcontribution.ArchiveContribution;
import aos.nextoperator.IOperatorSelector;
import aos.operator.AOSVariation;
import aos.operatorselectors.AdaptivePursuit;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hipparchus.util.FastMath;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.EpsilonBoxDominanceArchive;
import org.moeaframework.core.Initialization;
import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Problem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import org.moeaframework.core.comparator.DominanceComparator;
import org.moeaframework.core.comparator.ParetoDominanceComparator;
import org.moeaframework.core.operator.CompoundVariation;
import org.moeaframework.core.operator.RandomInitialization;
import org.moeaframework.core.operator.TournamentSelection;
import org.moeaframework.core.operator.binary.BitFlip;
import org.moeaframework.core.operator.real.PM;
import org.moeaframework.core.operator.real.SBX;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import seak.orekit.propagation.PropagatorFactory;
import seak.orekit.propagation.PropagatorType;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import seak.conmop.operators.BinaryUniformCrossover;
import seak.conmop.operators.OrbitElementOperator;
import seak.conmop.operators.RepairNumberOfSatellites;
import seak.conmop.operators.StaticOrbitElementOperator;
import seak.conmop.operators.StaticLengthOnePointCrossover;
import seak.conmop.operators.VariableLengthOnePointCrossover;
import seak.conmop.operators.VariablePM;
import seak.conmop.util.Bounds;
import seak.orekit.STKGRID;
import seak.orekit.object.CoverageDefinition;
import seak.orekit.object.CoveragePoint;
import seak.orekit.util.OrekitConfig;

/**
 *
 * @author nozomihitomi
 */
public class Search {

    private static final double DEG_TO_RAD = Math.PI / 180.;

    /**
     * @param args the command line arguments
     * @throws org.orekit.errors.OrekitException
     */
    public static void main(String[] args) throws OrekitException {
        //setup logger
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);

        //if running on a non-US machine, need the line below
        Locale.setDefault(new Locale("en", "US"));

        OrekitConfig.init();

        TimeScale utc = TimeScalesFactory.getUTC();
        AbsoluteDate startDate = new AbsoluteDate(2016, 1, 1, 00, 00, 00.000, utc);
        AbsoluteDate endDate = new AbsoluteDate(2016, 1, 8, 00, 00, 00.000, utc);

        //Enter satellite orbital parameters
        double a = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;

        PropagatorFactory pf = new PropagatorFactory(PropagatorType.KEPLERIAN);

        Properties problemProperty = new Properties();
        problemProperty.setProperty("numThreads", "10");

        Bounds<Integer> tBounds = new Bounds(1, 20);
        Bounds<Double> smaBounds = new Bounds(a + 400000, a + 1000000);
        Bounds<Double> incBounds = new Bounds(30. * DEG_TO_RAD, 100. * DEG_TO_RAD);

        Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2003, true);
        BodyShape earthShape = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                Constants.WGS84_EARTH_FLATTENING, earthFrame);
        CoverageDefinition cdef = new CoverageDefinition("cdef", 20.0, earthShape, CoverageDefinition.GridStyle.EQUAL_AREA);
        Set<GeodeticPoint> points = new HashSet<>();
        for (CoveragePoint pt : cdef.getPoints()) {
            points.add(pt.getPoint());
        }
        Problem problem = new ConstellationOptimizer("", startDate, endDate, pf,
                points, FastMath.toRadians(45),
                tBounds, smaBounds, incBounds, problemProperty);

        //set up the search parameters
        int populationSize = 100;
        int maxNFE = 5000;
//        String mode = "static_";
        String mode = "variable_";

        for (int i = 0; i < 30; i++) {

            long startTime = System.nanoTime();
            Initialization initialization = new RandomInitialization(problem,
                    populationSize);

            Population population = new Population();
            DominanceComparator comparator = new ParetoDominanceComparator();
            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(new double[]{1, 0.1});
            final TournamentSelection selection = new TournamentSelection(2, comparator);
            AOSVariation variation = new AOSVariation();
            EpsilonMOEA emoea = new EpsilonMOEA(problem, population, archive,
                    selection, variation, initialization, comparator);

            //set up variations
            //example of operators you might use
            ArrayList<Variation> operators = new ArrayList();
            operators.add(new CompoundVariation(
                    new OrbitElementOperator(
                            new CompoundVariation(new SBX(1, 20), new VariablePM(20))),
                    new RepairNumberOfSatellites()));
            operators.add(new CompoundVariation(
                    new VariableLengthOnePointCrossover(1.0, true),
                    new RepairNumberOfSatellites()));
//            operators.add(new CompoundVariation(
//                    new StaticOrbitElementOperator(
//                            new CompoundVariation(new SBX(1, 20),
//                                    new BinaryUniformCrossover(0.5), new VariablePM(20),
//                                    new BitFlip(1./140.))),
//                    new RepairNumberOfSatellites()
//            ));
//            operators.add(new CompoundVariation(
//                    new StaticLengthOnePointCrossover(1.0), new StaticOrbitElementOperator(
//                            new CompoundVariation(new VariablePM(20), new BitFlip(1./140.))),
//                            new RepairNumberOfSatellites()));

            //create operator selector
            IOperatorSelector operatorSelector = new AdaptivePursuit(operators, 0.8, 0.8, 0.1);

            //create credit assignment
            ICreditAssignment creditAssignment = new ArchiveContribution(1, 0);

            //create AOS
            AOSStrategy aosStrategy = new AOSStrategy(creditAssignment, operatorSelector);
            AOSMOEA aos = new AOSMOEA(emoea, variation, aosStrategy);

            HashSet<Solution> allSolutions = new HashSet<>();

            System.out.println(String.format("Initializing population... Size = %d", populationSize));
            while (aos.getNumberOfEvaluations() < maxNFE) {
                aos.step();
                double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
                System.out.println(
                        String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
                                + " Approximate time remaining %10f min.",
                                aos.getNumberOfEvaluations(), maxNFE, currentTime,
                                currentTime / emoea.getNumberOfEvaluations() * (maxNFE - aos.getNumberOfEvaluations())));
                for (Solution solution : aos.getPopulation()) {
                    allSolutions.add(solution);
                }
            }
            System.out.println(aos.getArchive().size());

            long endTime = System.nanoTime();
            Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));

            try {
                PopulationIO.write(new File(mode + i + ".pop"), allSolutions);
                PopulationIO.writeObjectives(new File(mode + i + ".obj"), allSolutions);
            } catch (IOException ex) {
                Logger.getLogger(Search.class.getName()).log(Level.SEVERE, null, ex);
            }
            IOCreditHistory.saveHistory(aos.getCreditHistory(), mode + i + ".credit", ",");
            IOSelectionHistory.saveHistory(aos.getSelectionHistory(), mode + i + ".select", ",");
        }
    }

}
