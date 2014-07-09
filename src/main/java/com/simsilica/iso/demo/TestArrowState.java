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
import com.jme3.input.KeyInput;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.debug.Arrow;
import com.jme3.scene.shape.Sphere;
import com.jme3.util.SafeArrayList;
import com.simsilica.iso.DensityVolume;
import com.simsilica.iso.collision.Collider;
import com.simsilica.iso.collision.Contact;
import com.simsilica.iso.collision.SimpleVolumeCollider;
import com.simsilica.iso.volume.ArrayDensityVolume;
import com.simsilica.iso.volume.CachingDensityVolume;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.GuiMaterial;
import com.simsilica.lemur.event.BaseAppState;
import com.simsilica.lemur.input.FunctionId;
import com.simsilica.lemur.input.InputMapper;


/**
 *
 *
 *  @author    Paul Speed
 */
public class TestArrowState extends BaseAppState {

    public static final String GROUP = "ArrowTest";
    public static final FunctionId F_SHOOT = new FunctionId(GROUP, "Shoot");
    public static final FunctionId F_SHOOT2 = new FunctionId(GROUP, "Shoot2");
    public static final FunctionId F_SHOOT3 = new FunctionId(GROUP, "Shoot3");
    public static final FunctionId F_SHOOT4 = new FunctionId(GROUP, "Shoot4");
    public static final FunctionId F_SHOOT5 = new FunctionId(GROUP, "Shoot5");
    public static final FunctionId F_SHOOT6 = new FunctionId(GROUP, "Shoot6");
    public static final FunctionId F_SHOOT7 = new FunctionId(GROUP, "Shoot7");

    private Node root;
    private InputMapper inputMapper;
    
    private DensityVolume worldVolume;
    private Vector3f worldOffset;
 
    private DensityVolume cachedVolume;

    private Collider worldCollider;
                    
    private SafeArrayList<ArrowHolder> arrows = new SafeArrayList<ArrowHolder>(ArrowHolder.class);
    private SafeArrayList<BallHolder> balls = new SafeArrayList<BallHolder>(BallHolder.class);
    private SafeArrayList<BallHolder2> balls2 = new SafeArrayList<BallHolder2>(BallHolder2.class);
 
    private SafeArrayList<Projectile> projectiles = new SafeArrayList<Projectile>(Projectile.class);
    
    public TestArrowState() {        
    }

    public void shoot() {
        System.out.println( "BANG!" );
        Vector3f facing = getState(MovementState.class).getRotation().mult(Vector3f.UNIT_Z);
        Camera cam = getApplication().getCamera();
        ArrowHolder arrow = new ArrowHolder(worldOffset.add(0, cam.getLocation().y, 0), facing);
        root.attachChild(arrow.geom);
        arrows.add(arrow);
    }

    public void shoot2() {
        System.out.println( "BANG 2!" );
        
        int xBase = (int)FastMath.floor(worldOffset.x);
        int zBase = (int)FastMath.floor(worldOffset.z);
        
        Camera cam = getApplication().getCamera();
        float y = cam.getLocation().y;
        for( int x = xBase - 2; x <= xBase + 2; x++ ) {
            for( int z = zBase - 2; z <= zBase + 2; z++ ) {
                ArrowHolder arrow = new ArrowHolder(new Vector3f(x, y, z), Vector3f.UNIT_Y.negate());
                root.attachChild(arrow.geom);
                arrows.add(arrow);   
            }
        }
    }

    public void shoot3() {
        System.out.println( "BANG 3!" );
        
        int xBase = (int)FastMath.floor(worldOffset.x);
        int zBase = (int)FastMath.floor(worldOffset.z);
        
        Camera cam = getApplication().getCamera();
        float y = cam.getLocation().y;
        for( float x = xBase - 2; x <= xBase + 2; x += 0.5f ) {
            for( float z = zBase - 2; z <= zBase + 2; z += 0.5f ) {
                ColorRGBA color = ColorRGBA.Green;
                if( x == (int)x && z == (int)z ) {
                    color = ColorRGBA.Blue;
                }
                ArrowHolder arrow = new ArrowHolder(new Vector3f(x, y, z), Vector3f.UNIT_Y.negate(), color);
                root.attachChild(arrow.geom);
                arrows.add(arrow);   
            }
        }
    
    }

