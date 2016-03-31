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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.cts.Identifier;
import org.cts.Parameter;
import org.cts.datum.Ellipsoid;
import org.cts.op.AbstractCoordinateOperation;
import org.cts.op.CoordinateOperation;
import org.cts.op.NonInvertibleOperationException;
import org.cts.parser.prj.PrjWriter;
import org.cts.units.Measure;
import org.cts.units.Unit;
import org.cts.util.AngleFormat;

/**
 * A map projection is any method used in cartography (mapmaking) to represent
 * the two-dimensional curved surface of the earth or other body on a plane. The
 * term "projection" here refers to any function defined on the earth's surface
 * and with values on the plane, and not necessarily a geometric projection.<p>
 *
 * @author Michaël Michaud, Erwan Bocher
 */
public abstract class Projection extends AbstractCoordinateOperation {

    public static final Parameter[] DEFAULT_PARAMETERS = new Parameter[]{
        new Parameter(Parameter.FALSE_EASTING, new Measure(0, Unit.METER)),
        new Parameter(Parameter.FALSE_NORTHING, new Measure(0, Unit.METER)),
        new Parameter(Parameter.CENTRAL_MERIDIAN, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.STANDARD_PARALLEL_1, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.STANDARD_PARALLEL_2, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.LATITUDE_OF_TRUE_SCALE, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.AZIMUTH, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.RECTIFIED_GRID_ANGLE, new Measure(0, Unit.DEGREE)),
        new Parameter(Parameter.SCALE_FACTOR, new Measure(1, Unit.UNIT)),
        new Parameter(Parameter.LATITUDE_OF_ORIGIN, new Measure(0, Unit.DEGREE))};

    public static ConcurrentHashMap<String, Measure> getDefaultParameters() {
        ConcurrentHashMap<String, Measure> parameters = new ConcurrentHashMap<String, Measure>();
        for (Parameter param : DEFAULT_PARAMETERS) {
            parameters.put(param.getName(), param.getMeasure());
        }
        return parameters;
    }

    /**
     * Projection classification based on the surface type.
     */
    public static enum Surface {

        AZIMUTHAL, // or stereographic
        CONICAL,
        CYLINDRICAL,
        HYBRID,
        MISCELLANEOUS,
        POLYCONICAL,
        PSEUDOAZIMUTHAL,
        PSEUDOCONICAL,
        PSEUDOCYLINDRICAL,
        RETROAZIMUTHAL
    }

    /**
     * Projection property.
     */
    public static enum Property {

        APHYLACTIC, // A term sometimes used to describe a map projection
        //which is neither equal-area  nor conformal
        CONFORMAL, // Locally shape preserving (angle preserving)
        EQUAL_AREA, // Area preserving (also called Equiarea, Equivalent, Authalic)
        EQUIDISTANT, // Distance preserving
        GNOMONIC
    }       // Shortest route preserving;

    /**
     * Projection orientation.
     */
    public static enum Orientation {

        OBLIQUE,
        SECANT,
        TANGENT,
        TRANSVERSE
    }
    /**
     * Ellispoid used for this projection.
     */
    Ellipsoid ellipsoid;
    /**
     * Parameters other than the ellipsoid used in this projection.
     */
    final Map<String, Measure> parameters;

    /**
     * Creates a new Projection
     *
     * @param identifier identifier of the projection
     * @param ellipsoid ellipsoid used for this projection
     * @param parameters other projection parameters
     */
    protected Projection(final Identifier identifier, final Ellipsoid ellipsoid,
            final Map<String, Measure> parameters) {
        super(identifier);
        this.ellipsoid = ellipsoid;
        // store parameters in a new Map, because this Projection parameters
        // must never be modified after initialization
        // NOTE : I also wanted to make it unmodifiable, but could not
        // use Collections.<String,Measure>unmodifiableMap(clone)
        if (parameters == null) {
            this.parameters = Collections.<String, Measure>unmodifiableMap(new HashMap<String, Measure>());
        } else {
            this.parameters = Collections.<String, Measure>unmodifiableMap(new HashMap(parameters));
        }
    }

    /**
     * Return the semi-major axis of the ellipsoid used for this projection (fr
     * : demi grand axe).
     */
    public double getSemiMajorAxis() {
        return ellipsoid.getSemiMajorAxis();
    }

    /**
     * Return the semi-minor axis of the ellipsoid used for this projection (fr
     * : demi petit axe).
     */
    public double getSemiMinorAxis() {
        return ellipsoid.getSemiMinorAxis();
    }

    /**
     * Return the central meridian used for this projection.
     */
    public double getCentralMeridian() {
        return parameters.get(Parameter.CENTRAL_MERIDIAN).getSValue();
    }

    /**
     * Return the reference latitude used for this projection.
     */
    public double getLatitudeOfOrigin() {
        return parameters.get(Parameter.LATITUDE_OF_ORIGIN).getSValue();
    }

    /**
     * Return the the first standard parallel of secant conformal conic
     * projections.
     */
    public double getStandardParallel1() {
        return parameters.get(Parameter.STANDARD_PARALLEL_1).getSValue();
    }

    /**
     * Return the the second standard parallel of secant conformal conic
     * projections.
     */
    public double getStandardParallel2() {
        return parameters.get(Parameter.STANDARD_PARALLEL_2).getSValue();
    }

