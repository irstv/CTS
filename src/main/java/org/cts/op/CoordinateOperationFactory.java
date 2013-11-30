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

import java.util.ArrayList;
import java.util.List;



import org.cts.Identifier;
import org.cts.crs.GeodeticCRS;
import org.cts.datum.GeodeticDatum;
import org.cts.op.transformation.NTv2GridShiftTransformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CoordinateOperationFactory is a factory used to create
 * {@linkplain  org.cts.op.CoordinateOperation CoordinateOperations} from source
 * and target
 * {@linkplain org.cts.crs.CoordinateReferenceSystem CoordinateReferenceSystems}.
 *
 * @author Michaël Michaud, Jules Party
 */
public final class CoordinateOperationFactory {

    private static final Logger LOG = LoggerFactory.getLogger(CoordinateOperationFactory.class);
    public final static int GDATUM_OP = 1; // ex. NTF 2 RGF
    public final static int VDATUM_OP = 2; // ex. height instead of altitude
    public final static int ELLIPSOID_OP = 4; // ex. height instead of altitude
    public final static int PRIME_MERIDIAN_OP = 8; // ex. height instead of altitude
    public final static int GEOGRAPHIC_OP = 16; // ex. geographic from/to geocentric
    public final static int PROJECTION_OP = 32; // ex. project or unproject
    public final static int DIMENSION_OP = 64; // ex. 2D to 3D or 3D to 2D
    public final static int AXIS_ORDER_OP = 128; // ex. lon/lat instead of lat/lon
    public final static int UNIT_OP = 256; // ex. heights from meters to feet

    /**
     * Creates a list of {@link CoordinateOperation}s from a source
     * {@link org.cts.crs.GeodeticCRS} to a target
     * {@link org.cts.crs.GeodeticCRS}.
     * {@link org.cts.crs.GeodeticCRS}s include {@link org.cts.crs.GeocentricCRS}s,
     * {@link org.cts.crs.Geographic2DCRS}, {@link org.cts.crs.Geographic3DCRS} and
     * {@link org.cts.crs.ProjectedCRS}.
     *
     * @param source the (non null) source geodetic coordinate reference system
     * @param target the (non null) target geodetic coordinate reference system
     */
    public static List<CoordinateOperation> createCoordinateOperations(
            GeodeticCRS source, GeodeticCRS target) {
        if (source == null) {
            throw new IllegalArgumentException("The source CRS must not be null");
        }
        if (target == null) {
            throw new IllegalArgumentException("The target CRS must not be null");
        }
        List<CoordinateOperation> opList = source.getCRSTransformations(target);
        if (opList != null) {
            return opList;
        } else {
            opList = new ArrayList<CoordinateOperation>();
            GeodeticDatum sourceDatum = source.getDatum();
            if (sourceDatum == null) {
                LOG.warn(source.getName() + " has no Geodetic Datum");
                throw new IllegalArgumentException("The source datum must not be null");
            }
            GeodeticDatum targetDatum = target.getDatum();
            if (targetDatum == null) {
                LOG.warn(target.getName() + " has no Geodetic Datum");
                throw new IllegalArgumentException("The target datum must not be null");
            }

            if (source.getGridTransformations(targetDatum) != null) {
                addNadgridsOperationDir(sourceDatum, source, targetDatum, target, source.getGridTransformations(targetDatum), opList);
            } else if (target.getGridTransformations(sourceDatum) != null) {
                addNadgridsOperationInv(sourceDatum, source, targetDatum, target, target.getGridTransformations(sourceDatum), opList);
            }
            if (sourceDatum.equals(targetDatum)) {
                addCoordinateOperations(source, target, opList);
            } else {
                addCoordinateOperations(sourceDatum, source, targetDatum, target, opList);
            }
            source.addCRSTransformation(target, opList);
        }
        return opList;
    }

