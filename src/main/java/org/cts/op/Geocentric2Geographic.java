/*
 * Coordinate Transformations Suite (abridged CTS)  is a library developped to 
 * perform Coordinate Transformations using well known geodetic algorithms 
 * and parameter sets. 
 * Its main focus are simplicity, flexibility, interoperability, in this order.
 *
 * This library has been originally developed by Michaël Michaud under the JGeod
 * name. It has been renamed CTS in 2009 and shared to the community from 
 * the Atelier SIG code repository.
 * 
 * Since them, CTS is supported by the Atelier SIG team in collaboration with Michaël 
 * Michaud.
 * The new CTS has been funded  by the French Agence Nationale de la Recherche 
 * (ANR) under contract ANR-08-VILL-0005-01 and the regional council 
 * "Région Pays de La Loire" under the projet SOGVILLE (Système d'Orbservation 
 * Géographique de la Ville).
 *
 * CTS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * CTS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * CTS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <https://github.com/irstv/cts/>
 */
package org.cts.op;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import org.cts.CoordinateDimensionException;
import org.cts.datum.Ellipsoid;
import org.cts.Identifier;
import org.cts.IllegalCoordinateException;

/**
 * <p>Transform geographic coordinates (latitude, longitude, ellipsoidal height
 * into geocentric coordinates.</p>
 * <p>Geographic coordinates and geocentric coordinates are supposed to use
 * the same reference datum and to be standardized :</p>
 * <ul>
 * <li>Geographic coordinates are given in the following order : latitude
 * (radians), longitude (radians from Greenwich) and optionnaly ellipsoidal
 * height (default = 0.0).</li>
 * <li>The center of the geocentric system (center of the mass) is equal to
 * the geographic coordinates reference ellipsoid.</p>
 * <li>Z axis is oriented from origin to North Pole</li>
 * <li>Y axis is oriented from origin to intersection of equator and Greenwich
 * Meridian</li>
 * <li>OXYZ is direct</li>
 * <li>Units = radian, meter (to facilitate transformation operations).</li>
 * </ul>
 * @author Michaël Michaud
 */
public class Geocentric2Geographic extends AbstractCoordinateOperation {

    /**
     * Useful transformation to go from geographic to geocentric coordinates
     * using radians and the standard GRS80 ellipsoid.
     */
    public static final Geocentric2Geographic CG_GRS80 =
            new Geocentric2Geographic(Ellipsoid.GRS80);
    private static final Identifier opId =
            new Identifier("EPSG", "9602",
            "Geocentric to geographic conversion", "Geocentric to geographic");
    private Ellipsoid ellipsoid;
    private double epsilon;

    /**
     * <p>Create a new Geographic2Geocentric transformation for a specific
     * ellipsoid. The reference datum for both geographic and geocentric
     * coordinates is the same.</p>
     *
     * @param ellipsoid the ellipsoid used to define geographic coordinates
     */
    public Geocentric2Geographic(Ellipsoid ellipsoid) {
        super(opId);
        this.ellipsoid = ellipsoid;
        this.precision = 1E-4;   // 0.0001 mm
        this.epsilon = 1E-11;    // condition d'arret = 1E-11 radian < 1E-4 m
    }

    /**
     * <p>Create a new Geographic2Geocentric transformation for a specific
     * ellipsoid. The reference datum for both geographic and geocentric
     * coordinates is the same.</p>
     *
     * @param ellipsoid the ellipsoid used to define geographic coordinates
     * @param epsilon stop condition for the Geocentric to Geographic
     * transformation algorithm (epsilon is a value in radian, 1E-11 is the
     * default epsilon and it means that error is less than 1E-4 m)
     */
    public Geocentric2Geographic(Ellipsoid ellipsoid, double epsilon) {
        super(opId);
        this.ellipsoid = ellipsoid;
        this.precision = 1E-4;   // 0.0001 mm
        this.epsilon = epsilon;  // condition d'arret = 1E-11 radian < 1E-4 m
    }

    /**
     * <p>Return coordinates representing the same point in a standard
     * geocentric coordinate system.</p>
     *
     * @param coord is an array containing 2 or 3 double representing geographic
     * coordinates in the following order : latitude (radians), longitude
     * (radians from Greenwich) and optionnaly ellipsoidal height (if coord
     * contains only 2 double's, height is set to 0).
     * @throws IllegalCoordinateException if
     * <code>coord</code> is not compatible with this
     * <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord)
            throws IllegalCoordinateException {
        if (coord.length != 3) {
            throw new CoordinateDimensionException(coord, 3);
        }
        double X = coord[0];
        double Y = coord[1];
        double Z = coord[2];
        double a = ellipsoid.getSemiMajorAxis();
        double e2 = ellipsoid.getSquareEccentricity();
        // Calcul de la longitude
        double lon = atan2(Y, X);
        // Calcul de la latitude
        double XY2 = sqrt(X * X + Y * Y);
        double lat0 = atan(Z / (XY2 * (1 - (a * e2 / sqrt(X * X + Y * Y + Z * Z)))));
        double lati = lat0;
        double lati1 = 0;
        while (abs(lati1 - lati) > epsilon) {
            lati = lati1;
            double exp1 = a * e2 * cos(lati);
            double exp2 = sqrt(1 - (e2 * sin(lati) * sin(lati)));
            lati1 = atan((Z / XY2) / (1 - (exp1 / (XY2 * exp2))));
        }
        double lat = lati1;
        // Calcul de la hauteur
        double height = XY2 / cos(lat) - a / sqrt(1 - (e2 * sin(lat) * sin(lat)));
        coord[0] = lat;
        coord[1] = lon;
        coord[2] = height;
        return coord;
    }

    /**
     * Apply the inverse transformation to coord (geocentric to geographic)
     */
    /*
     * public void inverseTransform(double[] coord) throws
     * CoordinateDimensionException, NonInvertibleOperationException { if
     * (coord.length < 2 || coord.length > 3) { throw new
     * CoordinateDimensionException(coord, 3); } double lat = coord[0]; double
     * lon = coord[1]; double height = coord.length==3?coord[2]:0.0; double N =
     * ellipsoid.transverseRadiusOfCurvature(lat); coord[0] = (N+height) *
     * cos(lat) * cos(lon); coord[1] = (N+height) * cos(lat) * sin(lon);
     * coord[2] = (N * (1-ellipsoid.getSquareEccentricity()) + height) *
     * sin(lat); }
     */
    /**
     * Creates the inverse CoordinateOperation.
     */
    @Override
    public CoordinateOperation inverse() throws NonInvertibleOperationException {
        return new Geographic2Geocentric(ellipsoid);
    }

    /**
     * Return a String representation of this Geographic/Geocentric converter.
     */
    @Override
    public String toString() {
        return getName() + " (" + ellipsoid.getName() + ")";
    }
}
