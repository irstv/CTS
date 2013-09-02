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
package org.cts.op.transformation.grids;

import org.cts.cs.GeographicExtent;
import org.cts.cs.OutOfExtentException;

/**
 * A grid with column and rows representing meridians and parallels and cell
 * values representing any parameter as heights (geoid definition), altitudes
 * (digital elevation model), or transformation parameters for transformations
 * based on a model.
 *
 * @author Michaël Michaud, Jules Party
 */
public class GeographicGrid implements Grid {

    protected int colNumber, rowNumber;
    /**
     * The number of values stored in the grid (for instance 3 for 3D
     * translation grid).
     */
    protected int dim;
    protected double x0, y0, xL, yL;
    protected double dx, dy;
    /**
     * Extent of use of the Geographic grid.
     */
    protected GeographicExtent extent;
    protected double modulo;
    /**
     * Scale may be interpreted as the number of digits to write into an ASCII
     * text grid, or as the scale factor to transform float values into int
     * values while using a format supporting only int values (BLEGG).
     */
    int scale = 1;
    /**
     * 3-dimensions float array. Float values has been choosen because
     * quantities represented in a grid may vary from small quantities
     * (rotation) to greater quantities (translation), but never need a great
     * precision (6 digits are generally sufficient).
     */
    protected double[][][] values;
    /**
     * Context object (may be used to specify the reference Datum).
     */
    protected Object context;

    /**
     * Creates a new GeographicGrid.
     */
    protected GeographicGrid() {
    }

    /**
     * Create a new Geographic grid.
     *
     * @param westernLongitude
     * @param northernLatitude
     * @param easternLongitude
     * @param southernLatitude
     * @param colNumber number of column
     * @param rowNumber number of rows
     * @param dim dimension of the stored value(s)
     * @param modulo a tour (360.0 for longitude in degrees, PI*2 for radians
     * and 400 for grades)
     * @param context optional context object
     */
    public GeographicGrid(double westernLongitude, double northernLatitude,
            double easternLongitude, double southernLatitude,
            int colNumber, int rowNumber, int dim, double modulo,
            int scale, Object context) {
        this.x0 = westernLongitude;
        this.y0 = northernLatitude;
        this.xL = easternLongitude < westernLongitude ? easternLongitude + modulo : easternLongitude;
        this.yL = southernLatitude;
        this.colNumber = colNumber;
        this.rowNumber = rowNumber;
        this.dim = dim;
        this.modulo = modulo;
        this.dx = (xL - x0) / (colNumber - 1);
        this.dy = (yL - y0) / (rowNumber - 1);
        this.extent = new GeographicExtent("GG", yL, y0, x0, xL, modulo);
        this.scale = scale;
        this.context = context;
        values = new double[rowNumber][colNumber][dim];
    }

    /**
     * Get the number of columns of this grid (also called cn).
     */
    @Override
    public int getColumnNumber() {
        return colNumber;
    }

    /**
     * Get the number of rows of this grid (also called rn).
     */
    @Override
    public int getRowNumber() {
        return rowNumber;
    }

    /**
     * Get the real world abscisse (x) of the first column.
     */
    @Override
    public double getX0() {
        return x0;
    }

    /**
     * Get the real world ordinate (y) of the first row.
     */
    @Override
    public double getY0() {
        return y0;
    }

    /**
     * Get the first ordinate of the last grid column.
     */
    @Override
    public double getXL() {
        return xL;
    }

    /**
     * Get the second ordinate of the last grid row.
     */
    @Override
    public double getYL() {
        return yL;
    }

    /**
     * Real world interval between two consecutive columns. The method returns a
     * negative value if x values decreases when column indices increases.
     */
    @Override
    public double getDX() {
        return (xL - x0) / (colNumber - 1);
    }

    /**
     * Real world interval between two consecutive row. The method returns a
     * negative value if y values decreases when column indices increases.
     */
    @Override
    public double getDY() {
        return (yL - y0) / (rowNumber - 1);
    }

    /**
     * Get the total grid width as a positive number.
     */
    public double getGridWidth() {
        return Math.abs(xL - x0);
    }

    /**
     * Get the total grid height as a positive number.
     */
    public double getGridHeight() {
        return Math.abs(yL - y0);
    }

    /**
     * Get the scale which determine the number of decimal to read/write or to
     * 'scale' factor to use to obtain an integer parameter.
     */
    public int getScale() {
        return scale;
    }