    /**
     * Adds a CoordinateOperation based on nadGrids to the list of CoordinateOperations
     * usable to transform coordinates from source CRS to target CRS.<br>
     * NadGrids operations link a source {@link org.cts.crs.GeodeticCRS} to a target
     * {@link org.cts.crs.GeodeticCRS} using different {@link org.cts.datum.Datum}s
     * but without making use of Geocentric coordinates.<br>
     * Remember that {@link org.cts.crs.GeodeticCRS} include {@link org.cts.crs.GeocentricCRS},
     * {@link org.cts.crs.Geographic2DCRS}, {@link org.cts.crs.Geographic3DCRS} and
     * {@link org.cts.crs.ProjectedCRS}, but here the use of {@link org.cts.crs.GeocentricCRS}
     * is senseless.<br>
     * NB : This method is used when nadgrid is defined in the source CRS. If nadgrid is defined
     * in the target CRS, use {@link #addNadgridsOperationInv(org.cts.datum.GeodeticDatum, org.cts.crs.GeodeticCRS, org.cts.datum.GeodeticDatum, org.cts.crs.GeodeticCRS, java.util.List, java.util.List)} .
     *
     * @param sourceDatum the (non null) datum used by source CRS.
     * @param source the source coordinate reference system. Should not be a GeocentricCRS.
     * @param targetDatum the (non null) datum used by target CRS
     * @param target the target coordinate reference system. Should not be a GeocentricCRS.
     * @param nadgridsTransformations the transformation between two Geographic CRS
     * @param opList the list to which the CoordinateOperation must be added
     */
    private static void addNadgridsOperationDir(
            GeodeticDatum sourceDatum, GeodeticCRS source,
            GeodeticDatum targetDatum, GeodeticCRS target, List<CoordinateOperation> nadgridsTransformations,
            List<CoordinateOperation> opList) {
        for (CoordinateOperation coordOp : nadgridsTransformations) {
            try {
                if (!(coordOp instanceof NTv2GridShiftTransformation) || (sourceDatum.getShortName().equals(((NTv2GridShiftTransformation) coordOp).getFromDatum()))) {
                    opList.add(new CoordinateOperationSequence(
                            new Identifier(CoordinateOperationSequence.class, source.getName() + " to " + target.getName()),
                            source.toGeographicCoordinateConverter(),
                            coordOp,
                            target.fromGeographicCoordinateConverter()));
                } else {
                    NTv2GridShiftTransformation gt = (NTv2GridShiftTransformation) coordOp;
                    GeodeticDatum gtSource = GeodeticDatum.datumFromName.get(gt.getFromDatum());
                    opList.add(new CoordinateOperationSequence(
                            new Identifier(CoordinateOperationSequence.class, sourceDatum.getName() + " to " + targetDatum.getName() + " through " + gt.getName() + " transformation"),
                            source.toGeographicCoordinateConverter(),
                            sourceDatum.getCoordinateOperations(gtSource).get(0),
                            gt,
                            target.fromGeographicCoordinateConverter()));
                }
            } catch (NonInvertibleOperationException e) {
                LOG.warn("Operation from " + source.getName() + " to " + target.getName() + " could not be created");
                LOG.error("CoordinateOperationFactory", e);
            }
        }
    }

    /**
     * Adds a CoordinateOperation based on nadGrids to the list of CoordinateOperations
     * usable to transform coordinates from source CRS to target CRS.<br>
     * NadGrids operations link a source {@link org.cts.crs.GeodeticCRS} to a target
     * {@link org.cts.crs.GeodeticCRS} using different {@link org.cts.datum.Datum}s
     * but without making use of Geocentric coordinates.<br>
     * Remember that {@link org.cts.crs.GeodeticCRS} include {@link org.cts.crs.GeocentricCRS},
     * {@link org.cts.crs.Geographic2DCRS}, {@link org.cts.crs.Geographic3DCRS} and
     * {@link org.cts.crs.ProjectedCRS}, but here the use of {@link org.cts.crs.GeocentricCRS}
     * is senseless.<br>
     * NB : This method is used when nadgrid is defined in the target CRS. If nadgrids is defined
     * in the source CRS, use {@link #addNadgridsOperationDir(org.cts.datum.GeodeticDatum, org.cts.crs.GeodeticCRS, org.cts.datum.GeodeticDatum, org.cts.crs.GeodeticCRS, java.util.List, java.util.List)}
     *
     * @param sourceDatum the (non null) datum used by source CRS
     * @param source the source coordinate reference system. Should not be a GeocentricCRS.
     * @param targetDatum the (non null) datum used by target CRS
     * @param target the target coordinate reference system. Should not be a GeocentricCRS.
     * @param nadgridsTransformations the transformation between two Geographic CRS
     * @param opList the list in which the CoordinateOperation must be added
     */
    private static void addNadgridsOperationInv(
            GeodeticDatum sourceDatum, GeodeticCRS source,
            GeodeticDatum targetDatum, GeodeticCRS target, List<CoordinateOperation> nadgridsTransformations,
            List<CoordinateOperation> opList) {
        for (CoordinateOperation coordOp : nadgridsTransformations) {
            try {
                if (!(coordOp instanceof NTv2GridShiftTransformation) || sourceDatum.getShortName().equals(((NTv2GridShiftTransformation) coordOp).getFromDatum())) {
                    opList.add(new CoordinateOperationSequence(
                            new Identifier(CoordinateOperationSequence.class, source.getName() + " to " + target.getName()),
                            source.toGeographicCoordinateConverter(),
                            coordOp.inverse(),
                            target.fromGeographicCoordinateConverter()));
                } else {
                    NTv2GridShiftTransformation gt = (NTv2GridShiftTransformation) coordOp;
                    GeodeticDatum gtSource = GeodeticDatum.datumFromName.get(gt.getFromDatum());
                    opList.add(new CoordinateOperationSequence(
                            new Identifier(CoordinateOperationSequence.class, source.getName() + " to " + target.getName()),
                            source.toGeographicCoordinateConverter(),
                            gt.inverse(),
                            gtSource.getCoordinateOperations(targetDatum).get(0),
                            target.fromGeographicCoordinateConverter()));
                }
            } catch (NonInvertibleOperationException e) {
                LOG.warn("Operation from " + source.getName() + " to " + target.getName() + " could not be created");
                LOG.error("CoordinateOperationFactory", e);
            }
        }
    }

