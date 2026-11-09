package com.itsoeh.sbsc.buti;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Confeti de celebración con los colores de B'ÜTI:
 * rombos (como el bordado), tiras y puntitos que salen disparados
 * y caen con gravedad. Se usa en la pantalla final del colibrí.
 *
 *   confeti.lanzar(x, y, 70);
 */
public class ConfetiView extends View {

    private static final int RECTANGULO = 0;
    private static final int ROMBO = 1;
    private static final int CIRCULO = 2;

    private static class Particula {
        float x, y, vx, vy, giro, velocidadGiro, tamano, vida, balanceo;
        int color, forma;
    }

    private final List<Particula> particulas = new ArrayList<>();
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path rombo = new Path();
    private final Random azar = new Random();
    private final float densidad;
    private final int[] colores;

    private long ultimoCuadro = 0;

    public ConfetiView(Context context) {
        this(context, null);
    }

    public ConfetiView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        densidad = context.getResources().getDisplayMetrics().density;

        colores = new int[]{
                ContextCompat.getColor(context, R.color.buti_dorado),
                ContextCompat.getColor(context, R.color.buti_rosa),
                ContextCompat.getColor(context, R.color.buti_verde),
                ContextCompat.getColor(context, R.color.buti_naranja),
                ContextCompat.getColor(context, R.color.buti_siena),
                ContextCompat.getColor(context, R.color.buti_crema)
        };

        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setClickable(false);
    }

    /** Explosión de confeti desde un punto (en píxeles dentro de esta vista). */
    public void lanzar(float x, float y, int cantidad) {

        for (int i = 0; i < cantidad; i++) {

            Particula p = new Particula();

            double angulo = Math.toRadians(-90 + (azar.nextFloat() * 150 - 75));
            float velocidad = (260 + azar.nextFloat() * 520) * densidad;

            p.x = x;
            p.y = y;
            p.vx = (float) (Math.cos(angulo) * velocidad);
            p.vy = (float) (Math.sin(angulo) * velocidad);
            p.giro = azar.nextFloat() * 360;
            p.velocidadGiro = (azar.nextFloat() * 2 - 1) * 540;
            p.tamano = (5 + azar.nextFloat() * 6) * densidad;
            p.vida = 1f;
            p.balanceo = azar.nextFloat() * 6.28f;
            p.color = colores[azar.nextInt(colores.length)];
            p.forma = azar.nextInt(3);

            particulas.add(p);
        }

        ultimoCuadro = 0;
        postInvalidateOnAnimation();
    }

    /** Lluvia suave desde arriba (para cuando el colibrí festeja). */
    public void llover(int cantidad) {

        for (int i = 0; i < cantidad; i++) {

            Particula p = new Particula();

            p.x = azar.nextFloat() * getWidth();
            p.y = -azar.nextFloat() * 60 * densidad;
            p.vx = (azar.nextFloat() * 2 - 1) * 40 * densidad;
            p.vy = (60 + azar.nextFloat() * 120) * densidad;
            p.giro = azar.nextFloat() * 360;
            p.velocidadGiro = (azar.nextFloat() * 2 - 1) * 360;
            p.tamano = (4 + azar.nextFloat() * 5) * densidad;
            p.vida = 1f;
            p.balanceo = azar.nextFloat() * 6.28f;
            p.color = colores[azar.nextInt(colores.length)];
            p.forma = azar.nextInt(3);

            particulas.add(p);
        }

        ultimoCuadro = 0;
        postInvalidateOnAnimation();
    }

    public void detener() {
        particulas.clear();
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (particulas.isEmpty()) return;

        long ahora = System.nanoTime();
        float dt = ultimoCuadro == 0 ? 0.016f : Math.min(0.05f, (ahora - ultimoCuadro) / 1_000_000_000f);
        ultimoCuadro = ahora;

        float gravedad = 900 * densidad;
        float alto = getHeight();

        Iterator<Particula> it = particulas.iterator();

        while (it.hasNext()) {

            Particula p = it.next();

            // Física: gravedad, aire y un vaivén como papel.
            p.vy += gravedad * dt;
            p.vx *= (1 - 1.6f * dt);
            p.vy *= (1 - 0.9f * dt);
            p.balanceo += dt * 6;
            p.x += (p.vx + (float) Math.sin(p.balanceo) * 30 * densidad) * dt;
            p.y += p.vy * dt;
            p.giro += p.velocidadGiro * dt;

            if (p.y > alto * 0.7f) p.vida -= dt * 1.6f;

            if (p.vida <= 0 || p.y > alto + 40 * densidad) {
                it.remove();
                continue;
            }

            pincel.setColor(p.color);
            pincel.setAlpha((int) (255 * Math.max(0, Math.min(1, p.vida))));

            canvas.save();
            canvas.translate(p.x, p.y);
            canvas.rotate(p.giro);

            // Efecto 3D: la pieza "voltea" al girar.
            float volteo = (float) Math.abs(Math.cos(Math.toRadians(p.giro * 2)));
            canvas.scale(1f, 0.35f + 0.65f * volteo);

            float t = p.tamano;

            switch (p.forma) {
                case ROMBO:
                    rombo.reset();
                    rombo.moveTo(0, -t);
                    rombo.lineTo(t * 0.7f, 0);
                    rombo.lineTo(0, t);
                    rombo.lineTo(-t * 0.7f, 0);
                    rombo.close();
                    canvas.drawPath(rombo, pincel);
                    break;

                case CIRCULO:
                    canvas.drawCircle(0, 0, t * 0.45f, pincel);
                    break;

                case RECTANGULO:
                default:
                    canvas.drawRect(-t * 0.35f, -t, t * 0.35f, t, pincel);
                    break;
            }

            canvas.restore();
        }

        if (!particulas.isEmpty()) postInvalidateOnAnimation();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        particulas.clear();
    }
}