    public void shoot4() {
        System.out.println( "BANG 4!" );
        Camera cam = getApplication().getCamera();
        
        int xBase = (int)FastMath.floor(worldOffset.x);
        int yBase = (int)FastMath.floor(cam.getLocation().y);
        int zBase = (int)FastMath.floor(worldOffset.z);

        float x = worldOffset.x;        
        for( float y = yBase - 2; y <= yBase + 2; y += 0.5f ) {
            for( float z = zBase - 2; z <= zBase + 2; z += 0.5f ) {
                ColorRGBA color = ColorRGBA.Green;
                if( y == (int)y && z == (int)z ) {
                    color = ColorRGBA.Blue;
                }
                ArrowHolder arrow = new ArrowHolder(new Vector3f(x, y, z), Vector3f.UNIT_X.negate(), color);
                root.attachChild(arrow.geom);
                arrows.add(arrow);   
            }
        }
    
    }
    
    public void shoot5() {
        System.out.println( "BOOM!" );
        Vector3f facing = getState(MovementState.class).getRotation().mult(Vector3f.UNIT_Z);
        Camera cam = getApplication().getCamera();
        BallHolder ball = new BallHolder(worldOffset.add(0, cam.getLocation().y, 0), facing, ColorRGBA.Blue);
        root.attachChild(ball.node);
        balls.add(ball);
    }

    public void shoot6() {
        System.out.println( "BOOM 2!" );
        Vector3f facing = getState(MovementState.class).getRotation().mult(Vector3f.UNIT_Z);
        Camera cam = getApplication().getCamera();
        BallHolder2 ball = new BallHolder2(worldOffset.add(0, cam.getLocation().y, 0), facing, ColorRGBA.Blue);
        root.attachChild(ball.node);
        balls2.add(ball);
    }

    public void shoot7() {
        System.out.println( "POW!" );
        Vector3f facing = getState(MovementState.class).getRotation().mult(Vector3f.UNIT_Z);
        Camera cam = getApplication().getCamera();
        Projectile ball = new Projectile(worldOffset.add(0, cam.getLocation().y, 0), facing, ColorRGBA.Blue);
        root.attachChild(ball.node);
        projectiles.add(ball);
    }    

    @Override
    protected void initialize(Application app) {
        Node globalRoot = ((SimpleApplication)app).getRootNode(); 
        this.root = new Node("arrow root");
        globalRoot.attachChild(root);
 
    
        this.inputMapper = GuiGlobals.getInstance().getInputMapper(); 
        inputMapper.addDelegate(F_SHOOT, this, "shoot");
        inputMapper.addDelegate(F_SHOOT2, this, "shoot2");
        inputMapper.addDelegate(F_SHOOT3, this, "shoot3");
        inputMapper.addDelegate(F_SHOOT4, this, "shoot4");
        inputMapper.addDelegate(F_SHOOT5, this, "shoot5");
        inputMapper.addDelegate(F_SHOOT6, this, "shoot6");
        inputMapper.addDelegate(F_SHOOT7, this, "shoot7");
        
        inputMapper.map(F_SHOOT, KeyInput.KEY_F);
        inputMapper.map(F_SHOOT2, KeyInput.KEY_G);
        inputMapper.map(F_SHOOT3, KeyInput.KEY_H);
        inputMapper.map(F_SHOOT4, KeyInput.KEY_J);
        inputMapper.map(F_SHOOT5, KeyInput.KEY_B);
        inputMapper.map(F_SHOOT6, KeyInput.KEY_N);
        inputMapper.map(F_SHOOT7, KeyInput.KEY_P);
        
        worldVolume = getState(TerrainState.class).getWorldVolume();
        worldOffset = getState(TerrainState.class).getWorldOffset();
 
        cachedVolume = new CachingDensityVolume(10, worldVolume, 16, 16);
 
        worldCollider = new SimpleVolumeCollider(cachedVolume);
                        
    }

    @Override
    protected void cleanup(Application app) {
        inputMapper.removeDelegate(F_SHOOT, this, "shoot");
        inputMapper.removeDelegate(F_SHOOT2, this, "shoot2");
        inputMapper.removeDelegate(F_SHOOT3, this, "shoot3");
        inputMapper.removeDelegate(F_SHOOT4, this, "shoot4");
        inputMapper.removeDelegate(F_SHOOT5, this, "shoot5");
        inputMapper.removeDelegate(F_SHOOT6, this, "shoot6");
        inputMapper.removeDelegate(F_SHOOT7, this, "shoot7");
    }