    /**
     * Adds a CoordinateOperation to the list of CoordinateOperations usable to transform
     * coordinates from source CRS to target CRS.
     * parameter. This CoordinateOperation links a source
     * {@link org.cts.crs.GeodeticCRS} to a target {@link org.cts.crs.GeodeticCRS} using the
     * same {@link org.cts.datum.GeodeticDatum}.
     * Remember that {@link org.cts.crs.GeodeticCRS}s include {@link org.cts.crs.GeocentricCRS}s,
     * {@link org.cts.crs.Geographic2DCRS}s, {@link org.cts.crs.Geographic3DCRS}s and
     * {@link org.cts.crs.ProjectedCRS}s.
     *
     * @param source the source geodetic coordinate reference system
     * @param target the target geodetic coordinate reference system
     * @param opList the list in which the CoordinateOperation must be added
     */
    private static void addCoordinateOperations(
            GeodeticCRS source, GeodeticCRS target,
            List<CoordinateOperation> opList) {
        try {
            opList.add(new CoordinateOperationSequence(
                    new Identifier(CoordinateOperationSequence.class, source.getName() + " to " + target.getName()),
                    source.toGeographicCoordinateConverter(),
                    target.fromGeographicCoordinateConverter()));
        } catch (NonInvertibleOperationException e) {
            LOG.warn("Operation from " + source.getName() + " to " + target.getName() + " could not be created");
            LOG.error("CoordinateOperationFactory", e);
        }
    }

    /**
     * Adds a CoordinateOperation to the list of CoordinateOperations usable to transform
     * coordinates from source CRS to target CRS.
     * This CoordinateOperation links a source {@link org.cts.crs.GeodeticCRS}
     * to a target {@link org.cts.crs.GeodeticCRS} which may use different {@link org.cts.datum.Datum}s.
     * Remember that {@link org.cts.crs.GeodeticCRS}s include {@link org.cts.crs.GeocentricCRS}s,
     * {@link org.cts.crs.Geographic2DCRS}s, {@link org.cts.crs.Geographic3DCRS}s and
     * {@link org.cts.crs.ProjectedCRS}s.
     *
     * @param sourceDatum the (non null) datum used by source CRS
     * @param source the source geodetic coordinate reference system
     * @param targetDatum the (non null) datum used by target CRS
     * @param target the target geodetic coordinate reference system
     * @param opList the list in which the CoordinateOperation must be added
     */
    private static void addCoordinateOperations(
            GeodeticDatum sourceDatum, GeodeticCRS source,
            GeodeticDatum targetDatum, GeodeticCRS target,
            List<CoordinateOperation> opList) {
        // We get registered transformation from source GeodeticDatum to target GeodeticDatum
        // There maybe one or more transformations available.
        List<CoordinateOperation> datumTransformations = sourceDatum.getCoordinateOperations(targetDatum);
        for (CoordinateOperation datumTf : datumTransformations) {
            try {
                opList.add(new CoordinateOperationSequence(
                        new Identifier(CoordinateOperationSequence.class,
                        source.getName() + " to " + target.getName() + " through " + datumTf.getName()),
                        source.toGeographicCoordinateConverter(),
                        datumTf,
                        target.fromGeographicCoordinateConverter()));
            } catch (NonInvertibleOperationException e) {
                LOG.warn("Operation from " + source.getName() + " to " + target.getName()
                        + " through " + datumTf.getName() + " could not be created");
                LOG.error("CoordinateOperationFactory", e);
            }
        }
    }
}
