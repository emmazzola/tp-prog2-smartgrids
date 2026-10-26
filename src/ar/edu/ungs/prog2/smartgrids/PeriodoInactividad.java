package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Intervalo [desde, hasta) en el que una planta estuvo inactiva.
 * Mientras la planta sigue inactiva, el periodo esta abierto (hasta == null).
 *
 * IREP: desde != null && (hasta == null || desde < hasta)
 */
public class PeriodoInactividad {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private LocalDateTime desde;
    private LocalDateTime hasta;

    public PeriodoInactividad(LocalDateTime desde) {
        if (desde == null) {
            throw new IllegalArgumentException("El periodo de inactividad necesita una fecha de inicio");
        }
        this.desde = desde;
        this.hasta = null;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public boolean estaAbierto() {
        return hasta == null;
    }

    /** Cierra el periodo cuando la planta vuelve a activarse. */
    public void cerrar(LocalDateTime hasta) {
        if (!estaAbierto()) {
            throw new IllegalStateException("El periodo de inactividad ya esta cerrado");
        }
        if (hasta == null || !hasta.isAfter(desde)) {
            throw new IllegalArgumentException("La reactivacion debe ser posterior a " + desde);
        }
        this.hasta = hasta;
    }

    /** Indica si el momento dado cae dentro del periodo. */
    public boolean contiene(LocalDateTime momento) {
        return !momento.isBefore(desde) && (hasta == null || momento.isBefore(hasta));
    }

    @Override
    public String toString() {
        return desde.format(FORMATO) + " -> " + (hasta == null ? "(sigue inactiva)" : hasta.format(FORMATO));
    }
}