    @Override
    protected void enable() {
        inputMapper.activateGroup(GROUP);        
    }

    @Override
    protected void disable() {
        inputMapper.deactivateGroup(GROUP);        
    }
 
    @Override
    public void update( float tpf ) {
        root.setLocalTranslation(-worldOffset.x, -worldOffset.y, -worldOffset.z);
        for( ArrowHolder arrow : arrows.getArray() ) {
            arrow.update(tpf);
        }
        for( BallHolder ball : balls.getArray() ) {
            ball.update(tpf);
        }
        for( BallHolder2 ball : balls2.getArray() ) {
            ball.update(tpf);
        }
        for( Projectile ball : projectiles.getArray() ) {
            ball.update(tpf);
        }
    }
 
    private class BallHolder {
        Node node;
        Arrow fieldDir;
        Arrow velDir;
        Vector3f lastPos = new Vector3f();
        Vector3f pos;
        Vector3f velocity;
        float traveled = 0;
        GuiMaterial material;
        GuiMaterial fieldDirMaterial;
        long time = 0;
        long iterations = 0;
        
        ArrayDensityVolume local = new ArrayDensityVolume(9, 9, 9);
        
        public BallHolder( Vector3f pos, Vector3f v, ColorRGBA color ) {
        
            this.pos = pos.clone();
            this.velocity = v.clone();
            this.node = new Node("ball");
            node.setLocalTranslation(pos);
        
            Sphere sphere = new Sphere(6, 12, 1);
            Geometry geom = new Geometry("ball", sphere);
            material = GuiGlobals.getInstance().createMaterial(color, true);
            material.getMaterial().getAdditionalRenderState().setWireframe(true);
            geom.setMaterial(material.getMaterial()); 
            //geom.setLocalTranslation(pos);
            node.attachChild(geom);
            
            fieldDir = new Arrow(v.normalize().mult(-2));
            geom = new Geometry("field dir", fieldDir);
            fieldDirMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Green, false);
            geom.setMaterial(fieldDirMaterial.getMaterial());
            node.attachChild(geom);
                        
