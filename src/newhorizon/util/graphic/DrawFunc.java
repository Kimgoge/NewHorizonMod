package newhorizon.util.graphic;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.*;
import arc.math.Angles;
import arc.math.Interp;
import arc.math.Mathf;
import arc.math.Rand;
import arc.math.geom.Position;
import arc.math.geom.Vec2;
import arc.scene.ui.layout.Scl;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.gen.Buildingc;
import mindustry.gen.Groups;
import mindustry.gen.Teamc;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.graphics.Pal;
import mindustry.ui.Fonts;
import newhorizon.NewHorizon;
import newhorizon.content.NHColor;
import newhorizon.util.func.NHFunc;
import newhorizon.util.func.NHInterp;

import static mindustry.Vars.tilesize;

public class DrawFunc{
    public static final Color bottomColor = Pal.gray;
    public static final float sinScl = 1f;
    public static float NOR_DISTANCE = 600f;
    
    private static final Vec2
        vec21 = new Vec2(),
        vec22 = new Vec2(),
        vec23 = new Vec2();
    
    public static final int[] oneArr = {1};
    
    private static final Seq<Position> pointPos = new Seq<>(Position.class);
    private static final Rand rand = new Rand();
    
    public static void fillRect(float srcX, float srcY, float w, float h){
        Fill.quad(srcX, srcY, srcX + w, srcY, srcX + w, srcY + h, srcX, srcY + h);
    }
    
    public static void randLenVectors(long seed, float fin, int amount, float minLength, float length, Angles.ParticleConsumer cons){
        rand.setSeed(seed);
        for(int i = 0; i < amount; i++){
            float l = rand.nextFloat();
            vec21.trns(rand.random(360f), length * l * fin + minLength);
            cons.accept(vec21.x, vec21.y, fin * l, (1f - fin) * l);
        }
    }
    
    public static float cameraDstScl(float x, float y, float norDst){
        vec21.set(Core.camera.position);
        float dst = Mathf.dst(x, y, vec21.x, vec21.y);
        return 1 - Mathf.clamp(dst / norDst);
    }
    
    public static <T extends Building> void drawWhileHold(Class<T> type, Cons<T> drawer){
        Draw.z(Layer.overlayUI + 1);
        Seq<T> builds = Groups.build.copy(new Seq<>()).filter(b -> b.team == Vars.player.team() && type.isAssignableFrom(b.getClass())).as();
        for(T build : builds)drawer.get(build);
    }
    
    public static float cameraDstScl(float x, float y){
        return cameraDstScl(x, y, NOR_DISTANCE);
    }
    
    public static void tri(float x, float y, float width, float length, float angle){
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        Fill.tri(x + wx, y + wy, x - wx, y - wy, Angles.trnsx(angle, length) + x, Angles.trnsy(angle, length) + y);
    }
    
    public static void arrow(float x, float y, float width, float length, float offset, float angle){
        float wx = Angles.trnsx(angle + 90, width), wy = Angles.trnsy(angle + 90, width);
        float ox = Angles.trnsx(angle, offset), oy = Angles.trnsy(angle, offset);
        float cx = Angles.trnsx(angle, length) + x, cy = Angles.trnsy(angle, length) + y;
        Fill.tri(x + ox, y + oy, x - wx, y - wy, cx, cy);
        Fill.tri(x + wx, y + wy, x + ox, y + oy, cx, cy);
    }
    
    public static void surround(long id, float x, float y, float rad, int num, float innerSize, float outerSize, float interp){
        Rand rand = NHFunc.rand;
        
        rand.setSeed(id);
        for(int i = 0; i < num; i++){
            float len = rad * rand.random(0.75f, 1.5f);
            vec21.trns(rand.random(360f) + rand.range(2f) * (1.5f - Mathf.curve(len, rad * 0.75f, rad * 1.5f)) * Time.time, len);
            float angle = vec21.angle();
            vec21.add(x, y);
            DrawFunc.tri(vec21.x, vec21.y, (interp + 1) * outerSize + rand.random(0, outerSize / 8), outerSize * (Interp.exp5In.apply(interp) + 0.25f) / 2f, angle);
            DrawFunc.tri(vec21.x, vec21.y, (interp + 1) / 2 * innerSize + rand.random(0, innerSize / 8), innerSize * (Interp.exp5In.apply(interp) + 0.5f), angle - 180);
        }
    }
    
