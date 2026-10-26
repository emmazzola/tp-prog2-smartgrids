package ar.edu.ungs.prog2.smartgrids;

import java.util.List;
import java.util.Map;

/**
 * Interfaz publica del sistema SmartGrids.
 *
 * Si consultas o actualizas una planta que no esta registrada,
 * se lanza RuntimeException.
 *
 * Si las fechas o fechasHora no son validas, lanza RuntimeException.
 *  - fecha: "yyyy-MM-dd". Por ejemplo: 2026-10-08
 *  - fechaHora: "yyyy-MM-dd'T'HH:mm:ss". Por ejemplo: 2026-10-08T10:15:30
 * Son los formatos por defecto que esperan LocalDate y LocalDateTime para
 * poder parsear el string.
 *
 */
public interface ISmartGrids {

    /*
     * 1.a) Registran una planta con sus datos y caracteristicas.
     *
     * Por defecto la planta se crea en estado activa.
     *
     * @throws RuntimeException si ya existe ese nombre o falta o es
     *         inaceptable algun dato.
     */
    void registrarPlantaSolar(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            double superficieCubierta, int cantidadPaneles, double horasRadiacionDiaria);

    void registrarPlantaEolica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadAerogeneradores, double alturaTorres, double velocidadMinimaViento);

    void registrarPlantaHidraulica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String fuentePrimaria, double eficienciaPromedio,
            int cantidadTurbinas, double capacidadEmbalse);

    void registrarPlantaTermica(String nombre, String direccion, double capacidadMaxima,
            String fechaHoraPuestaEnFuncionamiento, String tipoCombustible, double objetivoEmisionesCo2PorHora,
            double capacidadAlmacenamientoCombustible);

    /*
     * 1.b) Consultar plantas exstentes.
     *
     * Que devuelve un lista con el detalle de cada planta:
     *  cada item debe respetar el formato: "{NOMBRE} ({ACTIVA|INACTIVA}): {FECHA_PUESTA_EN_FUNCIONAMIENTO}"
     * Por ejemplo:
     *     "Atucha II (ACTIVA): 2014-06-27T09:00:00"
     */
    List<String> consultarPlantas();

    /*
     * 2) Consulta cuanta energia debe generar la planta en una hora para
     * cumplir la cuota. En las solares se tiene en cuenta la radiacion;
     * en las eolicas e hidraulicas, la eficiencia. Las termicas deben
     * generar la mitad de su capacidad y emitir menos CO2 que el objetivo
     * por hora.
     *
     * @throws RuntimeException si la planta no existe
     */
    double capacidadMinimaCuota(String nombre);

    /*
     * 3) Activar, desactivar una planta y consultar si está activa.
     *
     * La actualizacion y consulta se debe resolver en O(1).
     *
     * @throws RuntimeException si la planta no existe o el formato de
     *         fechaHora es inválido.
     */
    void activarPlanta(String nombre, String fechaHora);

    void desactivarPlanta(String nombre, String fechaHora);

    boolean estaActiva(String nombre);

    /*
     * 4) crea un registro de monitoreo en la planta especificada.
     *
     * Anota la energia y las incidencias de una hora en una planta.
     * y si corresponde, la velocidad del viento o emision de CO2 y
     * uso de combuistible, segun corresponda.
     *
     * @throws RuntimeException si la planta no existe o esta inactiva;
     *         si falta la fechaHora, es anterior a la puesta en marcha o ya
     *         hay un registro para esa hora; o si la energia o las incidencias
     *         son negativas, etc.
     */
    void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias);

    void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
            double velocidadViento);

    void registrarMonitoreo(String nombre, String fechaHora, double energiaGenerada, int incidencias,
            double co2Emitido, double combustibleUtilizado);

    /*
     * 5) Devuelve la energia total generada, o 0 si no hay monitoreos
     *
     * @throws RuntimeException si la planta no existe
     */
    double totalGenerado(String nombre);

    /*
     * 6) Devuelve un arreglo con 24 posiciones con las incidencias acumuladas
     *         para cada hora del dia.
     *
     * @throws RuntimeException si la planta no existe
     */
    int[] incidenciasPorHora(String nombre);

    /*
     * 7) Cuenta, para cada hora del dia, en cuantos dias se cumplio la cuota
     * entre las fechas indicadas, incluidas ambas.
     *
     * Devuelve un arreglo con 24 posiciones, una por cada hora del dia
     *
     * @throws RuntimeException si la planta no existe o si desde es
     *         posterior a hasta.
     */
    int[] histogramaCumplimiento(String nombre, String fechaDesde, String fechaHasta);

    /*
     * 8) Calcula y devulve la cantidad de plantas renovables por fuente
     * primaria, ordenadas por tipo de fuente.
     */
    Map<String, Integer> plantasRenovablesPorFuente();

    /*
     * 9) Devuelve nombres de las plantas que nunca cumplieron la cuota,
     * incluso si no tienen monitoreos.
     */
    List<String> plantasQueNuncaCumplieronCuota();

    /*
     * 10) Devuelve para cada planta eolica, las horas (de 0 a 23) con
     * registros en las que nunca se alcanzo la velocidad minima del viento.
     * Se debe respetar el siguiente formato:
     * """
     * Plantas Eolicas: Horas con poco viento
     * - Planta 1:  5 m/s
     *        07: 2 m/s
     *        11: 3 m/s
     *        12: 1 m/s
     *        13: 0 m/s
     * - Mi Super Planta:  4 m/s
     *        07: 2 m/s
     *        11: 3 m/s
     * """
     */
    String horasSinVelocidadMinimaPorPlantaEolica();

    /*
     * 11) Elimina todos los registros con energia e incidencias en cero.
     * Devuelve la cantidad de registros borrados.
     */
    int limpiarMonitoreosSinDatos();

    /*
     * 12) Devuelve las plantas ordenadas de mayor a menor energia generada.
     *      Cada elemento respeta el formato "{NOMBRE_PLANTA}: {TOTAL_GENERADO}".
     */
    List<String> plantasOrdenadasPorEnergiaGenerada();

    /*
     * 13) Devuelve los nombres de las plantas inactivas en la fecha y hora indicada.
     *
     */
    List<String> plantasInactivasEn(String fechaHora);
}
