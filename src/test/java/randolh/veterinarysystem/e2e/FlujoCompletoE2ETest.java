package randolh.veterinarysystem.e2e;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import randolh.veterinarysystem.actividad.ActividadController;
import randolh.veterinarysystem.actividad.JornadaVacunacion;
import randolh.veterinarysystem.actividad.RegistroJornadaVacunacion;
import randolh.veterinarysystem.alerta.AlertaController;
import randolh.veterinarysystem.cita.Cita;
import randolh.veterinarysystem.cita.CitaController;
import randolh.veterinarysystem.cita.EstadoCitaEnum;
import randolh.veterinarysystem.consulta.ConsultaController;
import randolh.veterinarysystem.consulta.ConsultaMedica;
import randolh.veterinarysystem.consulta.EstadoProcedimientoEnum;
import randolh.veterinarysystem.consulta.InsumoProcedimiento;
import randolh.veterinarysystem.consulta.Prescripcion;
import randolh.veterinarysystem.consulta.ProcedimientoEspecial;
import randolh.veterinarysystem.core.config.ConexionBD;
import randolh.veterinarysystem.core.config.DatabaseAdminService;
import randolh.veterinarysystem.dueno.Dueno;
import randolh.veterinarysystem.dueno.DuenoController;
import randolh.veterinarysystem.facturacion.Factura;
import randolh.veterinarysystem.facturacion.FacturacionController;
import randolh.veterinarysystem.facturacion.ItemFactura;
import randolh.veterinarysystem.facturacion.MetodoPagoEnum;
import randolh.veterinarysystem.inventario.InventarioController;
import randolh.veterinarysystem.inventario.InventarioDAO;
import randolh.veterinarysystem.inventario.Producto;
import randolh.veterinarysystem.mascota.Mascota;
import randolh.veterinarysystem.mascota.MascotaController;
import randolh.veterinarysystem.mascota.SexoEnum;
import randolh.veterinarysystem.reporte.ReporteController;
import randolh.veterinarysystem.reporte.ServicioSolicitadoDTO;
import randolh.veterinarysystem.veterinario.Veterinario;
import randolh.veterinarysystem.veterinario.VeterinarioController;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Prueba E2E de integracion de todo el flujo de negocio sobre MySQL
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FlujoCompletoE2ETest {

    private static DuenoController duenoCtrl;
    private static MascotaController mascotaCtrl;
    private static VeterinarioController vetCtrl;
    private static CitaController citaCtrl;
    private static ConsultaController consultaCtrl;
    private static InventarioController inventarioCtrl;
    private static InventarioDAO inventarioDAO;
    private static FacturacionController facturaCtrl;
    private static ActividadController actividadCtrl;
    private static ReporteController reporteCtrl;
    private static AlertaController alertaCtrl;

    private static Dueno duenoPrueba;
    private static Mascota mascotaPrueba;
    private static Veterinario vetPrueba;
    private static Cita citaPrueba;

    @BeforeAll
    public static void setUp() {
        ConexionBD.getInstancia().cargarPropiedadesCascada();

        duenoCtrl = new DuenoController();
        mascotaCtrl = new MascotaController();
        vetCtrl = new VeterinarioController();
        citaCtrl = new CitaController();
        consultaCtrl = new ConsultaController();
        inventarioCtrl = new InventarioController();
        inventarioDAO = new InventarioDAO();
        facturaCtrl = new FacturacionController();
        actividadCtrl = new ActividadController();
        reporteCtrl = new ReporteController();
        alertaCtrl = new AlertaController();
    }

    @Test
    @Order(1)
    public void testPaso1ConectividadYDiagnosticoMySQL() {
        DatabaseAdminService admin = new DatabaseAdminService();
        DatabaseAdminService.DiagnosticoConexion diag = admin.probarConexion();

        assertTrue(diag.isExitoso(), "MySQL debe estar conectado exitosamente");
        assertTrue(diag.isBaseDatosExiste(), "La base de datos happy_feet_veterinaria debe existir");
        assertTrue(diag.getCantidadTablas() >= 30, "Deben existir al menos 30 tablas y vistas en el esquema");
    }

    @Test
    @Order(2)
    public void testPaso2RegistrarDuenoYValidar() {
        String numDoc = "CC-" + System.currentTimeMillis();
        String email = "cliente." + System.currentTimeMillis() + "@test.com";

        Dueno d = new Dueno("CC", numDoc, "Cliente E2E", "Calle 100 #20-30", "3119876543", email, "Contacto Emergencia");
        duenoPrueba = duenoCtrl.registrarDueno(d);

        assertNotNull(duenoPrueba);
        assertNotNull(duenoPrueba.getId());
        assertTrue(duenoPrueba.getId() > 0);
    }

    @Test
    @Order(3)
    public void testPaso3RegistrarMascotaConBuilder() {
        String chip = "CHIP-E2E-" + System.currentTimeMillis();

        Mascota m = Mascota.builder()
                .conDueno(duenoPrueba.getId())
                .conNombre("MascotaE2E")
                .conEspecie(1)
                .conRaza(1)
                .conFechaNacimiento(LocalDate.now().minusYears(2))
                .conSexo(SexoEnum.MACHO)
                .conPeso(14.5)
                .conMicrochip(chip)
                .conFotoUrl("http://fotos.com/mascota.jpg")
                .conAlergias("Ninguna")
                .conCondiciones("Sano")
                .build();

        mascotaPrueba = mascotaCtrl.registrarMascota(m);

        assertNotNull(mascotaPrueba);
        assertNotNull(mascotaPrueba.getId());
        assertEquals("MascotaE2E", mascotaPrueba.getNombre());
    }

    @Test
    @Order(4)
    public void testPaso4RegistrarVeterinario() {
        String doc = "VET-" + System.currentTimeMillis();
        String lic = "LIC-" + System.currentTimeMillis();
        String email = "vet." + System.currentTimeMillis() + "@clinica.com";

        Veterinario v = new Veterinario(null, "CC", doc, "Dr. Veterinario E2E", lic,
                "Medicina General", "3201112233", email, LocalDate.now(), true);

        vetPrueba = vetCtrl.registrarVeterinario(v);

        assertNotNull(vetPrueba);
        assertNotNull(vetPrueba.getId());
    }

    @Test
    @Order(5)
    public void testPaso5AgendarCitaYCicloDeVida() {
        LocalDateTime fechaCita = LocalDateTime.now().plusDays(2).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Cita c = new Cita(mascotaPrueba.getId(), vetPrueba.getId(), fechaCita, "Chequeo semestral E2E", "Sin antecedentes");

        citaPrueba = citaCtrl.agendarCita(c);
        assertNotNull(citaPrueba);
        assertEquals(EstadoCitaEnum.PROGRAMADA, citaPrueba.getEstado());

        boolean confirmada = citaCtrl.confirmarCita(citaPrueba.getId());
        assertTrue(confirmada);

        boolean iniciada = citaCtrl.iniciarAtencion(citaPrueba.getId());
        assertTrue(iniciada);
    }

    @Test
    @Order(6)
    public void testPaso6ConsultaMedicaConDeduccionTransaccionalStock() {
        List<Producto> productos = inventarioCtrl.listarTodos();
        assertTrue(!productos.isEmpty(), "Debe haber productos en inventario");
        Producto med = productos.stream().filter(p -> p.getProductoTipoId() == 1).findFirst().orElseThrow();
        int stockInicial = med.getCantidadStock();
        assertTrue(stockInicial >= 2, "Debe haber al menos 2 unidades para la prueba");

        ConsultaMedica consulta = new ConsultaMedica(
                null, mascotaPrueba.getId(), vetPrueba.getId(), citaPrueba.getId(),
                LocalDateTime.now(), "Control regular", "Sin sintomas graves",
                "Paciente en optimas condiciones", "Continuar alimentacion balanceada",
                "Revision en 6 meses", BigDecimal.valueOf(14.5), BigDecimal.valueOf(38.2)
        );

        List<Prescripcion> prescripciones = new ArrayList<>();
        prescripciones.add(new Prescripcion(med.getId(), 2, "1 tableta", "Cada 12 horas", 5, "Oral"));

        ConsultaMedica guardada = consultaCtrl.registrarConsulta(consulta, prescripciones);
        assertNotNull(guardada);
        assertNotNull(guardada.getId());

        Producto medActualizado = inventarioDAO.buscarProductoPorId(med.getId()).orElseThrow();
        assertEquals(stockInicial - 2, medActualizado.getCantidadStock(), "El stock debe haberse reducido en 2 unidades de forma atomica");
    }

    @Test
    @Order(7)
    public void testPaso7CirugiaYProcedimientoEspecial() {
        List<Producto> productos = inventarioCtrl.listarTodos();
        Producto material = productos.stream().filter(p -> p.getProductoTipoId() == 3).findFirst().orElse(productos.get(0));
        int stockInicial = material.getCantidadStock();

        ProcedimientoEspecial proc = new ProcedimientoEspecial(
                null, mascotaPrueba.getId(), vetPrueba.getId(), "Cirugia Menor",
                "Profilaxis Dental E2E", LocalDateTime.now().plusDays(1), 45, "Ayuno 8 horas", "Limpieza ultrasonica",
                "Sin complicaciones", "Reposo 24 horas", LocalDate.now().plusDays(15),
                EstadoProcedimientoEnum.PROGRAMADO, BigDecimal.valueOf(120.0)
        );

        List<InsumoProcedimiento> insumosUso = new ArrayList<>();
        insumosUso.add(new InsumoProcedimiento(material.getId(), 1, "Uso profilactico"));

        ProcedimientoEspecial procGuardado = consultaCtrl.registrarProcedimiento(proc, insumosUso);
        assertNotNull(procGuardado);
        assertNotNull(procGuardado.getId());

        Producto materialActualizado = inventarioDAO.buscarProductoPorId(material.getId()).orElseThrow();
        assertEquals(stockInicial - 1, materialActualizado.getCantidadStock(), "El stock de material debe haberse reducido en 1");
    }

    @Test
    @Order(8)
    public void testPaso8FacturacionConStrategyYExportacionDual() {
        List<ItemFactura> items = new ArrayList<>();
        items.add(ItemFactura.paraServicio(1, "Consulta Clinica", 1, BigDecimal.valueOf(50.0)));
        items.add(ItemFactura.paraProducto(1, "Insumo Farmacia", 2, BigDecimal.valueOf(15.0)));

        Factura factura = facturaCtrl.emitirFactura(duenoPrueba.getId(), items, MetodoPagoEnum.EFECTIVO, "Factura E2E");
        assertNotNull(factura);
        assertNotNull(factura.getId());
        assertNotNull(factura.getNumeroFactura());
        assertTrue(factura.getTotal().compareTo(BigDecimal.ZERO) > 0);

        facturaCtrl.exportarComprobantes(factura);

        File txt = new File("exportaciones/facturas/factura_" + factura.getNumeroFactura() + ".txt");
        File pdf = new File("exportaciones/facturas/factura_" + factura.getNumeroFactura() + ".pdf");
        assertTrue(txt.exists(), "Debe haberse generado el archivo txt de la factura");
        assertTrue(pdf.exists(), "Debe haberse generado el archivo PDF de la factura");
    }

    @Test
    @Order(9)
    public void testPaso9JornadaVacunacionFastTrack() {
        List<JornadaVacunacion> jornadas = actividadCtrl.listarJornadas();
        assertTrue(!jornadas.isEmpty(), "Debe haber jornadas creadas en las semillas");
        JornadaVacunacion jornada = jornadas.get(0);

        List<Producto> productos = inventarioCtrl.listarTodos();
        Producto vacuna = productos.stream().filter(p -> p.getProductoTipoId() == 2).findFirst().orElseThrow();
        int stockInicial = vacuna.getCantidadStock();

        RegistroJornadaVacunacion guardado = actividadCtrl.registrarPacienteClinica(
                jornada.getId(), mascotaPrueba.getId(), vacuna.getId(), vetPrueba.getId(),
                vacuna.getLote(), "Dosis anual de prueba E2E"
        );

        assertNotNull(guardado);
        assertNotNull(guardado.getId());

        Producto vacunaActualizada = inventarioDAO.buscarProductoPorId(vacuna.getId()).orElseThrow();
        assertEquals(stockInicial - 1, vacunaActualizada.getCantidadStock(), "La vacuna debe descontarse automaticamente del almacen");
    }

    @Test
    @Order(10)
    public void testPaso10ReportesGerencialesStreamAPI() {
        List<ServicioSolicitadoDTO> top = reporteCtrl.obtenerTopServicios(5, "TODOS");
        assertNotNull(top);
        assertFalse(top.isEmpty(), "Los reportes gerenciales con Stream API deben retornar datos");
    }
}
