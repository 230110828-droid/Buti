package com.itsoeh.sbsc.buti;   // <- cambia al paquete donde lo pongas

import com.itsoeh.sbsc.buti.R;

/** Devuelve el dibujo del maguey segun los dias de racha. */
public class RachaMaguey {

    /** Dibujo según el estado: congelado, seco o el que toca por los días. */
    public static int dibujo(com.itsoeh.sbsc.buti.ProgresoButi.EstadoRacha racha) {
        switch (racha.estado) {
            case CONGELADA:
                return R.drawable.ic_maguey_congelado;
            case PERDIDA:
                return racha.diasPerdidos > 0 ? R.drawable.ic_maguey_seco : dibujoPorRacha(0);
            default:
                return dibujoPorRacha(racha.dias);
        }
    }

    public static int dibujoPorRacha(int racha) {
        if (racha >= 30) return R.drawable.ic_maguey;          // florecido (ya lo tienes)
        if (racha >= 14) return R.drawable.ic_maguey2;         // grande   (ya lo tienes)
        if (racha >= 7)  return R.drawable.ic_maguey_mediano;  // nuevo
        if (racha >= 3)  return R.drawable.ic_maguey_chico;    // nuevo
        return R.drawable.ic_maguey_brote;                     // nuevo
    }
}

/* USO en tu fragment de progreso, donde ya pones la racha:
 *
 *   ImageView img = view.findViewById(R.id.imgRachaMaguey);
 *   img.setImageResource(RachaMaguey.dibujoPorRacha(racha));
 */