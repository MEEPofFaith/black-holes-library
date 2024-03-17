package blackhole.entities.abilities;

import arc.graphics.*;
import arc.util.*;
import blackhole.graphics.*;
import blackhole.utils.*;
import mindustry.entities.abilities.*;
import mindustry.gen.*;
import mindustry.type.*;

public class BlackHoleAbility extends Ability{
    public float x, y;
    public float reload = 2f;
    public float horizonRadius = 6f, lensingRadius = -1f;
    public float damageRadius = -1f, suctionRadius = 160f;
    public boolean repel = false;
    public float force = 10f, scaledForce = 800f, bulletForce = 0.1f, scaledBulletForce = 1f;
    public float damage = 30f, bulletDamage = 10f;
    public @Nullable Color color = null;

    protected float timer;

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
            color == null ? unit.team.color : color
        );
    }

    @Override
    public void update(Unit unit){
        if((timer += Time.delta) >= reload){
            Tmp.v1.set(x, y).rotate(unit.rotation - 90f);
            BlackHoleUtils.blackHoleUpdate(
                unit.team, unit, Tmp.v1.x, Tmp.v1.y,
                damage, bulletDamage,
                damageRadius, suctionRadius,
                repel, force, scaledForce, bulletForce, scaledBulletForce
            );
            timer = 0f;
        }
    }
}
