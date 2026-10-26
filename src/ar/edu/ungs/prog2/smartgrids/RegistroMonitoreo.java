package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * Datos de monitoreo de una planta durante una hora.
 *
 * IREP: nombrePlanta no vacio && fechaHora != null && fechaHora en hora exacta
 *       && energiaGenerada >= 0 && incidencias >= 0
 */
public class RegistroMonitoreo {

    private String nombrePlanta;
    private LocalDateTime fechaHora;
    private double energiaGenerada;
    private int incidencias;

    public RegistroMonitoreo(String nombrePlanta, LocalDateTime fechaHora, double energiaGenerada,
            int incidencias) {
        if (nombrePlanta == null || nombrePlanta.isBlank()) {
            throw new IllegalArgumentException("El registro debe indicar la planta");
        }
        if (fechaHora == null) {
            throw new IllegalArgumentException("Falta la fecha y hora del registro");
        }
        if (energiaGenerada < 0 || incidencias < 0) {
            throw new IllegalArgumentException("La energia y las incidencias no pueden ser negativas");
        }
        if (fechaHora.getMinute() != 0 || fechaHora.getSecond() != 0 || fechaHora.getNano() != 0) {
            throw new IllegalArgumentException("El registro debe ser de una hora exacta: " + fechaHora);
        }
        this.nombrePlanta = nombrePlanta;
        this.fechaHora = fechaHora;
        this.energiaGenerada = energiaGenerada;
        this.incidencias = incidencias;
    }

    public String getNombrePlanta() {
        return nombrePlanta;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public int getHora() {
        return fechaHora.getHour();
    }

    public double getEnergiaGenerada() {
        return energiaGenerada;
    }

    public int getIncidencias() {
        return incidencias;
    }

    /** Un registro sin datos no genero energia ni tuvo incidencias. */
    public boolean estaVacio() {
        return energiaGenerada == 0 && incidencias == 0;
    }

    @Override
    public String toString() {
        return fechaHora + " | energia: " + energiaGenerada + " | incidencias: " + incidencias;
    }
}
