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
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.TabbedPanel;
import com.simsilica.lemur.component.BorderLayout;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.InputMapper;
import com.simsilica.lemur.style.ElementId;


/**
 *
 *
 *  @author    Paul Speed
 */
public class SettingsPanelState extends BaseAppState {

    private Container mainWindow;
    private Container mainContents; 
 
    private TabbedPanel tabs;
 
    public SettingsPanelState() {
    }

    public TabbedPanel getParameterTabs() {
        return tabs;
    }

    public void toggleHud() {
        setEnabled( !isEnabled() );
    }

    @Override
    protected void initialize( Application app ) {
    
        // Always register for our hot key as long as
        // we are attached.
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addDelegate( MainFunctions.F_HUD, this, "toggleHud" );

        mainWindow = new Container(new BorderLayout(), new ElementId("window"), "glass");
        mainWindow.addChild(new Label("Settings", mainWindow.getElementId().child("title.label"), "glass"),
                            BorderLayout.Position.North); 
        mainWindow.setLocalTranslation(10, app.getCamera().getHeight() - 10, 0);        
 
        mainContents = mainWindow.addChild(new Container(mainWindow.getElementId().child("contents.container"), "glass"),
                                                        BorderLayout.Position.Center); 
        
        tabs = new TabbedPanel("glass");
        mainContents.addChild(tabs);
    }

    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_HUD, this, "toggleHud" ); 
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(mainWindow);
    }
    
    @Override
    protected void disable() {
        mainWindow.removeFromParent();
    }
}
