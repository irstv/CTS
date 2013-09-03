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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.cts.CoordinateDimensionException;
import org.cts.Identifier;
import org.cts.IllegalCoordinateException;
import org.cts.op.AbstractCoordinateOperation;
import org.cts.op.CoordinateOperation;
import org.cts.op.NonInvertibleOperationException;
import org.cts.op.transformation.grid.GridShift;
import org.cts.op.transformation.grid.GridShiftFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Geographic Offset by Interpolation of Gridded Data.<p>
 * The relationship between some geographical 2D coordinate reference systems is
 * available through gridded data sets of latitude and longitude offsets.
 *
 * @author Michaël Michaud
 */
public class NTv2GridShiftTransformation extends AbstractCoordinateOperation {

    static final Logger LOGGER = LoggerFactory.getLogger(NTv2GridShiftTransformation.class);
    /**
     * The Identifier used for all NTv2 Grid Shift Transformation.
     */
    private static final Identifier opId =
            new Identifier("EPSG", "9615", "NTv2 Geographic Offset", "NTv2");
    public static final int SPEED = 0;
    public static final int LOW_MEMORY = 1;
    /**
     * if set to true, this class will use a RandomAccessFile to access the
     * gridded data instead of loading it into memory.
     */
    private int mode = 1;
    /**
     * The URL used to find the grid associated to the NTv2 transformation.
     */
    private URL grid_file;
    /**
     * The GridShiftFile that define this transformation.
     */
    private GridShiftFile gsf;

    /**
     * Create a NTv2GridShiftTransformation from the name of the file that
     * defined it.
     *
     * @param ntv2_gridName the name of the file that defined the wanted grid
     * transformation (for instance : ntf_r93.gsb).
     * @throws URISyntaxException
     * @throws MalformedURLException
     */
    public static NTv2GridShiftTransformation createNTv2GridShiftTransformation(String ntv2_gridName) throws URISyntaxException, MalformedURLException, NullPointerException {
        return new NTv2GridShiftTransformation(GridShift.class.getResource(ntv2_gridName).toURI().toURL());
    }

    /**
     * NTv2GridShiftTransformation constructor.
     *
     * @param ntv2_gridFile file containing the description of the NTv2 grid
     * @param precision mean precision of the geodetic transformation
     */
    public NTv2GridShiftTransformation(URL ntv2_gridFile, double precision) {
        super(opId);
        this.grid_file = ntv2_gridFile;
        this.gsf = new GridShiftFile();
        this.precision = Math.max(0.000000001, precision);
    }

    /**
     * NTv2GridShiftTransformation constructor.
     *
     * @param ntv2_gridFile file containing the description of the NTv2 grid
     */
    public NTv2GridShiftTransformation(URL ntv2_gridFile) {
        super(opId);
        this.grid_file = ntv2_gridFile;
        if (ntv2_gridFile == null) {
            LOGGER.warn("No NTv2 Grid file specified.");
        }
        this.gsf = new GridShiftFile();
    }

    /**
     * Shift geographic coordinates (in decimal degrees) by an offset
     * interpolated in a grid.
     *
     * @param coord coordinate to shift
     * @throws IllegalCoordinateException if <code>coord</code> is not
     * compatible with this <code>CoordinateOperation</code>.
     */
    @Override
    public double[] transform(double[] coord) throws IllegalCoordinateException {
        if (coord.length < 2) {
            throw new CoordinateDimensionException(coord, 2);
        }
        GridShift gs = new GridShift();
        gs.setLatDegrees(coord[0] * 180d / Math.PI);
        gs.setLonPositiveEastDegrees(coord[1] * 180d / Math.PI);
        try {
            if (gsf == null) {
                loadGridShiftFile();
            }
            boolean withinGrid = gsf.gridShiftForward(gs);
            if (withinGrid) {
                coord[0] = gs.getShiftedLatDegrees() * Math.PI / 180d;
                coord[1] = gs.getShiftedLonPositiveEastDegrees() * Math.PI / 180d;
            }
        } catch (IOException ioe) {
            throw new CoordinateDimensionException(ioe.getMessage());
        }
        return coord;
    }

