package com.itsoeh.sbsc.buti.lecciones;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ButiAviso;
import com.itsoeh.sbsc.buti.ButiDialogo;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;
import com.itsoeh.sbsc.buti.admin.UsuarioAdmin;

/**
 * Camino de niveles.
 * Los niveles se desbloquean en orden: para abrir el Nivel N hay que
 * terminar las 5 etapas del Nivel N-1.
 *  · Completado  → palomita en la insignia.
 *  · Actual      → el círculo "late" para indicar dónde vas.
 *  · Bloqueado   → imagen en gris, candado y etiqueta apagada.
 */
public class LeccionesFragment extends Fragment {

    private static final int TOTAL_NIVELES = 11;
    private static final int TOTAL_ETAPAS = 5;
    private static final String TAG_CANDADO = "candadoNivel";

    private static final String[] TITULOS = {
            "Saludos", "Números", "Colores", "Animales", "Plantas", "Objetos",
            "Cuerpo", "Comida", "Tiempo y celebraciones", "Naturaleza", "Lugares"
    };

    private final MaterialCardView[] tarjetas = new MaterialCardView[TOTAL_NIVELES];
    private final ColorStateList[] bordesOriginales = new ColorStateList[TOTAL_NIVELES];
    private final ColorStateList[] insigniasOriginales = new ColorStateList[TOTAL_NIVELES];

    // Hasta qué nivel está abierto (1 = solo el primero).
    private int nivelDesbloqueado = 1;
    private boolean[] completados = new boolean[TOTAL_NIVELES];
    private boolean esAdmin = false;

    private ObjectAnimator latido;
    private NavController navController;

    public LeccionesFragment() {
        super(R.layout.fragment_lecciones);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        navController = Navigation.findNavController(view);

        int[] ids = {
                R.id.cardNivel1, R.id.cardNivel2, R.id.cardNivel3, R.id.cardNivel4,
                R.id.cardNivel5, R.id.cardNivel6, R.id.cardNivel7, R.id.cardNivel8,
                R.id.cardNivel9, R.id.cardNivel10, R.id.cardNivel11
        };

        for (int i = 0; i < TOTAL_NIVELES; i++) {

            tarjetas[i] = view.findViewById(ids[i]);

            if (tarjetas[i] == null) continue;

            bordesOriginales[i] = tarjetas[i].getStrokeColorStateList();

            TextView insignia = insigniaDe(tarjetas[i]);
            if (insignia != null) insigniasOriginales[i] = insignia.getBackgroundTintList();

            final int nivel = i + 1;
            tarjetas[i].setOnClickListener(v -> alTocarNivel(nivel));
        }

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        esAdmin = usuario != null && UsuarioAdmin.esAdmin(usuario.getEmail());

        // Primero se pinta con lo último que se sabía (para que no "parpadee")
        // y luego se actualiza con Firebase.
        nivelDesbloqueado = esAdmin ? TOTAL_NIVELES : leerNivelGuardado();
        pintarNiveles(-1);

        cargarProgreso(view.findViewById(R.id.txtPuntosLecciones));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        detenerLatido();
    }

    // =========================================================
    // PROGRESO DESDE FIREBASE
    // =========================================================

