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
import com.jme3.asset.AssetManager;
import com.jme3.math.Vector3f;
import com.jme3.post.Filter;
import com.jme3.post.FilterPostProcessor;
import com.jme3.post.filters.BloomFilter;
import com.jme3.post.filters.DepthOfFieldFilter;
import com.jme3.post.filters.FXAAFilter;
import com.jme3.post.filters.LightScatteringFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.renderer.ViewPort;
import com.jme3.shadow.DirectionalLightShadowFilter;
import com.jme3.system.AppSettings;
import com.jme3.texture.Texture2D;
import com.jme3.water.WaterFilter;
import com.simsilica.fx.LightingState;
import com.simsilica.lemur.Checkbox;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.props.PropertyPanel;


/**
 *
 *
 *  @author    Paul Speed
 */
public class PostProcessingState extends BaseAppState {

    private FilterPostProcessor fpp;

    private WaterFilter water;
    private DirectionalLightShadowFilter shadows;
    private SSAOFilter ssao;
    private FXAAFilter fxaa;
    private BloomFilter bloom;
    private LightScatteringFilter lightScattering;
    private DepthOfFieldFilter dof;
    
    private VersionedReference<Vector3f> lightDir;

    @Override
    protected void initialize( Application app ) {
        
        AssetManager assets = app.getAssetManager();
        AppSettings settings = app.getContext().getSettings();
        ViewPort viewport = app.getViewPort();
        
        fpp = new FilterPostProcessor(assets);
        viewport.addProcessor(fpp);
 
        // See if sampling is enabled
        boolean aa = settings.getSamples() != 0;
        if( aa ) {
            fpp.setNumSamples(settings.getSamples());
        }
        
        Container main = new Container("glass");
        getState(SettingsPanelState.class).getParameterTabs().addTab("Filters", main);               
        PropertyPanel properties;
 
        // Setup shadows
        //-------------------------------------
        shadows = new DirectionalLightShadowFilter(assets, 4096, 4);
        shadows.setShadowIntensity(0.3f);
        shadows.setLight(getState(LightingState.class).getSun());
        shadows.setEnabled(false);
        fpp.addFilter(shadows);
        
        properties = createFilterPanel("Shadows", shadows, main);        
        properties.addFloatProperty("Intensity", shadows, "shadowIntensity", 0.0f, 1, 0.01f);        


        // Then SSAO
        //--------------------------------------
        ssao = new SSAOFilter();
        ssao.setEnabled(false);
        fpp.addFilter(ssao);

        properties = createFilterPanel("SSAO", ssao, main);                 
        properties.addFloatProperty("Intensity", ssao, "intensity", 0.01f, 2, 0.01f);
        properties.addFloatProperty("Bias", ssao, "bias", 0.01f, 1, 0.01f);
        properties.addFloatProperty("Sample Radius", ssao, "sampleRadius", 1f, 10, 0.1f);
        properties.addFloatProperty("Scale", ssao, "scale", 0.01f, 1, 0.01f);

        // Setup Water
        //--------------------------------------
        water = new WaterFilter(((SimpleApplication)app).getRootNode(), 
                                getState(LightingState.class).getSun().getDirection());
        water.setFoamIntensity(0.1f);
        water.setWaterHeight(-11);
        water.setFoamTexture( (Texture2D)assets.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
        fpp.addFilter(water);

        // For water we will create a container manually so that we can have
        // multiple columns.
        TabbedPanel waterTabs = new TabbedPanel("glass");
        RollupPanel rollup = main.addChild(new RollupPanel("Water", waterTabs, "glass"));

        properties = waterTabs.addTab("Visualization", new PropertyPanel("glass"));
        properties.setEnabledProperty(water, "enabled");        
        rollup.getTitleContainer().addChild(new Checkbox("", properties.getEnabledModel(), "glass"));
                
        properties.addFloatProperty("Height", water, "waterHeight", -20, 20, 0.1f);
        properties.addFloatProperty("speed", water, "speed", 0.0f, 5, 0.1f);
        properties.addFloatProperty("Sun Scale", water, "sunScale", 0.0f, 10, 0.01f);
        
        properties = waterTabs.addTab("Waves", new PropertyPanel("glass"));
        properties.addBooleanProperty("Use Ripples", water, "useRipples");
        properties.addBooleanProperty("Use Specular", water, "useSpecular");
        properties.addFloatProperty("Shininess", water, "shininess", 0.0f, 1, 0.01f);
        properties.addFloatProperty("Wave Scale", water, "waveScale", 0.001f, 1, 0.001f);
        properties.addFloatProperty("Max Amplitude", water, "maxAmplitude", 0.0f, 2, 0.01f);
        properties.addFloatProperty("Normal Scale", water, "normalScale", 0.0f, 10, 0.01f);                                         
 
        properties = waterTabs.addTab("Foam", new PropertyPanel("glass"));
        properties.addBooleanProperty("Use Foam", water, "useFoam");
        properties.addBooleanProperty("Use HQ Shoreline", water, "useHQShoreline");
        properties.addFloatProperty("Foam Hardness", water, "foamHardness", 0.0f, 10, 0.01f);                                         
        properties.addFloatProperty("Foam Intensity", water, "foamIntensity", 0.0f, 1, 0.01f);                                         
        properties.addFloatProperty("Shore Hardness", water, "shoreHardness", 0.0f, 1, 0.01f);
 
        properties = waterTabs.addTab("Refraction", new PropertyPanel("glass"));
        properties.addBooleanProperty("Use Refraction", water, "useRefraction");
        properties.addFloatProperty("Transparency", water, "waterTransparency", 0, 2, 0.001f);
        properties.addFloatProperty("Reflection Displace", water, "reflectionDisplace", 0.0f, 100, 0.1f);                                         
        properties.addFloatProperty("Refraction Constant", water, "refractionConstant", 0.0f, 1, 0.01f);                                         
        properties.addFloatProperty("Refraction Strength", water, "refractionStrength", 0.0f, 1, 0.01f);
        
        properties = waterTabs.addTab("Under", new PropertyPanel("glass"));
        properties.addBooleanProperty("Use Caustics", water, "useCaustics");
        properties.addFloatProperty("Caustics Intensity", water, "causticsIntensity", 0.0f, 1, 0.01f);                                         
        properties.addFloatProperty("Fog Distance", water, "underWaterFogDistance", 0.0f, 200, 0.1f);
 
 
        main = new Container("glass");
        getState(SettingsPanelState.class).getParameterTabs().addTab("Post", main);               
 
        // Setup light scattering
        //--------------------------------------
        lightScattering = new LightScatteringFilter();
        lightScattering.setLightDensity(1);
        lightScattering.setBlurWidth(1.1f);
        lightScattering.setEnabled(false);
        fpp.addFilter(lightScattering);
        
        lightDir = getState(LightingState.class).getLightDirRef();

        resetLightPosition();             
 
        properties = createFilterPanel("Light Scattering", lightScattering, main);        
        properties.addFloatProperty("Light Density", lightScattering, "lightDensity", 0.5f, 2.0f, 0.1f);
        properties.addFloatProperty("Blur Start", lightScattering, "blurStart", 0.0f, 2.0f, 0.1f);
        properties.addFloatProperty("Blur Width", lightScattering, "blurWidth", 0.1f, 2.0f, 0.1f);
 

        // Setup Bloom
        //--------------------------------------
        bloom = new BloomFilter();
        bloom.setEnabled(false);
        bloom.setExposurePower(55);
        bloom.setBloomIntensity(1.0f);
        fpp.addFilter(bloom);
            
        properties = createFilterPanel("Bloom", bloom, main);        
        properties.addFloatProperty("Bloom Intensity", bloom, "bloomIntensity", 0, 10, 0.1f);
        properties.addFloatProperty("Blur Scale", bloom, "blurScale", 0, 10, 0.1f);
        properties.addFloatProperty("Exposure Cut-off", bloom, "exposureCutOff", 0, 1, 0.01f);
        properties.addFloatProperty("Exposure Power", bloom, "exposurePower", 1f, 100, 0.1f); 


        // Setup FXAA only if regular AA is off
        //--------------------------------------
        if( !aa ) {
            fxaa = new FXAAFilter();
            fxaa.setEnabled(false);
            fpp.addFilter(fxaa);
    
            properties = createFilterPanel("FXAA", fxaa, main);
            properties.addFloatProperty("Reduce Mul", fxaa, "reduceMul", 0, 1, 0.01f);           
            properties.addFloatProperty("Span Max", fxaa, "spanMax", 0, 10, 0.01f);           
            properties.addFloatProperty("Sub Pixel Shift", fxaa, "subPixelShift", 0, 10, 0.01f);           
            properties.addFloatProperty("VX Offset", fxaa, "vxOffset", 0, 10, 0.01f);           
        }
        
        // And finally DoF                      
        //--------------------------------------
        dof = new DepthOfFieldFilter();
        dof.setFocusDistance(0);
        dof.setFocusRange(384);
        dof.setEnabled(false);            
        fpp.addFilter(dof);

        properties = createFilterPanel("Depth of Field", dof, main);        
        properties.addFloatProperty("Blur Scale", dof, "blurScale", 0.1f, 10f, 0.1f);
        properties.addFloatProperty("Focus Distance", dof, "focusDistance", 0, 100, 1);
        properties.addFloatProperty("Focus Range", dof, "focusRange", 0, 600, 1);            
        
        
    }

    protected void resetLightPosition() {
        lightScattering.setLightPosition(lightDir.get().mult(-300));       
    }

    protected PropertyPanel createFilterPanel( String name, Filter filter, Container container ) {
        PropertyPanel properties = new PropertyPanel("glass");
        RollupPanel rollup = container.addChild(new RollupPanel(name, properties, "glass"));
        properties.setEnabledProperty(filter, "enabled");
        rollup.getTitleContainer().addChild(new Checkbox("", properties.getEnabledModel(), "glass"));
        return properties;
    }


    @Override
    protected void cleanup( Application app ) {
    }
 
    @Override
    public void update( float tpf ) {
        if( lightDir.update() ) {
            resetLightPosition();
        }
    }
 
    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }
}


