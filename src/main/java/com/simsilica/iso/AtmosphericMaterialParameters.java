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

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix4f;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;




/**
 *
 *
 *  @author    Paul Speed
 */
public class AtmosphericMaterialParameters {

    // This one will be common and global so we might as
    // well keep an instance around.
    private Material skyMaterial;
    
    /**
     *  The 'position' of the light in the sky, ie:
     *  -direction.
     */
    private Vector3f lightPosition = new Vector3f();
 
    private float lightIntensity;   
    private float skyExposure;
    private float groundExposure;
    private float skyGamma;
    private float groundGamma;
    
    private Vector3f wavelengths = new Vector3f();
    private Vector3f wavelengthsPow4 = new Vector3f();
    private Vector3f invPow4Wavelengths = new Vector3f();
    private Vector3f invPow4WavelengthsKrESun = new Vector3f();
    private Vector4f scatteringConstants = new Vector4f();
    private Vector3f kWavelengths4PI = new Vector3f();
    private float mpaFactor;
        
    private float innerRadius;
    private float outerRadius;
    private float averageDensityScale;
    private float kFlatteningSky;
    private float skyDomeRadius;
    private float planetRadius; // used for ground scale 
    
    public AtmosphericMaterialParameters() {
        setWavelengths(0.650f, 0.570f, 0.475f);
        setRayleighConstant(0.0025f);
        setMieConstant(0.001f);
        this.mpaFactor = -0.990f;
 
        this.lightPosition.set(0, 1, 0);       
        this.lightIntensity = 20;
        this.skyExposure = 1;
        this.groundExposure = 1;
        this.skyGamma = 2.0f;
        this.groundGamma = 0;

        this.innerRadius = 10;
        this.outerRadius = 10.25f;
        this.averageDensityScale = 0.25f;
        this.skyDomeRadius = 10;
        this.planetRadius = 10;  
        this.kFlatteningSky = 0.0f;
    }
    
    public Material getSkyMaterial( AssetManager assets ) {
 
        if( skyMaterial != null ) {
            return skyMaterial;
        }
       
        skyMaterial = new Material(assets, "MatDefs/SkyAtmospherics.j3md");
        skyMaterial.setVector3("LightPosition", lightPosition);
        skyMaterial.setVector3("InvWavelengthsKrESun", invPow4WavelengthsKrESun);        
        skyMaterial.setVector3("KWavelengths4PI", kWavelengths4PI);        

        updateSkyMaterial(skyMaterial);        

        return skyMaterial; 
    }

    protected void updateMaterials() {
        // Right now just the one potential
        if( skyMaterial != null ) {
            updateSkyMaterial(skyMaterial);
        }
    }

    protected void updatePackedStructures() {
        //vec3 attenuation = exp(-scatter * (m_InvWavelengths * r4PI + m4PI));
        // K(wavelengths) * 4 * PI = m_InvWavelengths * r4PI + m4PI        
        float r4PI = scatteringConstants.y; 
        float m4PI = scatteringConstants.w; 
        kWavelengths4PI.x = invPow4Wavelengths.x * r4PI + m4PI;            
        kWavelengths4PI.y = invPow4Wavelengths.y * r4PI + m4PI;            
        kWavelengths4PI.z = invPow4Wavelengths.z * r4PI + m4PI;
                    
        float rESun = scatteringConstants.x * lightIntensity;
        invPow4WavelengthsKrESun.x = invPow4Wavelengths.x * rESun;
        invPow4WavelengthsKrESun.y = invPow4Wavelengths.y * rESun;
        invPow4WavelengthsKrESun.z = invPow4Wavelengths.z * rESun;
    }

    protected void updateSkyMaterial( Material m ) {    
        updatePackedStructures();
        
        m.setFloat("KmESun", scatteringConstants.z * lightIntensity); 
        m.setFloat("Exposure", skyExposure);
        m.setFloat("InnerRadius", innerRadius);
        m.setFloat("RadiusScale", 1 / (outerRadius - innerRadius));
        m.setFloat("Flattening", kFlatteningSky);
        m.setFloat("PlanetScale", outerRadius / skyDomeRadius); 
        m.setFloat("AverageDensityScale", averageDensityScale);
        m.setFloat("InvAverageDensityHeight", 1 / ((outerRadius - innerRadius) * averageDensityScale));
 
        float g = mpaFactor;
        float g2 = g * g;
        float phasePrefix1 = 1.5f * ((1.0f - g2) / (2.0f + g2));
        float phasePrefix2 = 1.0f + g2;
        float phasePrefix3 = 2.0f * g;  
        m.setFloat("PhasePrefix1", phasePrefix1);                
        m.setFloat("PhasePrefix2", phasePrefix2);                
        m.setFloat("PhasePrefix3", phasePrefix3);
    }

    public void applyGroundParameters( Material m ) {
        updatePackedStructures();
        
        // We may have never set them before
        m.setFloat("KmESun", scatteringConstants.z * lightIntensity); 
        m.setVector3("LightPosition", lightPosition);
        m.setVector3("InvWavelengthsKrESun", invPow4WavelengthsKrESun);        
        m.setVector3("KWavelengths4PI", kWavelengths4PI);
                
        m.setFloat("Exposure", groundExposure);
        m.setFloat("InnerRadius", innerRadius);
        m.setFloat("RadiusScale", 1 / (outerRadius - innerRadius));
        m.setFloat("PlanetScale", innerRadius / planetRadius); 
        m.setFloat("AverageDensityScale", averageDensityScale);
        m.setFloat("InvAverageDensityHeight", 1 / ((outerRadius - innerRadius) * averageDensityScale));
    }

