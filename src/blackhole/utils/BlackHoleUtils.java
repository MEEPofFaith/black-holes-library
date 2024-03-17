package blackhole.utils;

import arc.math.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import mindustry.content.*;
import mindustry.entities.*;
import mindustry.entities.bullet.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;

public class BlackHoleUtils{
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

    /**
     * Add specific bullets you want to be immune to suction to this Seq.
     * This Seq can also be referenced for anything else you do involving suction.
     */
    public static final Seq<BulletType> immuneBullets = new Seq<>();

    /**
     * Add unit types you want to be immune to suction to this Seq.
     * This Seq can also be referenced for anything else you do involving suction.
     */
    public static final Seq<Class<?>> immuneUnitTypes = new Seq<>();

    /**
     * Add unit components you want to be immune to suction to this Seq.
     * This Seq can also be referenced for anything else you do involving suction.
     */
    public static final Seq<Class<?>> immuneUnitComps = Seq.with(
        BlockUnitUnit.class
    );


    /**
     * Add specific units you want to be immune to suction to this Seq.
     * This Seq can also be referenced for anything else you do involving suction.
     */
    public static final Seq<UnitType> immuneUnits = Seq.with(
        UnitTypes.block
    );

    /**
     * Handles the suction and damage dealt by black holes
     *
     * @param team Team of the black hole. Suction & damage affects other teams.
     * @param source What the black hole is updated from. Used to prevent a unit/bullet from affecting itself.
     * @param offsetX x offset from the source's position
     * @param offsetY y offset from the source's position
     * @param damageRadius Radius where units and bullets are damaged
     * @param suctionRadius Radius where units and bullets are sucked towards the black hole
     * @param damage Damage dealt to units
     * @param bulletDamage Damage dealt to bullets
     * @param repel If true, pushes away instead of pulls in
     * @param force Base amount of force applied to units
     * @param scaledForce Scaled amount of force applied to units. As units get closer to the center, more of scaledForce is added to force.
     * @param bulletForce Base amount of force applied to bullets.
     * @param scaledBulletForce Scaled amount of force applied to bullets. As bullets get closer to the center, more of scaledForce is added to force.
     */
    public static void blackHoleUpdate(
        Team team, Posc source, float offsetX, float offsetY,
        float damageRadius, float suctionRadius,
        float damage, float bulletDamage,
        boolean repel, float force, float scaledForce, float bulletForce, float scaledBulletForce
    ){
        float x = source.x() + offsetX, y = source.y() + offsetY;

        if(damage > 0f) Damage.damage(team, x, y, damageRadius, damage);

        Units.nearbyEnemies(team, x - suctionRadius, y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, unit -> {
            if(!unit.type.internal && !isUnitImmune(unit) && unit.hittable() && unit.within(x, y, suctionRadius) && source != unit){
                Vec2 impulse = Tmp.v1.trns(unit.angleTo(x, y), force + (1f - unit.dst(x, y) / suctionRadius) * scaledForce);
                if(repel) impulse.rotate(180f);
                unit.impulseNet(impulse);
            }
        });

        Groups.bullet.intersect(x - suctionRadius, y - suctionRadius, suctionRadius * 2f, suctionRadius * 2f, other -> {
            if(other != null && !isBulletImmune(other.type) && Mathf.within(x, y, other.x, other.y, suctionRadius) && source != other && team != other.team && other.type.speed > 0.01f){
                Vec2 impulse = Tmp.v1.trns(other.angleTo(x, y), bulletForce + (1f - other.dst(x, y) / suctionRadius) * scaledBulletForce);
                if(repel) impulse.rotate(180f);

                //Replicate unit impulseNet
                other.vel().add(impulse);

                if(other.isRemote()){
                    other.move(impulse.x, impulse.y);
                }

                //Damage/absorb bullets
                if(bulletDamage > 0f && other.type.hittable && Mathf.within(x, y, other.x, other.y, damageRadius)){
                    if(other.damage > bulletDamage){
                        other.damage(other.damage - bulletDamage);
                    }else{
                        other.remove();
                    }
                }
            }
        });
    }

    /**
     * Handles the suction and damage dealt by black holes
     *
     * @param team Team of the black hole. Suction & damage affects other teams.
     * @param source What the black hole is updated from. Used to prevent a unit/bullet from affecting itself.
     * @param damageRadius Radius where units and bullets are damaged
     * @param suctionRadius Radius where units and bullets are sucked towards the black hole
     * @param damage Damage dealt to units
     * @param bulletDamage Damage dealt to bullets
     * @param repel If true, pushes away instead of pulls in
     * @param force Base amount of force applied to units
     * @param scaledForce Scaled amount of force applied to units. As units get closer to the center, more of scaledForce is added to force.
     * @param bulletForce Base amount of force applied to bullets.
     * @param scaledBulletForce Scaled amount of force applied to bullets. As bullets get closer to the center, more of scaledForce is added to force.
     */
    public static void blackHoleUpdate(
        Team team, Posc source,
        float damageRadius, float suctionRadius,
        float damage, float bulletDamage,
        boolean repel, float force, float scaledForce, float bulletForce, float scaledBulletForce
    ){
        blackHoleUpdate(team, source, 0f, 0f, damageRadius, suctionRadius, damage, bulletDamage, repel, force, scaledForce, bulletForce, scaledBulletForce);
    }

    /** @return Whether the bullet type is immune to suction. */
    public static boolean isBulletImmune(BulletType type){
        return immuneBulletTypes.contains(c -> c.isAssignableFrom(type.getClass()))
            || immuneBullets.contains(type);
    }

    /** @return Whether the unit type is immune to suction. */
    public static boolean isUnitImmune(Unit unit){
        return immuneUnitTypes.contains(c -> c.isAssignableFrom(unit.type.getClass()))
            || immuneUnitComps.contains(c -> c.isAssignableFrom(unit.getClass()))
            || immuneUnits.contains(unit.type);
    }
}
