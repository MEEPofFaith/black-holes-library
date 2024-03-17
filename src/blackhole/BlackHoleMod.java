package blackhole;

import blackhole.entities.effect.*;
import blackhole.graphics.*;
import mindustry.entities.*;
import mindustry.graphics.*;
import mindustry.mod.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class BlackHoleMod extends Mod{
    public static Effect defaultSwirlEffect = new SwirlEffect(90f, 8, 3f, 120f, 480f, true).layer(Layer.effect + 0.005f);

    public BlackHoleMod(){
        ClassMap.classes.put("BlackHoleAbility", blackhole.entities.abilities.BlackHoleAbility.class);
        ClassMap.classes.put("BlackHoleBulletType", blackhole.entities.bullet.BlackHoleBulletType.class);
        ClassMap.classes.put("BlackHolePart", blackhole.entities.part.BlackHolePart.class);
        ClassMap.classes.put("DrawBlackHole", blackhole.world.draw.DrawBlackHole.class);
        ClassMap.classes.put("SwirlEffect", blackhole.entities.effect.SwirlEffect.class);
    }

    @Override
    public void init(){
        BlackHoleRenderer.init(settings.getBool("advanced-black-hole-rendering", true));

        ui.settings.graphics.checkPref("advanced-black-hole-rendering", true, BlackHoleRenderer::toggleAdvanced);
    }
}
