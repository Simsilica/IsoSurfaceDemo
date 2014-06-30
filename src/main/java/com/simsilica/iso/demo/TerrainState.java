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

import com.google.common.base.Supplier;
import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.SceneGraphVisitorAdapter;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.simsilica.builder.Builder;
import com.simsilica.builder.BuilderState;
import com.simsilica.fx.sky.AtmosphericParameters;
import com.simsilica.fx.sky.SkyState;
import com.simsilica.iso.DensityVolume;
import com.simsilica.iso.IsoTerrainZoneFactory;
import com.simsilica.iso.MeshGenerator;
import com.simsilica.iso.fractal.GemsFractalDensityVolume;
import com.simsilica.iso.mc.MarchingCubesMeshGenerator;
import com.simsilica.iso.plot.GrassZone;
import com.simsilica.iso.plot.PlotFrequencyZone;
import com.simsilica.iso.plot.TreeZone;
import com.simsilica.iso.util.BilinearArray;
import com.simsilica.iso.volume.ResamplingVolume;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.props.PropertyPanel;
import com.simsilica.pager.Grid;
import com.simsilica.pager.PagedGrid;
import com.simsilica.pager.ZoneFactory;
import com.simsilica.pager.debug.BBoxZone;
import java.util.HashSet;
import java.util.Set;



/**
 *  Manages the paged grid for the land mass, vegetation, etc..
 *
 *  @author    Paul Speed
 */
public class TerrainState extends BaseAppState {

    public static final int CHUNK_SIZE_XZ = 64;
    public static final int CHUNK_SIZE_Y = 32;
    
    /**
     *  The root level terrain pager that constructs the land geometry
     *  and has all of the other children that generate flora, debris,
     *  etc.
     */
    private PagedGrid pager;
    
    /**
     *  A convenient root node for wholesale removing or adding     
     *  the terrain to the scene.
     */
    private Node land;

    /**
     *  The density volume representing the world terrain.
     */
    private DensityVolume worldVolume;

    /**
     *  Materials that are based on world space can use
     *  this to determine what the _actual_ world space is rather than
     *  the current camera-centric view.
     */
    private Vector3f worldOffset = new Vector3f();

    /**
     *  The root level grid model from which all children will
     *  derive.  (Children must be the same spacing or an even
     *  subspacing, ie: only one parent per child.)
     */
    private Grid rootGrid;
     
    private Material terrainMaterial;
    private Material grassMaterial;

    private Set<Material> treeMaterials = new HashSet<Material>();

    private PropertyPanel settings;
    private boolean useScattering;

    public TerrainState() {
        this.worldVolume = new GemsFractalDensityVolume();
    }

    public PropertyPanel getSettings() {
        return settings;
    }
    
    public void setUseAtmospherics( boolean b ) {
        if( this.useScattering == b ) {
            return;
        }
        this.useScattering = b;
        resetAtmospherics();
    }
 
    public boolean getUseAtmospherics() {
        return useScattering;
    }
    
    protected void resetAtmospherics() {
        terrainMaterial.setBoolean("UseScattering", useScattering);
        for( Material m : treeMaterials ) {
            m.setBoolean("UseScattering", useScattering);
        }        
    }    

