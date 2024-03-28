package blackhole.graphics;

import arc.graphics.g2d.*;
import arc.util.*;

import static arc.math.Mathf.*;

public class BHDrawf{
    public static void drawStar(float x, float y, float w, float h, float angleOffset, float centerColor, float edgeColor){
        int sides = Lines.circleVertices(w + h) / 4 * 4; //Ensure multiple of 4
        float space = 360f / sides;

        for(int i = 0; i < sides; i++){
            float t1 = i * space, t2 = (i + 1) * space;
            Tmp.v1.trns(t1, circleStarPoint(t1)).scl(w, h).rotate(angleOffset).add(x, y);
            Tmp.v2.trns(t2, circleStarPoint(t2)).scl(w, h).rotate(angleOffset).add(x, y);
            Fill.quad(
                x, y, centerColor,
                x, y, centerColor,
                Tmp.v1.x, Tmp.v1.y, edgeColor,
                Tmp.v2.x, Tmp.v2.y, edgeColor
            );
        }
    }

    public static void drawStar(float x, float y, float w, float h, float centerColor, float edgeColor){
        drawStar(x, y, w, h, 0f, centerColor, edgeColor);
    }

    public static float circleStarPoint(float theta){
        theta = mod(theta, 90f);
        theta *= degRad;
        float b = -2 * sqrt2 * cos(theta - pi / 4f);
        return (-b - sqrt(b * b - 4)) / 2;
    }
}
