/*
 * Coordinate Transformations Suite (abridged CTS)  is a library developped to 
 * perform Coordinate Transformations using well known geodetic algorithms 
 * and parameter sets. 
 * Its main focus are simplicity, flexibility, interoperability, in this order.
 *
 * This library has been originaled developed by Michael Michaud under the JGeod
 * name. It has been renamed CTS in 2009 and shared to the community from 
 * the Atelier SIG code repository.
 * 
 * Since them, CTS is supported by the Atelier SIG team in collaboration with Michael 
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

import org.cts.CoordinateDimensionException;
import org.cts.Identifier;
import org.cts.IllegalCoordinateException;

/**
 * A class to change the coordinate precision
 *
 * @author Michaël Michaud
 */
public class CoordinateRounding extends AbstractCoordinateOperation {

    public final static CoordinateRounding MILLIMETER = createCoordinateRoundingOperation(0.001);
    public final static CoordinateRounding CENTIMETER = createCoordinateRoundingOperation(0.01);
    public final static CoordinateRounding DECIMETER = createCoordinateRoundingOperation(0.1);
    public final static CoordinateRounding METER = createCoordinateRoundingOperation(0.0);
    public final static CoordinateRounding KILOMETER = createCoordinateRoundingOperation(1000.0);
    private double inv_resolution = 1.0;

    /**
     * Creates a new coordinate rounding operation from the smallest
     * representable value. Note that CoordinateRounding is not invertible.
     *
     * @param decimalPlaces number of decimal places
     */
    private CoordinateRounding(double resolution) {
        super(new Identifier(CoordinateRounding.class,
                "ordinates are multiple of " + resolution));
        this.inv_resolution = 1.0 / resolution;
    }

    /**
     * Returns a coordinate representing the same point as coord but with
     * coordinates rounded as specified.
     *
     * @param coord is an array containing one, two or three ordinates.
     * @throws IllegalCoordinateException if
     * <code>coord</code> is not compatible with this
     * <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord) throws IllegalCoordinateException {
        if (coord == null || coord.length == 0) {
            throw new CoordinateDimensionException("" + coord
                    + " is an invalid coordinate");
        }
        for (int i = 0; i < coord.length; i++) {
            if (Double.isNaN(coord[i])) {
                continue;
            }
            coord[i] = Math.rint(coord[i] * inv_resolution) / inv_resolution;
        }
        return coord;
    }

    /**
     * Creates a coordinate rounding operation from a decimal place number.
     *
     * @param decimalPlaces number of decimal places
     */
    public static CoordinateRounding createCoordinateRoundingOperationFromDecimalPlaces(int decimalPlaces) {
        double u = 1.0;
        for (int i = 0; i < decimalPlaces; i++) {
            u *= 10.0;
        }
        return new CoordinateRounding(1.0 / u);
    }

    /**
     * Creates a coordinate rounding operation from the smallest representable
     * value.
     *
     * @param resolution smallest representable value
     */
    public static CoordinateRounding createCoordinateRoundingOperation(double resolution) {
        return new CoordinateRounding(resolution);
    }
}