    public static void drawConnected(float x, float y, float size, Color color){
        Draw.reset();
        float sin = Mathf.absin(Time.time * sinScl, 8f, 1.25f);
        
        for(int i = 0; i < 4; i++){
            float length = size / 2f + 3 + sin;
            Tmp.v1.trns(i * 90, -length);
            Draw.color(Pal.gray);
            Draw.rect(NewHorizon.name("linked-arrow-back"), x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
            Draw.color(color);
            Draw.rect(NewHorizon.name("linked-arrow"), 	 x + Tmp.v1.x, y + Tmp.v1.y, i * 90);
        }
        Draw.reset();
    }
    
    public static void overlayText(Font font, String text, float x, float y, float offset, float offsetScl, float size, Color color, boolean underline, boolean align){
        GlyphLayout layout = Pools.obtain(GlyphLayout.class, GlyphLayout::new);
        boolean ints = font.usesIntegerPositions();
        font.setUseIntegerPositions(false);
        font.getData().setScale(size / Scl.scl(1.0f));
        layout.setText(font, text);
        font.setColor(color);
        
        float dy = offset + 3.0F;
        font.draw(text, x, y + layout.height / (align ? 2 : 1) + (dy + 1.0F) * offsetScl, 1);
        --dy;
    
        if(underline){
            Lines.stroke(2.0F, Color.darkGray);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
            Lines.stroke(1.0F, color);
            Lines.line(x - layout.width / 2.0F - 2.0F, dy + y, x + layout.width / 2.0F + 1.5F, dy + y);
            Draw.color();
        }
    
        font.setUseIntegerPositions(ints);
        font.setColor(Color.white);
        font.getData().setScale(1.0F);
        Draw.reset();
        Pools.free(layout);
    }
    
    public static void drawRail(float x, float y, float rotation, float shootY, float fin, float length, float width, float spacing, float scl, TextureRegion arrowRegion){
        float railF = Mathf.curve(Interp.pow2Out.apply(fin), 0f, 0.25f) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) * fin;
    
        for(int i = 0; i <= length / spacing; i++){
            Tmp.v1.trns(rotation, i * spacing * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f) + shootY);
            float f = Interp.pow3Out.apply(Mathf.clamp((fin * length - i * spacing) / spacing)) * (0.6f + railF * 0.4f) * 0.8f * scl;
            Draw.rect(arrowRegion, x + Tmp.v1.x, y + Tmp.v1.y, arrowRegion.width * Draw.scl * f, arrowRegion.height * Draw.scl * f, rotation - 90f);
        }
    
