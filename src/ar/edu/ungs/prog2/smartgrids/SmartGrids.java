package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Sistema SmartGrids: registra plantas generadoras y sus datos de monitoreo.
 *
 * Las plantas se guardan en un LinkedHashMap por nombre: la busqueda es O(1)
 * (por eso activar, desactivar y consultar el estado son O(1)) y se conserva
 * el orden de registro para las consultas que devuelven listas.
 *
 * IREP: plantas != null && para cada (k, p) de plantas: p != null && k == p.getNombre()
 *       (como la clave es el nombre, no hay dos plantas con el mismo nombre)
 */
public class SmartGrids implements ISmartGrids {

    private LinkedHashMap<String, Planta> plantas;

    public SmartGrids() {
        this.plantas = new LinkedHashMap<>();
    }

    // ------------------------------------------------------------------ 1.a) registro de plantas

    @Override
    public void registrarPlantaSolar(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            double superficieCubierta, int cantidadPaneles, double horasRadiacionDiaria) {
        agregarPlanta(new PlantaSolar(nombre, direccion, capacidadMaxima,
                parsearFechaHora(fechaHoraPuestaEnFuncionamiento), fuentePrimaria, eficienciaPromedio,
                superficieCubierta, cantidadPaneles, horasRadiacionDiaria));
    }

