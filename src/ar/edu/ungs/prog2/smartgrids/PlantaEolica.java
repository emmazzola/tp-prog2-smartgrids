package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * IREP: el de PlantaRenovable && cantidadAerogeneradores > 0 && alturaTorres > 0
 *       && velocidadMinimaViento > 0 && todos sus registros son RegistroEolica
 */
public class PlantaEolica extends PlantaRenovable {

    private int cantidadAerogeneradores;
    private double alturaTorres;
    private double velocidadMinimaViento;

    public PlantaEolica(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadAerogeneradores, double alturaTorres, double velocidadMinimaViento) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento, fuentePrimaria,
                eficienciaPromedio);
        validarPositivo(cantidadAerogeneradores, "cantidad de aerogeneradores");
        validarPositivo(alturaTorres, "altura de las torres");
        validarPositivo(velocidadMinimaViento, "velocidad minima del viento");
        this.cantidadAerogeneradores = cantidadAerogeneradores;
        this.alturaTorres = alturaTorres;
        this.velocidadMinimaViento = velocidadMinimaViento;
    }

    /** Una planta eolica solo acepta registros con la velocidad del viento. */
    @Override
    public void validarRegistro(RegistroMonitoreo registro) {
        if (!(registro instanceof RegistroEolica)) {
            throw new IllegalArgumentException("La planta eolica " + getNombre()
                    + " necesita la velocidad del viento en cada registro");
        }
    }

    /**
     * Seccion del reporte del punto 10: las horas con registros en las que nunca se alcanzo
     * la velocidad minima, junto con la mayor velocidad registrada en esa hora.
     */
    public String horasSinVelocidadMinima() {
        double[] maximaPorHora = new double[24];
        boolean[] hayRegistros = new boolean[24];
        for (RegistroMonitoreo registro : getRegistros()) {
            RegistroEolica eolico = (RegistroEolica) registro;
            int hora = eolico.getHora();
            if (!hayRegistros[hora] || eolico.getVelocidadVientoPromedio() > maximaPorHora[hora]) {
                maximaPorHora[hora] = eolico.getVelocidadVientoPromedio();
            }
            hayRegistros[hora] = true;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" - ").append(getNombre()).append(":  ")
          .append(formatearVelocidad(velocidadMinimaViento)).append(" m/s\n");
        for (int hora = 0; hora < 24; hora++) {
            if (hayRegistros[hora] && maximaPorHora[hora] < velocidadMinimaViento) {
                sb.append("        ").append(String.format("%02d", hora)).append(": ")
                  .append(formatearVelocidad(maximaPorHora[hora])).append(" m/s\n");
            }
        }
        return sb.toString();
    }

    /** Muestra 5 en lugar de 5.0, y conserva los decimales cuando los hay. */
    private static String formatearVelocidad(double velocidad) {
        if (velocidad == Math.floor(velocidad)) {
            return String.valueOf((long) velocidad);
        }
        return String.valueOf(velocidad);
    }

    public int getCantidadAerogeneradores() {
        return cantidadAerogeneradores;
    }

    public double getAlturaTorres() {
        return alturaTorres;
    }

    public double getVelocidadMinimaViento() {
        return velocidadMinimaViento;
    }

    @Override
    public String tipo() {
        return "Eolica";
    }

    @Override
    public String datosEspecificos() {
        return super.datosEspecificos() + " | aerogeneradores: " + cantidadAerogeneradores
                + " | altura de torres: " + alturaTorres
                + " | velocidad minima: " + velocidadMinimaViento + " m/s";
    }
}
