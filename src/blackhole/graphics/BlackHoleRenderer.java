package blackhole.graphics;

import arc.*;
import arc.graphics.*;
import arc.graphics.g2d.*;
import arc.graphics.gl.*;
import arc.struct.*;
import arc.util.*;
import mindustry.game.EventType.*;
import mindustry.graphics.*;

import static arc.Core.*;
import static mindustry.Vars.*;

/**
 * Handles rendering of gravitational lensing and the glow around the center.
 * @author MEEPofFaith
 * */
public class BlackHoleRenderer{
    private final Seq<BlackHoleZone> zones = new Seq<>(BlackHoleZone.class);
    private static BlackHoleRenderer bRenderer;
    private boolean advanced = true;

    private FrameBuffer buffer;

    protected BlackHoleRenderer(boolean advanced){
        BShaders.createBlackHoleShaders();
        advanced(advanced);

        Events.run(Trigger.draw, () -> {
            if(this.advanced){
                advancedDraw();
            }else{
                simplifiedDraw();
            }
        });
    }

    public static void init(boolean advanced){
        if(bRenderer == null) bRenderer = new BlackHoleRenderer(advanced);
    }

    public static void toggleAdvanced(boolean advanced){
        if(bRenderer != null) bRenderer.advanced(advanced);
    }

    /**
     * Adds a black hole to the renderer.
     *
     * @param x x-coordinate of the center
     * @param y y-coordinate of the center
     * @param inRadius size of the black hole (radius of where it's black)
     * @param outRadius end of the gravitational lensing range
     * @param color color of the glowing rim
     */
    public static void addBlackHole(float x, float y, float inRadius, float outRadius, Color color){
        bRenderer.add(x, y, inRadius, outRadius, color);
    }

    private void advancedDraw(){
        Draw.draw(Layer.min + 0.01f, () -> {
            buffer.resize(graphics.getWidth(), graphics.getHeight());
            buffer.begin();
        });

        Draw.draw(Layer.max, () -> {
            buffer.end();

            if(zones.size >= BShaders.maxCount) BShaders.createBlackHoleShaders();

            float[] blackholes = new float[zones.size * 4];
            float[] colors = new float[zones.size * 4];
            for(int i = 0; i < zones.size; i++){
                BlackHoleZone zone = zones.get(i);
                blackholes[i * 4] = zone.x;
                blackholes[i * 4 + 1] = zone.y;
                blackholes[i * 4 + 2] = zone.inRadius;
                blackholes[i * 4 + 3] = zone.outRadius;

                Tmp.c1.abgr8888(zone.color);
                colors[i * 4] = Tmp.c1.r;
                colors[i * 4 + 1] = Tmp.c1.g;
                colors[i * 4 + 2] = Tmp.c1.b;
                colors[i * 4 + 3] = Tmp.c1.a;
            }
            BShaders.lensingShader.blackHoles = blackholes;
            buffer.blit(BShaders.lensingShader);

            BShaders.rimShader.blackHoles = blackholes;
            BShaders.rimShader.colors = colors;
            buffer.begin();
            Draw.rect();
            buffer.end();

            Bloom bloom = renderer.bloom;
            if(bloom != null){
                bloom.capture();
                buffer.blit(BShaders.rimShader);
                bloom.render();
            }else{
                buffer.blit(BShaders.rimShader);
            }
            zones.clear();
        });
    }

    private void simplifiedDraw(){
        Draw.draw(Layer.max, () -> {
            for(BlackHoleZone zone : zones){
                float rad = zone.inRadius * 4;
                Fill.light(
                    zone.x, zone.y,
                    Lines.circleVertices(rad), rad,
                    Tmp.c1.abgr8888(zone.color), Tmp.c2.set(Tmp.c1).a(0f)
                );
            }
            Draw.color(Color.black);
            for(BlackHoleZone zone : zones){
                Fill.circle(zone.x, zone.y, zone.inRadius);
            }
            Draw.color();
        });

        zones.clear();
    }

    private void advanced(boolean advanced){
        this.advanced = advanced;
        if(advanced){
            buffer = new FrameBuffer();
        }else{
            if(buffer != null) buffer.dispose();
        }
    }

    private void add(float x, float y, float inRadius, float outRadius, Color color){
        if(inRadius > outRadius || outRadius <= 0) return;

        float res = Color.toFloatBits(color.r, color.g, color.b, 1);

        zones.add(new BlackHoleZone(x, y, res, inRadius, outRadius));
    }

    private static class BlackHoleZone{
        public float x, y, color, inRadius, outRadius;

        public BlackHoleZone(float x, float y, float color, float inRadius, float outRadius){
            this.x = x;
            this.y = y;
            this.color = color;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
        }

        public void set(float x, float y, float color, float inRadius, float outRadius){
            this.x = x;
            this.y = y;
            this.color = color;
            this.inRadius = inRadius;
            this.outRadius = outRadius;
        }
    }
}
