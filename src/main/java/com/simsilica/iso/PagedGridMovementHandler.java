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

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.pager.PagedGrid;


/**
 *  A movement handler implementation that keeps the camera centered
 *  over the 0, y, 0 land location while moving the land underneath
 *  instead. 
 *
 *  @author    Paul Speed
 */
public class PagedGridMovementHandler implements MovementHandler {

    private Camera camera;
 
    private Vector3f location = new Vector3f();
    private Vector3f camLoc = new Vector3f();
    private PagedGrid pagedGrid;
    
    public PagedGridMovementHandler( PagedGrid pagedGrid, Camera camera ) {
        this.camera = camera;
        this.pagedGrid = pagedGrid;
        setLocation(camera.getLocation());
    }
    
    protected void setLandLocation( float x, float z ) {
        pagedGrid.setCenterWorldLocation(x, z);
    }
    
    @Override
    public final void setLocation( Vector3f loc ) {
        // If the camera has not moved then don't bother passing the
        // information on.  It's an easy check for us to make and in
        // JME, sometimes moving a node with lots of children can
        // be expensive if unnnecessary.
        if( loc.x == location.x && loc.y == location.y && loc.z == location.z ) {
            return;
        }
        
        // Keep the world location.
        location.set(loc);
        
        // Set just the elevation to the camera
        camLoc.set(0, loc.y, 0);
        
        // Pass the land location onto the setLandLocation() method for
        // applying to the paged grid.
        setLandLocation(loc.x, loc.z);
        
        // Give the camera it's new location.
        camera.setLocation(camLoc);
    }
    
    @Override
    public final Vector3f getLocation() { 
        return location;
    }
    
    @Override
    public void setFacing( Quaternion facing ) {
        camera.setRotation(facing);
    }
    
    @Override
    public Quaternion getFacing() {
        return camera.getRotation();
    }
}



