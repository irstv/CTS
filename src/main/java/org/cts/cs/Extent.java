/*
 * Coordinate Transformations Suite (abridged CTS)  is a library developped to 
 * perform Coordinate Transformations using well known geodetic algorithms 
 * and parameter sets. 
 * Its main focus are simplicity, flexibility, interoperability, in this order.
 *
 * This library has been originally developed by Michaël Michaud under the JGeod
 * name. It has been renamed CTS in 2009 and shared to the community from 
 * the OrbisGIS code repository.
 *
 * CTS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License.
 *
 * CTS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * CTS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <https://github.com/orbisgis/cts/>
 */
package org.cts.cs;

/**
 * An
 * <code>Extent</code> defines a part of an infinite n-dimension space thanks to
 * a method returning true for any set of coordinates lying inside this part and
 * false for coordinates lying outside (input coordinates must be an array of n
 * doubles).
 *
 * @author Michaël Michaud
 */
public interface Extent {

    /**
     * Return the name of this extent.
     */
    String getName();

    /**
     * Returns wether coord is inside this Extent or not. It's up to the user to
     * check consistency between coord and extent type.
     */
    boolean isInside(double[] coord);
}