    @Override
    public void registrarPlantaEolica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadAerogeneradores, double alturaTorres, double velocidadMinimaViento) {
        agregarPlanta(new PlantaEolica(nombre, direccion, capacidadMaxima,
                parsearFechaHora(fechaHoraPuestaEnFuncionamiento), fuentePrimaria, eficienciaPromedio,
                cantidadAerogeneradores, alturaTorres, velocidadMinimaViento));
    }

    @Override
    public void registrarPlantaHidraulica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadTurbinas, double capacidadEmbalse) {
        agregarPlanta(new PlantaHidraulica(nombre, direccion, capacidadMaxima,
                parsearFechaHora(fechaHoraPuestaEnFuncionamiento), fuentePrimaria, eficienciaPromedio,
                cantidadTurbinas, capacidadEmbalse));
    }

    @Override
    public void registrarPlantaTermica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String tipoCombustible,
            double objetivoEmisionesCo2PorHora, double capacidadAlmacenamientoCombustible) {
        agregarPlanta(new PlantaTermica(nombre, direccion, capacidadMaxima,
                parsearFechaHora(fechaHoraPuestaEnFuncionamiento), tipoCombustible,
                objetivoEmisionesCo2PorHora, capacidadAlmacenamientoCombustible));
    }

    private void agregarPlanta(Planta planta) {
        if (plantas.containsKey(planta.getNombre())) {
            throw new IllegalArgumentException("Ya existe una planta llamada " + planta.getNombre());
        }
        plantas.put(planta.getNombre(), planta);
    }

    // ------------------------------------------------------------------ 1.b) consulta

    @Override
    public List<String> consultarPlantas() {
        List<String> detalles = new ArrayList<>();
        for (Planta planta : plantas.values()) {
            detalles.add(planta.detalle());
        }
        return detalles;
    }

    // ------------------------------------------------------------------ 2) cuota

    @Override
    public double capacidadMinimaCuota(String nombre) {
        return buscarPlanta(nombre).cuotaMinima();
    }

    // ------------------------------------------------------------------ 3) estado, O(1)

    @Override
    public void activarPlanta(String nombre, String fechaHora) {
        buscarPlanta(nombre).activar(parsearFechaHora(fechaHora));
    }

    @Override
    public void desactivarPlanta(String nombre, String fechaHora) {
        buscarPlanta(nombre).desactivar(parsearFechaHora(fechaHora));
    }

    @Override
    public boolean estaActiva(String nombre) {
        return buscarPlanta(nombre).estaActiva();
    }

    // ------------------------------------------------------------------ 4) monitoreo (sobrecarga)

    @Override
    public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias) {
        Planta planta = buscarPlanta(nombre);
        planta.registrarMonitoreo(new RegistroMonitoreo(nombre, horaDelRegistro(fechaHora),
                energiaGenerada, incidencias));
    }

    @Override
    public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
            double velocidadViento) {
        Planta planta = buscarPlanta(nombre);
        planta.registrarMonitoreo(new RegistroEolica(nombre, horaDelRegistro(fechaHora),
                energiaGenerada, incidencias, velocidadViento));
    }

    @Override
    public void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
            double co2Emitido, double combustibleUtilizado) {
        Planta planta = buscarPlanta(nombre);
        planta.registrarMonitoreo(new RegistroTermica(nombre, horaDelRegistro(fechaHora),
                energiaGenerada, incidencias, co2Emitido, combustibleUtilizado));
    }

    // ------------------------------------------------------------------ 5) a 7) consultas por planta

    @Override
    public double totalGenerado(String nombre) {
        return buscarPlanta(nombre).getTotalGenerado();
    }

    @Override
    public int[] incidenciasPorHora(String nombre) {
        return buscarPlanta(nombre).incidenciasPorHora();
    }

    @Override
    public int[] histogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta) {
        Planta planta = buscarPlanta(nombre);
        LocalDate desde = parsearFecha(fechaDesde);
        LocalDate hasta = parsearFecha(fechaHasta);
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha desde (" + desde + ") es posterior a hasta (" + hasta + ")");
        }
        return planta.histogramaCumplimiento(desde, hasta);
    }

    // ------------------------------------------------------------------ 8) a 13) consultas generales

    @Override
    public Map<String, Integer> plantasRenovablesPorFuente() {
        // TreeMap: las fuentes quedan ordenadas alfabeticamente
        Map<String, Integer> cantidadPorFuente = new TreeMap<>();
        for (Planta planta : plantas.values()) {
            if (planta.esRenovable()) {
                String fuente = ((PlantaRenovable) planta).getFuentePrimaria();
                cantidadPorFuente.put(fuente, cantidadPorFuente.getOrDefault(fuente, 0) + 1);
            }
        }
        return cantidadPorFuente;
    }

    @Override
    public List<String> plantasQueNuncaCumplieronCuota() {
        List<String> nombres = new ArrayList<>();
        for (Planta planta : plantas.values()) {
            if (planta.nuncaCumplioCuota()) {
                nombres.add(planta.getNombre());
            }
        }
        return nombres;
    }

    @Override
    public String horasSinVelocidadMinimaPorPlantaEolica() {
        StringBuilder sb = new StringBuilder("Plantas Eolicas: Horas con poco viento\n");
        for (Planta planta : plantas.values()) {
            if (planta instanceof PlantaEolica) {
                sb.append(((PlantaEolica) planta).horasSinVelocidadMinima());
            }
        }
        return sb.toString();
    }

    @Override
    public int limpiarMonitoreosSinDatos() {
        int borrados = 0;
        for (Planta planta : plantas.values()) {
            borrados += planta.limpiarRegistrosSinDatos();
        }
        return borrados;
    }

    @Override
    public List<String> plantasOrdenadasPorEnergiaGenerada() {
        List<Planta> ordenadas = new ArrayList<>(plantas.values());
        ordenadas.sort((a, b) -> Double.compare(b.getTotalGenerado(), a.getTotalGenerado()));
        List<String> resultado = new ArrayList<>();
        for (Planta planta : ordenadas) {
            resultado.add(planta.getNombre() + ": " + planta.getTotalGenerado());
        }
        return resultado;
    }

    @Override
    public List<String> plantasInactivasEn(String fechaHora) {
        LocalDateTime momento = parsearFechaHora(fechaHora);
        List<String> nombres = new ArrayList<>();
        for (Planta planta : plantas.values()) {
            if (!planta.estuvoActivaEn(momento)) {
                nombres.add(planta.getNombre());
            }
        }
        return nombres;
    }

    // ------------------------------------------------------------------ auxiliares

    private Planta buscarPlanta(String nombre) {
        Planta planta = plantas.get(nombre);
        if (planta == null) {
            throw new IllegalArgumentException("No existe una planta llamada " + nombre);
        }
        return planta;
    }

    /** Los registros se guardan por hora: 10:15:30 corresponde a la hora 10:00:00. */
    private static LocalDateTime horaDelRegistro(String fechaHora) {
        return parsearFechaHora(fechaHora).truncatedTo(ChronoUnit.HOURS);
    }

    private static LocalDateTime parsearFechaHora(String fechaHora) {
        if (fechaHora == null) {
            throw new IllegalArgumentException("Falta la fecha y hora");
        }
        try {
            return LocalDateTime.parse(fechaHora);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha y hora invalida: " + fechaHora
                    + " (formato esperado yyyy-MM-ddTHH:mm:ss)");
        }
    }

    private static LocalDate parsearFecha(String fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("Falta la fecha");
        }
        try {
            return LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha invalida: " + fecha + " (formato esperado yyyy-MM-dd)");
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SmartGrids - ").append(plantas.size()).append(" planta(s) registrada(s)\n");
        for (Planta planta : plantas.values()) {
            sb.append(planta);
        }
        return sb.toString();
    }
}
