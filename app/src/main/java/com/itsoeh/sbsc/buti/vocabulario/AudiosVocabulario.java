package com.itsoeh.sbsc.buti.vocabulario;

import android.content.Context;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class AudiosVocabulario {

    private AudiosVocabulario() {
    }

    // Nombres exactos de los archivos, sin la extensión .mp3.

    private static final String[] COLORES = {
            "colores_01_blanco",
            "colores_02_rojo",
            "colores_03_negro",
            "colores_04_verde",
            "colores_05_gris",
            "colores_06_amarillo",
            "colores_07_azul"
    };

    private static final String[] NUMEROS = {
            "numeros_01_1",
            "numeros_02_2",
            "numeros_03_3",
            "numeros_04_4",
            "numeros_05_5",
            "numeros_06_6",
            "numeros_07_7",
            "numeros_08_8",
            "numeros_09_9",
            "numeros_10_10",
            "numeros_11_20",
            "numeros_12_40",
            "numeros_13_60",
            "numeros_14_80",
            "numeros_15_100",
            "numeros_16_1000"
    };

    private static final String[] SALUDOS = {
            "frases_01_buenosdias",
            "frases_02_buenastardes",
            "frases_03_comoestas",
            "frases_04_mellamo",
            "frases_05_gracias",
            "frases_06_bievenidos_as",
            "frases_07_venacomer",
            "frases_08_venatomaragua",
            "frases_09_quierounaayuda",
            "frases_10_estoytriste_estoypreocupado",
            "frases_11_ayudame",
            "frases_12_medapermisopasar",
            "frases_13_nosvemosmanana",
            "frases_14_hastaluego_hastapronto",
            "frases_15_veadormir",
            "frases_16_veacomer",
            "frases_17_hablashnahnu",
            "frases_18_dondeestas_dondeandas",
            "frases_19_dedondeeres",
            "frases_20_cuantosanostienes",
            "frases_21_hastamanana",
            "frases_22_no",
            "frases_23_nose",
            "frases_24_comotellamas",
            "frases_25_esperame",
            "frases_26_tengosed",
            "frases_27_tengofrio",
            "frases_28_dimelootravez"
    };

    private static final String[] TIEMPO = {
            "tiempo_01_hoyahora",
            "tiempo_02_ayer",
            "tiempo_03_anteayer",
            "tiempo_04_anoche",
            "tiempo_05_haceano",
            "tiempo_06_enlamanana",
            "tiempo_07_manana",
            "tiempo_08_mananatemprano",
            "tiempo_09_mananaporlamanana",
            "tiempo_10_mananaporlanoche",
            "tiempo_11_pasadomanana"
    };

    private static final String[] PLANTAS = {
            "plantas_01_maguey",
            "plantas_02_nopal",
            "plantas_03_mezquite",
            "plantas_04_xoconostle",
            "plantas_05_tuna",
            "plantas_06_organocactus",
            "plantas_07_biznaga",
            "plantas_08_flordemaguey",
            "plantas_09_flor",
            "plantas_10_hoja",
            "plantas_11_raiz",
            "plantas_12_semilla",
            "plantas_13_huizache",
            "plantas_14_maiz",
            "plantas_15_frijol",
            "plantas_16_jitomate",
            "plantas_17_calabaza",
            "plantas_18_haba",
            "plantas_19_aguacate",
            "plantas_20_chayote",
            "plantas_21_garambullo",
            "plantas_22_huitlacoche",
            "plantas_23_quelite"
    };

    private static final String[] ANIMALES = {
            "animales_01_perro",
            "animales_02_gato",
            "animales_03_gallina",
            "animales_04_pollo",
            "animales_05_guilos",
            "animales_06_guajolote",
            "animales_07_guajolota",
            "animales_08_borrego_oveja",
            "animales_09_chivo_cabra",
            "animales_10_burro",
            "animales_11_conejo",
            "animales_12_vibora_culebra",
            "animales_13_coyote",
            "animales_14_aguila",
            "animales_15_zopilote",
            "animales_16_alacran",
            "animales_17_avispa",
            "animales_18_correcaminos",
            "animales_19_ardilla",
            "animales_20_tlacuache",
            "animales_21_tejon",
            "animales_22_armadillo",
            "animales_23_zorro",
            "animales_24_buho",
            "animales_25_lechuza",
            "animales_26_puerco",
            "animales_27_raton",
            "animales_28_arana"
    };

    private static final String[] OBJETOS = {
            "objeto_01_puerta",
            "objeto_02_mesa",
            "objeto_03_silla",
            "objeto_04_dormitorio_recamara",
            "objeto_05_cuchara",
            "objeto_06_plato",
            "objeto_07_olla",
            "objeto_08_molcajete",
            "objeto_09_metate",
            "objeto_10_comal",
            "objeto_11_petate",
            "objeto_12_dinero",
            "objeto_13_llavepuerta",
            "objeto_14_ayate",
            "objeto_15_rebozo",
            "objeto_16_mecapal",
            "objeto_17_cuchillo",
            "objeto_18_chiquiuite",
            "objeto_19_canasta",
            "objeto_20_servilletadetortillas",
            "objeto_21_cobija"
    };

    private static final String[] CUERPO = {
            "cuerpo_01_cabeza",
            "cuerpo_02_cara",
            "cuerpo_03_ojos",
            "cuerpo_04_nariz",
            "cuerpo_05_boca",
            "cuerpo_06_oido",
            "cuerpo_07_cabello",
            "cuerpo_08_mano",
            "cuerpo_09_brazo",
            "cuerpo_10_pierna",
            "cuerpo_11_pie",
            "cuerpo_12_corazon",
            "cuerpo_13_diente",
            "cuerpo_14_estomago",
            "cuerpo_15_espalda",
            "cuerpo_16_pecho",
            "cuerpo_17_dedomano",
            "cuerpo_18_dedopie",
            "cuerpo_19_codo",
            "cuerpo_20_rodilla",
            "cuerpo_21_gluteonalga"
    };

    // Los audios de esta categoría utilizan el nombre "alimentos".
    private static final String[] COMIDA = {
            "alimentos_01_tamal",
            "alimentos_02_tortilla",
            "alimentos_03_atole",
            "alimentos_04_comida",
            "alimentos_05_pan",
            "alimentos_06_barbacoa",
            "alimentos_07_tequila",
            "alimentos_08_cerveza",
            "alimentos_09_elote",
            "alimentos_10_caldo",
            "alimentos_11_chicle"
    };

    private static final String[] NATURALEZA = {
            "naturaleza_01_mundo",
            "naturaleza_02_sol",
            "naturaleza_03_estrella",
            "naturaleza_04_lluvia",
            "naturaleza_05_fuego",
            "naturaleza_06_tierra",
            "naturaleza_07_viento",
            "naturaleza_08_luna",
            "naturaleza_09_agua",
            "naturaleza_10_nube",
            "naturaleza_11_rio",
            "naturaleza_12_manantial",
            "naturaleza_13_barranca",
            "naturaleza_14_pena",
            "naturaleza_15_cerro",
            "naturaleza_16_piedra",
            "naturaleza_17_roca",
            "naturaleza_18_arena",
            "naturaleza_19_tepetate"
    };

    private static final String[] LUGARES = {
            "lugares_01_iglesia",
            "lugares_02_carcel",
            "lugares_03_puente",
            "lugares_04_camino",
            "lugares_05_carretera",
            "lugares_06_casa",
            "lugares_07_mercado",
            "lugares_08_balneario",
            "lugares_09_pueblociudad",
            "lugares_10_escuela",
            "lugares_11_delegacion"
    };


    // Obtiene todas las pronunciaciones de una palabra.
    // Si tiene una variante terminada en 2, también la devuelve.

    public static int[] obtenerAudios(
            Context context,
            String categoria,
            String palabra
    ) {

        if (context == null || categoria == null || palabra == null) {
            return new int[0];
        }

        List<String> nombres;

        // 1) Si la palabra ya está en Firebase, se usan los audios guardados ahí.
        PalabraButi enNube = VocabularioNube.buscar(categoria, palabra);

        if (enNube != null) {

            nombres = new ArrayList<>();

            if (!enNube.audio.isEmpty()) nombres.add(enNube.audio);
            if (!enNube.audio2.isEmpty()) nombres.add(enNube.audio2);

        } else {

            // 2) Si no, se buscan con la lista original de la app.
            nombres = nombresPorDefecto(categoria, palabra);
        }

        List<Integer> resultados = new ArrayList<>();

        for (String nombre : nombres) {
            agregarAudio(context, resultados, nombre);
        }

        int[] audios = new int[resultados.size()];

        for (int i = 0; i < resultados.size(); i++) {
            audios[i] = resultados.get(i);
        }

        return audios;
    }

    /**
     * Nombres de los audios que trae la app para esa palabra y que sí existen
     * en res/raw. Lo usa el Admin al importar el vocabulario a Firebase.
     */
    public static List<String> nombresDeLaApp(
            Context context,
            String categoria,
            String palabra
    ) {

        List<String> existentes = new ArrayList<>();

        if (context == null) return existentes;

        for (String nombre : nombresPorDefecto(categoria, palabra)) {

            List<Integer> prueba = new ArrayList<>();
            agregarAudio(context, prueba, nombre);

            if (!prueba.isEmpty()) existentes.add(nombre.toLowerCase(Locale.ROOT));
        }

        return existentes;
    }

    // Busca los nombres de los audios con la lista original (por posición).
    private static List<String> nombresPorDefecto(
            String categoria,
            String palabra
    ) {

        List<String> nombres = new ArrayList<>();

        if (categoria == null || palabra == null) {
            return nombres;
        }

        String cat = normalizar(categoria);

        String[][] datos;
        String[] archivos;

        switch (cat) {

            case "colores":
                datos = VocabularioDatos.COLORES;
                archivos = COLORES;
                break;

            case "numeros":
                datos = VocabularioDatos.NUMEROS;
                archivos = NUMEROS;
                break;

            case "saludos":
                datos = VocabularioDatos.SALUDOS;
                archivos = SALUDOS;
                break;

            case "tiempo":
                datos = VocabularioDatos.TIEMPO;
                archivos = TIEMPO;
                break;

            case "plantas":
                datos = VocabularioDatos.PLANTAS;
                archivos = PLANTAS;
                break;

            case "animales":
                datos = VocabularioDatos.ANIMALES;
                archivos = ANIMALES;
                break;

            case "objetos":
                datos = VocabularioDatos.OBJETOS;
                archivos = OBJETOS;
                break;

            case "cuerpo":
                datos = VocabularioDatos.CUERPO;
                archivos = CUERPO;
                break;

            // Acepta tanto "Comida" como "Alimentos".
            case "comida":
            case "alimentos":
                datos = VocabularioDatos.COMIDA;
                archivos = COMIDA;
                break;

            case "naturaleza":
                datos = VocabularioDatos.NATURALEZA;
                archivos = NATURALEZA;
                break;

            case "lugares":
                datos = VocabularioDatos.LUGARES;
                archivos = LUGARES;
                break;

            default:
                return nombres;
        }

        int indice = -1;

        // Buscar la palabra dentro de los datos de vocabulario.
        for (int i = 0; i < datos.length && i < archivos.length; i++) {

            if (datos[i] != null
                    && datos[i].length > 0
                    && datos[i][0] != null
                    && normalizar(datos[i][0]).equals(normalizar(palabra))) {

                indice = i;
                break;
            }
        }

        if (indice == -1) {
            return nombres;
        }

        // Audio principal.
        nombres.add(archivos[indice]);

        // Pronunciaciones alternativas.
        if (cat.equals("animales")
                && (indice == 6 || indice == 17 || indice == 23)) {

            nombres.add(archivos[indice] + "2");
        }

        if (cat.equals("colores") && indice == 2) {
            nombres.add(archivos[indice] + "2");
        }

        if (cat.equals("objetos") && indice == 14) {
            nombres.add(archivos[indice] + "2");
        }

        return nombres;
    }


    // Obtiene solamente la primera pronunciación.

    public static int obtenerAudio(
            Context context,
            String categoria,
            String palabra
    ) {

        int[] audios = obtenerAudios(context, categoria, palabra);

        if (audios.length > 0) {
            return audios[0];
        }

        return 0;
    }


    // Busca el audio dentro de res/raw.

    private static void agregarAudio(
            Context context,
            List<Integer> resultados,
            String nombre
    ) {

        if (context == null || nombre == null) {
            return;
        }

        int id = context.getResources().getIdentifier(
                nombre.toLowerCase(Locale.ROOT),
                "raw",
                context.getPackageName()
        );

        if (id != 0) {
            resultados.add(id);
        }
    }


    // Normaliza palabras para comparar sin problemas de acentos,
    // mayúsculas, espacios o caracteres especiales.

    private static String normalizar(String texto) {

        if (texto == null) {
            return "";
        }

        String resultado = texto.toLowerCase(Locale.ROOT).trim();

        resultado = Normalizer.normalize(
                resultado,
                Normalizer.Form.NFD
        );

        // Eliminar acentos.
        resultado = resultado.replaceAll("\\p{M}", "");

        // Normalizar la letra ñ.
        resultado = resultado.replace("ñ", "n");

        // Cambiar espacios y caracteres especiales por guiones bajos.
        resultado = resultado.replaceAll("[^a-z0-9]+", "_");

        // Eliminar guiones bajos al principio y al final.
        return resultado.replaceAll("^_+|_+$", "");
    }
}