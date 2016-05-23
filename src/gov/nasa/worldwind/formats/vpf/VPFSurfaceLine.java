/*
 * Copyright (C) 2012 United States Government as represented by the Administrator of the
 * National Aeronautics and Space Administration.
 * All Rights Reserved.
 */
package gov.nasa.worldwind.formats.vpf;

import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Matrix;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.globes.Globe;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.util.SurfaceTileDrawContext;
import gov.nasa.worldwind.util.VecBufferSequence;

/**
 * @author dcollins
 * @version $Id: VPFSurfaceLine.java 1171 2013-02-11 21:45:02Z dcollins $
 */
public class VPFSurfaceLine extends SurfacePolyline // TODO: consolidate with SurfacePolylines
{
    protected Sector sector;
    protected VecBufferSequence buffer;
    protected LatLon referenceLocation;

    public VPFSurfaceLine(VPFFeature feature, VPFPrimitiveData primitiveData)
    {
        String primitiveName = feature.getFeatureClass().getPrimitiveTableName();
        int[] primitiveIds = feature.getPrimitiveIds();

        this.sector = feature.getBounds().toSector();
        this.buffer = (VecBufferSequence) primitiveData.getPrimitiveCoords(primitiveName).slice(primitiveIds);
        this.referenceLocation = feature.getBounds().toSector().getCentroid();
    }

    protected List<Sector> computeSectors(Globe globe)
    {
        if (this.sector == null || this.sector.equals(Sector.EMPTY_SECTOR))
            return null;

        return Arrays.asList(this.sector);
    }

    public Iterable<? extends LatLon> getLocations()
    {
        return this.buffer.getLocations();
    }

    public void setLocations(Iterable<? extends LatLon> iterable)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Position getReferencePosition()
    {
        return new Position(this.referenceLocation, 0d);
    }

    @Override
    protected void applyModelviewTransform(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Apply the geographic to surface tile coordinate transform.
        Matrix modelview = sdc.getModelviewMatrix();
        GL2 gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glMultMatrixd(modelview.toArray(new double[16], 0, false), 0);
    }

    @Override
    protected ShapeAttributes createActiveAttributes()
    {
        return new VPFSymbolAttributes();
    }

    protected void determineActiveGeometry(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        // Intentionally left blank in order to override the superclass behavior with nothing.
    }

    protected void drawOutline(DrawContext dc, SurfaceTileDrawContext sdc)
    {
        this.applyOutlineState(dc, this.getActiveAttributes());

        int drawMode = (this.isClosed() ? GL.GL_LINE_LOOP : GL.GL_LINE_STRIP);
        this.buffer.bindAsVertexBuffer(dc);
        this.buffer.multiDrawArrays(dc, drawMode);
    }
}
