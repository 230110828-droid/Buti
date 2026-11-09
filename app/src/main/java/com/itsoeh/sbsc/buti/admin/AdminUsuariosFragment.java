package com.itsoeh.sbsc.buti.admin;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.itsoeh.sbsc.buti.ProgresoButi;
import com.itsoeh.sbsc.buti.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Admin · Usuarios: lista real de estudiantes con su avance. */
public class AdminUsuariosFragment extends Fragment {

    private static final String[] ORDENES = {"Más puntos", "Mayor racha", "Más recientes", "Nombre"};

    private static final String[] NOMBRES_NIVELES = {
            "Saludos", "Números", "Colores", "Animales", "Plantas", "Objetos",
            "Cuerpo", "Alimentos", "Tiempo", "Naturaleza", "Lugares"
    };

    private final List<UsuarioAdmin> todos = new ArrayList<>();
    private UsuariosAdapter adapter;
    private EditText edtBuscar;
    private TextView txtCantidad;
    private TextView txtVacio;
    private RecyclerView lista;
    private int orden = 0;

    private DatabaseReference refUsuarios;
    private ValueEventListener escucha;

    public AdminUsuariosFragment() {
        super(R.layout.fragment_admin_usuarios);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        edtBuscar = view.findViewById(R.id.edtBuscarUsuarioAdmin);
        txtCantidad = view.findViewById(R.id.txtCantidadUsuariosAdmin);
        txtVacio = view.findViewById(R.id.txtVacioUsuariosAdmin);
        lista = view.findViewById(R.id.rvUsuariosAdmin);

        adapter = new UsuariosAdapter(this::mostrarDetalle);
        lista.setLayoutManager(new LinearLayoutManager(requireContext()));
        lista.setAdapter(adapter);

        crearChipsOrden(view.findViewById(R.id.chipsOrdenUsuariosAdmin));

        edtBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mostrarLista();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        refUsuarios = FirebaseDatabase.getInstance().getReference("usuarios");

        escucha = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getView() == null) return;
                todos.clear();
                todos.addAll(UsuarioAdmin.lista(snapshot));
                mostrarLista();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (isAdded() && txtCantidad != null) {
                    txtCantidad.setText("No se pudieron leer los usuarios: " + error.getMessage());
                }
            }
        };

        refUsuarios.addValueEventListener(escucha);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (refUsuarios != null && escucha != null) refUsuarios.removeEventListener(escucha);
    }

    private void crearChipsOrden(ChipGroup grupo) {

        for (int i = 0; i < ORDENES.length; i++) {

            final int indice = i;

            Chip chip = new Chip(requireContext());
            chip.setId(View.generateViewId());
            chip.setText(ORDENES[i]);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(false);
            chip.setChipStrokeWidth(2f);
            chip.setChipStrokeColor(ContextCompat.getColorStateList(requireContext(), R.color.buti_borde));
            pintarChip(chip, false);

            chip.setOnCheckedChangeListener((boton, marcado) -> {
                pintarChip((Chip) boton, marcado);
                if (marcado) {
                    orden = indice;
                    mostrarLista();
                }
            });

            grupo.addView(chip);

            if (i == orden) chip.setChecked(true);
        }
    }

    private void pintarChip(Chip chip, boolean marcado) {
        chip.setChipBackgroundColor(ContextCompat.getColorStateList(requireContext(),
                marcado ? R.color.buti_acento : R.color.buti_superficie_alt));
        chip.setTextColor(ContextCompat.getColor(requireContext(),
                marcado ? R.color.buti_sobre_acento : R.color.buti_titulo));
    }

    private void mostrarLista() {

        if (adapter == null) return;

        String busqueda = edtBuscar.getText().toString().trim();
        List<UsuarioAdmin> resultado = new ArrayList<>();

        for (UsuarioAdmin u : todos) {
            if (u.coincide(busqueda)) resultado.add(u);
        }

        switch (orden) {
            case 1:
                Collections.sort(resultado, (a, b) -> Integer.compare(b.racha, a.racha));
                break;
            case 2:
                Collections.sort(resultado, (a, b) -> b.ultimaFecha.compareTo(a.ultimaFecha));
                break;
            case 3:
                Collections.sort(resultado, (a, b) -> a.nombre.compareToIgnoreCase(b.nombre));
                break;
            default:
                Collections.sort(resultado, (a, b) -> Integer.compare(b.puntos, a.puntos));
                break;
        }

        adapter.mostrar(resultado);

        txtCantidad.setText(busqueda.isEmpty()
                ? todos.size() + (todos.size() == 1 ? " estudiante registrado" : " estudiantes registrados")
                : resultado.size() + " de " + todos.size() + " estudiantes");

        boolean vacio = resultado.isEmpty();
        lista.setVisibility(vacio ? View.GONE : View.VISIBLE);
        txtVacio.setVisibility(vacio ? View.VISIBLE : View.GONE);
        txtVacio.setText(todos.isEmpty()
                ? "Todavía no hay estudiantes registrados."
                : "No se encontró ningún estudiante con ese nombre o correo.");
    }

    // =========================================
    // DETALLE DE UN ESTUDIANTE
    // =========================================

    private void mostrarDetalle(UsuarioAdmin u) {

        Dialog dialogo = new Dialog(requireContext());
        dialogo.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialogo.setContentView(R.layout.dialog_usuario_admin);
        dialogo.setCancelable(true);

        u.ponerAvatar(dialogo.findViewById(R.id.imgFotoDetalleAdmin),
                dialogo.findViewById(R.id.txtInicialesDetalleAdmin));

        ((TextView) dialogo.findViewById(R.id.txtNombreDetalleAdmin)).setText(u.nombre);
        ((TextView) dialogo.findViewById(R.id.txtCorreoDetalleAdmin)).setText(u.correo);
        ((TextView) dialogo.findViewById(R.id.txtPuntosDetalleAdmin)).setText(u.puntos + "\npuntos");
        ((TextView) dialogo.findViewById(R.id.txtRachaDetalleAdmin)).setText(u.racha + "\nracha");
        ((TextView) dialogo.findViewById(R.id.txtMejorRachaDetalleAdmin)).setText(u.mejorRacha + "\nmejor racha");

        ((TextView) dialogo.findViewById(R.id.txtUltimoDiaDetalleAdmin)).setText(
                "Última vez que practicó: " + fechaLegible(u.ultimaFecha)
                        + "\nEtapas completadas: " + u.etapasCompletadas + " de "
                        + (UsuarioAdmin.NIVELES * UsuarioAdmin.ETAPAS)
                        + "\nVa en el nivel " + u.nivelActual + " (" + NOMBRES_NIVELES[u.nivelActual - 1] + ")");

        LinearLayout niveles = dialogo.findViewById(R.id.contenedorNivelesDetalleAdmin);

        for (int n = 0; n < UsuarioAdmin.NIVELES; n++) {
            niveles.addView(filaNivel(n, u.etapasPorNivel[n]));
        }

        dialogo.findViewById(R.id.btnCerrarDetalleAdmin).setOnClickListener(v -> dialogo.dismiss());

        dialogo.show();

        Window ventana = dialogo.getWindow();
        if (ventana != null) {
            ventana.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            ventana.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private View filaNivel(int indice, int etapas) {

        LinearLayout fila = new LinearLayout(requireContext());
        fila.setOrientation(LinearLayout.HORIZONTAL);
        fila.setPadding(0, dp(4), 0, dp(4));
        fila.setGravity(android.view.Gravity.CENTER_VERTICAL);

        TextView nombre = new TextView(requireContext());
        nombre.setText((indice + 1) + ". " + NOMBRES_NIVELES[indice]);
        nombre.setTextSize(13);
        nombre.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_texto));
        fila.addView(nombre, new LinearLayout.LayoutParams(dp(110), -2));

        LinearProgressIndicator barra = new LinearProgressIndicator(requireContext());
        barra.setMax(UsuarioAdmin.ETAPAS);
        barra.setProgressCompat(etapas, false);
        barra.setTrackThickness(dp(8));
        barra.setTrackCornerRadius(dp(4));
        barra.setIndicatorColor(ContextCompat.getColor(requireContext(),
                etapas == UsuarioAdmin.ETAPAS ? R.color.buti_exito : R.color.buti_acento));
        barra.setTrackColor(ContextCompat.getColor(requireContext(), R.color.buti_superficie_alt));
        fila.addView(barra, new LinearLayout.LayoutParams(0, -2, 1f));

        TextView cuenta = new TextView(requireContext());
        cuenta.setText(etapas + "/" + UsuarioAdmin.ETAPAS);
        cuenta.setTextSize(12);
        cuenta.setTypeface(cuenta.getTypeface(), Typeface.BOLD);
        cuenta.setTextColor(ContextCompat.getColor(requireContext(), R.color.buti_titulo));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-2, -2);
        lp.setMarginStart(dp(8));
        fila.addView(cuenta, lp);

        return fila;
    }

    private String fechaLegible(String fecha) {

        if (fecha == null || fecha.isEmpty()) return "todavía no ha practicado";
        if (fecha.equals(ProgresoButi.hoy())) return "hoy";
        if (fecha.equals(ProgresoButi.ayer())) return "ayer";

        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(fecha);
            return d == null ? fecha : new SimpleDateFormat("d 'de' MMMM yyyy", new Locale("es", "MX")).format(d);
        } catch (ParseException e) {
            return fecha;
        }
    }

    private int dp(int valor) {
        return Math.round(valor * getResources().getDisplayMetrics().density);
    }
}
