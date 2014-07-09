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
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.simsilica.iso.DensityVolume;
import com.simsilica.iso.collision.Collider;
import com.simsilica.iso.collision.Contact;
import com.simsilica.iso.collision.SimpleVolumeCollider;
import com.simsilica.iso.volume.CachingDensityVolume;
import com.simsilica.lemur.event.BaseAppState;



/**
 *  Wraps a movement handler to provide terrain
 *  collision, eye height above the surface, basic physics, 
 *  etc..
 *
 *  @author    Paul Speed
 */
public class WalkingMovementHandler extends BaseAppState
                                    implements MovementHandler {

    private MovementHandler delegate;
    private DensityVolume worldVolume;  
    private DensityVolume cachedVolume;
    private Collider worldCollider;
 
    private float radius = 0.2f;   

    private float eyeHeight = 1.5f;
    
    private Vector3f rawPosition = new Vector3f();
    private Vector3f lastPosition = new Vector3f();
    private Vector3f position = new Vector3f();
    private float brakingAcceleration = 10;
    private float maxSpeed = 5;
    private float terminalVelocity = 10;
    private Vector3f velocity = new Vector3f();
    private Vector3f acceleration = new Vector3f(0, -10, 0);
    private boolean moving;
    private Vector3f movement = new Vector3f();
 
    
    public WalkingMovementHandler() {
        setEnabled(false);
    }
 
    public void setEyeOffset( float f ) {
        this.eyeHeight = f - 0.2f;
    }
    
    public float getEyeOffset() {
        return eyeHeight + 0.2f;
    }
     
    @Override
    public void setLocation( Vector3f loc ) {
        rawPosition.set(loc);
        
        float x = loc.x - position.x;
        float z = loc.z - position.z; 

        // We don't want drift, so we'll just set the velocity
        // to x,z directly.
        movement.x = x;
        movement.y = 0;
        movement.z = z;
        moving = true; 
    }
    
    @Override
    public Vector3f getLocation() {
        return delegate.getLocation();
    }
    
    @Override
    public void setFacing( Quaternion facing ) {
        delegate.setFacing(facing);
    }
    
    @Override
    public Quaternion getFacing() {
        return delegate.getFacing();
    }    

    @Override
    protected void initialize( Application app ) {
        this.worldVolume = getState(TerrainState.class).getWorldVolume();
        this.cachedVolume = new CachingDensityVolume(10, worldVolume, 16, 16); 
        this.worldCollider = new SimpleVolumeCollider(cachedVolume);
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    public void update( float tpf ) {

        // Without drift, we just set the movement
        // directly as an impulse
        if( moving ) {
            velocity.x = movement.x / tpf;
            velocity.z = movement.z / tpf;
        }
        
        // Integrate
        velocity.x += acceleration.x * tpf;
        velocity.y += acceleration.y * tpf;
        velocity.z += acceleration.z * tpf;

        // Terminal velocity 
        if( velocity.y * tpf < -terminalVelocity ) {
            velocity.y = -terminalVelocity / tpf;
        } 
 
        lastPosition.set(position);       
        position.x += velocity.x * tpf;
        position.y += velocity.y * tpf;
        position.z += velocity.z * tpf;

        // Clamp to the water level for now
        if( position.y < -10 ) {
            position.y = -10;
        }

        // See if we are colliding
        Contact c = worldCollider.getContact(position, radius);
        if( c != null ) {            
            position.addLocal(c.contactNormal.mult(c.penetration));            
        }
        if( c != null && c.contactNormal.y > 0.70711 ) {
            // Not falling
            acceleration.set(0, 0, 0);
            velocity.y = 0;
        } else {
            // We are falling
            acceleration.set(0, -10, 0);
        }         
        
        delegate.setLocation(position.add(0, eyeHeight, 0));
        
        if( moving ) {
            moving = false;
        } else {
            // Start braking
            float x = velocity.x;
            float z = velocity.z;
            float d = FastMath.sqrt(x * x + z * z);
            float slow = Math.min(d, brakingAcceleration * tpf);
            if( Math.abs(d) < 0.001 ) {
                velocity.x = 0;
                velocity.z = 0;
            } else {                           
                velocity.x -= (x/d) * slow;
                velocity.z -= (z/d) * slow;
            }
        }
    }

    @Override
    protected void enable() {
        
        MovementState movementState = getState(MovementState.class);
        this.delegate = movementState.getMovementHandler();       
    
        position.set(delegate.getLocation());
 
        movementState.setMovementHandler(this);
    }

    @Override
    protected void disable() {
        getState(MovementState.class).setMovementHandler(delegate);
    }
}
