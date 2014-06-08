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
import com.jme3.bounding.BoundingSphere;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.math.Vector4f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial.CullHint;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.geom.MBox;
import com.simsilica.lemur.props.PropertyPanel;
import java.util.Random;


/**
 *
 *
 *  @author    Paul Speed
 */
public class SkyState extends BaseAppState {

    private ColorRGBA skyColor;
    private ColorRGBA sunColor;
    private Geometry sky;
    private Geometry sun;
    private boolean showSky = true;
    
    private boolean flat = false;
 
private Node ground; 
    
    private Material flatMaterial;
    private Material atmosphericMaterial;
private Material groundMaterial;    
private Material flatGroundMaterial;    
    
    private AtmosphericMaterialParameters atmosphericParms;
    
    private VersionedReference<Vector3f> lightDir;

    private Vector4f rayleighAndMie;
    private float lightIntensity;
    private float mpaFactor;
    private float mieScale;
    private float rayleighScale;
    private float kFlattening;
    private float exposure;
    private PropertyPanel settings;

    public SkyState() {
        this.sunColor = new ColorRGBA(1, 1, 0.9f, 1);
        this.skyColor = new ColorRGBA(0.5f, 0.5f, 1f, 1);
    }

    public void setShowSky( boolean b ) {
        this.showSky = b;
        resetShowSky();
    }
 
    public void setFlatShaded( boolean b ) {
        if( flat == b ) {
            return;
        }
        this.flat = b;
        resetMaterials();
    }
    
    public boolean isFlatShaded() {
        return flat;
    }
    
    protected void resetMaterials() {
        if( flat ) {
            sky.setMaterial(flatMaterial);
            ground.setMaterial(flatGroundMaterial);
        } else {
            sky.setMaterial(atmosphericMaterial);
            ground.setMaterial(groundMaterial);
        }
    }
    
    protected void resetShowSky() {
        if( sky == null ) {
            return;
        }
        if( showSky ) {
            sky.setCullHint(CullHint.Inherit);
        } else {
            sky.setCullHint(CullHint.Always);
        }
    }

    public boolean getShowSky() {
        return sky.getCullHint() == CullHint.Inherit; 
    }

    public void setMieConstant( float f ) {
        atmosphericParms.setMieConstant(f / 100);
        atmosphericParms.applyGroundParameters(groundMaterial);    
        rayleighAndMie.z = f / 100;
    }
    
    public float getMieConstant() {
        return rayleighAndMie.z * 100;
    }
    
    public void setRayleighConstant( float f ) {
        atmosphericParms.setRayleighConstant(f / 100);
        atmosphericParms.applyGroundParameters(groundMaterial);    
        rayleighAndMie.x = f / 100;
    }
    
    public float getRayleighConstant() {
        return rayleighAndMie.x * 100;
    }
    
    public void setLightIntensity( float f ) {
        this.lightIntensity = f;
        atmosphericParms.setLightIntensity(f);
        atmosphericParms.applyGroundParameters(groundMaterial);    
    }
    
    public float getLightIntensity() {
        return lightIntensity;
    }
    
    public void setRedWavelength( float f ) {
        atmosphericParms.setRedWavelength(f);
        atmosphericParms.applyGroundParameters(groundMaterial);            
    }
    
    public float getRedWavelength() {
        return atmosphericParms.getRedWavelength();
    }

    public void setGreenWavelength( float f ) {
        atmosphericParms.setGreenWavelength(f);
        atmosphericParms.applyGroundParameters(groundMaterial);            
    }

    public float getGreenWavelength() {
        return atmosphericParms.getGreenWavelength();
    }
    
    public void setBlueWavelength( float f ) {
        atmosphericParms.setBlueWavelength(f);
        atmosphericParms.applyGroundParameters(groundMaterial);            
    }

    public float getBlueWavelength() {
        return atmosphericParms.getBlueWavelength();
    }

    public void setMiePhaseAsymmetryFactor( float f ) {
        atmosphericParms.setMiePhaseAsymmetryFactor(f);
        atmosphericParms.applyGroundParameters(groundMaterial);    
        this.mpaFactor = f;
    }
    
    public float getMiePhaseAsymmetryFactor() {
        return mpaFactor;
    } 

