package com.itsoeh.sbsc.buti.juegos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.card.MaterialCardView;
import com.itsoeh.sbsc.buti.R;

import java.util.ArrayList;
import java.util.List;

public class DiferenciasFragment extends Fragment {

    // Tamaño original de las imágenes (1254 x 1254 px).
    // Las zonas se escriben en píxeles de la imagen original
    // y se convierten a proporción (0 a 1) al usarlas.
    private static final float BASE = 1254f;

    private static final int TOTAL_DIFERENCIAS = 5;

    // ===== Escenarios =====

    private final int[] imagenesOriginales = {
            R.drawable.tapiz_bordado_panales_valle_agricola,
            R.drawable.nith_iglesia_bordado_floral,
            R.drawable.cerro_mirador_valle_punto_de_cruz
    };

    private final int[] imagenesCambios = {
            R.drawable.tapiz_bordado_panales_valle_agricola_dif,
            R.drawable.nith_iglesia_bordado_floral_dif,
            R.drawable.cerro_mirador_valle_punto_de_cruz_dif
    };

    private final String[] nombres = {
            "Valle de Panales",
            "Iglesia de Nith",
            "Mirador de El Dexthi"
    };

    // Nombre de cada diferencia (se muestra al encontrarla).
    private final String[][] nombresDiferencias = {
            {"El quiote del maguey", "La mariposa azul", "La cerca del camino",
                    "La casita que desapareció", "Las tunas del nopal"},
            {"El pájaro verde", "El rombo rojo", "La ventana de la fachada",
                    "La campana de la torre", "La cruz de la fachada"},
            {"El colibrí verde", "La florecita turquesa", "La nube que desapareció",
                    "La flor morada", "El rombo rojo"}
    };

    // Zonas: {izquierda, arriba, derecha, abajo} en píxeles de la imagen.
    private final float[][][] zonas = {
            { // Valle de Panales
                    {125, 480, 295, 905},
                    {1025, 307, 1156, 423},
                    {1002, 685, 1215, 840},
                    {363, 695, 477, 771},
                    {418, 843, 565, 980}
            },
            { // Iglesia de Nith
                    {956, 9, 1174, 151},
                    {198, 1039, 404, 1250},
                    {567, 478, 685, 618},
                    {363, 366, 451, 487},
                    {594, 344, 665, 413}
            },
            { // Mirador de El Dexthi
                    {55, 15, 300, 190},
                    {388, 148, 459, 219},
                    {507, 370, 783, 431},
                    {205, 960, 310, 1070},
                    {845, 1075, 1010, 1245}
            }
    };

    // ===== Vistas =====

    private LinearLayout layoutSeleccion;
    private LinearLayout layoutJuego;
    private LinearLayout contenedorEscenarios;

    private FrameLayout frameOriginal;
    private FrameLayout frameCambios;

    private ImageView imgOriginal;
    private ImageView imgCambios;

    private MarcasView marcasOriginal;
    private MarcasView marcasCambios;

    private TextView txtNombre;
    private TextView txtEncontradas;
    private TextView txtErrores;

    // ===== Estado del juego =====

    private int escenario = 0;
    private boolean[] encontradas = new boolean[TOTAL_DIFERENCIAS];
    private int errores = 0;
    private int pistas = 0;
    private boolean juegoTerminado = false;

    private Bitmap bitmapOriginal;
    private Bitmap bitmapCambios;

    private float toqueX;
    private float toqueY;

