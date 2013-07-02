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
package org.cts.parser.prj;

/**
 * List all parameters used to define a PRJ file
 *
 * @author Erwan Bocher
 */
public class PrjKeyParameters {

    public static final String GEOGCS = "geogcs";
    public static final String UNIT = "unit";
    public static final String PROJECTION = "projection";
    public static final String PARAMETER = "parameter";
    public static final String AUTHORITY = "authority";
    public static final String PROJCS = "projcs";
    public static final String PRIMEM = "primem";
    public static final String SPHEROID = "spheroid";
    //Internal key parameters used by the parser
    public static final String UNITVAL = "unitval";
    public static final String NAME = "name";
    public static final String REFCODE = "refcode";
    public static final String REFNAME = "refname";
    public static final String REFAUTHORITY = "refauthority";
}
