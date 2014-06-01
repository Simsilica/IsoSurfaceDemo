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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedHolder;
import com.simsilica.lemur.core.VersionedObject;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.AnalogFunctionListener;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.input.InputState;
import com.simsilica.lemur.input.StateFunctionListener;


/**
 *
 *
 *  @author    Paul Speed
 */
public class MovementState extends BaseAppState
                           implements AnalogFunctionListener, StateFunctionListener {

    private InputMapper inputMapper;
    private MovementHandler mover;
    private double turnSpeed = 2.5;  // one half complete revolution in 2.5 seconds
    private double yaw = FastMath.PI;
    private double pitch;
    private double maxPitch = FastMath.HALF_PI;
    private double minPitch = -FastMath.HALF_PI;
    private Quaternion facing = new Quaternion().fromAngles((float)pitch, (float)yaw, 0);
    private double forward;
    private double side;
    private double elevation;
    private double speed = 3.0;
    private VersionedHolder<Vector3f> worldPos = new VersionedHolder<Vector3f>(new Vector3f());        

    public MovementState() {
    }

    public void toggleEnabled() {
        setEnabled(!isEnabled());
    }

    public VersionedObject<Vector3f> getWorldPosition() {
        return worldPos;
    }

    public void setMovementHandler( MovementHandler mover ) {
        this.mover = mover;
        updateFacing();
    }
    
    public MovementHandler getMovementHandler() {
        return mover;
    }

    public void setPitch( double pitch ) {
        this.pitch = pitch;
        updateFacing();
    }

    public double getPitch() {
        return pitch;
    }
    
    public void setYaw( double yaw ) {
        this.yaw = yaw;
        updateFacing();
    }
    
    public double getYaw() {
        return yaw;
    }

    public void setRotation( Quaternion rotation ) {
        // Do our best
        float[] angle = rotation.toAngles(null);
        this.pitch = angle[0];
        this.yaw = angle[1];
        updateFacing();
    }
    
    public Quaternion getRotation() {
        return mover.getFacing();
    }

    @Override
    protected void initialize( Application app ) {
        if( this.mover == null ) {
            this.mover = new CameraMovementHandler(app.getCamera());
        }
        
        if( inputMapper == null )
            inputMapper = GuiGlobals.getInstance().getInputMapper();
 
        inputMapper.addDelegate( MainFunctions.F_TOGGLE_MOVEMENT, this, "toggleEnabled" );
        
        // Most of the movement functions are treated as analog.        
        inputMapper.addAnalogListener(this,
                                      MovementFunctions.F_Y_LOOK,
                                      MovementFunctions.F_X_LOOK,
                                      MovementFunctions.F_MOVE,
                                      MovementFunctions.F_ELEVATE,
                                      MovementFunctions.F_STRAFE);

        // Only run mode is treated as a 'state' or a trinary value.
        // (Positive, Off, Negative) and in this case we only care about
        // Positive and Off.  See MovementFunctions for a description
        // of alternate ways this could have been done.
        inputMapper.addStateListener(this,
                                     MovementFunctions.F_RUN,
                                     MovementFunctions.F_SUPER_RUN);
    }

    @Override
    protected void cleanup(Application app) {

        inputMapper.removeDelegate( MainFunctions.F_TOGGLE_MOVEMENT, this, "toggleEnabled" );

        inputMapper.removeAnalogListener( this,
                                          MovementFunctions.F_Y_LOOK,
                                          MovementFunctions.F_X_LOOK,
                                          MovementFunctions.F_MOVE,
                                          MovementFunctions.F_ELEVATE,
                                          MovementFunctions.F_STRAFE);
        inputMapper.removeStateListener( this,
                                         MovementFunctions.F_RUN,
                                         MovementFunctions.F_SUPER_RUN);
    }

    @Override
    protected void enable() {
        // Make sure our input group is enabled
        inputMapper.activateGroup( MovementFunctions.GROUP_MOVEMENT );
        
        // And kill the cursor
        GuiGlobals.getInstance().setCursorEventsEnabled(false);
        
        // A 'bug' in Lemur causes it to miss turning the cursor off if
        // we are enabled before the MouseAppState is initialized.
        getApplication().getInputManager().setCursorVisible(false);        
    }

    @Override
    protected void disable() {
        inputMapper.deactivateGroup( MovementFunctions.GROUP_MOVEMENT );
        GuiGlobals.getInstance().setCursorEventsEnabled(true);        
    }

    @Override
    public void update( float tpf ) {
    
        // 'integrate' camera position based on the current move, strafe,
        // and elevation speeds.
        if( forward != 0 || side != 0 || elevation != 0 ) {
            Vector3f loc = mover.getLocation();
            
            Quaternion rot = mover.getFacing();
            Vector3f move = rot.mult(Vector3f.UNIT_Z).multLocal((float)(forward * speed * tpf)); 
            Vector3f strafe = rot.mult(Vector3f.UNIT_X).multLocal((float)(side * speed * tpf));
            
            // Note: this camera moves 'elevation' along the camera's current up
            // vector because I find it more intuitive in free flight.
            Vector3f elev = rot.mult(Vector3f.UNIT_Y).multLocal((float)(elevation * speed * tpf));
                        
            loc = loc.add(move).add(strafe).add(elev);
            mover.setLocation(loc);
            worldPos.setObject(loc); 
        }
    }
 
    /**
     *  Implementation of the StateFunctionListener interface.
     */
    @Override
    public void valueChanged( FunctionId func, InputState value, double tpf ) {
 
        // Change the speed based on the current run mode
        // Another option would have been to use the value
        // directly:
        //    speed = 3 + value.asNumber() * 5
        //...but I felt it was slightly less clear here.   
        boolean b = value == InputState.Positive;
        if( func == MovementFunctions.F_RUN ) {
            if( b ) {
                speed = 10;
            } else {
                speed = 3;
            }
        } else if( func == MovementFunctions.F_SUPER_RUN ) {
            if( b ) {
                speed = 20;
            } else {
                speed = 3;
            }
        }
    }

    /**
     *  Implementation of the AnalogFunctionListener interface.
     */
    @Override
    public void valueActive( FunctionId func, double value, double tpf ) {
 
        // Setup rotations and movements speeds based on current
        // axes states.
        if( func == MovementFunctions.F_Y_LOOK ) {
            pitch += -value * tpf * turnSpeed;
            if( pitch < minPitch )
                pitch = minPitch;
            if( pitch > maxPitch )
                pitch = maxPitch;
        } else if( func == MovementFunctions.F_X_LOOK ) {
            double yawDelta = -value * tpf * turnSpeed; 
            yaw += yawDelta; 
            if( yaw < 0 )
                yaw += Math.PI * 2;
            if( yaw > Math.PI * 2 )
                yaw -= Math.PI * 2;
        } else if( func == MovementFunctions.F_MOVE ) {
            this.forward = value;
            return;
        } else if( func == MovementFunctions.F_STRAFE ) {
            this.side = -value;
            return;
        } else if( func == MovementFunctions.F_ELEVATE ) {
            this.elevation = value;
            return;
        } else {
            return;
        }
        updateFacing();        
    }

    protected void updateFacing() {
        facing.fromAngles( (float)pitch, (float)yaw, 0 );
        mover.setFacing(facing);
    }
    
    public static class CameraMovementHandler implements MovementHandler {
        private Camera camera;
        
        public CameraMovementHandler( Camera camera ) {
            this.camera = camera;
        }

        public void setLocation( Vector3f loc ) {
            camera.setLocation(loc);
        }
        
        public Vector3f getLocation() {
            return camera.getLocation();
        }

        public void setFacing( Quaternion facing ) {
            camera.setRotation(facing);            
        }
        
        public Quaternion getFacing() {
            return camera.getRotation();
        } 
    }
}


