package ar.edu.ungs.prog2.smartgrids;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

public class SmartGridsTest {

    private static final String PUESTA = "2020-01-01T00:00:00";
    private static final String DIA1 = "2026-03-01";
    private static final String DIA2 = "2026-03-02";

    private ISmartGrids sistema;

    @Before
    public void setUp() {
        sistema = new SmartGrids();
        // cuota = 1000 * 0.5 * 12 / 24 = 250
        sistema.registrarPlantaSolar("Solar1", "Ruta 1", 1000, PUESTA, "Sol", 0.5, 5000, 2000, 12);
        // cuota = 400 * 0.5 = 200
        sistema.registrarPlantaEolica("Eolica1", "Ruta 2", 400, PUESTA, "Viento", 0.5, 10, 80, 5);
        // cuota = 800 * 0.25 = 200
        sistema.registrarPlantaHidraulica("Hidro1", "Ruta 3", 800, PUESTA, "Agua", 0.25, 4, 1000);
        // cuota = 600 / 2 = 300 y CO2 < 50
        sistema.registrarPlantaTermica("Termica1", "Ruta 4", 600, PUESTA, "Gas", 50, 900);
    }


    @Test
    public void registraYConsultaPlantas() {
        assertEquals(Arrays.asList(
            "Solar1 (ACTIVA): " + PUESTA,
            "Eolica1 (ACTIVA): " + PUESTA,
            "Hidro1 (ACTIVA): " + PUESTA,
            "Termica1 (ACTIVA): " + PUESTA), sistema.consultarPlantas());
        assertTrue(sistema.consultarPlantas().get(0).startsWith("Solar1"));
    }

