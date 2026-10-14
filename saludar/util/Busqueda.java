package saludar.util;

import java.util.List;
import saludar.modelo.Paciente;

/** Algoritmos de BUSQUEDA implementados a mano. */
public final class Busqueda {

    private Busqueda() { }

    /**
     * BUSQUEDA BINARIA por DNI. Precondicion: la lista debe estar ordenada
     * por DNI de forma ascendente (ver Ordenamiento.ordenarPorDni).
     */
    public static Paciente binariaPorDni(List<Paciente> lista, String dni) {
        long objetivo = Long.parseLong(dni);
        int inicio = 0, fin = lista.size() - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            long valor = Long.parseLong(lista.get(medio).getDni());
            if (valor == objetivo) {
                return lista.get(medio);
            } else if (valor < objetivo) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return null;   // no encontrado
    }
}
