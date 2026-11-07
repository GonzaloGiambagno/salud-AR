package saludar.util;

import saludar.modelo.Paciente;

public final class Busqueda {

    private Busqueda() { }

    public static Paciente binariaPorDni(Paciente[] v, String dni) {
        long objetivo = Long.parseLong(dni);
        int inicio = 0, fin = v.length - 1;
        while (inicio <= fin) {
            int medio = (inicio + fin) / 2;
            long valor = Long.parseLong(v[medio].getDni());
            if (valor == objetivo) {
                return v[medio];
            } else if (valor < objetivo) {
                inicio = medio + 1;
            } else {
                fin = medio - 1;
            }
        }
        return null; 
    }
}