    @Override
    protected void initialize( Application app ) {
 
        // Create the root node that we'll attach everything to
        // for convenient add/remove
        land = new Node("Terrain");
 
        // Grab the builder from the builder state
        // The builder will build the pager's zones on a background thread and
        // apply them on the update thread.
        Builder builder = getState(BuilderState.class).getBuilder();
        
        // Setup the grid size information based on
        // the trunk size and a potential xz scaling.
        int cx = CHUNK_SIZE_XZ;
        int cy = CHUNK_SIZE_Y;
        int cz = CHUNK_SIZE_XZ;
 
        // We can use xzScale to scale the land zones out and then
        // super-sample the density field.  In other words, instead
        // of ending up with a 64x64 grid with 1 meter sampling we
        // end up with a 128x128 meter grid with 2 meter sampling.
        // It's a small reduction in quality but a huge win in the
        // number of zones we can display at once.
        final float xzScale = 1;
                
        int xzSize = (int)(cx * xzScale);
        
        // Figure out what visible radius we should use for the grid
        // based on a desired size
        int desiredSize = 192; // roughly
        
        float idealRadius = (float)desiredSize / xzSize;
        int radius = (int)Math.ceil(idealRadius);
        // We always want to show at least the desired size
 
        // We will clamp our land to -32 to 96 meters
        int yStart = -32;
        int yEnd = 96;
        int yLayers = (yEnd - yStart) / cy;       
 
        // Our terrain will eventually be generated such that we want to
        // offset it down by 42 meters.  It's a magic number arrived at
        // visually.
        int yBase = -42;

        // Now we have enough to create our grid model.
        // The first parameter is the grid spacing in x,y,z.  The second one
        // is the grid offset. 
        rootGrid = new Grid(new Vector3f(xzSize, cy, xzSize), new Vector3f(0, yBase, 0));

        // For the moment, we will create just a bounding box zone
        // factory to test that the paging grid is working.           
        //Material boxMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Red, false).getMaterial();
        //boxMaterial.getAdditionalRenderState().setWireframe(true);
        //ZoneFactory rootFactory = new BBoxZone.Factory(boxMaterial);
        
        // Create the factory that will generate the base terrain.  It carves
        // out chunks of the world based on the values we've defined above.
        //------------------------------------------------------------------
        
        // We will need a material
        Material terrainMaterial = getTerrainMaterial();
 
        // A potentially resampled world volume if we are super-sampling
        DensityVolume volume = worldVolume;
        if( xzScale != 1 ) {
            // We're going to stretch the land geometry so we'll also 
            // stretch the sampling.  The terrain will be slightly less
            // interesting because we're skipping samples and stretching,
            // but we'll cover a lot more (ahem) ground for the same
            // amount of work.       
            volume = new ResamplingVolume(new Vector3f(xzScale, 1, xzScale), volume);
        }                                             
 
        // And a mesh generator.
        // This may look a bit strange but the factory nicely takes a Guava Supplier
        // object.  This could have been anything... a singleton, a factory, whatever.
        // In our case, it will act as a sort of per-thread singleton.  We want to be
        // able to flexibly create any size pool but the marching cubes mesh generator
        // keeps some internal non-thread-safe book-keeping.
        Supplier<MeshGenerator> generator = new Supplier<MeshGenerator>() {
                private ThreadLocal<MarchingCubesMeshGenerator> generator = new ThreadLocal() {
                        @Override 
                        protected MarchingCubesMeshGenerator initialValue() {
                            return new MarchingCubesMeshGenerator( CHUNK_SIZE_XZ, 
                                                               CHUNK_SIZE_Y, 
                                                               CHUNK_SIZE_XZ,
                                                               xzScale );
                        }                                                               
                    };
                
                @Override
                public MeshGenerator get() {
                    return generator.get();
                }
            };                
        
        // And finally the factory
        ZoneFactory rootFactory = new IsoTerrainZoneFactory(volume, 
                                                            new Vector3f(cx, cy, cz),
                                                            new Vector3f(0, yBase, 0),
                                                            generator,
                                                            terrainMaterial,
                                                            false);
                
        pager = new PagedGrid(rootFactory, builder, rootGrid, yLayers, radius);        
        land.attachChild(pager.getGridRoot());
        
        boolean grass = true;
        if( grass ) {
            // Create the Grass pager
            //---------------------------
            Material grassMaterial = getGrassMaterial();
 
            // Grass uses the same noise texture that the shader uses to plot
            // borders, etc.
            BilinearArray noise = BilinearArray.fromTexture(app.getAssetManager().loadTexture("Textures/noise-x3-512.png"));        
 
            Grid grassGrid = new Grid(new Vector3f(32, 32, 32), new Vector3f(0, (yBase + 32), 0));  
            ZoneFactory grassFactory = new GrassZone.Factory(grassMaterial, noise);
        
            int grassDistance = 64;
            grassMaterial.setFloat("DistanceFalloff", grassDistance + 16);      
        
            PagedGrid grassPager = new PagedGrid(pager, grassFactory, builder, grassGrid, 2, grassDistance / 32);
            grassPager.setPriorityBias(2);
            land.attachChild(grassPager.getGridRoot());
        } 

        boolean freqPlot = false;
        if( freqPlot ) {
            // Testing some plotting frequencies
            Material plotMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.White, false).getMaterial();
            plotMaterial.getAdditionalRenderState().setWireframe(true);
            plotMaterial.setBoolean("VertexColor", true);
        
            BilinearArray noise = BilinearArray.fromTexture(app.getAssetManager().loadTexture("Textures/noise-x3-512.png"));        
    
            Grid plotGrid = new Grid(new Vector3f(32, 32, 32), new Vector3f(0, (yBase + 32), 0));  
            ZoneFactory plotFactory = new PlotFrequencyZone.Factory(plotMaterial, noise);
            
            int plotDistance = 32; //64;
            PagedGrid plotPager = new PagedGrid(pager, plotFactory, builder, plotGrid, 2, plotDistance / 32);
            plotPager.setPriorityBias(1);
            land.attachChild(plotPager.getGridRoot());
        } 

