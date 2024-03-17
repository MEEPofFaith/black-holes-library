package blackhole.entities.abilities;

import arc.graphics.*;
import arc.util.*;
import blackhole.*;
import blackhole.graphics.*;
import blackhole.utils.*;
import mindustry.entities.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;

public class BlackHoleAbility extends Ability{
    public float x, y;
    public float reload = 2f;
    public float horizonRadius = 6f, lensingRadius = -1f;
    public float damageRadius = -1f, suctionRadius = 160f;
    public boolean repel = false;
    /** Base amount of force applied to units */
    public float force = 10f;
    /** Scaled amount of force applied to units. As units get closer to the center, more of scaledForce is added to force. */
    public float scaledForce = 800f;
    /** Base amount of force applied to bullets. */
    public float bulletForce = 0.1f;
    /** Scaled amount of force applied to bullets. As bullets get closer to the center, more of scaledForce is added to force. */
    public float scaledBulletForce = 1f;
    public float damage = 30f, bulletDamage = 10f;
    /** Color of black hole and effects. If null, uses team color. */
    public @Nullable Color color = null;

    public @Nullable Effect swirlEffect = BlackHoleMod.defaultSwirlEffect;
    public float swirlInterval = 3f;
    public int swirlEffects = 4;
    public boolean counterClockwise = false;

    protected float effectTimer;
    protected float suctionTimer;

    @Override
    public void init(UnitType type){
        if(damageRadius < 0f) damageRadius = horizonRadius;
        if(lensingRadius < 0f) lensingRadius = suctionRadius;
    }

    @Override
    public void draw(Unit unit){
        Tmp.v1.set(x, y).rotate(unit.rotation - 90f).add(unit);
        BlackHoleRenderer.addBlackHole(
            Tmp.v1.x, Tmp.v1.y,
            horizonRadius, lensingRadius,
            blackHoleColor(unit)
        );
    }

    @Override
    public void update(Unit unit){
        Tmp.v1.set(x, y).rotate(unit.rotation - 90f);
        if((suctionTimer += Time.delta) >= reload){
            BlackHoleUtils.blackHoleUpdate(
                unit.team, unit, Tmp.v1.x, Tmp.v1.y,
                damage, bulletDamage,
                damageRadius, suctionRadius,
                repel, force, scaledForce, bulletForce, scaledBulletForce
            );
            suctionTimer = 0f;
        }

        if(swirlEffect != null && (effectTimer += Time.delta) >= swirlInterval){
            for(int i = 0; i < swirlEffects; i++){
                swirlEffect.at(unit.x + Tmp.v1.x, unit.y + Tmp.v1.y, suctionRadius * (counterClockwise ? -1f : 1f), blackHoleColor(unit), unit);
            }
        }
    }

    public Color blackHoleColor(Unit unit){
        return color == null ? unit.team.color : color;
    }
}
