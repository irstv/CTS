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
import org.cts.Identifier;
import org.cts.datum.Ellipsoid;
import org.cts.op.CoordinateOperation;
import org.cts.op.NonInvertibleOperationException;
import org.cts.units.Measure;
import org.cts.util.Complex;

/**
 * The Universal Transverse Mercator Projection (UTM).<p>
 *
 * @author Michaël Michaud
 */
public class UniversalTransverseMercator extends Projection {

    /**
     * The Identifier used for all Universal Transverse Mercator projections.
     */
    public static final Identifier UTM =
            new Identifier("EPSG", "9824", "Transverse Mercator Zoned Grid System", "UTM");
    protected final double FE, // false easting
            lon0, // the reference longitude (from the datum prime meridian)
            n, // projection exponent
            xs, // x coordinate of the pole
            ys;   // y coordinate of the pole
    protected final double[] dircoeff, invcoeff;

    /**
     * Create a new Universal Transverse Mercator Projection corresponding to
     * the
     * <code>Ellipsoid</code> and the list of parameters given in argument and
     * initialize common parameters lon0, FE and other parameters useful for the
     * projection.
     *
     * @param ellipsoid ellipsoid used to define the projection.
     * @param parameters a map of useful parameters to define the projection.
     */
    public UniversalTransverseMercator(final Ellipsoid ellipsoid,
            final Map<String, Measure> parameters) {
        super(UTM, ellipsoid, parameters);
        FE = 500000;
        double y0 = getFalseNorthing();
        double k0 = 0.9996;
        lon0 = getCentralMeridian();
        double lat0 = 0.0;
        //C    = 0.0; // ????????
        n = k0 * ellipsoid.getSemiMajorAxis();
        xs = FE;
        ys = y0 - n * ellipsoid.curvilinearAbscissa(lat0);
        dircoeff = getDirectUTMCoeff(ellipsoid);
        invcoeff = getInverseUTMCoeff(ellipsoid);
    }

    /**
     * Return the coefficients for the direct UTM projection associated with the
     * ellipsoid in parameter.
     *
     * @param ellps the projected ellipsoid
     */
    public static double[] getDirectUTMCoeff(Ellipsoid ellps) {
        double e2 = ellps.getSquareEccentricity();
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e4 * e4;
        double[] dir_utm_coeff = new double[5];
        dir_utm_coeff[0] = 1.0 - e2 * 1 / 4 - e4 * 3 / 64 - e6 * 5 / 256 - e8 * 175 / 16384;
        dir_utm_coeff[1] = e2 * 1 / 8 - e4 * 1 / 96 - e6 * 9 / 1024 - e8 * 901 / 184320;
        dir_utm_coeff[2] = e4 * 13 / 768 + e6 * 17 / 5120 - e8 * 311 / 737280;
        dir_utm_coeff[3] = e6 * 61 / 15360 + e8 * 899 / 430080;
        dir_utm_coeff[4] = e8 * 49561 / 41287680;
        return dir_utm_coeff;
    }

    /**
     * Return the coefficients for the inverse UTM projection associated with
     * the ellipsoid in parameter.
     *
     * @param ellps the projected ellipsoid
     */
    public static double[] getInverseUTMCoeff(Ellipsoid ellps) {
        double e2 = ellps.getSquareEccentricity();
        double e4 = e2 * e2;
        double e6 = e4 * e2;
        double e8 = e4 * e4;
        double[] inv_utm_coeff = new double[5];
        inv_utm_coeff[0] = 1.0 - e2 * 1 / 4 - e4 * 3 / 64 - e6 * 5 / 256 - e8 * 175 / 16384;
        inv_utm_coeff[1] = e2 * 1 / 8 + e4 * 1 / 48 + e6 * 7 / 2048 + e8 * 1 / 61440;
        inv_utm_coeff[2] = e4 * 1 / 768 + e6 * 3 / 1280 + e8 * 559 / 368640;
        inv_utm_coeff[3] = e6 * 17 / 30720 + e8 * 283 / 430080;
        inv_utm_coeff[4] = e8 * 4397 / 41287680;
        return inv_utm_coeff;
    }

    /**
     * Transform coord using the Universal Transverse Mercator Projection. Input
     * coord is supposed to be a geographic latitude / longitude coordinate in
     * radians.
     *
     * @param coord coordinate to transform
     * @throws CoordinateDimensionException if <code>coord</code> length is not
     * compatible with this <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord) throws CoordinateDimensionException {
        double latIsoPhi = ellipsoid.isometricLatitude(coord[0]);
        double PHI = Math.asin(Math.sin(coord[1] - lon0) / Math.cosh(latIsoPhi));
        double latIsoPHI = Ellipsoid.SPHERE.isometricLatitude(PHI);
        double lambda = Math.atan(Math.sinh(latIsoPhi) / Math.cos(coord[1] - lon0));
        Complex z = new Complex(lambda, latIsoPHI);
        Complex Z = z.times(n * dircoeff[0]);
        for (int i = 1; i < 5; i++) {
            Z = Z.plus(Complex.sin(z.times(2.0 * i)).times(n * dircoeff[i]));
        }
        coord[0] = xs + Z.im();
        coord[1] = ys + Z.re();
        return coord;
    }

    /**
     * Creates the inverse CoordinateOperation.
     */
    @Override
    public CoordinateOperation inverse() throws NonInvertibleOperationException {
        return new UniversalTransverseMercator(ellipsoid, parameters) {
            @Override
            public double[] transform(double[] coord) throws CoordinateDimensionException {
                Complex z = new Complex((coord[1] - ys) / (n * invcoeff[0]),
                        (coord[0] - xs) / (n * invcoeff[0]));
                Complex Z = z;
                for (int i = 1; i < 5; i++) {
                    Z = Z.plus(Complex.sin(z.times((2.0 * i))).times(-invcoeff[i]));
                }
                double lon = lon0 + Math.atan(Math.sinh(Z.im()) / Math.cos(Z.re()));
                double PHI = Math.asin(Math.sin(Z.re()) / Math.cosh(Z.im()));
                double latIso = Ellipsoid.SPHERE.isometricLatitude(PHI);
                double lat = ellipsoid.latitude(latIso);
                coord[0] = lat;
                coord[1] = lon;
                return coord;
            }
        };
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
        return Projection.Orientation.TRANSVERSE;
    }
}
