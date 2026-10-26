package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * Registro de monitoreo de una planta termica: agrega el CO2 emitido y el combustible usado.
 *
 * IREP: el de RegistroMonitoreo && co2Emitido >= 0 && combustibleUtilizado >= 0
 */
public class RegistroTermica extends RegistroMonitoreo {

    private double co2Emitido;
    private double combustibleUtilizado;

    public RegistroTermica(String nombrePlanta, LocalDateTime fechaHora, double energiaGenerada,
            int incidencias, double co2Emitido, double combustibleUtilizado) {
        super(nombrePlanta, fechaHora, energiaGenerada, incidencias);
        if (co2Emitido < 0 || combustibleUtilizado < 0) {
            throw new IllegalArgumentException("El CO2 y el combustible no pueden ser negativos");
        }
        this.co2Emitido = co2Emitido;
        this.combustibleUtilizado = combustibleUtilizado;
    }

    public double getCo2Emitido() {
        return co2Emitido;
    }

    public double getCombustibleUtilizado() {
        return combustibleUtilizado;
    }

    @Override
    public String toString() {
        return super.toString() + " | CO2: " + co2Emitido + " | combustible: " + combustibleUtilizado;
    }
}
