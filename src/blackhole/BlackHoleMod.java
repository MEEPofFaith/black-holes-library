package blackhole;

import blackhole.graphics.*;
import mindustry.mod.*;

import static arc.Core.settings;
import static mindustry.Vars.ui;

public class BlackHoleMod extends Mod{
    public BlackHoleMod(){
    }

    @Override
    public void init(){
        BlackHoleRenderer.init(settings.getBool("advanced-black-hole-rendering", true));

        ui.settings.graphics.checkPref("advanced-black-hole-rendering", true, BlackHoleRenderer::toggleAdvanced);
    }
}
