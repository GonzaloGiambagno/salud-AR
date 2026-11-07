package saludar.util;

import saludar.modelo.Paciente;


public final class Ordenamiento {

    private Ordenamiento() { }

    /** Ordena por apellido (y nombre) con el metodo de INSERCION. */
    public static void ordenarPorApellido(Paciente[] v) {
        for (int i = 1; i < v.length; i++) {
            Paciente actual = v[i];
            int j = i - 1;
            while (j >= 0 && comparaPorApellido(v[j], actual) > 0) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = actual;
        }
    }

    /** Ordena por DNI (numerico) con el metodo de SELECCION. */
    public static void ordenarPorDni(Paciente[] v) {
        for (int i = 0; i < v.length - 1; i++) {
            int min = i;
            for (int j = i + 1; j < v.length; j++) {
                if (dni(v[j]) < dni(v[min])) {
                    min = j;
                }
            }
            if (min != i) {
                Paciente tmp = v[i];
                v[i] = v[min];
                v[min] = tmp;
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
