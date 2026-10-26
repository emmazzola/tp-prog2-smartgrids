package ar.edu.ungs.prog2.smartgrids;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Planta generadora. Reune los datos y el comportamiento comun a todos los tipos de planta.
 *
 * IREP:
 *  - nombre y direccion no vacios, capacidadMaxima > 0, fechaPuestaEnFuncionamiento != null
 *  - para cada (k, r) de registros: k == r.getFechaHora() && r.getNombrePlanta() == nombre
 *    && k no es anterior a fechaPuestaEnFuncionamiento
 *  - ningun registro cae dentro de un periodo de inactividad
 *  - totalGenerado == suma de la energia de todos los registros
 *  - periodosInactividad ordenados por desde, sin superponerse, y solo el ultimo puede estar abierto
 *  - activa == false <=> hay periodos y el ultimo esta abierto
 */
public abstract class Planta {

    private static final DateTimeFormatter FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private String nombre;
    private String direccion;
    private double capacidadMaxima;
    private boolean activa;
    private LocalDateTime fechaPuestaEnFuncionamiento;
    private HashMap<LocalDateTime, RegistroMonitoreo> registros;
    private ArrayList<PeriodoInactividad> periodosInactividad;
    private double totalGenerado;

    public Planta(String nombre, String direccion, double capacidadMaxima,
            LocalDateTime fechaPuestaEnFuncionamiento) {
        validarTexto(nombre, "nombre");
        validarTexto(direccion, "direccion");
        validarPositivo(capacidadMaxima, "capacidad maxima");
        if (fechaPuestaEnFuncionamiento == null) {
            throw new IllegalArgumentException("Falta la fecha de puesta en funcionamiento");
        }
        this.nombre = nombre;
        this.direccion = direccion;
        this.capacidadMaxima = capacidadMaxima;
        this.fechaPuestaEnFuncionamiento = fechaPuestaEnFuncionamiento;
        this.activa = true;
        this.registros = new HashMap<>();
        this.periodosInactividad = new ArrayList<>();
        this.totalGenerado = 0;
    }

    // ------------------------------------------------------------------ cuota (polimorfismo)

    /** Energia minima que la planta debe generar en una hora para cumplir la cuota. */
    public abstract double cuotaMinima();

    /** Indica si el registro de una hora cumple la cuota de la planta. */
    public abstract boolean cumpleCuota(RegistroMonitoreo registro);

    public abstract boolean esRenovable();

    /** Cada tipo de planta valida que el registro de monitoreo tenga los datos que le corresponden. */
    public void validarRegistro(RegistroMonitoreo registro) {
        if (registro instanceof RegistroEolica || registro instanceof RegistroTermica) {
            throw new IllegalArgumentException("La planta " + nombre
                    + " no registra datos de viento ni de emisiones");
        }
    }

    // ------------------------------------------------------------------ estado

    public boolean estaActiva() {
        return activa;
    }

    /** O(1): agrega un periodo abierto al final de la lista. Si ya estaba inactiva no hace nada. */
    public void desactivar(LocalDateTime fechaHora) {
        if (!activa) {
            return;
        }
        validarCambioDeEstado(fechaHora);
        periodosInactividad.add(new PeriodoInactividad(fechaHora));
        activa = false;
    }

    /** O(1): cierra el ultimo periodo de inactividad. Si ya estaba activa no hace nada. */
    public void activar(LocalDateTime fechaHora) {
        if (activa) {
            return;
        }
        ultimoPeriodo().cerrar(fechaHora);
        activa = true;
    }

    /** Indica si la planta estaba activa en un momento dado, segun su historial de inactividad. */
    public boolean estuvoActivaEn(LocalDateTime momento) {
        for (PeriodoInactividad periodo : periodosInactividad) {
            if (periodo.contiene(momento)) {
                return false;
            }
        }
        return true;
    }

    private void validarCambioDeEstado(LocalDateTime fechaHora) {
        if (fechaHora.isBefore(fechaPuestaEnFuncionamiento)) {
            throw new IllegalArgumentException("La fecha " + fechaHora
                    + " es anterior a la puesta en funcionamiento de " + nombre);
        }
        if (!periodosInactividad.isEmpty() && fechaHora.isBefore(ultimoPeriodo().getHasta())) {
            throw new IllegalArgumentException("La fecha " + fechaHora
                    + " es anterior al ultimo cambio de estado de " + nombre);
        }
    }

    private PeriodoInactividad ultimoPeriodo() {
        return periodosInactividad.get(periodosInactividad.size() - 1);
    }

    // ------------------------------------------------------------------ monitoreo