    public DiferenciasFragment() {
        super(R.layout.fragment_diferencias);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        layoutSeleccion = view.findViewById(R.id.layoutSeleccionDiferencias);
        layoutJuego = view.findViewById(R.id.layoutJuegoDiferencias);
        contenedorEscenarios = view.findViewById(R.id.contenedorEscenariosDiferencias);

        frameOriginal = view.findViewById(R.id.frameOriginalDiferencias);
        frameCambios = view.findViewById(R.id.frameCambiosDiferencias);

        imgOriginal = view.findViewById(R.id.imgOriginalDiferencias);
        imgCambios = view.findViewById(R.id.imgCambiosDiferencias);

        txtNombre = view.findViewById(R.id.txtNombreDiferencias);
        txtEncontradas = view.findViewById(R.id.txtEncontradasDiferencias);
        txtErrores = view.findViewById(R.id.txtErroresDiferencias);

        Button btnPista = view.findViewById(R.id.btnPistaDiferencias);
        Button btnReiniciar = view.findViewById(R.id.btnReiniciarDiferencias);
        Button btnCambiar = view.findViewById(R.id.btnCambiarEscenarioDiferencias);

        view.findViewById(R.id.btnVolverDiferencias)
                .setOnClickListener(v ->
                        NavHostFragment.findNavController(this)
                                .popBackStack());

        // Capa transparente encima de cada imagen para dibujar los círculos.
        marcasOriginal = new MarcasView(requireContext());
        marcasCambios = new MarcasView(requireContext());

        frameOriginal.addView(marcasOriginal, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        frameCambios.addView(marcasCambios, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        configurarToque(frameOriginal);
        configurarToque(frameCambios);

        btnPista.setOnClickListener(v -> darPista());
        btnReiniciar.setOnClickListener(v -> iniciarJuego());
        btnCambiar.setOnClickListener(v -> mostrarSeleccion());

        ajustarTamanoImagenes();
        mostrarEscenarios();
    }

    // ===== Selección de escenario =====

    private void mostrarEscenarios() {

        contenedorEscenarios.removeAllViews();

        for (int i = 0; i < imagenesOriginales.length; i++) {

            final int posicion = i;

            MaterialCardView tarjeta = new MaterialCardView(requireContext());

            LinearLayout.LayoutParams parametros = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            parametros.setMargins(0, dp(7), 0, dp(7));

            tarjeta.setLayoutParams(parametros);
            tarjeta.setRadius(dp(16));
            tarjeta.setCardElevation(dp(4));
            tarjeta.setCardBackgroundColor(Color.rgb(255, 250, 240));
            tarjeta.setStrokeWidth(dp(1));
            tarjeta.setStrokeColor(Color.rgb(201, 122, 43));

            LinearLayout contenido = new LinearLayout(requireContext());
            contenido.setGravity(Gravity.CENTER_VERTICAL);
            contenido.setOrientation(LinearLayout.HORIZONTAL);
            contenido.setPadding(dp(10), dp(10), dp(10), dp(10));

            ImageView miniatura = new ImageView(requireContext());
            miniatura.setImageBitmap(cargarBitmap(imagenesOriginales[i], dp(95)));
            miniatura.setScaleType(ImageView.ScaleType.CENTER_CROP);
            miniatura.setLayoutParams(new LinearLayout.LayoutParams(dp(95), dp(95)));

            LinearLayout textos = new LinearLayout(requireContext());
            textos.setOrientation(LinearLayout.VERTICAL);
            textos.setPadding(dp(15), 0, 0, 0);

            TextView nombre = new TextView(requireContext());
            nombre.setText(nombres[i]);
            nombre.setTextColor(Color.rgb(79, 0, 11));
            nombre.setTextSize(15);
            nombre.setTypeface(null, 1);

            TextView detalle = new TextView(requireContext());
            detalle.setText("5 diferencias escondidas");
            detalle.setTextColor(Color.rgb(201, 122, 43));
            detalle.setTextSize(13);

            textos.addView(nombre);
            textos.addView(detalle);

            contenido.addView(miniatura);
            contenido.addView(textos);
            tarjeta.addView(contenido);

            tarjeta.setOnClickListener(v -> {
                escenario = posicion;
                iniciarJuego();
            });

            contenedorEscenarios.addView(tarjeta);
        }
    }

    // ===== Juego =====

    private void iniciarJuego() {

        liberarBitmaps();

        int lado = frameOriginal.getLayoutParams().width;

        bitmapOriginal = cargarBitmap(imagenesOriginales[escenario], lado);
        bitmapCambios = cargarBitmap(imagenesCambios[escenario], lado);

        if (bitmapOriginal == null || bitmapCambios == null) {
            Toast.makeText(requireContext(),
                    "No se pudo cargar la imagen",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        imgOriginal.setImageBitmap(bitmapOriginal);
        imgCambios.setImageBitmap(bitmapCambios);

        encontradas = new boolean[TOTAL_DIFERENCIAS];
        errores = 0;
        pistas = 0;
        juegoTerminado = false;

        marcasOriginal.limpiar();
        marcasCambios.limpiar();

        txtNombre.setText(nombres[escenario]);
        actualizarContadores();

        layoutSeleccion.setVisibility(View.GONE);
        layoutJuego.setVisibility(View.VISIBLE);
    }

    // Detecta un toque (no un desplazamiento) sobre la imagen.
    private void configurarToque(FrameLayout frame) {

        frame.setOnTouchListener((v, evento) -> {

            switch (evento.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    toqueX = evento.getX();
                    toqueY = evento.getY();
                    return true;

                case MotionEvent.ACTION_UP:
                    float dx = Math.abs(evento.getX() - toqueX);
                    float dy = Math.abs(evento.getY() - toqueY);

                    if (dx < dp(12) && dy < dp(12)) {
                        v.performClick();
                        revisarToque(
                                evento.getX() / v.getWidth(),
                                evento.getY() / v.getHeight());
                    }
                    return true;

                default:
                    return true;
            }
        });
    }

    // x y y van de 0 a 1 (proporción dentro de la imagen).
    private void revisarToque(float x, float y) {

        if (juegoTerminado) {
            return;
        }

        float[][] zonasEscenario = zonas[escenario];

        for (int i = 0; i < zonasEscenario.length; i++) {

            RectF zona = zonaConTolerancia(zonasEscenario[i]);

            if (zona.contains(x, y)) {

                if (encontradas[i]) {
                    Toast.makeText(requireContext(),
                            "Esa ya la encontraste 😉",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                encontradas[i] = true;

                RectF circulo = zonaNormalizada(zonasEscenario[i]);
                marcasOriginal.agregarEncontrada(circulo);
                marcasCambios.agregarEncontrada(circulo);

                Toast.makeText(requireContext(),
                        "¡Bien! " + nombresDiferencias[escenario][i],
                        Toast.LENGTH_SHORT).show();

                actualizarContadores();

                if (contarEncontradas() == TOTAL_DIFERENCIAS) {
                    juegoTerminado = true;
                    marcasOriginal.postDelayed(this::mostrarVictoria, 600);
                }
                return;
            }
        }

        // No había ninguna diferencia en ese lugar.
        errores++;
        actualizarContadores();
        marcasOriginal.mostrarFallo(x, y);
        marcasCambios.mostrarFallo(x, y);
    }

    private void darPista() {

        if (juegoTerminado) {
            return;
        }

        for (int i = 0; i < TOTAL_DIFERENCIAS; i++) {

            if (!encontradas[i]) {

                pistas++;

                RectF zona = zonaNormalizada(zonas[escenario][i]);
                marcasOriginal.mostrarPista(zona);
                marcasCambios.mostrarPista(zona);

                Toast.makeText(requireContext(),
                        "Observa la zona que parpadea ✨",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    private void mostrarVictoria() {

        if (!isAdded()) {
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("¡Tienes ojo de colibrí! 🎉")
                .setMessage(
                        "Encontraste las 5 diferencias.\n\n"
                                + "Lugar: " + nombres[escenario]
                                + "\nIntentos fallidos: " + errores
                                + "\nPistas usadas: " + pistas)
                .setCancelable(false)
                .setPositiveButton("Volver a jugar",
                        (dialog, which) -> iniciarJuego())
                .setNegativeButton("Elegir otro lugar",
                        (dialog, which) -> mostrarSeleccion())
                .show();
    }

    private void mostrarSeleccion() {

        layoutJuego.setVisibility(View.GONE);
        layoutSeleccion.setVisibility(View.VISIBLE);

        imgOriginal.setImageDrawable(null);
        imgCambios.setImageDrawable(null);
        liberarBitmaps();
    }

    private void actualizarContadores() {

        txtEncontradas.setText(
                "Encontradas: " + contarEncontradas() + " / " + TOTAL_DIFERENCIAS);
        txtErrores.setText("Intentos fallidos: " + errores);
    }

    private int contarEncontradas() {

        int total = 0;
        for (boolean e : encontradas) {
            if (e) total++;
        }
        return total;
    }

    // ===== Zonas =====

    private RectF zonaNormalizada(float[] z) {
        return new RectF(z[0] / BASE, z[1] / BASE, z[2] / BASE, z[3] / BASE);
    }

    // Agranda un poco la zona para que sea fácil de tocar con el dedo.
    // Las zonas muy pequeñas se agrandan hasta medir al menos 10 % de la imagen.
    private RectF zonaConTolerancia(float[] z) {

        RectF r = zonaNormalizada(z);

        float margenX = Math.max(0.03f, (0.10f - r.width()) / 2f);
        float margenY = Math.max(0.03f, (0.10f - r.height()) / 2f);

        r.inset(-margenX, -margenY);
        return r;
    }

    // ===== Imágenes =====

    // Hace que las dos imágenes quepan en pantalla sin tener que desplazarse mucho.
    private void ajustarTamanoImagenes() {

        int ancho = getResources().getDisplayMetrics().widthPixels;
        int alto = getResources().getDisplayMetrics().heightPixels;

        int porAncho = ancho - dp(36);
        int porAlto = (alto - dp(250)) / 2;

        int lado = Math.max(dp(220), Math.min(porAncho, porAlto));

        ViewGroup.LayoutParams p1 = frameOriginal.getLayoutParams();
        p1.width = lado;
        p1.height = lado;
        frameOriginal.setLayoutParams(p1);

        ViewGroup.LayoutParams p2 = frameCambios.getLayoutParams();
        p2.width = lado;
        p2.height = lado;
        frameCambios.setLayoutParams(p2);
    }

    // Carga la imagen reducida al tamaño necesario para no gastar memoria.
    private Bitmap cargarBitmap(int recurso, int ladoDeseado) {

        BitmapFactory.Options opciones = new BitmapFactory.Options();
        opciones.inJustDecodeBounds = true;
        opciones.inScaled = false;
        BitmapFactory.decodeResource(getResources(), recurso, opciones);

        int muestra = 1;
        while (opciones.outWidth / (muestra * 2) >= ladoDeseado) {
            muestra *= 2;
        }

        opciones.inJustDecodeBounds = false;
        opciones.inSampleSize = muestra;

        return BitmapFactory.decodeResource(getResources(), recurso, opciones);
    }

    private void liberarBitmaps() {

        if (bitmapOriginal != null && !bitmapOriginal.isRecycled()) {
            imgOriginal.setImageDrawable(null);
            bitmapOriginal.recycle();
        }
        if (bitmapCambios != null && !bitmapCambios.isRecycled()) {
            imgCambios.setImageDrawable(null);
            bitmapCambios.recycle();
        }
        bitmapOriginal = null;
        bitmapCambios = null;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onDestroyView() {
        liberarBitmaps();
        super.onDestroyView();
    }

    // =====================================================
    // Capa que dibuja los círculos encima de cada imagen.
    // =====================================================

    private static class MarcasView extends View {

        private final List<RectF> encontradas = new ArrayList<>();

        private RectF pista = null;
        private float falloX = -1;
        private float falloY = -1;

        private final Paint pincelBorde = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pincelInterior = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pincelPista = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint pincelFallo = new Paint(Paint.ANTI_ALIAS_FLAG);

        private final float densidad;

        MarcasView(Context context) {
            super(context);

            densidad = context.getResources().getDisplayMetrics().density;

            // Círculo de diferencia encontrada: borde vino con línea blanca interior.
            pincelBorde.setStyle(Paint.Style.STROKE);
            pincelBorde.setStrokeWidth(4 * densidad);
            pincelBorde.setColor(Color.rgb(79, 0, 11));

            pincelInterior.setStyle(Paint.Style.STROKE);
            pincelInterior.setStrokeWidth(1.5f * densidad);
            pincelInterior.setColor(Color.WHITE);

            // Pista: círculo dorado punteado.
            pincelPista.setStyle(Paint.Style.STROKE);
            pincelPista.setStrokeWidth(3.5f * densidad);
            pincelPista.setColor(Color.rgb(242, 169, 0));
            pincelPista.setPathEffect(new DashPathEffect(
                    new float[]{8 * densidad, 6 * densidad}, 0));

            // Toque equivocado: una X roja que desaparece.
            pincelFallo.setStyle(Paint.Style.STROKE);
            pincelFallo.setStrokeWidth(3.5f * densidad);
            pincelFallo.setStrokeCap(Paint.Cap.ROUND);
            pincelFallo.setColor(Color.rgb(200, 30, 45));
        }

        void limpiar() {
            encontradas.clear();
            pista = null;
            falloX = -1;
            invalidate();
        }

        void agregarEncontrada(RectF zona) {
            encontradas.add(zona);
            pista = null;
            invalidate();
        }

        void mostrarPista(RectF zona) {
            pista = zona;
            invalidate();

            // Parpadea y desaparece después de 2 segundos.
            animate().cancel();
            setAlpha(1f);
            animate().alpha(0.3f).setDuration(350).withEndAction(() ->
                    animate().alpha(1f).setDuration(350).withEndAction(() ->
                            animate().alpha(0.3f).setDuration(350).withEndAction(() ->
                                    animate().alpha(1f).setDuration(350).start()
                            ).start()
                    ).start()
            ).start();

            postDelayed(() -> {
                pista = null;
                invalidate();
            }, 2000);
        }

        void mostrarFallo(float x, float y) {
            falloX = x;
            falloY = y;
            invalidate();

            postDelayed(() -> {
                falloX = -1;
                invalidate();
            }, 600);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            float w = getWidth();
            float h = getHeight();
            float extra = 4 * densidad;

            for (RectF z : encontradas) {
                RectF r = new RectF(
                        z.left * w - extra, z.top * h - extra,
                        z.right * w + extra, z.bottom * h + extra);
                canvas.drawOval(r, pincelBorde);
                r.inset(3 * densidad, 3 * densidad);
                canvas.drawOval(r, pincelInterior);
            }

            if (pista != null) {
                RectF r = new RectF(
                        pista.left * w - extra * 2, pista.top * h - extra * 2,
                        pista.right * w + extra * 2, pista.bottom * h + extra * 2);
                canvas.drawOval(r, pincelPista);
            }

            if (falloX >= 0) {
                float cx = falloX * w;
                float cy = falloY * h;
                float t = 9 * densidad;
                canvas.drawLine(cx - t, cy - t, cx + t, cy + t, pincelFallo);
                canvas.drawLine(cx - t, cy + t, cx + t, cy - t, pincelFallo);
            }
        }
    }
}
