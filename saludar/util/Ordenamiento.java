package saludar.util;

import java.util.List;
import saludar.modelo.Paciente;

/**
 * Algoritmos de ORDENACION implementados a mano (sin Collections.sort),
 * para evidenciar el manejo de estructuras de repeticion e indices.
 */
public final class Ordenamiento {

    private Ordenamiento() { }

    /** Ordena por apellido (y nombre) con el metodo de INSERCION. */
    public static void ordenarPorApellido(List<Paciente> lista) {
        for (int i = 1; i < lista.size(); i++) {
            Paciente actual = lista.get(i);
            int j = i - 1;
            while (j >= 0 && comparaPorApellido(lista.get(j), actual) > 0) {
                lista.set(j + 1, lista.get(j));
                j--;
            }
            lista.set(j + 1, actual);
        }
    }

    /** Ordena por DNI (numerico) con el metodo de SELECCION. */
    public static void ordenarPorDni(List<Paciente> lista) {
        for (int i = 0; i < lista.size() - 1; i++) {
            int min = i;
            for (int j = i + 1; j < lista.size(); j++) {
                if (dni(lista.get(j)) < dni(lista.get(min))) {
                    min = j;
                }
            }
            if (min != i) {
                Paciente tmp = lista.get(i);
                lista.set(i, lista.get(min));
                lista.set(min, tmp);
            }
        }
    }

    private static int comparaPorApellido(Paciente a, Paciente b) {
        int c = a.getApellido().compareToIgnoreCase(b.getApellido());
        return c != 0 ? c : a.getNombre().compareToIgnoreCase(b.getNombre());
    }

    private static long dni(Paciente p) {
        return Long.parseLong(p.getDni());
    }
}
