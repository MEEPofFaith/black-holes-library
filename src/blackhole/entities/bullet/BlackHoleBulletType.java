package blackhole.entities.bullet;

import arc.audio.*;
import arc.graphics.*;
import arc.math.*;
import arc.util.*;
import blackhole.entities.effect.*;
import blackhole.graphics.*;
import blackhole.utils.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class BlackHoleBulletType extends BulletType{

    public Effect swirlEffect = new SwirlEffect(90f, 8, 3f, 120f, 480f, true).layer(Layer.effect + 0.005f);
    public Sound loopSound = Sounds.spellLoop;
    public float loopSoundVolume = 2f;
    public float suctionRadius = 160f, size = 6f, lensEdge = -1f, damageRadius = -1f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, scaledBulletForce = 1f;
    public float bulletDamage = 10f;
    public @Nullable Color color = null;
    public float growTime = 10f, shrinkTime = -1f;
    public float swirlInterval = 3f;
    public int swirlEffects = 4;
    public boolean repel;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = Fx.none;
        layer = Layer.effect + 0.03f;
    }

    @Override
    public void init(){
        super.init();
        if(lensEdge < 0f) lensEdge = suctionRadius;
        if(shrinkTime < 0f) shrinkTime = swirlEffect.lifetime;
        if(damageRadius < 0f) damageRadius = size + 4f;

        drawSize = Math.max(drawSize, lensEdge * 2f);
    }

    @Override
    public float continuousDamage(){
        return damage / 2f * 60f; //Damage every 2 ticks
    }

    @Override
    public void init(Bullet b){
        super.init(b);

        if(loopSound != null){
            b.data = new SoundLoop(loopSound, loopSoundVolume);
        }
    }

    @Override
    public void update(Bullet b){
        if(b.timer(1, 2f)){
            float fout = fout(b);
            BlackHoleUtils.blackHoleUpdate(
                b.team, b,
                damageRadius * fout, suctionRadius * fout,
                b.damage, bulletDamage * damageMultiplier(b),
                repel, force, scaledForce, bulletForce, scaledBulletForce
            );
        }

        if(!headless && b.data instanceof SoundLoop loop){
            loop.update(b.x, b.y, b.isAdded(), fout(b));
        }

        super.update(b);
    }

    @Override
    public void updateTrailEffects(Bullet b){
        super.updateTrailEffects(b);

        if(swirlInterval > 0f && b.time <= b.lifetime - swirlEffect.lifetime){
            if(b.timer(0, swirlInterval)){
                for(int i = 0; i < swirlEffects; i++){
                    swirlEffect.at(b.x, b.y, suctionRadius, blackHoleColor(b), b);
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float fout = fout(b);
        BlackHoleRenderer.addBlackHole(b.x, b.y, size * fout, lensEdge * fout, blackHoleColor(b));
    }

    @Override
    public void drawLight(Bullet b){
        //none
    }

    public float fout(Bullet b){
        return Interp.sineOut.apply(
            Mathf.curve(b.time, 0f, growTime)
                - Mathf.curve(b.time, b.lifetime - shrinkTime, b.lifetime)
        );
    }

    public Color blackHoleColor(Bullet b){
        return color == null ? b.team.color : color;
    }

    @Override
    public void despawned(Bullet b){
        despawnEffect.at(b.x, b.y, b.rotation(), b.team.color);

        hitSound.at(b);

        Effect.shake(despawnShake, despawnShake, b);

        if(!b.hit && (fragBullet != null || splashDamageRadius > 0f || lightning > 0)){
            hit(b);
        }
    }

    @Override
    public void removed(Bullet b){
        super.removed(b);
        if(b.data instanceof SoundLoop loop){
            loop.stop();
        }
    }
}
