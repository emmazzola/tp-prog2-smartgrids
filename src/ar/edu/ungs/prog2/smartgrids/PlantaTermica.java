package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDateTime;

/**
 * IREP: el de PlantaNoRenovable && todos sus registros son RegistroTermica
 */
public class PlantaTermica extends PlantaNoRenovable {

    public PlantaTermica(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento, String tipoCombustible,
            double objetivoEmisionesCo2PorHora, double capacidadAlmacenamientoCombustible) {
        super(nombre, direccion, capacidadMaxima, fechaPuestaEnFuncionamiento, tipoCombustible,
                objetivoEmisionesCo2PorHora, capacidadAlmacenamientoCombustible);
    }

    /** Cumple si genera al menos la mitad de su capacidad y emite menos CO2 que el objetivo. */
    @Override
    public boolean cumpleCuota(RegistroMonitoreo registro) {
        RegistroTermica termico = (RegistroTermica) registro;
        return termico.getEnergiaGenerada() >= cuotaMinima()
                && termico.getCo2Emitido() < getObjetivoEmisionesCo2PorHora();
    }

    /** Una planta termica solo acepta registros con CO2 y combustible. */
    @Override
    public void validarRegistro(RegistroMonitoreo registro) {
        if (!(registro instanceof RegistroTermica)) {
            throw new IllegalArgumentException("La planta termica " + getNombre()
                    + " necesita el CO2 emitido y el combustible utilizado en cada registro");
        }
    }

    @Override
    public String tipo() {
        return "Termica";
    }
}
