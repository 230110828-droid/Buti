package com.itsoeh.sbsc.buti.progreso;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.itsoeh.sbsc.buti.R;

/**
 * Gráfica de barras sencilla, dibujada sin librerías externas.
 * Se usa en "Mi progreso" para:
 *  - Puntos obtenidos en los últimos 7 días.
 *  - Etapas completadas por nivel.
 */
public class GraficaBarrasView extends View {

    // Paleta B'ÜTI (cambia sola en modo oscuro: se lee de colors.xml / values-night).
    private final int COLOR_BARRA;      // naranja
    private final int COLOR_DESTACADA;  // vino en claro, dorado en oscuro
    private final int COLOR_VACIA;      // fondo de la barra
    private final int COLOR_TEXTO;      // etiquetas
    private final int COLOR_LINEA;      // líneas guía

    private String[] etiquetas = new String[0];
    private int[] valores = new int[0];

    // Índice de la barra que se pinta de otro color (por ejemplo "Hoy"). -1 = ninguna.
    private int indiceDestacado = -1;

    // Valor máximo de la escala. Si es 0 se calcula con el valor más alto.
    private int maximoFijo = 0;

    private final Paint pincelBarra = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelTexto = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelValor = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelLinea = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final float densidad;

    public GraficaBarrasView(Context context) {
        this(context, null);
    }

    public GraficaBarrasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        densidad = context.getResources().getDisplayMetrics().density;

        COLOR_BARRA = ContextCompat.getColor(context, R.color.nivel_naranja);
        COLOR_DESTACADA = ContextCompat.getColor(context, R.color.buti_titulo);
        COLOR_VACIA = ContextCompat.getColor(context, R.color.buti_superficie_alt);
        COLOR_TEXTO = ContextCompat.getColor(context, R.color.buti_texto_secundario);
        COLOR_LINEA = ContextCompat.getColor(context, R.color.buti_borde);

        pincelTexto.setColor(COLOR_TEXTO);
        pincelTexto.setTextSize(11 * densidad);
        pincelTexto.setTextAlign(Paint.Align.CENTER);

        pincelValor.setColor(ContextCompat.getColor(context, R.color.buti_texto));
        pincelValor.setTextSize(11 * densidad);
        pincelValor.setTextAlign(Paint.Align.CENTER);
        pincelValor.setFakeBoldText(true);

        pincelLinea.setColor(COLOR_LINEA);
        pincelLinea.setStrokeWidth(1 * densidad);
        pincelLinea.setPathEffect(new DashPathEffect(
                new float[]{4 * densidad, 4 * densidad}, 0));
    }

    public void setDatos(String[] etiquetas, int[] valores,
                         int indiceDestacado, int maximoFijo) {

        this.etiquetas = etiquetas;
        this.valores = valores;
        this.indiceDestacado = indiceDestacado;
        this.maximoFijo = maximoFijo;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int n = valores.length;
        if (n == 0) return;

        float ancho = getWidth();
        float alto = getHeight();

        float espacioEtiqueta = 20 * densidad;
        float espacioValor = 16 * densidad;

        float base = alto - espacioEtiqueta;
        float altoUtil = base - espacioValor;

        int maximo = maximoFijo;
        if (maximo <= 0) {
            for (int v : valores) maximo = Math.max(maximo, v);
        }
        if (maximo <= 0) maximo = 1;

        // Líneas guía (mitad y tope).
        canvas.drawLine(0, base - altoUtil, ancho, base - altoUtil, pincelLinea);
        canvas.drawLine(0, base - altoUtil / 2f, ancho, base - altoUtil / 2f, pincelLinea);

        float columna = ancho / n;
        float anchoBarra = Math.min(columna * 0.6f, 34 * densidad);
        float radio = 6 * densidad;

        for (int i = 0; i < n; i++) {

            float centro = columna * i + columna / 2f;
            float izquierda = centro - anchoBarra / 2f;
            float derecha = centro + anchoBarra / 2f;

            // Fondo de la barra (lo que falta para llegar al máximo).
            pincelBarra.setColor(COLOR_VACIA);
            canvas.drawRoundRect(
                    new RectF(izquierda, base - altoUtil, derecha, base),
                    radio, radio, pincelBarra);

            // Barra con el valor.
            int valor = valores[i];
            if (valor > 0) {
                float altoBarra = altoUtil * Math.min(1f, valor / (float) maximo);
                altoBarra = Math.max(altoBarra, radio * 2);

                pincelBarra.setColor(i == indiceDestacado ? COLOR_DESTACADA : COLOR_BARRA);
                canvas.drawRoundRect(
                        new RectF(izquierda, base - altoBarra, derecha, base),
                        radio, radio, pincelBarra);

                canvas.drawText(String.valueOf(valor), centro,
                        base - altoBarra - 4 * densidad, pincelValor);
            }

            // Etiqueta debajo.
            if (i < etiquetas.length) {
                pincelTexto.setFakeBoldText(i == indiceDestacado);
                canvas.drawText(etiquetas[i], centro,
                        alto - 5 * densidad, pincelTexto);
            }
        }
    }
}