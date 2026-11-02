package randolh.veterinarysystem.mascota;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pruebas unitarias para Mascota y patron Builder
public class MascotaBuilderTest {

    @Test
    public void testConstruccionConBuilder() {
        LocalDate nacimiento = LocalDate.now().minusYears(3).minusMonths(2);

        Mascota mascota = Mascota.builder()
                .conId(10)
                .conDueno(5)
                .conNombre("Firulais")
                .conEspecie(1)
                .conRaza(2)
                .conFechaNacimiento(nacimiento)
                .conSexo(SexoEnum.MACHO)
                .conPeso(12.5)
                .conMicrochip("CHIP12345")
                .conTatuaje("TAT999")
                .conFotoUrl("https://example.com/foto.jpg")
                .conAlergias("Penicilina")
                .conCondiciones("Displasia leve")
                .conActivo(true)
                .conNombreEspecie("Canino")
                .conNombreRaza("Labrador")
                .conNombreDueno("Juan Perez")
                .build();

        assertNotNull(mascota);
        assertEquals(10, mascota.getId());
        assertEquals(5, mascota.getDuenoId());
        assertEquals("Firulais", mascota.getNombre());
        assertEquals(1, mascota.getEspecieId());
        assertEquals(2, mascota.getRazaId());
        assertEquals(nacimiento, mascota.getFechaNacimiento());
        assertEquals(SexoEnum.MACHO, mascota.getSexo());
        assertEquals(12.5, mascota.getPesoActual(), 0.001);
        assertEquals("CHIP12345", mascota.getMicrochip());
        assertEquals("TAT999", mascota.getTatuaje());
        assertEquals("https://example.com/foto.jpg", mascota.getFotoUrl());
        assertEquals("Penicilina", mascota.getAlergias());
        assertEquals("Displasia leve", mascota.getCondicionesPreexistentes());
        assertTrue(mascota.isActivo());
        assertEquals("Canino", mascota.getNombreEspecie());
        assertEquals("Labrador", mascota.getNombreRaza());
        assertEquals("Juan Perez", mascota.getNombreDueno());
    }

    @Test
    public void testValoresPorDefecto() {
        Mascota mascota = new Mascota();
        assertTrue(mascota.isActivo());
        assertEquals(SexoEnum.MACHO, mascota.getSexo());
        assertNotNull(mascota.getFechaRegistro());
    }

    @Test
    public void testGetEdadCalculada() {
        Mascota m1 = Mascota.builder()
                .conFechaNacimiento(LocalDate.now().minusYears(2).minusMonths(4))
                .build();
        assertTrue(m1.getEdadCalculada().contains("2 anos"));

        Mascota m2 = Mascota.builder()
                .conFechaNacimiento(LocalDate.now().minusMonths(6))
                .build();
        assertTrue(m2.getEdadCalculada().contains("6 meses"));

        Mascota m3 = new Mascota();
        assertEquals("Desconocida", m3.getEdadCalculada());
    }
}
