package blackhole.entities.effect;

import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import arc.util.pooling.*;
import blackhole.graphics.*;
import mindustry.entities.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.world.blocks.defense.turrets.BaseTurret.*;

import static arc.util.Tmp.*;
import static mindustry.Vars.*;

/**
 * A particle with a trail that falls into the center.
 * When spawning, input the radius into the rotation param.
 * Inputting a negative value makes the particle swirl counter-clockwise instead of clockwise.
 */
public class SwirlEffect extends Effect{
    /** How many points long the trail is. */
    public int length;
    /** Radius of the trail. */
    public float width;
    /** How much the particle will revolve around the center in degrees. */
    public float minRot, maxRot;
    /**
     * If set to values >= 0, the radius will be a random amount between minDst and maxDst,
     * however the sign of the input is still used to determine rotation direction.
     */
    public float minDst, maxDst;
    /** If < 0, don't emit light. */
    public float lightOpacity = 0.8f;
    /** If true, particle fades from edgeColor to effect color. Else, the particle is constantly the effect color. */
    public boolean lerp;
    public @Nullable Color edgeColor;
    public Interp fallterp = Interp.pow2Out;
    public Interp spinterp = Interp.pow3Out;
    /** Overrides spin direction from radius provided by bullet rotation. >1 for clockwise, <1 for counter-clockwise */
    public float spinDirectionOverride = 0f;

    public SwirlEffect(float lifetime, float clipsize, Color edgeColor, int length, float width, float minRot, float maxRot, float minDst, float maxDst, boolean lerp){
        super();
        this.lifetime = lifetime;
        this.clip = clipsize;
        this.edgeColor = edgeColor;
        this.length = length;
        this.width = width;
        this.minRot = minRot;
        this.maxRot = maxRot;
        this.minDst = minDst;
        this.maxDst = maxDst;
        this.lerp = lerp;

        followParent = rotWithParent = true;
        layer = Layer.effect + 0.005f;
    }

    public SwirlEffect(float lifetime, Color edgeColor, int length, float width, float minRot, float maxRot, float minDst, float maxDst, boolean lerp){
        this(lifetime, 400f, edgeColor, length, width, minRot, maxRot, minDst, maxDst, lerp);
    }

    public SwirlEffect(float lifetime, int length, float width, float minRot, float maxRot, boolean lerp){
        this(lifetime, Color.black, length, width, minRot, maxRot, -1, -1, lerp);
    }

    public SwirlEffect(){
        this(90f, 8, 3f, 120f, 480f, true);
    }

    public SwirlEffect setInterps(Interp fallterp, Interp spinterp){
        this.fallterp = fallterp;
        this.spinterp = spinterp;
        return this;
    }

    public SwirlEffect setInterps(Interp interp){
        return setInterps(interp, interp);
    }

    @Override
    public void render(EffectContainer e){
        float lifetime = e.lifetime - length;
        float dst;
        if(minDst < 0 || maxDst < 0){
            dst = Math.abs(e.rotation);
        }else{
            dst = Mathf.randomSeed(e.id, minDst, maxDst);
        }
        float l = Mathf.clamp(e.time / lifetime);
        if(lerp){
            Tmp.c1.set(edgeColor).lerp(e.color, l);
        }else{
            Tmp.c1.set(e.color);
        }

        float width = Mathf.clamp(e.time / (e.lifetime - length)) * this.width;
        float dir = spinDirectionOverride != 0 ? Mathf.sign(spinDirectionOverride) : Mathf.sign(e.rotation);
        float baseRot = Mathf.randomSeed(e.id + 1, 360f), addRot = Mathf.randomSeed(e.id + 2, minRot, maxRot) * dir;

        Trail trail = (Trail)e.data;
        if(!state.isPaused()){
            float f = 1f - (e.time / lifetime);
            if(f > 0f){
                v1.trns(baseRot + addRot * spinterp.apply(f), Mathf.maxZero(dst * fallterp.apply(f))).add(e.x, e.y);
                trail.update(v1.x, v1.y);
            }else{
                trail.shorten();
            }
        }

        trail.drawCap(Tmp.c1, width);

        if(trail instanceof LightTrail lightTrail){
            lightTrail.draw(Tmp.c1, width, l);
        }else{
            trail.draw(Tmp.c1, width);
        }
    }

    @Override
    protected void add(float x, float y, float rotation, Color color, Object data){
        BlackHoleEffectState entity = BlackHoleEffectState.create();
        entity.effect = this;
        entity.rotation = baseRotation + rotation;
        entity.lifetime = lifetime;
        entity.set(x, y);
        entity.color.set(color);
        if(followParent && data instanceof Posc p){
            entity.parent = p;
            entity.rotWithParent = rotWithParent;
        }
        entity.data = lightOpacity > 0f ? new LightTrail(length, lightOpacity) : new Trail(length);
        entity.add();
    }

    public static class BlackHoleEffectState extends EffectState{
        public static BlackHoleEffectState create(){
            return Pools.obtain(BlackHoleEffectState.class, BlackHoleEffectState::new);
        }

        @Override
        public void add(){
            if(!added){
                index__all = Groups.all.addIndex(this);
                index__draw = Groups.draw.addIndex(this);
                if(parent != null){
                    offsetX = x - parent.x();
                    offsetY = y - parent.y();
                    if(rotWithParent){
                        if(parent instanceof Rotc r){
                            offsetPos = -r.rotation();
                            offsetRot = rotation - r.rotation();
                        }else if(parent instanceof BaseTurretBuild build){
                            offsetPos = -build.rotation;
                            offsetRot = rotation - build.rotation;
                        }
                    }
                }

                added = true;
            }
        }

        @Override
        public void update(){
            followParent:
            if(parent != null){
                if(rotWithParent){
                    if(parent instanceof Rotc r){
                        x = parent.x() + Angles.trnsx(r.rotation() + offsetPos, offsetX, offsetY);
                        y = parent.y() + Angles.trnsy(r.rotation() + offsetPos, offsetX, offsetY);
                        //Do not change rotation. It is used for radius.
                        break followParent;
                    }else if(parent instanceof BaseTurretBuild build){
                        x = parent.x() + Angles.trnsx(build.rotation + offsetPos, offsetX, offsetY);
                        y = parent.y() + Angles.trnsy(build.rotation + offsetPos, offsetX, offsetY);
                        //Do not change rotation. It is used for radius.
                        break followParent;
                    }
                }

                x = parent.x() + offsetX;
                y = parent.y() + offsetY;
            }

            time = Math.min(time + Time.delta, lifetime);
            if(time >= lifetime){
                remove();
            }
        }
    }
}