    /**
     * Set the scale which determine the number of decimal to read/write or the
     * 'scale' factor to use to obtain an integer parameter.
     */
    public void setScale(int scale) {
        this.scale = scale;
    }

    /**
     * Get the context Object.
     */
    public Object getContext() {
        return context;
    }

    /**
     * get the value in row r and column c
     *
     * @param r row index
     * @param c column index
     */
    public double[] getValues(int r, int c) {
        return values[r][c];
    }

    /**
     * set the value in row r and column c
     *
     * @param r row index
     * @param c column index
     * @param value new value of row r column c
     */
    public void setValue(int r, int c, double[] values) {
        System.arraycopy(values, 0, this.values[r][c], 0, dim);
    }

    /**
     * Get the value corrsponding to the x,y position. WARNING : x, y represent
     * the real world coordinates and not the matrix coordinate.
     */
    @Override
    public double[] getValue(double x, double y, Grid.InterpolationMethod method)
            throws OutOfExtentException,
            InterpolationMethodException {
        switch (method) {
            case BILINEAR:
                return bilinearInterpolation(x, y);
            default:
                throw new InterpolationMethodException(method, this.getClass());
        }
    }

    /**
     * Return the array of values.
     */
    public double[][][] getValues() {
        return values;
    }

    /**
     * Return a double value interpolated in this geographic grid with a
     * bilinear interpolation method.<p>
     * dx<br>
     * ----------<br>
     * | |<br>
     * |ddx |dy<br>
     * | + ddy |<br>
     * ----------<br>
     * fx = ddx/dx<br>
     * fy = ddy/dy<br>
     *
     * @param latitude the latitude
     * @param longitude the longitude
     * @return the interpolated value as a double
     */
    // Bug corrigé le 15/11/03 : les indices de grille i+1 ou j+1 provoquent des
    // IndexOutOfBoundsException --> remplacé par
    // i\<nbL-1?i+1:i<br>
    // j\<nbC-1?j+1:j
    public double[] bilinearInterpolation(double latitude, double longitude)
            throws OutOfExtentException {
        if (!extent.isInside(latitude, longitude)) {
            throw new OutOfExtentException(new double[]{latitude, longitude}, extent);
        }
        double x = longitude < x0 ? longitude + modulo : longitude;
        double y = latitude;
        // Utiliser l'origine x0/y0 pour calculer fx/fy évite certains problemes
        // d'arrondis
        // Il est important que le calcul des ligne/colonne de référence se fasse
        // avec la même ecriture que le calcul de fx et fy pour éviter les
        // problemes d'arrondi
        int j = (int) Math.floor((x - x0) / dx);  // column
        double fx = (x - x0) / dx - j;
        int i = (int) Math.floor((y - y0) / dy);  // line
        double fy = (y - y0) / dy - i;

        // Les tests j<(cnb-1) et i<(rnb-1) permettent de gérer le cas des
        // coordonnées situées exactement sur la dernière ligne ou dernière
        // colonne (cela revient à les dupliquer)
        double[] shift = new double[dim];
        for (int k = 0; k < dim; k++) {
            double d1 = values[i][j][k];
            double d2 = values[i < (rowNumber - 1) ? i + 1 : i][j][k];
            double d3 = values[i][j < (colNumber - 1) ? j + 1 : j][k];
            double d4 = values[i < (rowNumber - 1) ? i + 1 : i][j < (colNumber - 1) ? j + 1 : j][k];
            shift[k] = ((1 - fx) * (1 - fy) * d1 + (1 - fx) * fy * d2 + fx * (1 - fy) * d3 + fx * fy * d4);
        }
        return shift;
    }

    /**
     * Return a short string representation of the grid.
     */
    @Override
    public String toString() {
        return "Geographic grid (westLon=" + x0 + " northLat=" + y0
                + " eastLon=" + xL + " southLat=" + yL + " Column[" + colNumber + "] Row[" + rowNumber + "])";
    }

    /**
     * Return a complete string representation of the grid.
     */
    public String toStringAll() {
        StringBuilder sb = new StringBuilder("Geographic grid (westLon=" + x0 + " northLat=" + y0
                + " eastLon=" + xL + " southLat=" + yL + " Column[" + colNumber + "] Row[" + rowNumber + "])\n");
        for (int i = 0; i < rowNumber; i++) {
            for (int j = 0; j < colNumber; j++) {
                sb.append(values[i][j]);
                if (j < (colNumber - 1)) {
                    sb.append("\t");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}