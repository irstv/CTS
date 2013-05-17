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
package org.cts.op.projection;

import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.exp;
import static java.lang.Math.log;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import java.util.HashMap;
import java.util.Map;
import org.cts.CoordinateDimensionException;
import org.cts.CoordinateOperation;
import org.cts.Ellipsoid;
import org.cts.Identifier;
import org.cts.IllegalCoordinateException;
import org.cts.units.Measure;
import org.cts.NonInvertibleOperationException;
import org.cts.Parameter;
import org.cts.units.Unit;

/**
 * A map projection is any method used in cartography (mapmaking) to represent
 * the two-dimensional curved surface of the earth or other body on a plane.
 * The term "projection" here refers to any function defined on the earth's surface
 * and with values on the plane, and not necessarily a geometric projection.<p>
 * @author Michaël Michaud
 */
public class LambertConicConformal1SP extends Projection {

	public static final Identifier LCC1SP =
		new Identifier("EPSG", "9801", "Lambert Conic Conformal (1SP)", "Lambert tangent");
	/**
	 * Lambert zone I projection, used in the north of France (with NTF datum)
	 */
	public static final LambertConicConformal1SP LAMBERT1 = createLCC1SP(
		Ellipsoid.CLARKE1880IGN, 55.0, 0.99987734, 0.0, Unit.GRAD,
		600000.0, 200000.0, Unit.METER);
	/**
	 * Lambert zone II projection, used in the center of France (with NTF datum)
	 */
	public static final LambertConicConformal1SP LAMBERT2 = createLCC1SP(
		Ellipsoid.CLARKE1880IGN, 52.0, 0.99987742, 0.0, Unit.GRAD,
		600000.0, 200000.0, Unit.METER);
	/**
	 * Lambert zone III projection, used in the south of France (with NTF datum)
	 */
	public static final LambertConicConformal1SP LAMBERT3 = createLCC1SP(
		Ellipsoid.CLARKE1880IGN, 49.0, 0.99987750, 0.0, Unit.GRAD,
		600000.0, 200000.0, Unit.METER);
	/**
	 * Lambert zone IV projection, used in Corsica (with NTF datum)
	 */
	public static final LambertConicConformal1SP LAMBERT4 = createLCC1SP(
		Ellipsoid.CLARKE1880IGN, 47.8, 0.99994471, 0.0, Unit.GRAD,
		234.358, 185861.369, Unit.METER);
	/**
	 * Lambert II étendu, used as a unique projection for France with NTF datum.
	 */
	public static final LambertConicConformal1SP LAMBERT2E = createLCC1SP(
		Ellipsoid.CLARKE1880IGN, 52.0, 0.99987742, 0.0, Unit.GRAD,
		600000.0, 2200000.0, Unit.METER);
	/**
	 * Lambert 93, ne new unique projection used in France with RGF93 datum.<p>
	 * Note that the projection is originally defined as a
	 * {@link LambertConicConformal2SP} with two standard parallels.
	 */
	public static final LambertConicConformal1SP LAMBERT93 = createLCC1SP(
		Ellipsoid.GRS80, 46.5, 0.9990510286374, 3.0, Unit.DEGREE,
		700000.0, 6600000.0, Unit.METER);
	// constants of the projections derived from definition parameters
	protected final double lon0, // the reference longitude (from the datum prime meridian)
		n, // projection exponent
		C, // projection constant
		xs, // x coordinate of the pole
		ys;   // y coordinate of the pole

	public LambertConicConformal1SP(final Ellipsoid ellipsoid,
		final Map<String, Measure> parameters) {
		super(LCC1SP, ellipsoid, parameters);
		double semimajor = getSemiMajorAxis();
		double semiminor = getSemiMinorAxis();
		double lat0 = getLatitudeOfOrigin();
		lon0 = getCentralMeridian();
		double k0 = getScaleFactor();
		double x0 = getFalseEasting();
		double y0 = getFalseNorthing();
		double latIso0 = ellipsoid.isometricLatitude(lat0);
		double N0 = ellipsoid.transverseRadiusOfCurvature(lat0);
		n = sin(lat0);
		C = k0 * N0 * exp(n * latIso0) / tan(lat0);
		xs = x0;
		ys = y0 + k0 * N0 / tan(lat0);
	}

	/**
	 * LambertConicConformal1SP factory to create a LambertConicConformal1SP
	 * projection from a latitude of origin and a central meridian in degrees,
	 * a scale factor and false coordinates in meters.
	 * @param latitude_of_origin latitude of origin of the projection in degrees
	 * @param scale_factor scale factor of the projection
	 * @param central_meridian central meridian of the projection en degrees
	 * @param false_easting false easting in meters
	 * @param false_northing false northing in meters
	 */
	public static LambertConicConformal1SP createLCC1SP(final Ellipsoid ellipsoid,
		double latitude_of_origin, double scale_factor, double central_meridian,
		double false_easting, double false_northing) {
		return createLCC1SP(ellipsoid, latitude_of_origin, scale_factor,
			central_meridian, Unit.DEGREE,
			false_easting, false_northing, Unit.METER);
	}

