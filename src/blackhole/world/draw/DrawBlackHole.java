package blackhole.world.draw;

import arc.graphics.*;
import arc.util.*;
import blackhole.graphics.*;
import mindustry.gen.*;
import mindustry.world.draw.*;

public class DrawBlackHole extends DrawBlock{
    public float x, y, size, edge;
    public @Nullable Color color;

    public DrawBlackHole(float size, float edge){
        this.size = size;
        this.edge = edge;
    }

    public DrawBlackHole(){
    }

    @Override
    public void draw(Building build){
        BlackHoleRenderer.addBlackHole(
            build.x + x, build.y + y,
            size * build.warmup(), edge * build.warmup(),
            color == null ? build.team.color : color
        );
    }
}
