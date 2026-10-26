package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * Planta de energia renovable.
 *
 * IREP: el de Planta && fuentePrimaria no vacia && 0 < eficienciaPromedio <= 1
 */
public abstract class PlantaRenovable extends Planta {

    private String fuentePrimaria;
    private double eficienciaPromedio;

    public PlantaRenovable(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento);
        validarTexto(fuentePrimaria, "fuente primaria");
        validarEficiencia(eficienciaPromedio);
        this.fuentePrimaria = fuentePrimaria;
        this.eficienciaPromedio = eficienciaPromedio;
    }

    @Override
    public boolean esRenovable() {
        return true;
    }

    /** Cuota por defecto de las renovables: capacidad maxima x eficiencia. */
    @Override
    public double cuotaMinima() {
        return getCapacidadMaxima() * eficienciaPromedio;
    }

    @Override
    public boolean cumpleCuota(RegistroMonitoreo registro) {
        return registro.getEnergiaGenerada() >= cuotaMinima();
    }

    public String getFuentePrimaria() {
        return fuentePrimaria;
    }

    public double getEficienciaPromedio() {
        return eficienciaPromedio;
    }

    public void setEficienciaPromedio(double eficienciaPromedio) {
        validarEficiencia(eficienciaPromedio);
        this.eficienciaPromedio = eficienciaPromedio;
    }

    private static void validarEficiencia(double eficiencia) {
        if (!(eficiencia > 0 && eficiencia <= 1)) {
            throw new IllegalArgumentException("La eficiencia promedio debe estar entre 0 y 1");
        }
    }

    @Override
    public String datosEspecificos() {
        return "fuente: " + fuentePrimaria + " | eficiencia: " + eficienciaPromedio;
    }
}