        boolean trees = true;
        if( trees ) {
            
            Node tree1 = (Node)app.getAssetManager().loadModel("Models/short-tree1-full-LOD.j3o");
            Node tree2 = (Node)app.getAssetManager().loadModel("Models/tall-tree2-full-LOD.j3o");
            Node tree3 = (Node)app.getAssetManager().loadModel("Models/tipped-tree-full-LOD.j3o");
            Node tree4 = (Node)app.getAssetManager().loadModel("Models/short-branching-full-LOD.j3o");
             
            // Collect the tree materials
            tree1.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit( Geometry geom ) {
                            MaterialDef matDef = geom.getMaterial().getMaterialDef();
                            System.out.println( "Geom:" + geom );
                            System.out.println( "Material def:" + matDef.getAssetName() );
                            if( matDef.getMaterialParam("UseScattering") != null ) {                             
                                treeMaterials.add(geom.getMaterial());
                            }
                        }
                    });
            tree2.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit( Geometry geom ) {
                            treeMaterials.add(geom.getMaterial());
                            MaterialDef matDef = geom.getMaterial().getMaterialDef();
                            System.out.println( "Geom:" + geom );
                            System.out.println( "Material def:" + matDef.getAssetName() );
                            if( matDef.getMaterialParam("UseScattering") != null ) {                             
                                treeMaterials.add(geom.getMaterial());
                            }
                        }
                    });
            tree3.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit( Geometry geom ) {
                            treeMaterials.add(geom.getMaterial());
                            MaterialDef matDef = geom.getMaterial().getMaterialDef();
                            geom.getMaterial().setBoolean("UseWind", false);
                            System.out.println( "Geom:" + geom );
                            System.out.println( "Material def:" + matDef.getAssetName() );
                            if( matDef.getMaterialParam("UseScattering") != null ) {                             
                                treeMaterials.add(geom.getMaterial());
                            }
                        }
                    });
            tree4.depthFirstTraversal(new SceneGraphVisitorAdapter() {
                        @Override
                        public void visit( Geometry geom ) {
                            treeMaterials.add(geom.getMaterial());
                            MaterialDef matDef = geom.getMaterial().getMaterialDef();
                            geom.getMaterial().setBoolean("UseWind", false);
                            System.out.println( "Geom:" + geom );
                            System.out.println( "Material def:" + matDef.getAssetName() );
                            if( matDef.getMaterialParam("UseScattering") != null ) {                             
                                treeMaterials.add(geom.getMaterial());
                            }
                        }
                    });
             
        
            Material treeMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.White, false).getMaterial();
            treeMaterial.getAdditionalRenderState().setWireframe(true);
            treeMaterial.setBoolean("VertexColor", true);
        
            BilinearArray noise = BilinearArray.fromTexture(app.getAssetManager().loadTexture("Textures/noise-x3-512.png"));        
 
            int treeGridSpacing = 16;   
            Grid treeGrid = new Grid(new Vector3f(treeGridSpacing, 32, treeGridSpacing), new Vector3f(0, (yBase + 32), 0));  
            ZoneFactory treeFactory = new TreeZone.Factory(treeMaterial, noise, tree4, tree1, tree3, tree2);
            
            int treeDistance = 128;
            PagedGrid treePager = new PagedGrid(pager, treeFactory, builder, treeGrid, 2, treeDistance / treeGridSpacing);
            //treePager.setPriorityBias(1);
            land.attachChild(treePager.getGridRoot());
        }

        boolean debugZone = false;
        if( debugZone ) {
            Material boxMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Red, false).getMaterial();
            boxMaterial.getAdditionalRenderState().setWireframe(true);            
            ZoneFactory debugFactory = new BBoxZone.Factory(boxMaterial);
            int debugSpacing = 16;
            int debugDistance = 128;
            Grid debugGrid = new Grid(new Vector3f(debugSpacing, 32, debugSpacing), new Vector3f(0, (yBase + 32), 0));
            PagedGrid debugPager = new PagedGrid(pager, debugFactory, builder, debugGrid, 2, debugDistance/debugSpacing);
            land.attachChild(debugPager.getGridRoot());
        }

        // A location I happen to know is a good starting point for this particular
        // terrain fractal:
        app.getCamera().setLocation(new Vector3f(-3.0589433f - 1, 19.916946f - 1, -19.72412f - 1));        

        
        // And finally, we need to have our camera movement go through the
        // pager instead of directly to the camera
        getState(MovementState.class).setMovementHandler(
                new PagedGridMovementHandler(pager, app.getCamera()) {
                    @Override 
                    protected void setLandLocation( float x, float z ) {
                        super.setLandLocation(x, z);
                        worldOffset.set(x, 0, z);
                    }
                });
 
        // Setup for atmospherics
        AtmosphericParameters atmosphericParms = getState(SkyState.class).getAtmosphericParameters();
        atmosphericParms.applyGroundParameters(getTerrainMaterial(), true);
 
        // Hook up the tree materials, too
        for( Material m : treeMaterials ) {
            MaterialDef matDef = m.getMaterialDef();
            System.out.println( "Material def:" + matDef.getAssetName() );
            atmosphericParms.applyGroundParameters(m, true);
            
            //m.setColor("Diffuse", ColorRGBA.Blue);
            System.out.println( "Existing ambient:" + m.getParam("Ambient") ); 
            System.out.println( "Existing diffuse:" + m.getParam("Diffuse") ); 
            m.setColor("Ambient", new ColorRGBA(1.1f, 1.1f, 1.1f, 1));
            m.setColor("Diffuse", new ColorRGBA(0.9f, 0.9f, 0.9f, 1));
        }
 
        // Setup a settings panel        
        settings = new PropertyPanel("glass");
        settings.addBooleanProperty("Use Atmospherics", this, "useAtmospherics");
        settings.addFloatProperty("Ground Exposure", atmosphericParms, "groundExposure", 0, 10, 0.1f);
        
        resetAtmospherics();
    }

    @Override
    protected void cleanup( Application app ) {
        pager.release();
    }

    @Override
    protected void enable() {
        ((SimpleApplication)getApplication()).getRootNode().attachChild(land);
    }

    @Override
    protected void disable() {
        land.removeFromParent();
    }
    
    public Material getTerrainMaterial() {
        if( terrainMaterial != null ) {
            return terrainMaterial;
        }
    
        AssetManager assets = getApplication().getAssetManager();
               
        terrainMaterial = new Material(assets, "MatDefs/TrilinearLighting.j3md");
        terrainMaterial.setFloat("Shininess", 0);
 
        terrainMaterial.setColor("Diffuse", ColorRGBA.White);
        terrainMaterial.setColor("Ambient", ColorRGBA.White);
        terrainMaterial.setBoolean("UseMaterialColors", true);

        
        terrainMaterial.setVector3("WorldOffset", worldOffset);
 
        // Setup the trilinear textures for the different axis,
        // X, Y, and Z.  We use the regular diffuse map for the top
        // texture.
        Texture texture;
 
        texture = assets.loadTexture("Textures/grass.jpg");
        texture.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMap", texture);  

        texture = assets.loadTexture("Textures/grass-flat.jpg");
        texture.setWrap(Texture.WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMapLow", texture);  
        
        texture = assets.loadTexture("Textures/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("NormalMap", texture);        
 
 
        
        texture = assets.loadTexture("Textures/brown-dirt2.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMapX", texture);
        
        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assets.loadTexture("Textures/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("NormalMapX", texture);

        texture = assets.loadTexture("Textures/brown-dirt2.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMapZ", texture);
        
        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assets.loadTexture("Textures/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("NormalMapZ", texture);
 
        // Now the default down texture... we use a separate one
        // and DiffuseMap will be used for the top 
        texture = assets.loadTexture("Textures/canvas128.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("DiffuseMapY", texture);
        
        //texture = assets.loadTexture("Textures/test-norm.png");
        texture = assets.loadTexture("Textures/brown-dirt-norm.jpg");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("NormalMapY", texture);
 
        // We will need a noise texture soon, might as well set it
        // now
        texture = assets.loadTexture("Textures/noise-x3-512.png");
        texture.setWrap(WrapMode.Repeat);
        terrainMaterial.setTexture("Noise", texture);        
 
        
        return terrainMaterial;   
    }
    
    public Material getGrassMaterial() {
 
        if( grassMaterial != null ) {
            return grassMaterial;
        }
           
        AssetManager assets = getApplication().getAssetManager();
        
        Texture grassTexture = assets.loadTexture("Textures/grass-blades.png");
        grassMaterial = new Material(assets, "MatDefs/Grass.j3md");
        grassMaterial.setTexture("DiffuseMap", grassTexture);
        grassMaterial.setFloat("AlphaDiscardThreshold", 0.25f);
        grassMaterial.setColor("Diffuse", ColorRGBA.White); //color);
        grassMaterial.setColor("Specular", ColorRGBA.Red); //color);
        grassMaterial.setColor("Ambient", ColorRGBA.White);
        grassMaterial.setFloat("Shininess", 0);
        grassMaterial.setBoolean("UseMaterialColors", true);
        
        Texture texture = assets.loadTexture("Textures/noise-x3-512.png");
        texture.setWrap(WrapMode.Repeat);
        grassMaterial.setTexture("Noise", texture);        

        grassMaterial.setVector3("WorldOffset", worldOffset);
                
        grassMaterial.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
        return grassMaterial;        
    }
}