	/**
	 * LambertConicConformal1SP factory to create a LambertConicConformal1SP
	 * projection from a latitude of origin, a central meridian and false
	 * coordinates in any unit.
	 * @param latitude_of_origin latitude of origin of the projection in degrees
	 * @param scale_factor scale factor of the projection
	 * @param central_meridian central meridian of the projection en degrees
	 * @param angleUnit unit used for central meridian and latitude of origin
	 * @param false_easting false easting in meters
	 * @param false_northing false northing in meters
	 * @param planimetricUnit unit used for false easting and false northing
	 */
	public static LambertConicConformal1SP createLCC1SP(
		final Ellipsoid ellipsoid,
		double latitude_of_origin, double scale_factor,
		double central_meridian, final Unit angleUnit,
		double false_easting, double false_northing, final Unit planimetricUnit) {
		Map<String, Measure> params = new HashMap<String, Measure>();
		params.put(Parameter.LATITUDE_OF_ORIGIN, new Measure(latitude_of_origin, angleUnit));
		params.put(Parameter.SCALE_FACTOR, new Measure(scale_factor, Unit.UNIT));
		params.put(Parameter.CENTRAL_MERIDIAN, new Measure(central_meridian, angleUnit));
		params.put(Parameter.FALSE_EASTING, new Measure(false_easting, planimetricUnit));
		params.put(Parameter.FALSE_NORTHING, new Measure(false_northing, planimetricUnit));
		return new LambertConicConformal1SP(ellipsoid, params);
	}

	/**
	 * Transform coord using a Lambert Conformal Conic projection.
	 * Input coord is supposed to be a geographic latitude / longitude
	 * coordinate in radians.
	 * @param coord coordinate to transform (in radians)
	 * @throws IllegalCoordinateException if <code>coord</code> is not
	 * compatible with this <code>CoordinateOperation</code>.
	 */
	@Override
	public double[] transform(double[] coord) throws IllegalCoordinateException {
		if (coord.length < 2) {
			throw new CoordinateDimensionException(coord, 2);
		}
		if (Double.isNaN(coord[0]) || Double.isNaN(coord[1])) {
			throw new IllegalCoordinateException("Input coordinates can't ne NaN : ", coord);
		}
		double latIso = ellipsoid.isometricLatitude(coord[0]);
		double x = xs + C * exp(-n * latIso) * sin(n * (coord[1] - lon0));
		double y = ys - C * exp(-n * latIso) * cos(n * (coord[1] - lon0));
		coord[0] = x;
		coord[1] = y;
		return coord;
	}

	/**
	 * Creates the inverse CoordinateOperation.
	 */
	@Override
	public CoordinateOperation inverse() throws NonInvertibleOperationException {
		return new LambertConicConformal1SP(ellipsoid, parameters) {

			@Override
			public double[] transform(double[] coord) throws IllegalCoordinateException {
				double x = coord[0];
				double y = coord[1];
				double R = sqrt((x - xs) * (x - xs) + (y - ys) * (y - ys));
				double g = atan((x - xs) / (ys - y));
				double lon = lon0 + g / n;
				double latIso = (-1 / n) * log(abs(R / C));
				double lat = ellipsoid.latitude(latIso);
				coord[0] = lat;
				coord[1] = lon;
				return coord;
			}

			@Override
			public CoordinateOperation inverse()
				throws NonInvertibleOperationException {
				return LambertConicConformal1SP.this;
			}
		};
	}

	/**
	 * Return the <code>Surface</code> type of this <code>Projection</code>.
	 */
	@Override
	public Surface getSurface() {
		return Projection.Surface.CONICAL;
	}

	/**
	 * Return the <code>Property</code> of this <code>Projection</code>.
	 */
	@Override
	public Property getProperty() {
		return Projection.Property.CONFORMAL;
	}

	/**
	 * Return the <code>Orientation</code> of this <code>Projection</code>.
	 */
	@Override
	public Orientation getOrientation() {
		return Projection.Orientation.TANGENT;
	}

	/**
	 * Return a String representation of this projection
	 */
	@Override
	public String toString() {
		return "Lambert Conic Conformal (1SP) ["
			+ "lat0=" + parameters.get(Parameter.LATITUDE_OF_ORIGIN) + ";"
			+ "lon0=" + parameters.get(Parameter.CENTRAL_MERIDIAN) + ";"
			+ "k=" + parameters.get(Parameter.SCALE_FACTOR) + ";"
			+ "x0=" + parameters.get(Parameter.FALSE_EASTING) + ";"
			+ "y0=" + parameters.get(Parameter.FALSE_NORTHING) + "]";
	}
}
