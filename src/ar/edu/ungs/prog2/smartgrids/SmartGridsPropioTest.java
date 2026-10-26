package ar.edu.ungs.prog2.smartgrids;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests propios del grupo: casos borde que no cubre el test de la catedra.
 */
public class SmartGridsPropioTest {

    private static final String PUESTA = "2020-01-01T00:00:00";
    private static final String DIA1 = "2026-03-01";

    private ISmartGrids sistema;

    @Before
    public void setUp() {
        sistema = new SmartGrids();
        sistema.registrarPlantaSolar("Solar1", "Ruta 1", 1000, PUESTA, "Sol", 0.5, 5000, 2000, 12);
        sistema.registrarPlantaEolica("Eolica1", "Ruta 2", 400, PUESTA, "Viento", 0.5, 10, 80, 5);
        sistema.registrarPlantaHidraulica("Hidro1", "Ruta 3", 800, PUESTA, "Agua", 0.25, 4, 1000);
        sistema.registrarPlantaTermica("Termica1", "Ruta 4", 600, PUESTA, "Gas", 50, 900);
    }

    @Test
    public void rechazaDatosInvalidosAlRegistrarPlantas() {
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("", "x", 100, PUESTA, "Sol", 0.5, 1, 1, 8));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("S", "x", -1, PUESTA, "Sol", 0.5, 1, 1, 8));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("S", "x", 100, PUESTA, "Sol", 1.5, 1, 1, 8));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("S", "x", 100, PUESTA, "Sol", 0.5, 1, 1, 25));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("S", "x", 100, "2020-13-01T00:00:00", "Sol", 0.5, 1, 1, 8));
        assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaSolar("S", "x", 100, null, "Sol", 0.5, 1, 1, 8));
        assertEquals(4, sistema.consultarPlantas().size());
    }

    @Test
    public void consultasSobrePlantaInexistenteFallan() {
        assertThrows(RuntimeException.class, () -> sistema.capacidadMinimaCuota("Nada"));
        assertThrows(RuntimeException.class, () -> sistema.estaActiva("Nada"));
        assertThrows(RuntimeException.class, () -> sistema.activarPlanta("Nada", DIA1 + "T00:00:00"));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Nada", DIA1 + "T00:00:00", 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.incidenciasPorHora("Nada"));
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Nada", DIA1, DIA1));
    }

    @Test
    public void consultarPlantasMuestraElEstadoActual() {
        sistema.desactivarPlanta("Hidro1", DIA1 + "T08:00:00");
        assertEquals("Hidro1 (INACTIVA): " + PUESTA, sistema.consultarPlantas().get(2));
        sistema.activarPlanta("Hidro1", DIA1 + "T09:00:00");
        assertEquals("Hidro1 (ACTIVA): " + PUESTA, sistema.consultarPlantas().get(2));
    }

    @Test
    public void noAceptaRegistrosAtrasadosDentroDeUnPeriodoDeInactividad() {
        sistema.desactivarPlanta("Solar1", DIA1 + "T09:00:00");
        sistema.activarPlanta("Solar1", DIA1 + "T12:00:00");
        assertTrue(sistema.estaActiva("Solar1"));
        // ya esta activa, pero a las 10 estaba inactiva
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Solar1", DIA1 + "T10:00:00", 100, 0));
        // antes del periodo y despues de reactivarse si se puede registrar
        sistema.registrarMonitoreo("Solar1", DIA1 + "T08:00:00", 100, 0);
        sistema.registrarMonitoreo("Solar1", DIA1 + "T12:00:00", 100, 0);
        assertEquals(200, sistema.totalGenerado("Solar1"), 0.1);
    }

    @Test
    public void plantasInactivasEnUsaElHistorial() {
        sistema.desactivarPlanta("Solar1", DIA1 + "T09:00:00");
        sistema.activarPlanta("Solar1", DIA1 + "T12:00:00");
        sistema.desactivarPlanta("Termica1", DIA1 + "T11:00:00");

        assertTrue(sistema.plantasInactivasEn(DIA1 + "T08:59:59").isEmpty());
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(DIA1 + "T09:00:00"));
        assertEquals(Arrays.asList("Solar1", "Termica1"), sistema.plantasInactivasEn(DIA1 + "T11:30:00"));
        // a las 12 Solar1 ya estaba activa de nuevo
        assertEquals(Collections.singletonList("Termica1"), sistema.plantasInactivasEn(DIA1 + "T12:00:00"));
    }

    @Test
    public void activarODesactivarDosVecesNoCambiaNada() {
        sistema.activarPlanta("Solar1", DIA1 + "T09:00:00");
        assertTrue(sistema.estaActiva("Solar1"));
        sistema.desactivarPlanta("Solar1", DIA1 + "T09:00:00");
        sistema.desactivarPlanta("Solar1", DIA1 + "T10:00:00");
        assertFalse(sistema.estaActiva("Solar1"));
        assertEquals(Collections.singletonList("Solar1"), sistema.plantasInactivasEn(DIA1 + "T09:30:00"));
    }

    @Test
    public void noSePuedeReactivarAntesDeDesactivar() {
        sistema.desactivarPlanta("Solar1", DIA1 + "T10:00:00");
        assertThrows(RuntimeException.class, () -> sistema.activarPlanta("Solar1", DIA1 + "T09:00:00"));
        assertFalse(sistema.estaActiva("Solar1"));
    }

    @Test
    public void cadaPlantaAceptaSoloSuTipoDeRegistro() {
        String h = DIA1 + "T01:00:00";
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Eolica1", h, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Termica1", h, 1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Termica1", h, 1, 0, 5));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Hidro1", h, 1, 0, 1, 1));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Eolica1", h, 1, 0, 1, 1));
        assertEquals(0, sistema.totalGenerado("Eolica1"), 0.1);
    }

    @Test
    public void rechazaValoresNegativos() {
        String h = DIA1 + "T01:00:00";
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Hidro1", h, -1, 0));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Hidro1", h, 1, -1));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Eolica1", h, 1, 0, -3));
        assertThrows(RuntimeException.class, () -> sistema.registrarMonitoreo("Termica1", h, 1, 0, -1, 1));
    }

    @Test
    public void elRegistroCorrespondeALaHoraCompleta() {
        sistema.registrarMonitoreo("Hidro1", DIA1 + "T10:15:30", 50, 1);
        // 10:45 es la misma hora que 10:15
        assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Hidro1", DIA1 + "T10:45:00", 50, 1));
        assertEquals(1, sistema.incidenciasPorHora("Hidro1")[10]);
    }

    @Test
    public void totalGeneradoEsCeroSinRegistros() {
        assertEquals(0, sistema.totalGenerado("Solar1"), 0.0);
        assertArrayEquals(new int[24], sistema.incidenciasPorHora("Solar1"));
    }

    @Test
    public void histogramaIgnoraRegistrosFueraDelRangoYAceptaUnSoloDia() {
        sistema.registrarMonitoreo("Hidro1", "2026-02-28T05:00:00", 500, 0);
        sistema.registrarMonitoreo("Hidro1", DIA1 + "T05:00:00", 500, 0);
        sistema.registrarMonitoreo("Hidro1", "2026-03-02T05:00:00", 500, 0);
        int[] hist = sistema.histogramaCumplimiento("Hidro1", DIA1, DIA1);
        assertEquals(1, hist[5]);
        assertEquals(24, hist.length);
        assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento("Hidro1", "01/03/2026", DIA1));
    }

    @Test
    public void renovablesPorFuenteEstanOrdenadasYExcluyenTermicas() {
        sistema.registrarPlantaHidraulica("Hidro2", "x", 100, PUESTA, "Agua", 0.5, 1, 1);
        Map<String, Integer> r = sistema.plantasRenovablesPorFuente();
        assertEquals(Arrays.asList("Agua", "Sol", "Viento"), Arrays.asList(r.keySet().toArray()));
        assertEquals(2, (int) r.get("Agua"));
        assertFalse(r.containsKey("Gas"));
    }

    @Test
    public void reporteDeVientoRespetaElFormato() {
        sistema.registrarPlantaEolica("Mi Super Planta", "x", 100, PUESTA, "Viento", 0.5, 1, 1, 4);
        sistema.registrarMonitoreo("Eolica1", DIA1 + "T07:00:00", 1, 0, 2);
        sistema.registrarMonitoreo("Eolica1", DIA1 + "T11:00:00", 1, 0, 3);
        sistema.registrarMonitoreo("Eolica1", DIA1 + "T12:00:00", 1, 0, 9);
        sistema.registrarMonitoreo("Mi Super Planta", DIA1 + "T07:00:00", 1, 0, 2.5);

        String esperado = "Plantas Eolicas: Horas con poco viento\n"
                + " - Eolica1:  5 m/s\n"
                + "        07: 2 m/s\n"
                + "        11: 3 m/s\n"
                + " - Mi Super Planta:  4 m/s\n"
                + "        07: 2.5 m/s\n";
        assertEquals(esperado, sistema.horasSinVelocidadMinimaPorPlantaEolica());
    }

    @Test
    public void limpiarSinRegistrosVaciosDevuelveCero() {
        sistema.registrarMonitoreo("Hidro1", DIA1 + "T01:00:00", 0, 1);
        assertEquals(0, sistema.limpiarMonitoreosSinDatos());
        // despues de limpiar se puede volver a registrar esa hora
        sistema.registrarMonitoreo("Solar1", DIA1 + "T01:00:00", 0, 0);
        assertEquals(1, sistema.limpiarMonitoreosSinDatos());
        sistema.registrarMonitoreo("Solar1", DIA1 + "T01:00:00", 10, 0);
        assertEquals(10, sistema.totalGenerado("Solar1"), 0.1);
    }

    @Test
    public void plantasSinEnergiaQuedanAlFinalDelRanking() {
        sistema.registrarMonitoreo("Termica1", DIA1 + "T01:00:00", 700, 0, 1, 1);
        assertEquals("Termica1: 700.0", sistema.plantasOrdenadasPorEnergiaGenerada().get(0));
        assertEquals(4, sistema.plantasOrdenadasPorEnergiaGenerada().size());
    }

    @Test
    public void toStringMuestraTodasLasPlantas() {
        sistema.desactivarPlanta("Termica1", DIA1 + "T01:00:00");
        String texto = sistema.toString();
        assertTrue(texto.contains("Solar1 (ACTIVA)"));
        assertTrue(texto.contains("Termica1 (INACTIVA)"));
        assertTrue(texto.contains("periodos de inactividad"));
    }
}
