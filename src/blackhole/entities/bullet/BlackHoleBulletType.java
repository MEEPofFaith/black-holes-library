package blackhole.entities.bullet;

import arc.audio.*;
import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import blackhole.entities.effect.*;
import blackhole.graphics.*;
import mindustry.audio.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.graphics.*;

import static mindustry.Vars.*;

public class BlackHoleBulletType extends BulletType{
    /**
     * Add bullet types you want to be immune to suction to this Seq.
     * This Seq can also be referenced for anything else you do involving suction.
     */
    public static final Seq<Class<?>> immuneBulletTypes = Seq.with(
        ContinuousBulletType.class,
        LaserBulletType.class,
        SapBulletType.class,
        ShrapnelBulletType.class
    );

    public Effect swirlEffect = new SwirlEffect(90f, 8, 3f, 120f, 480f, true).layer(Layer.effect + 0.005f);
    public Sound loopSound = Sounds.spellLoop;
    public float loopSoundVolume = 2f;
    public float suctionRadius = 160f, size = 6f, lensEdge = -1f, damageRadius = -1f;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, bulletScaledForce = 1f;
    public float bulletDamage = 10f;
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
            Damage.damage(b.team, b.x, b.y, damageRadius * fout, b.damage);

            float sR = suctionRadius * fout;
            Units.nearbyEnemies(b.team, b.x - sR, b.y - sR, sR * 2f, sR * 2f, unit -> {
                if(unit.within(b.x, b.y, sR)){
                    Vec2 impulse = Tmp.v1.trns(unit.angleTo(b), force + (1f - unit.dst(b) / sR) * scaledForce);
                    if(repel) impulse.rotate(180f);
                    unit.impulseNet(impulse);
                }
            });

            Groups.bullet.intersect(b.x - sR, b.y - sR, sR * 2f, sR * 2f, other -> {
                if(other != null && !checkType(other.type) && Mathf.within(b.x, b.y, other.x, other.y, sR) && b != other && b.team != other.team && other.type.speed > 0.01f){
                    Vec2 impulse = Tmp.v1.trns(other.angleTo(b), bulletForce + (1f - other.dst(b) / sR) * bulletScaledForce);
                    if(repel) impulse.rotate(180f);

                    //Replicate unit impulseNet
                    other.vel().add(impulse);

                    if(other.isRemote()){
                        other.move(impulse.x, impulse.y);
                    }

                    //Damage/absorb bullets
                    if(other.type.hittable && Mathf.within(b.x, b.y, other.x, other.y, size * 2f)){
                        float realDamage = bulletDamage * damageMultiplier(b);
                        if(other.damage > realDamage){
                            other.damage(other.damage - realDamage);
                        }else{
                            other.remove();
                        }
                    }
                }
            });
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
                    swirlEffect.at(b.x, b.y, suctionRadius, b.team.color, b);
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float fout = fout(b);
        BlackHoleRenderer.addBlackHole(b.x, b.y, size * fout, lensEdge * fout, b.team.color);
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

    public static boolean checkType(BulletType type){ //Returns true for bullets immune to suction.
        return immuneBulletTypes.contains(c -> c.isAssignableFrom(type.getClass()));
    }
}
