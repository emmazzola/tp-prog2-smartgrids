package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * IREP: el de PlantaRenovable && cantidadTurbinas > 0 && capacidadEmbalse > 0
 *       && ningun registro es eolico ni termico
 */
public class PlantaHidraulica extends PlantaRenovable {

    private int cantidadTurbinas;
    private double capacidadEmbalse;

    public PlantaHidraulica(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadTurbinas, double capacidadEmbalse) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento, fuentePrimaria,
                eficienciaPromedio);
        validarPositivo(cantidadTurbinas, "cantidad de turbinas");
        validarPositivo(capacidadEmbalse, "capacidad del embalse");
        this.cantidadTurbinas = cantidadTurbinas;
        this.capacidadEmbalse = capacidadEmbalse;
    }

    public int getCantidadTurbinas() {
        return cantidadTurbinas;
    }

    public double getCapacidadEmbalse() {
        return capacidadEmbalse;
    }

    @Override
    public String tipo() {
        return "Hidraulica";
    }

    @Override
    public String datosEspecificos() {
        return super.datosEspecificos() + " | turbinas: " + cantidadTurbinas
                + " | capacidad del embalse: " + capacidadEmbalse;
    }
}
