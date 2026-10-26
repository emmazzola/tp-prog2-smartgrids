package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * Planta que consume combustible y tiene un objetivo de emisiones.
 *
 * IREP: el de Planta && tipoCombustible no vacio && objetivoEmisionesCo2PorHora > 0
 *       && capacidadAlmacenamientoCombustible > 0
 */
public abstract class PlantaNoRenovable extends Planta {

    private String tipoCombustible;
    private double objetivoEmisionesCo2PorHora;
    private double capacidadAlmacenamientoCombustible;

    public PlantaNoRenovable(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String tipoCombustible,
            double objetivoEmisionesCo2PorHora, double capacidadAlmacenamientoCombustible) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento);
        validarTexto(tipoCombustible, "tipo de combustible");
        validarPositivo(objetivoEmisionesCo2PorHora, "objetivo de emisiones de CO2");
        validarPositivo(capacidadAlmacenamientoCombustible, "capacidad de almacenamiento de combustible");
        this.tipoCombustible = tipoCombustible;
        this.objetivoEmisionesCo2PorHora = objetivoEmisionesCo2PorHora;
        this.capacidadAlmacenamientoCombustible = capacidadAlmacenamientoCombustible;
    }

    @Override
    public boolean esRenovable() {
        return false;
    }

    /** Las no renovables deben generar al menos la mitad de su capacidad maxima. */
    @Override
    public double cuotaMinima() {
        return getCapacidadMaxima() / 2;
    }

    public String getTipoCombustible() {
        return tipoCombustible;
    }

    public double getObjetivoEmisionesCo2PorHora() {
        return objetivoEmisionesCo2PorHora;
    }

    public void setObjetivoEmisionesCo2PorHora(double objetivoEmisionesCo2PorHora) {
        validarPositivo(objetivoEmisionesCo2PorHora, "objetivo de emisiones de CO2");
        this.objetivoEmisionesCo2PorHora = objetivoEmisionesCo2PorHora;
    }

    public double getCapacidadAlmacenamientoCombustible() {
        return capacidadAlmacenamientoCombustible;
    }

    @Override
    public String datosEspecificos() {
        return "combustible: " + tipoCombustible + " | objetivo CO2/h: " + objetivoEmisionesCo2PorHora
                + " | almacenamiento de combustible: " + capacidadAlmacenamientoCombustible;
    }
}
