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
import com.jme3.renderer.Camera;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.props.PropertyPanel;


/**
 *  Provides externally settable properties and a convenient
 *  UI for settings things like FOV for the camera.
 *
 *  @author    Paul Speed
 */
public class CameraState extends BaseAppState {

    private float fieldOfView;
    private float near;
    private float far;

    private PropertyPanel properties;

    public CameraState() {
        this(45, 0.1f, 1000); // 45 is the default JME fov
    }
    
    public CameraState( float fov, float near, float far ) {
        this.fieldOfView = fov;
        this.near = near;
        this.far = far;
    }
    
    public void setFieldOfView( float f ) {
        if( this.fieldOfView == f ) {
            return;
        }
        this.fieldOfView = f;
        resetCamera();
    }
    
    public float getFieldOfView() {
        return fieldOfView;
    }
    
    public void setNear( float f ) {
        if( this.near == f ) {
            return;
        }
        this.near = f;
        resetCamera();
    }
    
    public float getNear() {
        return near;
    }
    
    public void setFar( float f ) {
        if( this.far == f ) {
            return;
        }
        this.far = f;
        resetCamera();
    }

    public float getFar() {
        return far;
    }    

    @Override
    protected void initialize( Application app ) {
        
        properties = new PropertyPanel("glass");
        properties.addFloatProperty("Field of View", this, "fieldOfView", 20, 120, 1);
 
        if( getState(WalkingMovementHandler.class) != null ) {
            WalkingMovementHandler walker = getState(WalkingMovementHandler.class);
            properties.addBooleanProperty("Walk Mode", walker, "enabled");
            properties.addFloatProperty("Eye Offset", walker, "eyeOffset", 0.2f, 2f, 0.1f); 
        }
        
        if( getState(SettingsPanelState.class) != null ) {
            getState(SettingsPanelState.class).getParameterTabs().addTab("Camera", properties);
        }
    }

    protected void resetCamera() {
        if( isEnabled() ) {
            Camera camera = getApplication().getCamera();
            float aspect = (float)camera.getWidth() / (float)camera.getHeight(); 
            camera.setFrustumPerspective(fieldOfView, aspect, near, far);
        }    
    }
    
    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        resetCamera();
    }

    @Override
    protected void disable() {
        // need to remove the tab when the state allows it
    }
}

