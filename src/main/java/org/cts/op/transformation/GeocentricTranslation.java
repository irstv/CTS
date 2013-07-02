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
package org.cts.op.transformation;

import org.cts.CoordinateDimensionException;
import org.cts.Identifier;
import org.cts.IllegalCoordinateException;
import org.cts.op.AbstractCoordinateOperation;
import org.cts.op.CoordinateOperation;
import org.cts.op.NonInvertibleOperationException;

/**
 * <p>GeocentricTranslation is a coordinate operation used to transform
 * geocentric coordinates with a 3D translation defined by three parameters
 * representing translation values along x axis, y axis and z axis.</p> <p>In
 * this operation, one assume that the axis of the ellipsoids are parallel, that
 * the prime meridian is Greenwich, and that there is no scale difference
 * between the source and target CoordinateReferenceSystem.</p> <p>Equations of
 * this transformation are : <ul> <li>X' = X + tx</li> <li>Y' = Y + ty</li>
 * <li>Z' = Z + tz</li> </ul> </p>
 *
 * @author Michaël Michaud, Erwan Bocher
 */
public class GeocentricTranslation extends AbstractCoordinateOperation implements GeoTransformation {

    /**
     * The Identifier used for all Geocentric translations.
     */
    private static final Identifier opId =
            new Identifier("EPSG", "9603", "Geocentric translation", "Translation");
    /**
     * Translation value used in this Geocentric translation.
     */
    private double tx, ty, tz;

    /**
     * <p>Geocentric translation.</p>
     *
     * @param tx translation parameter along x axis (meters)
     * @param ty translation parameter along y axis (meters)
     * @param tz translation parameter along z axis (meters)
     * @param precision mean precision of the geodetic transformation
     */
    public GeocentricTranslation(double tx, double ty, double tz, double precision) {
        super(opId);
        this.tx = tx;
        this.ty = ty;
        this.tz = tz;
        this.precision = Math.max(0.000000001, precision);
    }

    /**
     * Geocentric translation.
     *
     * @param tx translation parameter along x axis (meters)
     * @param ty translation parameter along y axis (meters)
     * @param tz translation parameter along z axis (meters)
     */
    public GeocentricTranslation(double tx, double ty, double tz) {
        this(tx, ty, tz, 1E-9);
    }

    /**
     * <p>Return a coordinates representing the same point as coord but in
     * another CoordinateReferenceSystem.</p> <p>Equations of this
     * transformation are : <ul> <li>X' = X + tx</li> <li>Y' = Y + ty</li>
     * <li>Z' = Z + tz</li> </ul> </p>
     *
     * @param coord coordinate to transform
     * @throws IllegalCoordinateException if <code>coord</code> is not
     * compatible with this <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord) throws IllegalCoordinateException {
        if (coord.length != 3) {
            throw new CoordinateDimensionException(coord, 3);
        }
        coord[0] = tx + coord[0];
        coord[1] = ty + coord[1];
        coord[2] = tz + coord[2];
        return coord;
    }

    /**
     * Creates the inverse CoordinateOperation.
     */
    @Override
    public CoordinateOperation inverse() throws NonInvertibleOperationException {
        return new GeocentricTranslation(-tx, -ty, -tz, precision);
    }

    /**
     * Returns this Geocentric translation as a String.
     */
    @Override
    public String toString() {
        return "Geocentric translation (dX=" + (tx < 0 ? "" : "+") + tx + "m, "
                + "dY=" + (ty < 0 ? "" : "+") + ty + "m, "
                + "dZ=" + (tz < 0 ? "" : "+") + tz + "m) "
                + "precision = " + precision;
    }

    /**
     * Returns this Geocentric translation as an OGC WKT String.
     */
    @Override
    public String toWKT() {
        StringBuilder w = new StringBuilder();
        w.append(",TOWGS84[");
        w.append((int) tx);
        w.append(',');
        w.append((int) ty);
        w.append(',');
        w.append((int) tz);
        w.append("]");
        return w.toString();
    }

    /**
     * Returns true if object is equals to
     * <code>this</code>. Tests equality between the references of both object,
     * then tests if the three translation values (tx, ty and tz) used by both
     * Geocentric Translation are equals.
     *
     * @param object The object to compare this ProjectedCRS against
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof GeocentricTranslation) {
            GeocentricTranslation gt = (GeocentricTranslation) o;
            return ((this.tx == gt.tx) && (this.ty == gt.ty) && (this.tz == gt.tz));
        }
        return false;
    }

    /**
     * Returns the hash code for this GeocentricTranslation.
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.tx) ^ (Double.doubleToLongBits(this.tx) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.ty) ^ (Double.doubleToLongBits(this.ty) >>> 32));
        hash = 19 * hash + (int) (Double.doubleToLongBits(this.tz) ^ (Double.doubleToLongBits(this.tz) >>> 32));
        return hash;
    }
}
