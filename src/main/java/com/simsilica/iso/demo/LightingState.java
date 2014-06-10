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

package com.simsilica.iso.demo;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;

/**
 *
 *
 *  @author    Paul Speed
 */
public class LightingState extends BaseAppState {

    public static final ColorRGBA DEFAULT_DIFFUSE = ColorRGBA.White.mult(2); 
    public static final ColorRGBA DEFAULT_AMBIENT = new ColorRGBA(0.25f, 0.25f, 0.25f, 1); 

    private VersionedHolder<Vector3f> lightDir = new VersionedHolder<Vector3f>();
    
    private ColorRGBA sunColor;
    private DirectionalLight sun;
    private ColorRGBA ambientColor;
    private AmbientLight ambient;
    private float timeOfDay = FastMath.atan2(1, 0.3f) / FastMath.PI;    
    private float inclination = FastMath.HALF_PI - FastMath.atan2(1, 0.4f);
    private float orientation = FastMath.HALF_PI; 
    
    private Node rootNode;  // the one we added the lights to
    
    public LightingState() {
        lightDir.setObject(new Vector3f(-0.2f, -1, -0.3f).normalizeLocal());
        this.sunColor = DEFAULT_DIFFUSE.clone();
        this.ambientColor = DEFAULT_AMBIENT.clone();
    }
 
    public DirectionalLight getSun() {
        return sun;
    }
    
    public VersionedReference<Vector3f> getLightDirRef() {
        return lightDir.createReference();
    }
    
    public void setTimeOfDay( float f ) {
        if( this.timeOfDay == f ) {
            return;
        }
        this.timeOfDay = f;
        resetLightDir();        
    }
    
    public float getTimeOfDay() {
        return timeOfDay;
    }

    public void setOrientation( float f ) {
        if( this.orientation == f ) {
            return;
        }
        this.orientation = f;
        resetLightDir();        
    }
    
    public float getOrientation() {
        return orientation;
    }
 
    protected void resetLightDir() {
        float angle = timeOfDay * FastMath.PI;
 
        Quaternion q1 = new Quaternion().fromAngles(0, 0, (angle - FastMath.HALF_PI));
        Quaternion q2 = new Quaternion().fromAngles(inclination, orientation, 0);
        Vector3f dir = q2.mult(q1).mult(Vector3f.UNIT_Y.negate());
        lightDir.setObject(dir);
        sun.setDirection(lightDir.getObject());
    }
    
    @Override
    protected void initialize( Application app ) {
        sun = new DirectionalLight();
        sun.setColor(sunColor);
        sun.setDirection(lightDir.getObject());
        
        ambient = new AmbientLight();
        ambient.setColor(ambientColor);
        resetLightDir();
        
        setTimeOfDay(0.05f);
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.addLight(sun);
        rootNode.addLight(ambient);
    }

    @Override
    protected void disable() {
        rootNode.removeLight(sun);
        rootNode.removeLight(ambient);
    }
}

