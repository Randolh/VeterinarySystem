# Documentación de la Base de Datos - Happy Feet

**Base de Datos:** `happy_feet_veterinaria`  
**Motor:** MySQL 8.0+ / InnoDB (`utf8mb4_unicode_ci`)  
**Total de Tablas:** 31 tablas relacionales  

Esta documentación resume la estructura de la base de datos dividida en sus 5 módulos funcionales, correspondiente a los diagramas ubicados en [ER Diagrams/](<ER Diagrams>).

---

## 1. Pacientes, Dueños y Catálogos Base

Gestiona los tutores legales, el expediente biológico de las mascotas, cambios de titularidad y el equipo médico veterinario.

![Diagrama Pacientes y Dueños](<ER Diagrams/Pacientes, Dueños y Catálogos Base.png>)

| Tabla | Descripción | Clave Primaria (PK) | Relaciones Principales (FK) |
|---|---|---|---|
| `especies` | Catálogo base de especies (Canino, Felino, etc.). | `id` | - |
| `razas` | Catálogo de razas por especie. | `id` | `especie_id` ➔ `especies(id)` |
| `duenos` | Registro de clientes/tutores, contacto y teléfono. | `id` | Email único (`UNIQUE`) |
| `veterinarios` | Personal médico veterinario, licencia y horarios. | `id` | Licencia única (`UNIQUE`) |
| `mascotas` | Ficha del paciente (nombre, peso, microchip, foto). | `id` | `dueno_id` ➔ `duenos(id)`<br>`especie_id` ➔ `especies(id)`<br>`raza_id` ➔ `razas(id)` |
| `historial_transferencias_mascota` | Registro de traspaso legal de una mascota a otro dueño. | `id` | `mascota_id` ➔ `mascotas(id)`<br>`dueno_anterior_id` ➔ `duenos(id)`<br>`dueno_nuevo_id` ➔ `duenos(id)` |

---

## 2. Citas, Consultas e Historial Médico

Administra la agenda médica, atención clínica general, prescripción de fármacos, cirugías e insumos utilizados en quirófano.

![Diagrama Citas y Atención Clínica](<ER Diagrams/Citas, Consultas e Historial Médico.png>)

| Tabla | Descripción | Clave Primaria (PK) | Relaciones Principales (FK) |
|---|---|---|---|
| `servicios` | Catálogo de servicios ofrecidos y precios base. | `id` | Código único (`UNIQUE`) |
| `cita_estados` | Estados del flujo de citas (Programada, Finalizada, etc.). | `id` | Nombre único (`UNIQUE`) |
| `citas` | Agenda de citas médicas por paciente y profesional. | `id` | `mascota_id` ➔ `mascotas(id)`<br>`veterinario_id` ➔ `veterinarios(id)`<br>`servicio_id` ➔ `servicios(id)`<br>`estado_id` ➔ `cita_estados(id)` |
| `consultas_medicas` | Atención clínica: signos vitales, diagnóstico y tratamiento. | `id` | `cita_id` ➔ `citas(id)`<br>`mascota_id` ➔ `mascotas(id)`<br>`veterinario_id` ➔ `veterinarios(id)` |
| `historial_medico` | Resumen histórico unificado de atenciones médicas. | `id` | `mascota_id` ➔ `mascotas(id)`<br>`consulta_id` ➔ `consultas_medicas(id)` |
| `prescripciones` | Medicamentos recetados con dosis y frecuencia. | `id` | `consulta_id` ➔ `consultas_medicas(id)`<br>`producto_id` ➔ `inventario(id)` |
| `procedimientos_especiales` | Registro quirúrgico: protocolo pre/postoperatorio. | `id` | `consulta_id` ➔ `consultas_medicas(id)`<br>`mascota_id` ➔ `mascotas(id)`<br>`cirujano_id` ➔ `veterinarios(id)` |
| `procedimiento_insumos` | Materiales y fármacos consumidos en el procedimiento. | `id` | `procedimiento_id` ➔ `procedimientos_especiales(id)`<br>`producto_id` ➔ `inventario(id)` |

---

## 3. Inventario, Farmacia e Insumos Médicos

Controla existencias de bodega, lotes, fechas de vencimiento, alertas automáticas de reabastecimiento y movimientos de Kardex.

![Diagrama Inventario y Farmacia](<ER Diagrams/Inventario, Farmacia e Insumos Médicos.png>)

| Tabla | Descripción | Clave Primaria (PK) | Relaciones Principales (FK) |
|---|---|---|---|
| `producto_tipos` | Categorías de productos (Medicamento, Vacuna, Material). | `id` | - |
| `proveedores` | Datos de laboratorios y distribuidores comerciales. | `id` | NIT/Documento único (`UNIQUE`) |
| `inventario` | Fármacos e insumos con lote, stock, costos y caducidad. | `id` | `tipo_id` ➔ `producto_tipos(id)`<br>`proveedor_id` ➔ `proveedores(id)` |
| `inventario_movimientos` | Asientos de Kardex (entradas, salidas, mermas, ajustes). | `id` | `producto_id` ➔ `inventario(id)` |
| `historico_precios_inventario` | Auditoría de variaciones históricas en costos y precios. | `id` | `producto_id` ➔ `inventario(id)` |
| `alertas_sistema` | Notificaciones automáticas (stock mínimo, vencimientos). | `id` | `producto_id` ➔ `inventario(id)` |

