package saludar.servicio;

import java.util.ArrayList;
import java.util.List;
import saludar.modelo.Diagnostico;

/** Catalogo CIE-10 en memoria para el prototipo (busqueda por codigo o texto). */
public class CatalogoCie10 {

    /** Entrada del catalogo (codigo + descripcion). */
    public static class Entrada {
        public final String codigo;
        public final String descripcion;
        public Entrada(String codigo, String descripcion) {
            this.codigo = codigo; this.descripcion = descripcion;
        }
        @Override public String toString() { return codigo + " - " + descripcion; }
    }

    private final List<Entrada> entradas = new ArrayList<>();

    public CatalogoCie10() {
        entradas.add(new Entrada("R51", "Cefalea"));
        entradas.add(new Entrada("J02.9", "Faringitis aguda, no especificada"));
        entradas.add(new Entrada("J00", "Rinofaringitis aguda (resfriado comun)"));
        entradas.add(new Entrada("Z00.0", "Examen medico general"));
        entradas.add(new Entrada("G43.9", "Migraña, no especificada"));
        entradas.add(new Entrada("I10", "Hipertension esencial (primaria)"));
        entradas.add(new Entrada("E11.9", "Diabetes mellitus tipo 2, sin complicaciones"));
        entradas.add(new Entrada("M54.5", "Lumbago no especificado"));
        entradas.add(new Entrada("K29.7", "Gastritis, no especificada"));
        entradas.add(new Entrada("J20.9", "Bronquitis aguda, no especificada"));
    }

    /** Devuelve todas las entradas (para poblar el combo). */
    public List<Entrada> todas() { return new ArrayList<>(entradas); }

    /** Busqueda por codigo o por texto de la descripcion. */
    public List<Entrada> buscar(String criterio) {
        List<Entrada> r = new ArrayList<>();
        String c = criterio == null ? "" : criterio.trim().toLowerCase();
        for (Entrada e : entradas) {
            if (c.isEmpty() || e.codigo.toLowerCase().contains(c)
                    || e.descripcion.toLowerCase().contains(c)) {
                r.add(e);
            }
        }
        return r;
    }

    public Diagnostico aDiagnostico(Entrada e, boolean principal) {
        return new Diagnostico(e.codigo, e.descripcion, principal);
    }
}
