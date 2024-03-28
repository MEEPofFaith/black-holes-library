package blackhole.world.draw;

import arc.graphics.*;
import arc.util.*;
import blackhole.graphics.*;
import mindustry.gen.*;
import mindustry.world.*;
import mindustry.world.draw.*;

public class DrawBlackHole extends DrawBlock{
    public float x, y, size, edge;
    public @Nullable Color color;
    public float starWidth = -1, starHeight = -1, starAngle;
    public @Nullable Color starIn, starOut;
    public boolean warmup = true;

    public DrawBlackHole(float size, float edge){
        this.size = size;
        this.edge = edge;
    }

    public DrawBlackHole(){
    }

    @Override
    public void draw(Building build){
        float scl = warmup ? build.warmup() : 1f;
        BlackHoleRenderer.addBlackHole(
            build.x + x, build.y + y,
            size * scl, edge * scl,
            getColor(build, color)
        );
        if(starWidth > 0){
            BlackHoleRenderer.addStar(
                build.x + x, build.y + y,
                starWidth * scl, starHeight * scl, starAngle,
                getColor(build, starIn), getColor(build, starOut)
            );
        }
    }

    @Override
    public void load(Block block){
        if(starWidth > 0 && starHeight < 0) starHeight = starWidth / 2;
    }

    public Color getColor(Building build, Color color){
        return color == null ? build.team.color : color;
    }
}