    @Test
    public void noPermiteNombresDuplicadosNiPlantasInexistentes() {
        // debe fallar porque ya existe una planta con ese nombre.
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.registrarPlantaTermica("Solar1", "x", 10, PUESTA, "Gas", 1, 1)));
        // debe fallar porque no existe ninguna planta con ese nombre.
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.totalGenerado("Nada")));
    }

    @Test
    public void capacidadMinimaParaCumplirCuota() {
        assertEquals(250, sistema.capacidadMinimaCuota("Solar1"), 0.1);
        assertEquals(200, sistema.capacidadMinimaCuota("Eolica1"), 0.1);
        assertEquals(200, sistema.capacidadMinimaCuota("Hidro1"), 0.1);
        assertEquals(300, sistema.capacidadMinimaCuota("Termica1"), 0.1);
    }

    @Test
    public void actualizaEstadoYBloqueaMonitoreoEnInactiva() {
        String d1_09 = DIA1+"T09:00:00";
        String d1_10 = DIA1+"T10:00:00";
        String d1_11 = DIA1+"T11:00:00";
        String d1_12 = DIA1+"T12:00:00";

        sistema.desactivarPlanta("Solar1", d1_09);
        assertFalse(sistema.estaActiva("Solar1"));
        // debe fallar porque la planta esta desactivada
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Solar1", d1_10, 100, 0)));

        sistema.activarPlanta("Solar1", d1_11);
        sistema.registrarMonitoreo("Solar1", d1_12, 100, 0);
        assertEquals(100, sistema.totalGenerado("Solar1"), 0.1);
    }

    @Test
    public void rechazaMonitoreoDeTipoIncorrectoDuplicadoOAnteriorAPuesta() {
        String d1_01 = DIA1+"T01:00:00";

        // llama al metodo para registrar un monitoreo de planta eolica para una planta solar.
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Solar1", d1_01, 1, 0, 1)));

        // el primero registra ok. el 2do lanza error por registro duplicado.
        sistema.registrarMonitoreo("Hidro1", d1_01, 1, 0);
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Hidro1", d1_01, 1, 0)));

        // debe lanzar excepcion por fecha anterior a puesta en marcha.
        assertNotNull(assertThrows(RuntimeException.class,
            () -> sistema.registrarMonitoreo("Hidro1", "2019-01-01T01:00:00", 1, 0)));
    }

    @Test
    public void totalGeneradoEIncidenciasPorHora() {
        String d1_03 = DIA1+"T03:00:00";
        String d2_03 = DIA2+"T03:00:00";
        String d2_04 = DIA2+"T04:00:00";
        sistema.registrarMonitoreo("Hidro1", d1_03, 100, 2);
        sistema.registrarMonitoreo("Hidro1", d2_03, 50, 1);
        sistema.registrarMonitoreo("Hidro1", d2_04, 25, 4);
        assertEquals(175, sistema.totalGenerado("Hidro1"), 0.1);
        int[] esperado = new int[24];
        esperado[3] = 3;
        esperado[4] = 4;
        assertArrayEquals(esperado, sistema.incidenciasPorHora("Hidro1"));
    }

    @Test
    public void histogramaDeCumplimientoPorHora() {
        String d1_12 = DIA1+"T12:00:00";
        String d1_13 = DIA1+"T13:00:00";
        String d2_12 = DIA2+"T12:00:00";
        String d2_13 = DIA2+"T13:00:00";

        sistema.registrarMonitoreo("Solar1", d1_12, 300, 0); // cumple
        sistema.registrarMonitoreo("Solar1", d2_12, 250, 0); // cumple
        sistema.registrarMonitoreo("Solar1", d1_13, 300, 0); // cumple
        sistema.registrarMonitoreo("Solar1", d2_13, 10, 0); // no cumple
        sistema.registrarMonitoreo("Solar1", "2026-03-03T12:00:00", 300, 0);

        int[] hist = sistema.histogramaCumplimiento("Solar1", DIA1, DIA2);
        assertEquals(2, hist[12]);
        assertEquals(1, hist[13]);
        assertEquals(0, hist[0]);

        // debe lanzar error porque el dia inicial es posterior al final.
        assertNotNull(assertThrows(RuntimeException.class, () -> sistema.histogramaCumplimiento(
            "Solar1", DIA2, DIA1)));
    }

    @Test
    public void cuotaTermicaRequiereGeneracionYEmisionesBajas() {
        String d1_01 = DIA1+"T01:00:00";
        String d1_02 = DIA1+"T02:00:00";
        String d1_03 = DIA1+"T03:00:00";

        sistema.registrarMonitoreo("Termica1", d1_01, 300, 0, 49, 10); // cumple
        sistema.registrarMonitoreo("Termica1", d1_02, 300, 0, 50, 10); // emite de mas
        sistema.registrarMonitoreo("Termica1", d1_03, 299, 0, 1, 10); // genera poco
        int[] hist = sistema.histogramaCumplimiento(
            "Termica1", DIA1, DIA1);
        assertEquals(1, hist[1]);
        assertEquals(0, hist[2]);
        assertEquals(0, hist[3]);
    }

    @Test
    public void cuentaPlantasRenovablesPorFuente() {
        sistema.registrarPlantaSolar("Solar2", "x", 100, PUESTA, "Sol", 0.5, 10, 10, 8);
        Map<String, Integer> r = sistema.plantasRenovablesPorFuente();
        assertEquals(2, (int) r.get("Sol"));
        assertEquals(1, (int) r.get("Viento"));
        assertEquals(1, (int) r.get("Agua"));
        assertEquals(3, r.size());
    }

    @Test
    public void plantasQueNuncaCumplieronCuota() {
        String d1_12 = DIA1+"T12:00:00";
        sistema.registrarMonitoreo("Solar1", d1_12, 300, 0);
        sistema.registrarMonitoreo("Hidro1", d1_12, 10, 0);
        sistema.registrarMonitoreo("Termica1", d1_12, 400, 0, 10, 1);
        // Eolica1 no tiene registros
        assertEquals(Arrays.asList("Eolica1", "Hidro1"), sistema.plantasQueNuncaCumplieronCuota());
    }

    @Test
    public void horasSinVelocidadMinimaEnPlantasEolicas() {
        String d1_01 = DIA1+"T01:00:00";
        String d1_02 = DIA1+"T02:00:00";
        String d1_05 = DIA1+"T05:00:00";
        String d2_01 = DIA2+"T01:00:00";
        String d2_02 = DIA2+"T02:00:00";

        sistema.registrarMonitoreo("Eolica1", d1_01, 10, 0, 3);
        sistema.registrarMonitoreo("Eolica1", d2_01, 10, 0, 6); // en hora 1 si alcanzo
        sistema.registrarMonitoreo("Eolica1", d1_02, 10, 0, 4);
        sistema.registrarMonitoreo("Eolica1", d2_02, 10, 0, 2);
        sistema.registrarMonitoreo("Eolica1", d1_05, 10, 0, 1);

        String resultado = sistema.horasSinVelocidadMinimaPorPlantaEolica();
        assertTrue(resultado.contains("Eolica1"));
        assertTrue(resultado.contains("02"));
        assertTrue(resultado.contains("05"));
        assertFalse(resultado.contains("01"));
    }

    @Test
    public void limpiaMonitoreosSinEnergiaNiIncidencias() {
        String d1_01 = DIA1+"T01:00:00";
        String d1_02 = DIA1+"T02:00:00";
        String d1_03 = DIA1+"T03:00:00";
        sistema.registrarMonitoreo("Solar1", d1_01, 0, 0);
        sistema.registrarMonitoreo("Solar1", d1_02, 100, 0);
        sistema.registrarMonitoreo("Solar1", d1_03, 0, 1);

        assertEquals(1, sistema.limpiarMonitoreosSinDatos());
        assertEquals(100, sistema.totalGenerado("Solar1"), 0.1);
    }

    @Test
    public void ordenaPlantasPorEnergiaGenerada() {
        String d1_01 = DIA1+"T01:00:00";
        sistema.registrarMonitoreo("Solar1", d1_01, 300, 0);
        sistema.registrarMonitoreo("Eolica1", d1_01, 200, 0, 6);
        sistema.registrarMonitoreo("Hidro1", d1_01, 100, 0);
        sistema.registrarMonitoreo("Termica1", d1_01, 50, 0, 10, 1);

        List<String> esperado= Arrays.asList(
                "Solar1: 300.0",
                "Eolica1: 200.0",
                "Hidro1: 100.0",
                "Termica1: 50.0");

        assertEquals(esperado, sistema.plantasOrdenadasPorEnergiaGenerada());
    }

    @Test
    public void consultaPlantasInactivasEnUnaFechaHora() {
        String d1_10 = DIA1+"T10:00:00";
        String d1_11 = DIA1+"T11:00:00";

        assertTrue(sistema.plantasInactivasEn(d1_11).isEmpty());

        sistema.desactivarPlanta("Solar1", d1_10);

        assertEquals(Collections.singletonList("Solar1"),
                sistema.plantasInactivasEn(d1_11));
    }
}
