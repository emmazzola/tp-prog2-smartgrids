package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * IREP: el de PlantaRenovable && superficieCubierta > 0 && cantidadPaneles > 0
 *       && 0 < horasRadiacionDiaria <= 24 && ningun registro es eolico ni termico
 */
public class PlantaSolar extends PlantaRenovable {

    private double superficieCubierta;
    private int cantidadPaneles;
    private double horasRadiacionDiaria;

    public PlantaSolar(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            double superficieCubierta, int cantidadPaneles, double horasRadiacionDiaria) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento, fuentePrimaria,
                eficienciaPromedio);
        validarPositivo(superficieCubierta, "superficie cubierta");
        validarPositivo(cantidadPaneles, "cantidad de paneles");
        validarHorasRadiacion(horasRadiacionDiaria);
        this.superficieCubierta = superficieCubierta;
        this.cantidadPaneles = cantidadPaneles;
        this.horasRadiacionDiaria = horasRadiacionDiaria;
    }

    /** La energia esperada en las horas de radiacion se reparte en las 24 horas del dia. */
    @Override
    public double cuotaMinima() {
        return super.cuotaMinima() * horasRadiacionDiaria / 24;
    }

    public double getSuperficieCubierta() {
        return superficieCubierta;
    }

    public int getCantidadPaneles() {
        return cantidadPaneles;
    }

    public double getHorasRadiacionDiaria() {
        return horasRadiacionDiaria;
    }

    public void setHorasRadiacionDiaria(double horasRadiacionDiaria) {
        validarHorasRadiacion(horasRadiacionDiaria);
        this.horasRadiacionDiaria = horasRadiacionDiaria;
    }

    private static void validarHorasRadiacion(double horas) {
        if (!(horas > 0 && horas <= 24)) {
            throw new IllegalArgumentException("Las horas de radiacion diaria deben estar entre 0 y 24");
        }
    }

    @Override
    public String tipo() {
        return "Solar";
    }

    @Override
    public String datosEspecificos() {
        return super.datosEspecificos() + " | superficie: " + superficieCubierta
                + " | paneles: " + cantidadPaneles + " | horas de radiacion: " + horasRadiacionDiaria;
    }
}