    /**
     *  Sets the percentage elevation of the average atmospheric 
     *  density.  For example, 0.25 is 25% of the distance between
     *  sea level and the outer atmosphere.  This controls the
     *  density curve of the atmosphere.
     */
    public void setAverageDensityScale( float f ) {
        if( this.averageDensityScale == f ) {
            return;
        }
        this.averageDensityScale = f;
        updateMaterials();
    }
    
    public float getAverageDensityScale() {
        return averageDensityScale;
    } 

    /**
     *  Sets the radius of the sky dome in geometry units.
     *  This is not based on the real world and is only based
     *  on the actual radius of the sky dome geometry and
     *  allows the shaders to properly scale points into
     *  the internal dimensions used by the shaders.
     */
    public void setSkyDomeRadius( float f ) {
        if( this.skyDomeRadius == f ) {
            return;
        }
        this.skyDomeRadius = f;
        updateMaterials();
    }
    
    public float getSkyDomeRadius() {
        return skyDomeRadius;
    }
 
    /**
     *  Controls the scale of ground-based scattering.  Set
     *  this to the real planet radius in geometry units.
     *  For example, if 1 unit = 1 meter then for earth the
     *  radius would be: 6378100
     *  Changing this value will change how fast ground
     *  points attenuate over distance. 
     */   
    public void setPlanetRadius( float f ) {
        if( this.planetRadius == f ) {
            return;
        }
        this.planetRadius = f;
        updateMaterials();
    }
    
    public float getPlanetRadius() {
        return planetRadius;
    }

    public final void setRayleighConstant( float f ) {
        if( this.scatteringConstants.x == f ) {
            return;
        }
        this.scatteringConstants.x = f;
        this.scatteringConstants.y = f * 4 * FastMath.PI;        
        updateMaterials();
    }
        
    public float getRayleighConstant() {
        return scatteringConstants.x;
    }
    
    public final void setMieConstant( float f ) {
        if( this.scatteringConstants.z == f ) {
            return;
        }
        this.scatteringConstants.z = f;
        this.scatteringConstants.w = f * 4 * FastMath.PI;        
        updateMaterials();
    }
        
    public float getMieConstant() {
        return scatteringConstants.z;
    }
    
    public final void setMiePhaseAsymmetryFactor( float f ) {
        if( this.mpaFactor == f ) {
            return;
        }
        this.mpaFactor = f;
        updateMaterials();
    }
    
    public float getMiePhaseAssymmetryFactor() {
        return mpaFactor;
    }
 
    public void setLightDirection( Vector3f dir ) {
        lightPosition.set(-dir.x, -dir.y, -dir.z);
    }
    
    public Vector3f getLightDirection() {
        return lightPosition.negate();
    }
    
    public void setLightIntensity( float f ) {
        if( this.lightIntensity == f ) {
            return;
        }
        this.lightIntensity = f;
        updateMaterials();
    }
    
    public float getLightIntensity() {
        return lightIntensity;
    }
    
    public void setSkyExposure( float f ) {
        if( this.skyExposure == f ) {
            return;
        }
        this.skyExposure = f;
        updateMaterials();        
    }
    
    public float getSkyExposure() {
        return skyExposure;
    }

    public void setGroundExposure( float f ) {
        if( this.groundExposure == f ) {
            return;
        }
        this.groundExposure = f;
        updateMaterials();        
    }
    
    public float getGroundExposure() {
        return groundExposure;
    }
    
    public final void setWavelengths( float r, float g, float b ) {
        wavelengths.set(r, g, b);
        wavelengthsPow4.x = FastMath.pow(wavelengths.x, 4);
        wavelengthsPow4.y = FastMath.pow(wavelengths.y, 4);
        wavelengthsPow4.z = FastMath.pow(wavelengths.z, 4);
        invPow4Wavelengths.x = 1 / wavelengthsPow4.x;
        invPow4Wavelengths.y = 1 / wavelengthsPow4.y;
        invPow4Wavelengths.z = 1 / wavelengthsPow4.z;               
    }
    
    public void setRedWavelength( float f ) {
        if( this.wavelengths.x == f ) {
            return;
        }
        setWavelengths(f, wavelengths.y, wavelengths.z); 
    }
    
    public float getRedWavelength() {
        return wavelengths.x;
    }
    
    public void setGreenWavelength( float f ) {
        if( this.wavelengths.y == f ) {
            return;
        }
        setWavelengths(wavelengths.x, f, wavelengths.z); 
    }
    
    public float getGreenWavelength() {
        return wavelengths.y;
    }
    
    public void setBlueWavelength( float f ) {
        if( this.wavelengths.z == f ) {
            return;
        }
        setWavelengths(wavelengths.x, wavelengths.y, f); 
    }
    
    public float getBlueWavelength() {
        return wavelengths.z;
    }
    
}


