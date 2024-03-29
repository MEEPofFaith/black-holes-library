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

import static blackhole.graphics.BHDrawf.teamColor;
import static mindustry.Vars.*;

public class BlackHoleBulletType extends BulletType{
    public float horizonRadius = -1f, lensingRadius = -1f;
    public float damageRadius = 6f, suctionRadius = 160f;
    public boolean repel;
    /** Base amount of force applied to units */
    public float force = 10f;
    /** Scaled amount of force applied to units. As units get closer to the center, more of scaledForce is added to force. */
    public float scaledForce = 800f;
    /** Base amount of force applied to bullets. */
    public float bulletForce = 0.1f;
    /** Scaled amount of force applied to bullets. As bullets get closer to the center, more of scaledForce is added to force. */
    public float scaledBulletForce = 1f;
    public float bulletDamage = 10f;
    /** Color of black hole and effects. If null, uses team color. */
    public @Nullable Color color = null;
    public float growTime = 10f, shrinkTime = -1f;
    public float starWidth = -1, starHeight = -1, starAngle;
    public @Nullable Color starIn, starOut;

    public Effect swirlEffect = new SwirlEffect();
    public float swirlInterval = 3f;
    public int swirlEffects = 4;
    public boolean counterClockwise = false;

    public Sound loopSound = Sounds.spellLoop;
    public float loopSoundVolume = 2f;

    public BlackHoleBulletType(float speed, float damage){
        super(speed, damage);
        hittable = absorbable = false;
        collides = collidesAir = collidesGround = collidesTiles = false;
        pierce = true;
        shootEffect = smokeEffect = Fx.none;
        despawnEffect = Fx.none;
        layer = BHLayer.end + 0.01f;
    }

    public BlackHoleBulletType(){
        this(1f, 1f);
    }

    @Override
    public void init(){
        super.init();
        if(lensingRadius < 0f) lensingRadius = suctionRadius;
        if(shrinkTime < 0f) shrinkTime = swirlEffect.lifetime;
        if(horizonRadius < 0f) horizonRadius = damageRadius;
        if(starWidth > 0 && starHeight < 0) starHeight = starWidth / 2;

        drawSize = Math.max(drawSize, lensingRadius * 2f);
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
                    swirlEffect.at(b.x, b.y, suctionRadius * (counterClockwise ? -1f : 1f) * fout(b), teamColor(b, color), b);
                }
            }
        }
    }

    @Override
    public void draw(Bullet b){
        float fout = fout(b);
        BlackHoleRenderer.addBlackHole(
            b.x, b.y,
            horizonRadius * fout, lensingRadius * fout,
            teamColor(b, color)
        );
        if(starWidth > 0){
            BlackHoleRenderer.addStar(
                b.x, b.y,
                starWidth * fout, starHeight * fout, starAngle,
                teamColor(b, starIn), teamColor(b, starOut)
            );
        }
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
}
