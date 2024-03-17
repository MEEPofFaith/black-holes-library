package blackhole;

import blackhole.graphics.*;
import mindustry.mod.*;

import static arc.Core.*;
import static mindustry.Vars.*;

public class BlackHoleMod extends Mod{
    public BlackHoleMod(){
        ClassMap.classes.put("BlackHolePart", blackhole.entities.part.BlackHolePart.class);
        ClassMap.classes.put("DrawBlackHole", blackhole.world.draw.DrawBlackHole.class);
        ClassMap.classes.put("SwirlEffect", blackhole.entities.effect.SwirlEffect.class);
        ClassMap.classes.put("BlackHoleBulletType", blackhole.entities.bullet.BlackHoleBulletType.class);
    }

    @Override
    public void init(){
        BlackHoleRenderer.init(settings.getBool("advanced-black-hole-rendering", true));

        ui.settings.graphics.checkPref("advanced-black-hole-rendering", true, BlackHoleRenderer::toggleAdvanced);
    }
}