    /**
     * Creates the inverse CoordinateOperation.
     */
    @Override
    public CoordinateOperation inverse() throws NonInvertibleOperationException {
        return new NTv2GridShiftTransformation(grid_file, precision) {
            @Override
            public double[] transform(double[] coord) throws IllegalCoordinateException {
                if (coord.length < 2) {
                    throw new CoordinateDimensionException(coord, 2);
                }
                GridShift gs = new GridShift();
                gs.setLatDegrees(coord[0] * 180d / Math.PI);
                gs.setLonPositiveEastDegrees(coord[1] * 180d / Math.PI);
                try {
                    if (gsf == null) {
                        loadGridShiftFile();
                    }
                    boolean withinGrid = gsf.gridShiftReverse(gs);
                    if (withinGrid) {
                        coord[0] = gs.getShiftedLatDegrees() * Math.PI / 180d;
                        coord[1] = gs.getShiftedLonPositiveEastDegrees() * Math.PI / 180d;
                    }
                } catch (IOException ioe) {
                    throw new CoordinateDimensionException(ioe.getMessage());
                }
                return coord;
            }

            @Override
            public CoordinateOperation inverse()
                    throws NonInvertibleOperationException {
                return NTv2GridShiftTransformation.this;
            }
        };
    }

    /**
     * Load the grid file that will be used to transform the coordinates.
     */
    public void loadGridShiftFile() throws IOException {
        if (grid_file != null) {
            if (mode == 0) {
                if (grid_file.getProtocol().equals("file")) {
                    File file = new File(grid_file.getFile());
                    if (!file.exists() || !file.canRead()) {
                        LOGGER.warn("This grid doesn't exist or cannot be read.");
                    } else {
                        gsf.loadGridShiftFile(new FileInputStream(file), false);
                    }
                } else {
                    InputStream is = new BufferedInputStream(grid_file.openConnection().getInputStream());
                    gsf.loadGridShiftFile(is, false);
                }
            } else if (mode == 1) {
                if (grid_file.getProtocol().equals("file")) {
                    File file = new File(grid_file.getFile());
                    if (!file.exists() || !file.canRead()) {
                        LOGGER.warn("This grid doesn't exist or cannot be read.");
                    } else {
                        gsf.loadGridShiftFile(new RandomAccessFile(file, "r"));
                    }
                } else {
                    LOGGER.warn("This grid cannot be accessed.");
                }
            } else {
                LOGGER.warn("This mode is not supported. The grid won't be used.");
            }
        } else {
            LOGGER.warn("The location of the grid is null. Any grid will be used.");
        }
    }

    /**
     * Return whether the grid shift file used by this transformation is loaded
     * or not.
     */
    public boolean isLoaded() {
        return gsf.isLoaded();
    }

    /**
     * Unload the grid shift file used by this transformation.
     *
     * @throws IOException
     */
    public void unload() throws IOException {
        gsf.unload();
    }

    /**
     * Set the mode to access the grid shift file. If mode = 0 (SPEED), it will
     * use an InputStream, if mode = 1 (LOW_MEMORY), it will use a
     * RandomAccessFile. If the parameter mode is different from 0 or 1 or if it
     * is equal to the current mode, this method will have no effect and return
     * false.
     *
     * @param mode an integer representing a mode to access to grid shift file
     * (see description above)
     * @return true if a new mode has been set, false if not.
     * @throws IOException
     */
    public boolean setMode(int mode) throws IOException {
        if ((mode == 0 || mode == 1) && this.mode != mode) {
            this.mode = mode;
            unload();
            loadGridShiftFile();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns this Geocentric translation as a String.
     */
    @Override
    public String toString() {
        return "NTv2 Geographic Offset (" + grid_file + ")";
    }

    /**
     * Return the short name of the datum from which the nadgrids transformation
     * must be used.
     */
    public String getFromDatum() {
        return gsf.getFromEllipsoid().trim().toLowerCase();
    }

    /**
     * Return the short name of the datum toward which the nadgrids
     * transformation must be used.
     */
    public String getToDatum() {
        return gsf.getToEllipsoid().trim().toLowerCase();
    }
}
