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
import com.jme3.material.Material;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.RollupPanel;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.props.PropertyPanel;


/**
 *  A state that adds material parameters to the settings
 *  panel.
 *
 *  @author    Paul Speed
 */
public class MaterialSettingsState extends BaseAppState {

    private boolean useWind = true;
    private boolean useGrassDarkening = true;
    private boolean useGrassAlphaDiscard = true;
    private boolean useTaper = true;
    private float grassAlphaDiscard = 0.25f;

    public MaterialSettingsState() {    
    }
    
    public void setUseWind( boolean b ) {
        this.useWind = b;
        resetGrass();
    }
    
    public boolean getUseWind() {
        return useWind;
    }
    
    public void setUseGrassDarkening( boolean b ) {
        this.useGrassDarkening = b;
        resetGrass();
    }
    
    public boolean getUseGrassDarkening() {
        return useGrassDarkening;
    }
    
    public void setUseGrassAlphaDiscard( boolean b ) {
        this.useGrassAlphaDiscard = b;
        resetGrass();
    }
    
    public boolean getUseGrassAlphaDiscard() {
        return useGrassAlphaDiscard;
    }

    public void setGrassAlphaDiscardThreshold( float f ) {
        this.grassAlphaDiscard = f;
        resetGrass();
    }
    
    public float getGrassAlphaDiscardThreshold() {
        return grassAlphaDiscard;
    }

    public void setUseGrassTaper( boolean b ) {
        this.useTaper = b;
        resetGrass();
    }
    
    public boolean getUseGrassTaper() {
        return useTaper;
    }

    protected void resetGrass() {
        TerrainState terrain = getState(TerrainState.class);
        if( terrain == null ) {
            return;
        }
        Material m = terrain.getGrassMaterial();
        m.setFloat("AlphaDiscardThreshold", grassAlphaDiscard);
        m.setBoolean("UseWind", useWind);
        m.setBoolean("UseDarkening", useGrassDarkening);
        m.setBoolean("UseTaper", useTaper);
        m.setBoolean("UseDiscard", useGrassAlphaDiscard);
    }

    @Override
    protected void initialize( Application app ) {
    
        Container main = new Container("glass");
        
        PropertyPanel properties = new PropertyPanel("glass");
        main.addChild(new RollupPanel("Grass", properties, "glass"));
        
        properties.addBooleanProperty("Wind", this, "useWind");
        properties.addBooleanProperty("Darkening", this, "useGrassDarkening");
        properties.addBooleanProperty("Y Taper", this, "useGrassTaper");
        properties.addBooleanProperty("Alpha Discard", this, "useGrassAlphaDiscard");
        properties.addFloatProperty("Alpha Threshold", this, "grassAlphaDiscardThreshold", 0.0f, 1.0f, 0.01f);
         
        getState(SettingsPanelState.class).getParameterTabs().addTab("Materials", main);
    
    }

    @Override
    protected void cleanup( Application app ) {
    }

    @Override
    protected void enable() {
    }

    @Override
    protected void disable() {
    }
}
