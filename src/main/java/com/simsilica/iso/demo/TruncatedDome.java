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

import com.jme3.math.FastMath;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;


/**
 *  A dome shape that has its base truncated such that it
 *  might represent the part of a sky sphere as seen from
 *  a point on the ground.  The actual height of the dome
 *  will be outer radius - inner radius while the circumference
 *  of the dome is length of the cord through the tip of
 *  inner radius.  The terms are chosen as
 *  if "outer radius" is the radius of the atmosphere while
 *  "inner radius" is the radius of the planet.  It is 
 *  important to note that the dome will then not extend
 *  below the center surface position.  The theory is that
 *  a ground plane would be covering this extra area.
 *
 *  @author    Paul Speed
 */
public class TruncatedDome extends Mesh {

    private int radials;
    private int slices;
    
    private float innerRadius;
    private float outerRadius;
    
    private boolean inside;
    
    public TruncatedDome( float innerRadius, float outerRadius, 
                          int radials, int slices, boolean inside ) {
        updateGeometry(innerRadius, outerRadius, radials, slices, inside);
    }
    
    public final void updateGeometry( float innerRadius, float outerRadius, 
                                int radials, int slices, boolean inside ) {
        if( this.innerRadius == innerRadius && this.outerRadius == outerRadius
            && this.radials == radials && this.slices == slices && this.inside == inside ) {
            return;
        }                                                                                        
                                                                                        
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.radials = radials;
        this.slices = slices;
        this.inside = inside;
 
        // How many vertexes will we need?
        int radialVertCount = radials + 1; // for the seam
        int elevVertCount = slices; 
        int vertCount = radialVertCount * elevVertCount + 1;
        int quadCount = radials * elevVertCount;
        int triCount = 2 * quadCount + radials;

        // Make sure we have buffers the proper size
        FloatBuffer pb = makeFloatBuffer(Type.Position, 3, vertCount);  
        FloatBuffer nb = makeFloatBuffer(Type.Normal, 3, vertCount);  
        FloatBuffer tb = makeFloatBuffer(Type.TexCoord, 3, vertCount);  
 
        // Overlap the first and last for the seam       
        float[] radialSines = new float[radialVertCount];
        float[] radialCosines = new float[radialVertCount];
        float aDelta = FastMath.TWO_PI / radials;
        for( int i = 0; i < radialVertCount; i++ ) {
            radialSines[i] = FastMath.sin(i * aDelta);      
            radialCosines[i] = FastMath.cos(i * aDelta);      
        }

        // For slices we want to try to maximize surface area
        // of each slice from the perspective of the dome's center.
        // For the most part, the triangles near the edge will
        // take care of themselves regardless.  It's the ones directly
        // above that matter most so we will split based on vertical
        // slices and then back them into angles.
        // Actually, we want it even tighter up top and looser towards
        // the horizon... so inverting a sin/cos or something... or
        // some other curve.  Trying a parabola.
        // The "right answer" is to project regular angles from the
        // eye point out to the surface but that math eludes me at the
        // moment and I don't feel like doing quadratic equations for
        // that.
        float[] sliceAngles = new float[elevVertCount];
        
        // For the center point on the base, the cos of the base
        // angle is innerRadius and the hypotenuse is the outerRadius.
        // So... angle is...
        sliceAngles[0] = FastMath.HALF_PI - FastMath.acos(innerRadius / outerRadius);
        float baseRadius = FastMath.cos(sliceAngles[0]) * outerRadius;
        for( int i = 1; i < elevVertCount; i++ ) {
            float t = 1.0f - ((float)i / elevVertCount);
            t = t * t;
            float r = baseRadius * t;
            sliceAngles[i] = FastMath.acos(r / outerRadius);
        }
 
        for( int i = 0; i < elevVertCount; i++ ) {
            float sliceCos = FastMath.cos(sliceAngles[i]);
            float sliceSin = FastMath.sin(sliceAngles[i]);
            
            for( int j = 0; j < radialVertCount; j++ ) {
                float x = radialCosines[j] * sliceCos;
                float y = sliceSin;
                float z = radialSines[j] * sliceCos;
 
                // Should be a unit vector
                if( inside ) {
                    nb.put(-x).put(-y).put(-z);
                } else { 
                    nb.put(x).put(y).put(z);
                }
                
                // Now project it for the position... subtracting inner
                // radius
                pb.put(x * outerRadius);
                pb.put(y * outerRadius - innerRadius);
                pb.put(z * outerRadius);
            }                
        }

        // Now add the pole
        nb.put(0).put(inside ? -1 : 1).put(0);
        pb.put(0).put(outerRadius - innerRadius).put(0);
        
        // Now we need to setup the index buffer
        ShortBuffer ib = makeShortBuffer(Type.Index, 3, triCount);  
        for( int i = 0; i < elevVertCount - 1; i++ ) {
            int base, next;
            if( inside ) {
                base = i * radialVertCount;
                next = (i + 1) * radialVertCount;
            } else {
                next = i * radialVertCount;
                base = (i + 1) * radialVertCount;
            }   
            for( int j = 0; j < radialVertCount - 1; j++ ) {
                ib.put((short)(base + j));
                ib.put((short)(base + j + 1));
                ib.put((short)(next + j + 1));
                ib.put((short)(base + j));
                ib.put((short)(next + j + 1));
                ib.put((short)(next + j));
            }
        }
        
        // Now close the pole
        int base = (elevVertCount - 1) * radialVertCount;
        int tip = elevVertCount * radialVertCount;
        for( int j = 0; j < radialVertCount - 1; j++ ) {
            if( inside ) {
                ib.put((short)(base + j)); 
                ib.put((short)(base + j + 1));                 
                ib.put((short)(tip)); 
            } else {
                ib.put((short)(base + j)); 
                ib.put((short)(tip)); 
                ib.put((short)(base + j + 1)); 
            }   
        }
 
        setBuffer(Type.Position, 3, pb);
        setBuffer(Type.Normal, 3, nb);
        setBuffer(Type.Index, 3, ib);
        
        updateBound();       
    }

    protected FloatBuffer makeFloatBuffer(Type type, int components, int size) {
        FloatBuffer result = getFloatBuffer(type);
        if( result != null && result.capacity() == components * size ) {
            // Current is good enough
            result.clear();
            return result;
        }
        
        // Else we need to create one
        result = BufferUtils.createFloatBuffer(components * size);
        return result;
    }

    protected ShortBuffer makeShortBuffer(Type type, int components, int size) {
        ShortBuffer result = getShortBuffer(type);
        if( result != null && result.capacity() == components * size ) {
            // Current is good enough
            result.clear();
            return result;
        }
        
        // Else we need to create one
        result = BufferUtils.createShortBuffer(components * size);
        return result;
    }

}