---

## 4. Facturación y Club de Fidelización

Gestiona el cobro fiscal de servicios y productos, medios de pago y el programa de puntos de clientes leales.

![Diagrama Facturación y Club](<ER Diagrams/Facturación y Club de Fidelización.png>)

| Tabla | Descripción | Clave Primaria (PK) | Relaciones Principales (FK) |
|---|---|---|---|
| `facturas` | Cabecera del comprobante: cliente, subtotal, IVA, total. | `id` | `dueno_id` ➔ `duenos(id)`<br>Número de factura único (`UNIQUE`) |
| `factura_detalles` | Renglones de venta: servicios o artículos dispensados. | `id` | `factura_id` ➔ `facturas(id)`<br>`servicio_id` ➔ `servicios(id)`<br>`producto_id` ➔ `inventario(id)` |
| `club_mascotas` | Membresía del cliente (nivel: Bronce, Plata, Oro, Platino). | `id` | `dueno_id` ➔ `duenos(id)` (`UNIQUE`) |
| `transacciones_puntos` | Historial de puntos acumulados o redimidos. | `id` | `club_id` ➔ `club_mascotas(id)`<br>`factura_id` ➔ `facturas(id)` |
| `beneficios_club` | Catálogo de premios y descuentos disponibles por puntos. | `id` | Código único (`UNIQUE`) |
| `canjes_beneficios` | Asientos de redención de puntos por clientes. | `id` | `club_id` ➔ `club_mascotas(id)`<br>`beneficio_id` ➔ `beneficios_club(id)` |

---

## 5. Adopciones y Jornadas de Vacunación

Modela los programas de impacto comunitario: rescate y adopción de animales y vacunación masiva comunitaria.

![Diagrama Adopciones y Vacunación](<ER Diagrams/Adopciones y Jornadas de Vacunación.png>)

| Tabla | Descripción | Clave Primaria (PK) | Relaciones Principales (FK) |
|---|---|---|---|
| `mascotas_adopcion` | Publicación de animales rescatados disponibles. | `id` | `mascota_id` ➔ `mascotas(id)` |
| `adopciones` | Contratos legales de entrega a nuevos tutores. | `id` | `adopcion_mascota_id` ➔ `mascotas_adopcion(id)`<br>`adoptante_dueno_id` ➔ `duenos(id)` |
| `jornadas_vacunacion` | Eventos y campañas comunitarias de vacunación. | `id` | - |
| `registro_jornada_vacunacion` | Registro masivo Fast-Track de dosis aplicadas. | `id` | `jornada_id` ➔ `jornadas_vacunacion(id)`<br>`mascota_id` ➔ `mascotas(id)`<br>`dueno_id` ➔ `duenos(id)`<br>`vacuna_producto_id` ➔ `inventario(id)` |

---

## 6. Lógica Programada en Base de Datos (Triggers y Eventos)

* **Deducción de Inventario (`trg_deducir_stock_prescripcion` / `trg_deducir_stock_insumo_procedimiento`):** Descuenta automáticamente las existencias en `inventario` y asienta el movimiento en `inventario_movimientos` al prescribir o usar insumos en cirugías.
* **Control de Stock Mínimo (`trg_inventario_stock_minimo_update`):** Genera una notificación en `alertas_sistema` si el stock cae por debajo del umbral de seguridad.
* **Bloqueo de Caducidad (`trg_bloquear_uso_medicamento_vencido`):** Rechaza con error (`SIGNAL SQLSTATE`) cualquier salida de insumos cuya fecha de vencimiento sea menor o igual a `CURDATE()`.
* **Antisolapamiento de Citas (`trg_validar_solapamiento_citas`):** Impide agendar dos citas al mismo profesional con un margen menor a 30 minutos.
* **Revisión Diaria (`evt_verificar_vencimientos_diarios`):** Evento programado de MySQL que alerta sobre lotes que vencen en los próximos 30 días.

---

## 7. Despliegue de Scripts SQL

Para inicializar la base de datos completa con estructura, disparadores y datos de prueba, ejecutar en orden:

```bash
mysql -u root -p < scripts_sql/01_ddl_schema.sql
mysql -u root -p happy_feet_veterinaria < scripts_sql/02_programacion_bd.sql
mysql -u root -p happy_feet_veterinaria < scripts_sql/03_dml_seeds.sql
```
*(O ejecutar el script unificado `scripts_sql/init_all.sql`).*