    private void cargarProgreso(TextView txtPuntos) {

        if (txtPuntos != null) txtPuntos.setText("0");

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();

        if (usuario == null) return;

        FirebaseDatabase.getInstance()
                .getReference("usuarios")
                .child(usuario.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (!isAdded() || getView() == null) return;

                        // Puntos totales (lecciones + práctica + juegos).
                        if (txtPuntos != null) {
                            txtPuntos.setText(String.valueOf(
                                    ProgresoButi.puntosTotales(snapshot)
                            ));
                        }

                        // ¿Qué niveles tienen sus 5 etapas completas?
                        DataSnapshot lecciones = snapshot.child("progresoLecciones");
                        completados = new boolean[TOTAL_NIVELES];

                        for (int n = 1; n <= TOTAL_NIVELES; n++) {

                            DataSnapshot nivel = lecciones.child("nivel" + n);
                            boolean todas = true;

                            for (int e = 1; e <= TOTAL_ETAPAS; e++) {
                                Boolean hecha = nivel.child("etapa" + e)
                                        .child("completada").getValue(Boolean.class);
                                if (!Boolean.TRUE.equals(hecha)) {
                                    todas = false;
                                    break;
                                }
                            }

                            completados[n - 1] = todas;
                        }

                        // Se abre el siguiente al último nivel terminado en orden.
                        int abierto = 1;
                        while (abierto < TOTAL_NIVELES && completados[abierto - 1]) {
                            abierto++;
                        }

                        if (esAdmin) abierto = TOTAL_NIVELES;

                        int anterior = leerNivelGuardado();
                        nivelDesbloqueado = abierto;
                        guardarNivel(abierto);

                        // Si se acaba de abrir un nivel nuevo, festejarlo.
                        int recienAbierto = (!esAdmin && abierto > anterior) ? abierto : -1;
                        pintarNiveles(recienAbierto);

                        if (recienAbierto > 0) {
                            ButiAviso.mostrar(
                                    LeccionesFragment.this,
                                    "¡Desbloqueaste el Nivel " + recienAbierto + " · "
                                            + TITULOS[recienAbierto - 1] + "!",
                                    ButiAviso.Tipo.EXITO
                            );
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
    }

    // =========================================================
    // ESTADOS DE CADA NIVEL
    // =========================================================

    private void pintarNiveles(int nivelRecienAbierto) {

        detenerLatido();

        int nivelActual = -1;

        for (int i = 0; i < TOTAL_NIVELES; i++) {

            MaterialCardView tarjeta = tarjetas[i];
            if (tarjeta == null) continue;

            int nivel = i + 1;
            boolean bloqueado = nivel > nivelDesbloqueado;
            boolean completado = completados[i];

            if (bloqueado) {
                ponerBloqueado(i);
            } else {
                ponerAbierto(i, completado);
                if (!completado && nivelActual == -1) nivelActual = nivel;
            }
        }

        // El nivel en el que vas "late" suavemente.
        if (nivelActual > 0) {
            iniciarLatido(tarjetas[nivelActual - 1]);
            if (nivelActual > 2 && nivelRecienAbierto < 0) desplazarHasta(tarjetas[nivelActual - 1]);
        }

        // Animación del candado que se abre.
        if (nivelRecienAbierto > 0 && tarjetas[nivelRecienAbierto - 1] != null) {
            MaterialCardView t = tarjetas[nivelRecienAbierto - 1];
            desplazarHasta(t);
            t.setScaleX(0.6f);
            t.setScaleY(0.6f);
            t.animate().scaleX(1f).scaleY(1f).setDuration(500).setStartDelay(250)
                    .setInterpolator(new OvershootInterpolator(3f)).start();
        }
    }

    private void ponerBloqueado(int i) {

        MaterialCardView tarjeta = tarjetas[i];
        Context c = requireContext();

        // Imagen en gris y apagada.
        ImageView imagen = imagenDe(tarjeta);
        if (imagen != null) {
            ColorMatrix gris = new ColorMatrix();
            gris.setSaturation(0f);
            imagen.setColorFilter(new ColorMatrixColorFilter(gris));
            imagen.setAlpha(0.35f);
        }

        tarjeta.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(c, R.color.buti_borde)));
        tarjeta.setCardElevation(dp(2));
        tarjeta.setContentDescription("Nivel " + (i + 1) + ", " + TITULOS[i] + ". Bloqueado");

        TextView insignia = insigniaDe(tarjeta);
        if (insignia != null) {
            insignia.setText(String.valueOf(i + 1));
            insignia.setBackgroundTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(c, R.color.buti_borde)));
        }

        ponerAlfaEtiqueta(tarjeta, 0.45f);
        ponerCandado(tarjeta, true);
    }

    private void ponerAbierto(int i, boolean completado) {

        MaterialCardView tarjeta = tarjetas[i];

        ImageView imagen = imagenDe(tarjeta);
        if (imagen != null) {
            imagen.clearColorFilter();
            imagen.setAlpha(1f);
        }

        if (bordesOriginales[i] != null) tarjeta.setStrokeColor(bordesOriginales[i]);
        tarjeta.setCardElevation(dp(8));
        tarjeta.setContentDescription("Nivel " + (i + 1) + ", " + TITULOS[i]
                + (completado ? ". Completado" : ". Disponible"));

        TextView insignia = insigniaDe(tarjeta);
        if (insignia != null) {
            insignia.setText(completado ? "✓" : String.valueOf(i + 1));
            if (insigniasOriginales[i] != null) insignia.setBackgroundTintList(insigniasOriginales[i]);
        }

        ponerAlfaEtiqueta(tarjeta, 1f);
        ponerCandado(tarjeta, false);
    }

    // Candado al centro del círculo (encima de la tarjeta).
    private void ponerCandado(MaterialCardView tarjeta, boolean mostrar) {

        if (!(tarjeta.getParent() instanceof FrameLayout)) return;

        FrameLayout marco = (FrameLayout) tarjeta.getParent();
        View existente = marco.findViewWithTag(TAG_CANDADO);

        if (!mostrar) {
            if (existente != null) marco.removeView(existente);
            return;
        }

        if (existente != null) return;

        Context c = requireContext();

        ImageView candado = new ImageView(c);
        candado.setTag(TAG_CANDADO);
        candado.setImageResource(recursoCandado());
        candado.setScaleType(ImageView.ScaleType.FIT_CENTER);
        candado.setPadding(dp(8), dp(8), dp(8), dp(8));
        candado.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);

        GradientDrawable fondo = new GradientDrawable();
        fondo.setShape(GradientDrawable.OVAL);
        fondo.setColor(ContextCompat.getColor(c, R.color.buti_superficie));
        fondo.setStroke(dp(2), ContextCompat.getColor(c, R.color.buti_borde));
        candado.setBackground(fondo);

        // Más elevación que la tarjeta para quedar encima.
        candado.setElevation(dp(12));

        FrameLayout.LayoutParams parametros = new FrameLayout.LayoutParams(dp(44), dp(44));
        parametros.gravity = Gravity.CENTER;
        candado.setLayoutParams(parametros);

        // Tocar el candado hace lo mismo que tocar el nivel.
        candado.setOnClickListener(v -> tarjeta.performClick());

        marco.addView(candado);
    }

    // Usa tu candado en punto de cruz si existe (ic_candado.png);
    // si no, el candado de las etapas.
    private int recursoCandado() {
        int propio = getResources().getIdentifier("ic_candado", "drawable", requireContext().getPackageName());
        return propio != 0 ? propio : R.drawable.ic_estado_bloqueada;
    }

    // =========================================================
    // AL TOCAR UN NIVEL
    // =========================================================

    private void alTocarNivel(int nivel) {

        if (!isAdded()) return;

        String titulo = TITULOS[nivel - 1];

        if (nivel > nivelDesbloqueado) {

            sacudir(tarjetas[nivel - 1]);

            new ButiDialogo(requireContext()).mostrarInfo(
                    "Nivel " + nivel + " bloqueado",
                    "Termina las 5 etapas del Nivel " + (nivel - 1) + " · "
                            + TITULOS[nivel - 2] + " para desbloquearlo.\n\n"
                            + "¡Paso a paso se aprende mejor!",
                    null
            );
            return;
        }

        new ButiDialogo(requireContext()).mostrarConfirmacion(
                "Nivel " + nivel + " · " + titulo,
                completados[nivel - 1]
                        ? "Ya completaste este nivel. Puedes repasarlo para mejorar tus puntos."
                        : "5 etapas de aprendizaje.\n\n"
                          + "Completa los desafíos para avanzar "
                          + "en tu camino hñähñu.",
                completados[nivel - 1] ? "Repasar" : "Empezar",
                "Ahora no",
                () -> abrirNivel(nivel, titulo),
                null
        );
    }

    private void abrirNivel(int nivel, String titulo) {

        Bundle argumentos = new Bundle();
        argumentos.putInt("numeroNivel", nivel);
        argumentos.putString("nivelSeleccionado", titulo);
        argumentos.putString("temaSeleccionado", titulo);

        if (navController.getCurrentDestination() != null
                && navController.getCurrentDestination().getId()
                == R.id.leccionesFragment) {

            navController.navigate(R.id.etapasLeccionFragment, argumentos);
        }
    }

    // =========================================================
    // ANIMACIONES
    // =========================================================

    private void iniciarLatido(View vista) {

        if (vista == null) return;

        latido = ObjectAnimator.ofPropertyValuesHolder(
                vista,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.07f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.07f)
        );
        latido.setDuration(750);
        latido.setRepeatCount(ValueAnimator.INFINITE);
        latido.setRepeatMode(ValueAnimator.REVERSE);
        latido.start();
    }

    private void detenerLatido() {
        if (latido != null) {
            latido.cancel();
            Object objetivo = latido.getTarget();
            if (objetivo instanceof View) {
                ((View) objetivo).setScaleX(1f);
                ((View) objetivo).setScaleY(1f);
            }
            latido = null;
        }
    }

    private void sacudir(View vista) {
        if (vista == null) return;
        ObjectAnimator.ofFloat(vista, View.TRANSLATION_X, 0, -14, 14, -9, 9, -4, 4, 0)
                .setDuration(420)
                .start();
    }

    // Lleva el scroll hasta el nivel indicado.
    private void desplazarHasta(View vista) {

        View raiz = getView();
        if (!(raiz instanceof ScrollView) || vista == null) return;

        ScrollView scroll = (ScrollView) raiz;

        scroll.post(() -> {
            int y = 0;
            View actual = vista;
            while (actual != null && actual != scroll) {
                y += actual.getTop();
                actual = actual.getParent() instanceof View ? (View) actual.getParent() : null;
            }
            scroll.smoothScrollTo(0, Math.max(0, y - scroll.getHeight() / 3));
        });
    }

    // =========================================================
    // AYUDANTES
    // =========================================================

    @Nullable
    private ImageView imagenDe(MaterialCardView tarjeta) {
        for (int i = 0; i < tarjeta.getChildCount(); i++) {
            if (tarjeta.getChildAt(i) instanceof ImageView) return (ImageView) tarjeta.getChildAt(i);
        }
        return null;
    }

    // La insignia con el número está junto a la tarjeta, dentro del mismo marco.
    @Nullable
    private TextView insigniaDe(MaterialCardView tarjeta) {
        if (!(tarjeta.getParent() instanceof ViewGroup)) return null;
        ViewGroup marco = (ViewGroup) tarjeta.getParent();
        for (int i = 0; i < marco.getChildCount(); i++) {
            if (marco.getChildAt(i) instanceof TextView) return (TextView) marco.getChildAt(i);
        }
        return null;
    }

    // La etiqueta "NIVEL N / Título" está en la misma fila que el marco.
    private void ponerAlfaEtiqueta(MaterialCardView tarjeta, float alfa) {
        if (!(tarjeta.getParent() instanceof View)) return;
        View marco = (View) tarjeta.getParent();
        if (!(marco.getParent() instanceof ViewGroup)) return;
        ViewGroup fila = (ViewGroup) marco.getParent();
        for (int i = 0; i < fila.getChildCount(); i++) {
            View hijo = fila.getChildAt(i);
            if (hijo != marco) hijo.setAlpha(alfa);
        }
    }

    private SharedPreferences preferencias() {
        return requireContext().getSharedPreferences("buti_prefs", Context.MODE_PRIVATE);
    }

    private String claveNivel() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return "nivel_desbloqueado_" + (u != null ? u.getUid() : "anonimo");
    }

    private int leerNivelGuardado() {
        return Math.max(1, Math.min(TOTAL_NIVELES, preferencias().getInt(claveNivel(), 1)));
    }

    private void guardarNivel(int nivel) {
        preferencias().edit().putInt(claveNivel(), nivel).apply();
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}