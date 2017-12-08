/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.deployment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.hipparchus.util.FastMath;
import seak.architecture.enumeration.FullFactorial;
import seak.conmop.comparators.SatelliteComparator;
import seak.conmop.launch.DeltaV;
import seak.conmop.util.OrbitalElementEnum;
import seak.conmop.variable.SatelliteVariable;
import seak.orekit.util.Orbits;

/**
 * Class that contains strategies on constellation deployment
 *
 * @author nozomihitomi
 */
public class ConstellationDeployment {

    /**
     * Checks to see if a given group of satellites can be deployed together
     * using a space tug that deploys each satellite at their proper orbits. The
     * space tug deltaV is used as the limiting factor. In this strategy, it is
     * assumed that the tug starts at the highest altitude satellite, deploys it
     * first, and then deploys the other satellites such that a minimal deltaV
     * is required. The highest altitude satellite is deployed first to reduce
     * the deltaV required for any plane changes. In addition, all orbits are
     * assumed to be circular. Altitude changes are conducted using a Hohmann
     * Transfer. Only semi-major axis and inclination are considered.
     *
     * @param satellites the satellites to be deployed
     * @param deltaVLimit the limited delta V [m/s]
     * @return a list defining the order in which to deploy the satellites with
     * a minimal deltaV. If the required deltaV exceeds the specified limit,
     * then an empty list is returned
     */
    public static List<SatelliteVariable> deltaVCompatible(Collection<SatelliteVariable> satellites, double deltaVLimit) {
        //handle the trivial case
        if (satellites.size() <= 1) {
            return new ArrayList<>(satellites);
        }

        //find highest altitude satellite
        double maxSma = Double.NEGATIVE_INFINITY;
        for (SatelliteVariable sat : satellites) {
            maxSma = FastMath.max(maxSma, sat.getSma());
        }

        //make a list ascedning in inclination
        ArrayList<SatelliteVariable> satIncList = new ArrayList(satellites);
        Collections.sort(satIncList, new SatelliteComparator(OrbitalElementEnum.INC));

        //Find best case deltaV for all the plane changes conducted at highest altitude
        double minDVPlaneChange = 0;
        double vel = Orbits.meanMotion(maxSma) * maxSma;
        for (int i = 1; i < satIncList.size(); i++) {
            minDVPlaneChange += DeltaV.simplePlaneChange(vel, satIncList.get(i).getInc() - satIncList.get(i - 1).getInc());
            if (minDVPlaneChange > deltaVLimit) {
                return new ArrayList();
            }
        }

        double minDV = Double.POSITIVE_INFINITY;
        ArrayList<SatelliteVariable> minDVOrder = new ArrayList<>();
        Collection<int[]> orderings = FullFactorial.ffPermuting(satellites.size());
        for (int[] permutation : orderings) {
            ArrayList<SatelliteVariable> order = new ArrayList<>();
            for (int i = 0; i < satellites.size(); i++) {
                order.add(satIncList.get(permutation[i]));
            }
            double dv = deploymentDV(order);
            if (dv < minDV) {
                minDV = dv;
                minDVOrder = order;
            }
        }

        return minDVOrder;
    }

    /**
     * Computes the delta v required to deploy the satellites in the order of
     * the given list using a single tug. Delta V from one satellite's orbit is
     * to another is given by a combined maneuver of a plane change and Hohmann
     * Transfer. Assumes all orbits are circular. Only semi-major axis and
     * inclination are considered.
     *
     * @param satellites a list of satellites to deploy in a specified order
     * with a single tug
     * @return the required deltaV [m/s]
     */
    public static double deploymentDV(List<SatelliteVariable> satellites) {
        double deltaV = 0;

        for (int i = 1; i < satellites.size(); i++) {
            double sma1 = satellites.get(i - 1).getSma();
            double inc1 = satellites.get(i - 1).getInc();

            double sma2 = satellites.get(i).getSma();
            double inc2 = satellites.get(i).getInc();

            double vel1 = Orbits.circularOrbitVelocity(sma1);
            deltaV
                    += DeltaV.combinedPlaneChange(vel1,
                            vel1 + DeltaV.hohmannFirstBurn(sma1, sma2),
                            FastMath.toRadians(inc1 - inc2))
                    + DeltaV.hohmannSecondBurn(sma1, sma2);
        }

        return deltaV;
    }

    /**
     * Checks to see if two spacecraft are close enough to each other in their
     * ascending nodes. The relative drift in their nodes is computed and if the
     * difference in their ascending nodes cannot be achieved through nodal
     * precession alone in a limited time, then the two spacecraft should not be
     * flown on the same launch vehicle. In this case, false is returned.
     *
     * @param sat1 satellite variable
     * @param sat2 satellite variable
     * @param time the time [s] limit for the two spacecraft to drift to their
     * ascending node due solely to nodal precession.
     * @return false if the two spacecraft should be flown together in the same
     * launch vehicle
     */
    public static boolean raanCompatitble(SatelliteVariable sat1, SatelliteVariable sat2, double time) {
        double deltaRaan = FastMath.abs(sat1.getRaan() - sat2.getRaan());
        double relativePrecessionRate = FastMath.abs(Orbits.nodalPrecession(sat1.getSma(), sat1.getEcc(), sat1.getInc())
                - Orbits.nodalPrecession(sat2.getSma(), sat2.getEcc(), sat2.getInc()));

        return deltaRaan <= relativePrecessionRate * time;
    }

}