    /**
     * Return the latitude of true scale of secant projections.
     */
    public double getLatitudeOfTrueScale() {
        return parameters.get(Parameter.LATITUDE_OF_TRUE_SCALE).getSValue();
    }

    /**
     * Return the azimuth of the initial line of oblique projections.
     */
    public double getAzimuth() {
        return parameters.get(Parameter.AZIMUTH).getSValue();
    }

    /**
     * Return the angle from the rectified grid to the skew (oblique) grid of
     * oblique projections.
     */
    public double getRectifiedGridAngle() {
        return parameters.get(Parameter.RECTIFIED_GRID_ANGLE).getSValue();
    }

    /**
     * Return the scale factor of this projection.
     */
    public double getScaleFactor() {
        Measure m = parameters.get(Parameter.SCALE_FACTOR);
        return parameters.get(Parameter.SCALE_FACTOR).getSValue();
    }

    /**
     * Return the false easting of this projection.
     */
    public double getFalseEasting() {
        return parameters.get(Parameter.FALSE_EASTING).getSValue();
    }

    /**
     * Return the false northing of this projection.
     */
    public double getFalseNorthing() {
        return parameters.get(Parameter.FALSE_NORTHING).getSValue();
    }

    /**
     * Return the
     * <code>Surface</code> type of this
     * <code>Projection</code>.
     */
    public abstract Surface getSurface();

    /**
     * Return the
     * <code>Property</code> of this
     * <code>Projection</code>.
     */
    public abstract Property getProperty();

    /**
     * Return the
     * <code>Orientation</code> of this
     * <code>Projection</code>.
     */
    public abstract Orientation getOrientation();

    @Override
    public Projection inverse()
            throws NonInvertibleOperationException {
        throw new NonInvertibleOperationException(this.toString()
                + " is non invertible");
    }

    /**
     * Return true for direct operation (projection) and false for the
     * inverse operation.
     */
    public boolean isDirect() {
        return true;
    }

    /**
     * Returns a WKT representation of the projection.
     *
     */
    public String toWKT() {
        StringBuilder w = new StringBuilder();
        w.append("PROJECTION[\"");
        w.append(this.getName());
        w.append("\"],PARAMETER[\"").append(Parameter.LATITUDE_OF_ORIGIN).append("\",");
        w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getLatitudeOfOrigin()), 1e-11));
        if (this.getStandardParallel1() != 0.0) {
            w.append("],PARAMETER[\"").append(Parameter.STANDARD_PARALLEL_1).append("\",");
            w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getStandardParallel1()), 1e-11));
        }
        if (this.getStandardParallel2() != 0.0) {
            w.append("],PARAMETER[\"").append(Parameter.STANDARD_PARALLEL_2).append("\",");
            w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getStandardParallel2()), 1e-11));
        }
        w.append("],PARAMETER[\"").append(Parameter.CENTRAL_MERIDIAN).append("\",");
        w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getCentralMeridian()), 1e-11));
        if (this.getAzimuth() != 0.0) {
            w.append("],PARAMETER[\"").append(Parameter.AZIMUTH).append("\",");
            w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getAzimuth()), 1e-11));
        }
        if (this.getRectifiedGridAngle() != 0.0) {
            w.append("],PARAMETER[\"").append(Parameter.RECTIFIED_GRID_ANGLE).append("\",");
            w.append(PrjWriter.roundToString(AngleFormat.rad2deg(this.getRectifiedGridAngle()), 1e-11));
        }
        w.append("],PARAMETER[\"").append(Parameter.SCALE_FACTOR).append("\",");
        w.append(PrjWriter.roundToString(this.getScaleFactor(), 1e-11));
        w.append("],PARAMETER[\"").append(Parameter.FALSE_EASTING).append("\",");
        w.append(PrjWriter.roundToString(this.getFalseEasting(), 1e-11));
        w.append("],PARAMETER[\"").append(Parameter.FALSE_NORTHING).append("\",");
        w.append(PrjWriter.roundToString(this.getFalseNorthing(), 1e-11));
        w.append("]");
        return w.toString();
    }

    /**
     * Returns true if object is equals to
     * <code>this</code>. Tests equality between the references of both object,
     * then tests if the string representation of these objects are equals.
     *
     * @param o The object to compare this ProjectedCRS against
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof Projection) {
            Projection proj = (Projection) o;
            if (this.toString() != null) {
                if (getClass().equals(proj.getClass())) {
                    for (String param : parameters.keySet()) {
                        if (parameters.get(param) == null && proj.parameters.get(param) == null) continue;
                        else if (parameters.get(param) == null && proj.parameters.get(param) != null) return false;
                        else if (parameters.get(param) != null && proj.parameters.get(param) == null) continue;
                        else if (parameters.get(param).equals(proj.parameters.get(param))) continue;
                        else if (!parameters.get(param).equals(proj.parameters.get(param))) return false;
                        else {
                            // Should not reach here
                        }
                    }
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * Returns the hash code for this Projection.
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.ellipsoid != null ? this.ellipsoid.hashCode() : 0);
        hash = 73 * hash + (this.parameters != null ? this.parameters.hashCode() : 0);
        return hash;
    }
}