    public void setRayleighScaleDepth( float f ) {
        atmosphericParms.setAverageDensityScale(f);
        atmosphericParms.applyGroundParameters(groundMaterial);    
        this.rayleighScale = f;
    }

    public float getRayleighScaleDepth() {
        return rayleighScale;
    }

    public void setFlattening( float f ) {
        this.kFlattening = f;
        atmosphericMaterial.setFloat("Flattening", f); 
    }
    
    public float getFlattening() {
        return kFlattening;
    }

    public void setExposure( float f ) {
        atmosphericParms.setSkyExposure(f);
        atmosphericParms.setGroundExposure(f);
        atmosphericParms.applyGroundParameters(groundMaterial);    
        this.exposure = f;
    }
    
    public float getExposure() {
        return exposure;
    }

    @Override
    protected void initialize( Application app ) {
    
        lightDir = getState(LightingState.class).getLightDirRef();

        float earthRadius = 6378100;
 
        float domeInnerRadius = 2000;
        float domeOuterRadius = 2000 * 1.025f;
 
        atmosphericParms = new AtmosphericMaterialParameters();
        atmosphericParms.setSkyDomeRadius(domeOuterRadius);
        //atmosphericParms.setPlanetRadius(40000); 
        atmosphericParms.setPlanetRadius(earthRadius * 0.01f); 
        atmosphericParms.setLightDirection(lightDir.get());
        
 
        // Add a sun sphere
        Sphere orb = new Sphere(6, 12, 1); //50);
        sun = new Geometry("Sun", orb);
        Material mat = GuiGlobals.getInstance().createMaterial(sunColor, false).getMaterial(); 
        sun.setMaterial(mat);
        sun.move(lightDir.get().mult(-900));

        float inner = 10;
        float outer = 10.25f;
 
        // PlanetScale is based on the difference between what we render
        // as a sky dome and the passed inner/outer values for the shader.
        float planetScale = 1f / 200;

System.out.println( "planetScale:" + planetScale + "  dome inner:" + (inner/planetScale) + "  outer:" + (outer/planetScale) );

        Camera cam = app.getCamera();
        cam.setLocation(new Vector3f(0, 1, 0));


        // Make a temporary ground 
        ground = new Node("ground");
        MBox box = new MBox(outer/planetScale, 0, outer/planetScale, 50, 0, 50);
        Geometry groundPlane = new Geometry("ground", box);
        ground.attachChild(groundPlane);        
        mat = GuiGlobals.getInstance().createMaterial(new ColorRGBA(0.25f, 0.25f, 0, 1), false).getMaterial();
        flatGroundMaterial = mat;
        //mat.getAdditionalRenderState().setWireframe(true); 


        Random rand = new Random(0);
        int count = 100;
        for( int i = 0; i < count; i++ ) {
            float angle = rand.nextFloat() * FastMath.TWO_PI;
            float distance = 300 + rand.nextFloat() * 100;
            float size = 3 + rand.nextFloat() * 10;
        
            float x = FastMath.cos(angle) * distance;
            float y = FastMath.sin(angle) * distance;
            
            Box b = new Box(new Vector3f(x, size, y), size, size, size);
            Geometry g = new Geometry("box", b);
            ground.attachChild(g);
        }
                
        ground.setMaterial(mat);
        
        //TruncatedDome skyDome = new TruncatedDome(inner/planetScale, outer/planetScale, 100, 50, true);
        TruncatedDome skyDome = new TruncatedDome(domeInnerRadius, domeOuterRadius, 100, 50, true);
        sky = new Geometry("Sky", skyDome); 
        sky.setModelBound(new BoundingSphere(Float.POSITIVE_INFINITY, Vector3f.ZERO));        
        flatMaterial = GuiGlobals.getInstance().createMaterial(skyColor, false).getMaterial();
        //flatMaterial.getAdditionalRenderState().setWireframe(true);
        sky.setMaterial(flatMaterial);
        atmosphericMaterial = atmosphericParms.getSkyMaterial(app.getAssetManager());
        
        sky.setMaterial(atmosphericMaterial); 
        sky.setQueueBucket(Bucket.Sky);
        sky.setCullHint(CullHint.Never);
 
        Material m = atmosphericMaterial;
        rayleighAndMie = new Vector4f();
        rayleighAndMie.x = 0.0025f; // Rayleigh scattering constant 
        //rayleighAndMie.x = 0.0033f; // Rayleigh scattering constant 
        //rayleighAndMie.x = 0.0030f; // Rayleigh scattering constant 
        rayleighAndMie.y = rayleighAndMie.x * 4 * FastMath.PI; 
        rayleighAndMie.z = 0.001f; // Mie scattering constant 
        //rayleighAndMie.z = 0.0034f; // Mie scattering constant 
        rayleighAndMie.w = rayleighAndMie.z * 4 * FastMath.PI;


        lightIntensity = 20;         
        mpaFactor = -0.990f; 
        exposure = 1.0f;

        Vector3f waveLength = new Vector3f();
        waveLength.x = 0.650f;
        waveLength.y = 0.570f;
        waveLength.z = 0.475f;
 
System.out.println( "waveLength:" + waveLength ); 
        Vector3f waveLengthPow4 = new Vector3f();
        waveLengthPow4.x = FastMath.pow(waveLength.x, 4);
        waveLengthPow4.y = FastMath.pow(waveLength.y, 4);
        waveLengthPow4.z = FastMath.pow(waveLength.z, 4);
        
        Vector3f invWaveLength = new Vector3f();
        invWaveLength.x = 1 / waveLengthPow4.x;
        invWaveLength.y = 1 / waveLengthPow4.y;
        invWaveLength.z = 1 / waveLengthPow4.z;
 
        rayleighScale = 0.25f;
        mieScale = 0.1f;
System.out.println( "Sky Material:" + m.getParams() );                
        
        groundMaterial = new Material(app.getAssetManager(), "MatDefs/GroundAtmospherics.j3md");
        ground.setMaterial(groundMaterial); 
        m = groundMaterial;
        
        atmosphericParms.applyGroundParameters(groundMaterial);
        
        resetShowSky();
        
        settings = new PropertyPanel("glass");
        settings.addFloatProperty("Intensity", this, "lightIntensity", 0, 100, 1);
        settings.addFloatProperty("Exposure", this, "exposure", 0, 10, 0.1f);
        settings.addFloatProperty("Rayleigh Constant(x100)", this, "rayleighConstant", 0, 1, 0.01f);
        settings.addFloatProperty("Rayleigh Scale", this, "rayleighScaleDepth", 0, 1, 0.001f);
        settings.addFloatProperty("Mie Constant(x100)", this, "mieConstant", 0, 1, 0.01f);
        settings.addFloatProperty("MPA Factor", this, "miePhaseAsymmetryFactor", -1.5f, 0, 0.001f);
        settings.addFloatProperty("Flattening", this, "flattening", 0, 1, 0.01f);
        settings.addFloatProperty("Red Wavelength (nm)", this, "redWavelength", 0, 1, 0.001f);
        settings.addFloatProperty("Green Wavelength (nm)", this, "greenWavelength", 0, 1, 0.001f);
        settings.addFloatProperty("Blue Wavelength (nm)", this, "blueWavelength", 0, 1, 0.001f);

        settings.addFloatProperty("Time", getState(LightingState.class), "timeOfDay", -0.1f, 1.1f, 0.01f);
        settings.setLocalTranslation(0, cam.getHeight(), 0);
        
        settings.addBooleanProperty("Flat Shaded", this, "flatShaded");
        
        getState(SettingsPanelState.class).getParameterTabs().addTab("Scattering", settings);        
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
        Node rootNode = ((SimpleApplication)getApplication()).getRootNode();
        rootNode.attachChild(sun);
        rootNode.attachChild(sky);
rootNode.attachChild(ground);
        
        //Node guiNode = ((SimpleApplication)getApplication()).getGuiNode();
        //guiNode.attachChild(settings);
    }

    @Override
    public void update( float tpf ) {
        if( lightDir.update() ) {
            sun.setLocalTranslation(lightDir.get().mult(-900));
            atmosphericParms.setLightDirection(lightDir.get());
            atmosphericParms.applyGroundParameters(groundMaterial);
        }
        
        if( flat ) {
            sky.setLocalTranslation(getApplication().getCamera().getLocation());
        }
    }

    @Override
    protected void disable() {
        sun.removeFromParent();
        sky.removeFromParent();
ground.removeFromParent();        
        //settings.removeFromParent();
    }
}
