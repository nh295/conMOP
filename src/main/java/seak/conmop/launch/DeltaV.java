/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package seak.conmop.launch;

import org.hipparchus.util.FastMath;
import org.orekit.utils.Constants;
import seak.orekit.util.Orbits;

/**
 * Computes delta v for different orbit maneuvers
 *
 * @author nozomihitomi
 */
public class DeltaV {

    /**
     * Computes the delta v required for the first burn of a Hohmann transfer
     * from one semi-major axis to another
     *
     * @param ra initial semi-major axis [m]
     * @param rb final semi-major axis [m]
     * @return the delta V required for the Hohmann transfer
     */
    public static double hohmannFirstBurn(double ra, double rb) {
        double atx = (ra + rb) / 2;
        double viA = Orbits.circularOrbitVelocity(ra);
        double vtxA = FastMath.sqrt(Constants.WGS84_EARTH_MU * (2 / ra - 1 / atx));

        return FastMath.abs(vtxA - viA);
    }

    /**
     * Computes the delta v required for the second burn of a Hohmann transfer
     * from one semi-major axis to another
     *
     * @param ra initial semi-major axis [m]
     * @param rb final semi-major axis [m]
     * @return the delta V required for the Hohmann transfer
     */
    public static double hohmannSecondBurn(double ra, double rb) {
        double atx = (ra + rb) / 2;
        double vfB = Orbits.circularOrbitVelocity(ra);
        double vtxB = FastMath.sqrt(Constants.WGS84_EARTH_MU * (2 / rb - 1 / atx));

        return FastMath.abs(vfB - vtxB);
    }

    /**
     * Computes the delta v required for a Hohmann transfer from one semi-major
     * axis to another
     *
     * @param ra initial semi-major axis [m]
     * @param rb final semi-major axis [m]
     * @return the delta V required for the Hohmann transfer
     */
    public static double hohmannTransfer(double ra, double rb) {
        return hohmannFirstBurn(ra, rb) + hohmannSecondBurn(ra, rb);
    }

    /**
     * Returns the delta v required for a plane change. The magnitude of the
     * initial velocity vector and the final vector are equal
     *
     * @param velocity the velocity [m/s] of the spacecraft
     * @param theta the angle [rad] between the initial plane and the final
     * plane
     * @return the delta v required for a simple plane change
     */
    public static double simplePlaneChange(double velocity, double theta) {
        return 2 * velocity * FastMath.sin(theta / 2);
    }

    /**
     * Returns the delta v required for a plane change where the magnitude of
     * the initial velocity vector and the final vector are not equal
     *
     * @param veli the initial velocity [m/s] of the spacecraft
     * @param velf the final velocity [m/s] of the spacecraft
     * @param theta the angle [rad] between the initial plane and the final
     * plane
     * @return the delta v required for a simple plane change
     */
    public static double combinedPlaneChange(double veli, double velf, double theta) {
        return FastMath.sqrt(FastMath.pow(veli, 2)
                + FastMath.pow(velf, 2)
                - 2 * veli * velf * FastMath.cos(theta));
    }

    /**
     * Computes the velocity needed to accelerate a payload from rest at the
     * launch site to the required burnout velocity. Components in
     * topocentric-horizon coordinates [Vs,Ve,Vz].
     *
     * Equations are from 6-45a to 6-45c from SMAD ed. 3 (1999).
     *
     * @param latitude latitude [rad] of the launch site
     * @param velBO velocity [m/s] at burnout
     * @param azimuthBO the launch azimuth [rad] at burnout
     * @param fltPathAngleBO the flight path angle [rad] at burnout
     * @return
     */
    public static double[] launch(double latitude, double velBO, double azimuthBO, double fltPathAngleBO) {
        double veq = 2 * FastMath.PI * Constants.WGS84_EARTH_EQUATORIAL_RADIUS / 86400;
        double vl = veq * FastMath.cos(latitude);

        double vs = -velBO * FastMath.cos(fltPathAngleBO) * FastMath.cos(azimuthBO);
        double ve = velBO * FastMath.cos(fltPathAngleBO) * FastMath.sin(azimuthBO) - vl;
        double vz = velBO * FastMath.sin(fltPathAngleBO);

        return new double[]{vs, ve, vz};
    }

    /**
     * Uses the Tsiolkovsky rocket equation to compute how much delta v a rocket
     * can provide. Assumes that Isp is provided using g=9.80665 m/s
     *
     * @param isp the specific impulse [1/s] of the propellant
     * @param mo the initial mass [kg] of the vehicle
     * @param mf the final mass [kg] of the vehicle
     * @return the delta v a rocket can provide
     */
    public static double rocketEquation(double isp, double mo, double mf) {
        return 9.81 * isp * FastMath.log(mo / mf);
    }

    /**
     * Uses the Tsiolkovsky rocket equation to compute how much delta v a staged
     * rocket can provide. Assumes that Isp is provided using g=9.80665 m/s
     *
     * @param isp an array of the specific impulse [1/s] of the propellant at
     * each stage i
     * @param moi an array of the total mass [kg] of the vehicle before stage i
     * ignites
     * @param mfi an array of the total mass [kg] of the vehicle when stage i
     * burns out but is not yet discarded
     * @return the delta v a staged rocket can provide
     */
    public static double stagedRocketEquation(double[] isp, double[] moi, double[] mfi) {
        double v = 0;

        //check if the arrays are of the same length
        if (isp.length != moi.length || isp.length != mfi.length) {
            throw new IllegalArgumentException("All arrays must be the same length.");
        }

        for (int stage = 0; stage < isp.length; stage++) {
            v += Constants.G0_STANDARD_GRAVITY * isp[stage] * FastMath.log(moi[stage] / mfi[stage]);
        }
        return v;
    }

    /**
     * Uses the Tsiolkovsky rocket equation to compute how much delta v a staged
     * rocket can provide. Assumes that Isp is provided using g=9.80665 m/s
     *
     * @param lv the launch vehicle
     * @return the delta v a staged rocket can provide
     */
    public static double stagedRocketEquation(LaunchVehicle lv) {
        double isp[] = new double[lv.getNumberOfStages()];
        double moi[] = new double[lv.getNumberOfStages()];
        double mfi[] = new double[lv.getNumberOfStages()];
        int i = 0;
        for (LaunchStage stage : lv) {
            isp[i] = stage.getPropellant().getIsp();
            moi[i] = stage.getWetMass();
            mfi[i] = stage.getDryMass();
            i++;
        }

        //Add masses of stages together
        for (i = 0; i < lv.getNumberOfStages() - 1; i++) {
            for (int j = i + 1; j < lv.getNumberOfStages(); j++) {
                moi[i] += moi[j];
                mfi[i] += moi[j];
            }
        }

        return stagedRocketEquation(isp, moi, mfi);
    }

    /**
     * Uses the Tsiolkovsky rocket equation to compute the required mass of the
     * propellant for a specified delta v. Assumes that Isp is provided using
     * g=9.80665 m/s.
     *
     * @param deltaV the delta v to provide
     * @param isp the specific impulse [1/s] of the propellant at each stage i
     * @param mi the total mass [kg] of the vehicle before rockets fire
     * @return the mass of the propellant
     */
    public static double requiredPropellantMass(double deltaV, double mi, double isp) {
        double mf = mi / FastMath.exp(deltaV / (Constants.G0_STANDARD_GRAVITY * isp));
        return mi-mf;
    }
}
