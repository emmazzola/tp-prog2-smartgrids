package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * Registro de monitoreo de una planta eolica: agrega la velocidad promedio del viento.
 *
 * IREP: el de RegistroMonitoreo && velocidadVientoPromedio >= 0
 */
public class RegistroEolica extends RegistroMonitoreo {

    private double velocidadVientoPromedio;

    public RegistroEolica(String nombrePlanta, LocalDateTime fechaHora, double energiaGenerada,
            int incidencias, double velocidadVientoPromedio) {
        super(nombrePlanta, fechaHora, energiaGenerada, incidencias);
        if (velocidadVientoPromedio < 0) {
            throw new IllegalArgumentException("La velocidad del viento no puede ser negativa");
        }
        this.velocidadVientoPromedio = velocidadVientoPromedio;
    }

    public double getVelocidadVientoPromedio() {
        return velocidadVientoPromedio;
    }

    @Override
    public String toString() {
        return super.toString() + " | viento: " + velocidadVientoPromedio + " m/s";
    }
}
