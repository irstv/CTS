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
package org.cts.op.projection;

import java.util.Map;
import org.cts.CoordinateDimensionException;
import org.cts.datum.Ellipsoid;
import org.cts.Identifier;
import org.cts.units.Measure;
import static java.lang.Math.*;
import org.cts.op.CoordinateOperation;
import org.cts.op.NonInvertibleOperationException;

/**
 * The Lambert Azimuthal Equal Area Projection (LAEA). <p>
 *
 * @author Jules Party
 */
public class LambertAzimuthalEqualArea extends Projection {

    public static final Identifier LAEA =
            new Identifier("EPSG", "9820", "Lambert Azimuthal Equal Area", "LAEA");
    protected final double lat0, // the reference latitude
            lon0, // the reference longitude (from the datum prime meridian)
            FE, // false easting
            FN,   // false northing
            beta0, // the authalic latitude corresponding to lat0
            qp, // a constant of the projection
            D, // another constant of the projection
            Rq; // another constant of the projection

    /**
     * Create a new Lambert Azimuthal Equal Area Projection corresponding to
     * the <code>Ellipsoid</code> and the list of parameters given in argument
     * and initialize common parameters lon0, lat0, FE, FN and other parameters
     * useful for the projection.
     * 
     * @param ellipsoid ellipsoid used to define the projection.
     * @param parameters a map of useful parameters to define the projection.
     */
    public LambertAzimuthalEqualArea(final Ellipsoid ellipsoid,
            final Map<String, Measure> parameters) {
        super(LAEA, ellipsoid, parameters);
        lon0 = getCentralMeridian();
        lat0 = getLatitudeOfOrigin();
        FE = getFalseEasting();
        FN = getFalseNorthing();
        double e = ellipsoid.getEccentricity();
        double e2 = ellipsoid.getSquareEccentricity();
        qp = 1 - (1-e2)/2/e*log((1-e)/(1+e));
        double esin0 = e*sin(lat0);
        double q0 = (1-e2)*(sin(lat0)/(1-esin0*esin0) - log((1-esin0)/(1+esin0))/2/e);
        beta0 = asin(q0/qp);
        Rq = ellipsoid.getSemiMajorAxis()*pow(qp/2, 0.5);
        D = ellipsoid.getSemiMajorAxis()*cos(lat0)/sqrt(1-esin0*esin0)/Rq/cos(beta0);
    }

    /**
     * Return the
     * <code>Surface</code> type of this
     * <code>Projection</code>.
     */
    @Override
    public Surface getSurface() {
        return Projection.Surface.CYLINDRICAL;
    }

    /**
     * Return the
     * <code>Property</code> of this
     * <code>Projection</code>.
     */
    @Override
    public Property getProperty() {
        return Projection.Property.CONFORMAL;
    }

    /**
     * Return the
     * <code>Orientation</code> of this
     * <code>Projection</code>.
     */
    @Override
    public Orientation getOrientation() {
        return Projection.Orientation.TANGENT;
    }

    /**
     * Transform coord using the Lambert Azimuthal Equal Area Projection. Input
     * coord is supposed to be a geographic latitude / longitude coordinate in
     * radians. Algorithm based on the OGP's Guidance Note Number 7 Part 2 :
     * <http://www.epsg.org/guides/G7-2.html>
     *
     * @param coord coordinate to transform
     * @throws CoordinateDimensionException if <code>coord</code> length is not
     * compatible with this <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord) throws CoordinateDimensionException {
        double e = ellipsoid.getEccentricity();
        double e2 = ellipsoid.getSquareEccentricity();
        double esin = e*sin(coord[0]);
        double q = (1-e2)*(sin(coord[0])/(1-esin*esin) - log((1-esin)/(1+esin))/2/e);
        double beta = asin(q/qp);
        double B = Rq*sqrt(2/(1 + sin(beta0)*sin(beta) + cos(beta0)*cos(beta)*cos(coord[1]-lon0)));
        coord[0] = FE + B*D*cos(beta)*sin(coord[1]-lon0);
        coord[1] = FN + B/D*(cos(beta0)*sin(beta) - sin(beta0)*cos(beta)*cos(coord[1]-lon0));
        return coord;
    }
    
    /**
     * Creates the inverse operation for Lambert Azimuthal Equal Area Projection.
     * Input coord is supposed to be a projected easting / northing coordinate in meters.
     * Algorithm based on the OGP's Guidance Note Number 7 Part 2 :
     * <http://www.epsg.org/guides/G7-2.html>
     * 
     * @param coord coordinate to transform
     */
    @Override
    public CoordinateOperation inverse() throws NonInvertibleOperationException {
        return new LambertAzimuthalEqualArea(ellipsoid, parameters) {

            @Override
            public double[] transform(double[] coord) throws CoordinateDimensionException {
                double e = ellipsoid.getEccentricity();
                double e2 = ellipsoid.getSquareEccentricity();
                double e4 = e2*e2;
                double e6 = e4*e2;
                double x = (coord[0]-FE)/D;
                double y = (coord[1]-FN)*D;
                double rho = sqrt(x*x+y*y);
                double C = 2*asin(rho/2/Rq);
                double betap = asin(cos(C)*sin(beta0) + y*sin(C)*cos(beta0)/rho);
                coord[0] = betap + (e2/3 + 31/180*e4 +517/5040*e6)*sin(2*betap)
                        + (23/360*e4 + 251/3780*e6)*sin(4*betap) + 761/45360*e6*sin(6*betap);
                coord[1] = lon0 + atan(x*sin(C)/(rho*cos(beta0)*cos(C)-y*sin(beta0)*sin(C)));
                return coord;
            }
        };
    }
}