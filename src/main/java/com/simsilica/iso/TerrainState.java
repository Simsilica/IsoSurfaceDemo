/*
 * $Id$
 * 
 * Copyright (c) 2014, Simsilica, LLC
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions 
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright 
 *    notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright 
 *    notice, this list of conditions and the following disclaimer in 
 *    the documentation and/or other materials provided with the 
 *    distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.simsilica.iso;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.builder.Builder;
import com.simsilica.builder.BuilderState;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.pager.Grid;
import com.simsilica.pager.PagedGrid;
import com.simsilica.pager.ZoneFactory;
import com.simsilica.pager.debug.BBoxZone;


/**
 *  Manages the paged grid for the land mass, vegetation, etc..
 *
 *  @author    Paul Speed
 */
public class TerrainState extends BaseAppState {

    public static final int CHUNK_SIZE_XZ = 64;
    public static final int CHUNK_SIZE_Y = 32;
    
    private PagedGrid pager;
    private Node land;

    @Override
    protected void initialize( Application app ) {
 
        // Create the root node that we'll attach everything to
        // for convenient add/remove
        land = new Node("Terrain");
 
        // Grab the builder from the builder state
        // The builder will build the pager's zones on a background thread and
        // apply them on the update thread.
        Builder builder = getState(BuilderState.class).getBuilder();
        
        // Setup the grid size information based on
        // the trunk size and a potential xz scaling.
        int cx = CHUNK_SIZE_XZ;
        int cy = CHUNK_SIZE_Y;
        int cz = CHUNK_SIZE_XZ;
 
        // We can use xzScale to scale the land zones out and then
        // super-sample the density field.  In other words, instead
        // of ending up with a 64x64 grid with 1 meter sampling we
        // end up with a 128x128 meter grid with 2 meter sampling.
        // It's a small reduction in quality but a huge win in the
        // number of zones we can display at once.
        float xzScale = 1;
                
        int xzSize = (int)(cx * xzScale);
        
        // Figure out what visible radius we should use for the grid
        // based on a desired size
        int desiredSize = 192; // roughly
        
        float idealRadius = (float)desiredSize / xzSize;
        int radius = (int)Math.ceil(idealRadius);
        // We always want to show at least the desired size
 
        // We will clamp our land to -32 to 96 meters
        int yStart = -32;
        int yEnd = 96;
        int yLayers = (yEnd - yStart) / cy;       
 
        // Our terrain will eventually be generated such that we want to
        // offset it down by 42 meters.  It's a magic number arrived at
        // visually.
        int yBase = -42;

        // Now we have enough to create our grid model.
        // The first parameter is the grid spacing in x,y,z.  The second one
        // is the grid offset. 
        Grid grid = new Grid(new Vector3f(xzSize, cy, xzSize), new Vector3f(0, yBase, 0));

        // For the moment, we will create just a bounding box zone
        // factory to test that the paging grid is working.           
        Material boxMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Red, false).getMaterial();
        boxMaterial.getAdditionalRenderState().setWireframe(true);
        ZoneFactory rootFactory = new BBoxZone.Factory(boxMaterial);
        
        pager = new PagedGrid(rootFactory, builder, grid, yLayers, radius);        
        land.attachChild(pager.getGridRoot());
        
        
        // And finally, we need to have our camera movement go through the
        // pager instead of directly to the camera
        getState(MovementState.class).setMovementHandler(
                new PagedGridMovementHandler(pager, app.getCamera()) {
                    @Override
                    protected void setLandLocation( float x, float z ) {
                        super.setLandLocation(x, z);
                        //worldOffset.set(x, 0, z);
                    }
                });
                                 
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getRootNode().attachChild(land);
    }

    @Override
    protected void disable() {
        land.removeFromParent();
    }
}


