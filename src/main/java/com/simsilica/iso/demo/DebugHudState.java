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
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.util.MemoryUtils;
import com.simsilica.lemur.Container;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.HAlignment;
import com.simsilica.lemur.Label;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.InputMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 *
 *  @author    Paul Speed
 */
public class DebugHudState extends BaseAppState {

    static Logger log = LoggerFactory.getLogger(DebugHudState.class);
    
    private VersionedReference<Vector3f> worldLoc;    
    private Runtime runtime = Runtime.getRuntime();
    
    private Label location;
    private Label memory;
    private Label directMem;
 
    private long lastUsedMem;
    private long lastMeg100;
    private long lastDirectMem;
    private long lastDirectMeg100;
    private long nextUpdate = System.currentTimeMillis() + 16; // 60 FPS max
    private long nextMemTime = System.currentTimeMillis() + 1000; 
    
    private long frameCounter;
    private long lastFrameCheck;
    private double lastFps;

    private Container debugHud;
    
    public DebugHudState() {
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
        
        worldLoc = getState( MovementState.class ).getWorldPosition().createReference();
 
        debugHud = new Container();
        
        location = debugHud.addChild(new Label( "000.00 000.00 00.00" ));
        location.setTextHAlignment( HAlignment.Right );
        resetLocation();
        
        memory = debugHud.addChild(new Label( "Mem: 0.0 meg (0.0 %)" ));
        memory.setTextHAlignment( HAlignment.Right );
        
        directMem = debugHud.addChild(new Label( "DMem: 0.0 meg / 0" ));
        directMem.setTextHAlignment( HAlignment.Right );
    }

    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeDelegate( MainFunctions.F_HUD, this, "toggleHud" ); 
    }

    protected void resetLocation() {
        Vector3f v = worldLoc.get();
        String loc = String.format( "%.2f, %.2f, %.2f", v.x, v.y, v.z );
        location.setText(loc);
    }

    @Override
    public void update( float tpf ) {
    
        frameCounter++;    
        long time = System.currentTimeMillis();    
        if( time < nextUpdate )
            return;
        nextUpdate = time + 16; // 60 FPS max
 
        if( worldLoc.update() ) {
            resetLocation();
        }
 
        /*if( time > lastFrameCheck + 1000 )
            {
            long delta = time - lastFrameCheck;
            lastFrameCheck = time;
            
            double fps = frameCounter / (delta / 1000.0);
            frameCounter = 0;
            if( fps != lastFps )
                {
                lastFps = fps;
                String s = String.format( "FPS: %.2f", fps );
                fpsText.setText(s);
                }
            }*/                        
 

        // Refresh memory and other things less often----------------------------
        //-----------------------------------------------------------------------
        if( time < nextMemTime )
            return;
        nextMemTime = time + 1000;
        
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        if( lastUsedMem != usedMemory ) {
            lastUsedMem = usedMemory;

            long maxMemory = runtime.maxMemory();
            long meg100 = (usedMemory * 100) / (1024 * 1024);
            if( lastMeg100 != meg100 ) {
                lastMeg100 = meg100;
                double meg = meg100 / 100.0;
                double percent = (usedMemory * 100.0 / maxMemory);
                String mem = String.format( "Mem: %.2f meg  (%.1f %%)", meg, percent );
                memory.setText( mem );
            }
        }                  
 
        long directUsage = MemoryUtils.getDirectMemoryUsage();
        if( directUsage != lastDirectMem ) {
            lastDirectMem = directUsage;
            
            long meg100 = (directUsage * 100) / (1024 * 1024);
            if( lastDirectMeg100 != meg100 ) {
                long directCount = MemoryUtils.getDirectMemoryCount();
                double meg = meg100 / 100.0;
                String mem = String.format( "DMem: %.2f meg / %d", meg, directCount );
                directMem.setText( mem );
            }
        }
 
        Camera cam = getApplication().getCamera();           
        Vector3f pref = debugHud.getPreferredSize();
        debugHud.setLocalTranslation(cam.getWidth() - pref.x - 10, cam.getHeight() - 10, 0);            
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getGuiNode().attachChild(debugHud);
    }
    
    @Override
    protected void disable() {
        debugHud.removeFromParent();
    }
}