        Tmp.v1.trns(rotation, 0f, (2 - railF) * width);
        Tmp.v2.trns(rotation, shootY);
        Lines.stroke(railF * 2f * scl);
        for(int i : Mathf.signs){
            Lines.lineAngle(x + Tmp.v1.x * i + Tmp.v2.x, y + Tmp.v1.y * i + Tmp.v2.y, rotation, length * (0.75f + railF / 4f) * Mathf.curve(Interp.pow5Out.apply(1 - fin) * Mathf.curve(Interp.pow4Out.apply(1 - fin), 0f, 0.1f), 0f, 0.1f));
        }
    }
    
    public static void circlePercentFlip(float x, float y, float rad, float in, float scl){
        boolean monoIncr = in % (scl * 4) < scl * 2;
        float f = Mathf.cos(in % (scl * 3f), scl, 1.1f);
        circlePercent(x, y, rad, f > 0 ? f : -f, in + -90 * Mathf.sign(f));
    }
    
    public static void fillCirclePercent(float centerX, float centerY, float x, float y, float rad, float percent, float angle){
        float p = Mathf.clamp(percent);
    
        int sides = Lines.circleVertices(rad);
    
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
    
        int i;
    
        vec21.trns(angle, rad);
    
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2, y + rad * sin2, centerX, centerY);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec21.trns(a, 0, len * (f - 1));
        Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2 + vec21.x, y + rad * sin2 + vec21.y, centerX, centerY);
    }
    
    public static void fillCirclePercentFade(float centerX, float centerY, float x, float y, float rad, float percent, float angle, float aScl, float start , float end){
        float p = Mathf.clamp(percent);
        
        int sides = Lines.circleVertices(rad);
        
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
        
        int i;
        
        vec21.trns(angle, rad);
        
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Draw.alpha(Mathf.curve(i / (sides * p), start, end) * aScl);
            Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2, y + rad * sin2, centerX, centerY);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec21.trns(a, 0, len * (f - 1));
        Draw.alpha(aScl);
        Fill.tri(x + rad * cos, y + rad * sin, x + rad * cos2 + vec21.x, y + rad * sin2 + vec21.y, centerX, centerY);
    }
    
    public static void circlePercent(float x, float y, float rad, float percent, float angle) {
//        Lines.swirl(x, y, rad, 360 * percent, angle);
        float p = Mathf.clamp(percent);
        
        int sides = Lines.circleVertices(rad);
        
        float space = 360.0F / (float)sides;
        float len = 2 * rad * Mathf.sinDeg(space / 2);
        float hstep = Lines.getStroke() / 2.0F / Mathf.cosDeg(space / 2.0F);
        float r1 = rad - hstep;
        float r2 = rad + hstep;
        
        int i;
        
        for(i = 0; i < sides * p - 1; ++i){
            float a = space * (float)i + angle;
            float cos = Mathf.cosDeg(a);
            float sin = Mathf.sinDeg(a);
            float cos2 = Mathf.cosDeg(a + space);
            float sin2 = Mathf.sinDeg(a + space);
            Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2, y + r1 * sin2, x + r2 * cos2, y + r2 * sin2, x + r2 * cos, y + r2 * sin);
        }
    
        float a = space * i + angle;
        float cos = Mathf.cosDeg(a);
        float sin = Mathf.sinDeg(a);
        float cos2 = Mathf.cosDeg(a + space);
        float sin2 = Mathf.sinDeg(a + space);
        float f = sides * p - i;
        vec21.trns(a, 0, len * (f - 1));
        Fill.quad(x + r1 * cos, y + r1 * sin, x + r1 * cos2 + vec21.x, y + r1 * sin2 + vec21.y, x + r2 * cos2 + vec21.x, y + r2 * sin2 + vec21.y, x + r2 * cos, y + r2 * sin);
    }
    
    public static void overlayText(String text, float x, float y, float offset, Color color, boolean underline){
        overlayText(Fonts.outline, text, x, y, offset, 1, 0.25f, color, underline, false);
    }
    
    public static void drawSine(float x, float y, float x2, float y2, int phase, float mag, float scale, float offset, float distant, boolean flip){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        int dst = (int)(dstTotal / distant);
    
        if(dst < 1)return;
    
        Vec2 vec = new Vec2().trns(Angles.angle(x, y, x2, y2), distant);
    
        for(int sign : flip ? Mathf.signs : oneArr){
            for(int p = 0; p < phase; p++){
                Fill.circle(x, y, Lines.getStroke());
            
                for(int i = 0; i < dst; i++){
                    vec21.trns(Angles.angle(x, y, x2, y2) + 90, (Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i) * sinScl,
                            scale,
                            mag
                    ) - mag / 2) * i);
                
                    vec22.trns(Angles.angle(x, y, x2, y2) + 90, (Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i + 1) * sinScl,
                            1 * scale,
                            mag
                    ) - mag / 2) * i);
                
                    Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
                
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                    Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
                }
            }
        }
    }
    
    public static void drawSineLerp(float x, float y, float x2, float y2, int phase, float mag, float scale, float offset, float distant){
        float dstTotal = Mathf.dst(x, y, x2, y2);
        int dst = (int)(dstTotal / distant);
        
        if(dst < 1)return;
        
        Vec2 vec = new Vec2().trns(Angles.angle(x, y, x2, y2), distant);
        
        //for(int sign = 0; sign < 1; sign++){
            for(int p = 0; p < phase; p++){
                Fill.circle(x, y, Lines.getStroke());
                
                for(int i = 0; i < dst; i++){
                    vec21.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
    
                    vec22.trns(Angles.angle(x, y, x2, y2) + 90, Mathf.absin(
                            (mag / phase) * (3 * p) + (dstTotal / dst) * (offset * mag + i) * sinScl,
                            scale,
                            mag
                    ) - mag / 2);
                    
                    Vec2 from = vec.cpy().scl(i).add(vec21).add(x, y), to = vec.cpy().scl(i + 1).add(vec22).add(x, y);
                    
                    Lines.line(from.x, from.y, to.x, to.y, false);
                    Fill.circle(from.x, from.y, Lines.getStroke() / 2f);
                    Fill.circle(to.x, to.y, Lines.getStroke() / 2f);
                }
            }
        //}
    }
    
    public static void link(Buildingc from, Buildingc to, Color color){
        float
                sin = Mathf.absin(Time.time * sinScl, 6f, 1f),
                r1 = from.block().size / 2f * tilesize + sin,
                x1 = from.getX(), x2 = to.getX(), y1 = from.getY(), y2 = to.getY(),
                r2 = to.block().size / 2f * tilesize + sin;
        
        Draw.color(color);
    
        Lines.square(x2, y2, to.block().size * tilesize / 2f + 1.0f);
    
        Tmp.v1.trns(from.angleTo(to), r1);
        Tmp.v2.trns(to.angleTo(from), r2);
        int signs = (int)(from.dst(to) / tilesize);
    
        Lines.stroke(4, Pal.gray);
        Lines.dashLine(x1 + Tmp.v1.x, y1 + Tmp.v1.y, x2 + Tmp.v2.x, y2 + Tmp.v2.y, signs);
        Lines.stroke(2, color);
        Lines.dashLine(x1 + Tmp.v1.x, y1 + Tmp.v1.y, x2 + Tmp.v2.x, y2 + Tmp.v2.y, signs);
    
        Drawf.arrow(x1, y1, x2, y2, from.block().size * tilesize / 2f + sin, 4 + sin, color);
    
        Drawf.circles(x2, y2, r2, color);
    }
    
    public static void arrow(TextureRegion arrow, float x, float y, float sizeScl, float angle, Color color){
        Draw.color(color);
        Draw.rect(arrow, x, y, arrow.width * sizeScl * Draw.scl, arrow.height * sizeScl * Draw.scl, angle);
    }

    public static void arrow(String arrow, float x, float y, float sizeScl, float angle, Color color){
        arrow(Core.atlas.find(arrow, "bridge-arrow"), x, y, sizeScl, angle, color);
    }

    public static void arrowLine(TextureRegion arrow, float sizeScl, Color color, float dst, float maxDst, int offset, Position from, Position to){
        float dstF = dst <= 0 ? Vars.tilesize * 2f : dst;
        int num = (int)(from.dst(to) / dstF);
        if(num - 2 * offset < 0)return;
        float maxDstF = maxDst <= 0 ? Float.MAX_VALUE : maxDst;
        float angle = from.angleTo(to);
        Vec2 vec = new Vec2().trns(angle, dstF);
        for (int i = offset; i <= num - offset; i++) {
            if(vec.len() > maxDstF)break;
            arrow(arrow, from.getX() + vec.cpy().scl(i).x, from.getY() + vec.cpy().scl(i).y, sizeScl, angle, color);
        }
    }

    public static void arrowLine(String arrow, float sizeScl, Color color, float dst, float maxDst, int offset, Position from, Position to){
        arrowLine(Core.atlas.find(arrow, "bridge-arrow"), sizeScl, color, dst, maxDst, offset, from, to);
    }

    public static void posSquare(Color color, float stroke, float size, float x1, float y1, float x2, float y2){

    }

    public static void posSquareLinkArr(Color color, float stroke, float size, boolean drawBottom, boolean linkLine, Position... pos){
        if(pos.length < 2 || (!linkLine && pos[0] == null))return;
        
        for (int c : drawBottom ? Mathf.signs : oneArr) {
            for (int i = 1; i < pos.length; i++) {
                if (pos[i] == null)continue;
                Position p1 = pos[i - 1], p2 = pos[i];
                Lines.stroke(stroke + 1 - c, c == 1 ? color : bottomColor);
                if(linkLine) {
                    if(p1 == null)continue;
                    Lines.line(p2.getX(), p2.getY(), p1.getX(), p1.getY());
                }else{
                    Lines.line(p2.getX(), p2.getY(), pos[0].getX(), pos[0].getY());
                }
                Draw.reset();
            }

            for (Position p : pos) {
                if (p == null)continue;
                Draw.color(c == 1 ? color : bottomColor);
                Fill.square(p.getX(), p.getY(), size + 1 -c / 1.5f, 45);
                Draw.reset();
            }
        }
    }

    public static Color markColor(Team target){
        return target == Vars.player.team() ? NHColor.ally : NHColor.hostile;
    }
    
    public static Color markColor(Teamc target){
        return target.team() == Vars.player.team() ? NHColor.ally : NHColor.hostile;
    }
    
    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, float x, float y, float x2, float y2){
        posSquareLink(color, stroke, size, drawBottom, vec21.set(x, y), vec22.set(x2, y2));
    }

    public static void posSquareLink(Color color, float stroke, float size, boolean drawBottom, Position from, Position to){
        posSquareLinkArr(color, stroke, size, drawBottom, false, from, to);
    }
    
    public static float rotator_90(float in /*(0, 1)*/, float margin){
        return 90 * NHInterp.pow10.apply(Mathf.curve(in, margin, 1 - margin));
    }
    
    public static float rotator_90(){return 90 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));}
    
    public static float rotator_180(){return 180 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));}
    
    public static float rotator_360(){return 360 * Interp.pow5.apply(Mathf.curve(cycle_100(), 0.15f, 0.85f));}
    
    public static float cycle_100(){return Time.time % 100 / 100;}
    
    public static float cycle(float T){
        return Time.time % T / T;
    }
}