            velDir = new Arrow(v.mult(2));
            geom = new Geometry("velocity dir", velDir);
            GuiMaterial mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Cyan, false);
            geom.setMaterial(mat.getMaterial());
            node.attachChild(geom);            
        }
        
        public void update( float tpf ) {
            if( traveled > 100 ) {
                balls.remove(this);
                return;
            }
            long start = System.nanoTime(); 
            lastPos.set(pos);            
            float speed = 1;
            pos.x += velocity.x * tpf * speed;
            pos.y += velocity.y * tpf * speed;
            pos.z += velocity.z * tpf * speed;            
            traveled += tpf;
            node.setLocalTranslation(pos);
 
            // Check for collision
            //float density = worldVolume.getDensity(pos.x + 1, pos.y, pos.z + 1);
            
            int xBase = (int)FastMath.floor(pos.x) - 3; 
            int yBase = (int)FastMath.floor(pos.y) - 3; 
            int zBase = (int)FastMath.floor(pos.z) - 3; 
            
            local.extract(worldVolume, xBase, yBase, zBase);
  
            float x = pos.x - xBase;
            float y = pos.y - yBase;
            float z = pos.z - zBase;
            float density = local.getDensity(x + 1, y + 1, z + 1);
 
            // x + 2
            // = 
            Vector3f norm = local.getFieldDirection(x + 1, y + 1, z + 1, null);
 
            fieldDir.setArrowExtent(norm.mult(-2));

            velDir.setArrowExtent(velocity.mult(2));
            
            boolean stop = false;
            
            Vector3f collision = null;
 
            float tipDensity1 = local.getDensity(x + 1 + norm.x * -1, y + 1 + norm.y * -1, z + 1 + norm.z * -1);
            float tipDensity2 = local.getDensity(x + 1 + norm.x * -2, y + 1 + norm.y * -2, z + 1 + norm.z * -2);
            if( tipDensity1 > 0 ) {
                fieldDirMaterial.setColor(ColorRGBA.Red);
                stop = true;
                collision = pos.subtract(norm);
            } else if( tipDensity2 > 0 ) {
                fieldDirMaterial.setColor(ColorRGBA.Pink);
            } else {
                fieldDirMaterial.setColor(ColorRGBA.Green);
            }
 
            if( density > -5 ) {
                material.setColor(ColorRGBA.Red);
            }
 
            /*if( density > 0 ) {
                stop = true;
                collision = pos;
            }*/
 
//System.out.println( "density:" + density + "   dir:" + norm );             
            if( stop ) {
                balls.remove(this);
 
                double avg = (double)time / iterations;
                System.out.println( "Average update time:" + (avg/1000000.0) + " ms" );
 
                float cx = collision.x - xBase;               
                float cy = collision.y - yBase;               
                float cz = collision.z - zBase;
                               
                Vector3f deepNorm = local.getFieldDirection(cx + 1, cy + 1, cz + 1, null);
                Arrow arrow = new Arrow(deepNorm);
                Geometry g = new Geometry("normal", arrow);
                Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(collision);
                root.attachChild(g);
                
                // See if we can find the actual collision point.
                // First let's try using the field direction from
                // our center.
System.out.println( "tip density:" + tipDensity1 + "   center density:" + density );                
                float part = Math.abs(tipDensity1) / Math.abs(density - tipDensity1);
                
                // Distance between tip density and density is 1 in
                // world units
                float penetration = part * 1;
System.out.println( "penetration:" + penetration );                
                Sphere sphere = new Sphere(4, 8, 0.1f);
                g = new Geometry("collision", sphere);
                mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Red, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(pos.subtract(norm.mult(1-penetration)));
                root.attachChild(g);               
                
                // Check in the direction of travel
                Vector3f rel = pos.subtract(lastPos).normalizeLocal();
                float rx = pos.x + rel.x - xBase;               
                float ry = pos.y + rel.y - yBase;               
                float rz = pos.z + rel.z - zBase;
 
                float relDensity = local.getDensity(rx + 1, ry + 1, rz + 1);
System.out.println( "relDensity:" + relDensity + "  center density:" + density );
                part = Math.abs(relDensity) / Math.abs(density - relDensity);
                penetration = part * 1;
System.out.println( "penetration:" + penetration );                
                
                sphere = new Sphere(4, 8, 0.1f);
                g = new Geometry("collision", sphere);
                mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Pink, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(pos.add(rel.mult(1-penetration)));
                root.attachChild(g);               
                 
            }
            
            long end = System.nanoTime();
            time += (end - start);
            iterations++;
        }
    }

    private class BallHolder2 {
        Node node;
        Arrow fieldDir;
        Arrow velDir;
        Vector3f lastPos = new Vector3f();
        Vector3f pos;
        Vector3f velocity;
        float traveled = 0;
        GuiMaterial material;
        GuiMaterial fieldDirMaterial;
        long time = 0;
        long iterations = 0;
        
        public BallHolder2( Vector3f pos, Vector3f v, ColorRGBA color ) {
        
            this.pos = pos.clone();
            this.velocity = v.clone();
            this.node = new Node("ball");
            node.setLocalTranslation(pos);
        
            Sphere sphere = new Sphere(6, 12, 1);
            Geometry geom = new Geometry("ball", sphere);
            material = GuiGlobals.getInstance().createMaterial(color, true);
            material.getMaterial().getAdditionalRenderState().setWireframe(true);
            geom.setMaterial(material.getMaterial()); 
            //geom.setLocalTranslation(pos);
            node.attachChild(geom);
            
            fieldDir = new Arrow(v.normalize().mult(-2));
            geom = new Geometry("field dir", fieldDir);
            fieldDirMaterial = GuiGlobals.getInstance().createMaterial(ColorRGBA.Green, false);
            geom.setMaterial(fieldDirMaterial.getMaterial());
            node.attachChild(geom);
                        
            velDir = new Arrow(v.mult(2));
            geom = new Geometry("velocity dir", velDir);
            GuiMaterial mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Cyan, false);
            geom.setMaterial(mat.getMaterial());
            node.attachChild(geom);            
        }
        
        public void update( float tpf ) {
            if( traveled > 100 ) {
                balls2.remove(this);
                return;
            } 
            long start = System.nanoTime(); 
            lastPos.set(pos);            
            float speed = 1;
            pos.x += velocity.x * tpf * speed;
            pos.y += velocity.y * tpf * speed;
            pos.z += velocity.z * tpf * speed;            
            traveled += tpf;
            node.setLocalTranslation(pos);
 
            // Check for collision
            //float density = worldVolume.getDensity(pos.x + 1, pos.y, pos.z + 1);
            
            int xBase = 0;//(int)FastMath.floor(pos.x) - 3; 
            int yBase = 0;//(int)FastMath.floor(pos.y) - 3; 
            int zBase = 0;//(int)FastMath.floor(pos.z) - 3; 
            
            float x = pos.x - xBase;
            float y = pos.y - yBase;
            float z = pos.z - zBase;
            float density = cachedVolume.getDensity(x + 1, y + 1, z + 1);
 
            // x + 2
            // = 
            Vector3f norm = cachedVolume.getFieldDirection(x + 1, y + 1, z + 1, null);
 
            fieldDir.setArrowExtent(norm.mult(-2));

            velDir.setArrowExtent(velocity.mult(2));
            
            boolean stop = false;
            
            Vector3f collision = null;
 
            float tipDensity1 = cachedVolume.getDensity(x + 1 + norm.x * -1, y + 1 + norm.y * -1, z + 1 + norm.z * -1);
            float tipDensity2 = cachedVolume.getDensity(x + 1 + norm.x * -2, y + 1 + norm.y * -2, z + 1 + norm.z * -2);
            if( tipDensity1 > 0 ) {
                fieldDirMaterial.setColor(ColorRGBA.Red);
                stop = true;
                collision = pos.subtract(norm);
            } else if( tipDensity2 > 0 ) {
                fieldDirMaterial.setColor(ColorRGBA.Pink);
            } else {
                fieldDirMaterial.setColor(ColorRGBA.Green);
            }
 
            if( density > -5 ) {
                material.setColor(ColorRGBA.Red);
            }
 
            /*if( density > 0 ) {
                stop = true;
                collision = pos;
            }*/
 
//System.out.println( "density:" + density + "   dir:" + norm );             
            if( stop ) {
                balls2.remove(this);
 
                double avg = (double)time / iterations;
                System.out.println( "Average update time:" + (avg/1000000.0) + " ms" );
                
                float cx = collision.x - xBase;               
                float cy = collision.y - yBase;               
                float cz = collision.z - zBase;
                               
                Vector3f deepNorm = cachedVolume.getFieldDirection(cx + 1, cy + 1, cz + 1, null);
                Arrow arrow = new Arrow(deepNorm);
                Geometry g = new Geometry("normal", arrow);
                Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(collision);
                root.attachChild(g);
                
                // See if we can find the actual collision point.
                // First let's try using the field direction from
                // our center.
System.out.println( "tip density:" + tipDensity1 + "   center density:" + density );                
                float part = Math.abs(tipDensity1) / Math.abs(density - tipDensity1);
                
                // Distance between tip density and density is 1 in
                // world units
                float penetration = part * 1;
System.out.println( "penetration:" + penetration );                
                Sphere sphere = new Sphere(4, 8, 0.1f);
                g = new Geometry("collision", sphere);
                mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Red, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(pos.subtract(norm.mult(1-penetration)));
                root.attachChild(g);               
                
                // Check in the direction of travel
                Vector3f rel = pos.subtract(lastPos).normalizeLocal();
                float rx = pos.x + rel.x - xBase;               
                float ry = pos.y + rel.y - yBase;               
                float rz = pos.z + rel.z - zBase;
 
                float relDensity = cachedVolume.getDensity(rx + 1, ry + 1, rz + 1);
System.out.println( "relDensity:" + relDensity + "  center density:" + density );
                part = Math.abs(relDensity) / Math.abs(density - relDensity);
                penetration = part * 1;
System.out.println( "penetration:" + penetration );                
                
                sphere = new Sphere(4, 8, 0.1f);
                g = new Geometry("collision", sphere);
                mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Pink, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(pos.add(rel.mult(1-penetration)));
                root.attachChild(g);               
                 
            }
            
            long end = System.nanoTime();
            time += (end - start);
            iterations++;
        }
    }

    private class Projectile {
        Node node;
        Vector3f pos;
        Vector3f velocity;
        Vector3f acceleration = new Vector3f(0, -2, 0);
        float speed = 5;
        float energy = 100;
        float radius = 0.1f;
        Vector3f lastPos = new Vector3f();
        
        public Projectile( Vector3f pos, Vector3f v, ColorRGBA color ) {
        
            this.pos = pos.clone();
            this.velocity = v.mult(speed);
            this.node = new Node("ball");
            node.setLocalTranslation(pos);
        
            Sphere sphere = new Sphere(6, 12, radius);
            Geometry geom = new Geometry("ball", sphere);
            GuiMaterial material = GuiGlobals.getInstance().createMaterial(color, true);
            //material.getMaterial().getAdditionalRenderState().setWireframe(true);
            geom.setMaterial(material.getMaterial()); 
            node.attachChild(geom);            
        }
        
        public void update( float tpf ) {
 
            lastPos.set(pos);
            
            velocity.x += acceleration.x * tpf;
            velocity.y += acceleration.y * tpf;
            velocity.z += acceleration.z * tpf;
            
            velocity.y = Math.max(-10, velocity.y);
        
            pos.x += velocity.x * tpf;
            pos.y += velocity.y * tpf;
            pos.z += velocity.z * tpf;
            node.setLocalTranslation(pos);

            Contact c = worldCollider.getContact(pos, radius);
            if( c == null ) {
                return;
            }
            
            //System.out.println( "Stopped at position:" + pos + "  v:" + velocity );
            //System.out.println( c );
            
            pos.addLocal(c.contactNormal.mult(c.penetration));
            //System.out.println( "Corrected to:" + pos );
            node.setLocalTranslation(pos);

            // Dampen velocity to simulate friction
            velocity.x *= 0.99;
            velocity.y *= 0.99;
            velocity.z *= 0.99;
                        
            //projectiles.remove(this);
 
            // Distance moved in a single frame, remember
            float d = lastPos.distanceSquared(pos);
System.out.println( "d:" + d );            
            if( d < 0.00001f ) {
                energy *= 0.9f;
                if( energy < 0.1f ) {
                    System.out.println( "Sleeping." );
                    projectiles.remove(this);
                }
            } else if( d > 0.0001f ) {
                energy = 100;
            }           
             
        }
    }
    
    private class ArrowHolder {
        Geometry geom;
        Vector3f lastPos = new Vector3f();
        Vector3f pos;
        Vector3f dir;
        float traveled = 0;
        
        ArrayDensityVolume local = new ArrayDensityVolume(5, 5, 5);
        
        public ArrowHolder( Vector3f pos, Vector3f dir ) {
            this(pos, dir, ColorRGBA.Red);
        }
        
        public ArrowHolder( Vector3f pos, Vector3f dir, ColorRGBA color ) {
            Arrow arrow = new Arrow(dir);
            this.pos = pos;
            this.dir = dir;
            this.geom = new Geometry("arrow", arrow);
            Material mat = GuiGlobals.getInstance().createMaterial(color, false).getMaterial();
            this.geom.setMaterial(mat);
            geom.setLocalTranslation(pos.subtract(dir));
        }
        
        public void update( float tpf ) {
            if( traveled > 100 ) {
                arrows.remove(this);
                return;
            } 
            lastPos.set(pos);            
            pos.x += dir.x * tpf * 0.5f;
            pos.y += dir.y * tpf * 0.5f;
            pos.z += dir.z * tpf * 0.5f;            
            traveled += tpf;
            geom.setLocalTranslation(pos.x - dir.x, pos.y - dir.y, pos.z - dir.z);
            
            // Check for collision
            //float density = worldVolume.getDensity(pos.x + 1, pos.y, pos.z + 1);
            
            int xBase = (int)FastMath.floor(pos.x) - 1; 
            int yBase = (int)FastMath.floor(pos.y) - 1; 
            int zBase = (int)FastMath.floor(pos.z) - 1; 
            
            local.extract(worldVolume, xBase, yBase, zBase);
  
            float x = pos.x - xBase;
            float y = pos.y - yBase;
            float z = pos.z - zBase;
            float density = local.getDensity(x + 1, y + 1, z + 1);
 
            // x + 2
            // = 
            
            if( density > 0 ) {
                arrows.remove(this);
                
                Vector3f norm = local.getFieldDirection(x + 1, y + 1, z + 1, null);
                Arrow arrow = new Arrow(norm);
                Geometry g = new Geometry("normal", arrow);
                Material mat = GuiGlobals.getInstance().createMaterial(ColorRGBA.Yellow, false).getMaterial();
                g.setMaterial(mat);
                g.setLocalTranslation(pos);
                root.attachChild(g);
                //ArrowHolder normalArrow = new ArrowHolder(pos, norm, ColorRGBA.Yellow);
                //root.attachChild(normalArrow.geom);
            }
        }
    }
}
