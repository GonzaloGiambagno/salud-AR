package saludar.servicio;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Registro de auditoria modelado con una PILA (LIFO): el ultimo evento
 * registrado es el primero que se muestra. Estructura de datos: Deque usado
 * como pila (push/pop).
 */
public class Auditoria {

    private final Deque<String> pila = new ArrayDeque<>();
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    public void registrar(String evento) {
        pila.push("[" + LocalTime.now().format(HORA) + "] " + evento);
    }

    /** Devuelve los ultimos n eventos, del mas reciente al mas antiguo. */
    public Iterable<String> ultimos(int n) {
        Deque<String> salida = new ArrayDeque<>();
        int i = 0;
        for (String e : pila) {          // recorre de tope a base (LIFO)
            if (i++ >= n) break;
            salida.addLast(e);
        }
        return salida;
    }

    public boolean vacia() { return pila.isEmpty(); }
}