    public void registrarMonitoreo(RegistroMonitoreo registro) {
        validarRegistro(registro);
        LocalDateTime fechaHora = registro.getFechaHora();
        if (!activa || !estuvoActivaEn(fechaHora)) {
            throw new IllegalStateException("La planta " + nombre + " esta inactiva en " + fechaHora);
        }
        if (fechaHora.isBefore(fechaPuestaEnFuncionamiento)) {
            throw new IllegalArgumentException("El registro " + fechaHora
                    + " es anterior a la puesta en funcionamiento de " + nombre);
        }
        if (registros.containsKey(fechaHora)) {
            throw new IllegalArgumentException("Ya existe un registro de " + nombre + " para " + fechaHora);
        }
        registros.put(fechaHora, registro);
        totalGenerado += registro.getEnergiaGenerada();
    }

    public double getTotalGenerado() {
        return totalGenerado;
    }

    public int[] incidenciasPorHora() {
        int[] incidencias = new int[24];
        for (RegistroMonitoreo registro : registros.values()) {
            incidencias[registro.getHora()] += registro.getIncidencias();
        }
        return incidencias;
    }

    /**
     * Para cada hora del dia, cuenta en cuantos dias entre desde y hasta (inclusive)
     * se cumplio la cuota. Recorre D dias x 24 horas con busquedas O(1) en el HashMap: O(D).
     */
    public int[] histogramaCumplimiento(LocalDate desde, LocalDate hasta) {
        int[] histograma = new int[24];
        for (LocalDate dia = desde; !dia.isAfter(hasta); dia = dia.plusDays(1)) {
            for (int hora = 0; hora < 24; hora++) {
                RegistroMonitoreo registro = registros.get(dia.atTime(hora, 0));
                if (registro != null && cumpleCuota(registro)) {
                    histograma[hora]++;
                }
            }
        }
        return histograma;
    }

    /** True si ningun registro cumple la cuota (incluye a las plantas sin registros). */
    public boolean nuncaCumplioCuota() {
        for (RegistroMonitoreo registro : registros.values()) {
            if (cumpleCuota(registro)) {
                return false;
            }
        }
        return true;
    }

    /** Borra los registros sin energia ni incidencias y devuelve cuantos borro. */
    public int limpiarRegistrosSinDatos() {
        int borrados = 0;
        Iterator<RegistroMonitoreo> it = registros.values().iterator();
        while (it.hasNext()) {
            RegistroMonitoreo registro = it.next();
            if (registro.estaVacio()) {
                it.remove();
                totalGenerado -= registro.getEnergiaGenerada();
                borrados++;
            }
        }
        return borrados;
    }

    public Iterable<RegistroMonitoreo> getRegistros() {
        return registros.values();
    }

    public int cantidadRegistros() {
        return registros.size();
    }

    // ------------------------------------------------------------------ getters y setters

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        validarTexto(direccion, "direccion");
        this.direccion = direccion;
    }

    public double getCapacidadMaxima() {
        return capacidadMaxima;
    }

    public LocalDateTime getFechaPuestaEnFuncionamiento() {
        return fechaPuestaEnFuncionamiento;
    }

    public List<PeriodoInactividad> getPeriodosInactividad() {
        return new ArrayList<>(periodosInactividad);
    }

    // ------------------------------------------------------------------ texto

    /** Formato pedido en 1.b: "{NOMBRE} ({ACTIVA|INACTIVA}): {FECHA_PUESTA_EN_FUNCIONAMIENTO}". */
    public String detalle() {
        return nombre + " (" + (activa ? "ACTIVA" : "INACTIVA") + "): "
                + fechaPuestaEnFuncionamiento.format(FORMATO_FECHA_HORA);
    }

    /** Tipo de planta para mostrar en toString (cada subclase lo redefine). */
    public abstract String tipo();

    /** Datos propios de cada tipo de planta para toString. */
    public abstract String datosEspecificos();

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(detalle()).append(" - ").append(tipo()).append('\n');
        sb.append("    direccion: ").append(direccion)
          .append(" | capacidad maxima: ").append(capacidadMaxima)
          .append(" | cuota por hora: ").append(Math.round(cuotaMinima() * 100) / 100.0).append('\n');
        sb.append("    ").append(datosEspecificos()).append('\n');
        sb.append("    registros: ").append(registros.size())
          .append(" | total generado: ").append(totalGenerado).append('\n');
        if (!periodosInactividad.isEmpty()) {
            sb.append("    periodos de inactividad: ").append(periodosInactividad).append('\n');
        }
        return sb.toString();
    }

    // ------------------------------------------------------------------ validaciones comunes

    public static void validarTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("Falta el dato: " + campo);
        }
    }

    public static void validarPositivo(double valor, String campo) {
        if (!(valor > 0)) {
            throw new IllegalArgumentException("El dato " + campo + " debe ser mayor a 0");
        }
    }
}
