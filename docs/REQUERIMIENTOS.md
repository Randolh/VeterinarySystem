# Especificación de Requerimientos del Sistema (ERS) - VeterinarySystem

**Proyecto:** Sistema de Gestión Integral para la Veterinaria "Happy Feet" (`VeterinarySystem`)  
**Autor:** Randolh ([github.com/Randolh](https://github.com/Randolh))  
**Estándar de Redacción:** Basado en IEEE 830 / ISO/IEC/IEEE 29148  
**Fecha de Emisión:** Noviembre 2026  

---

## 1. Introducción

Este documento especifica los requerimientos funcionales y no funcionales del sistema, organizados por módulos de negocio y ordenados cronológicamente según su ciclo de desarrollo y trazabilidad en el repositorio Git.

---

## 2. Requerimientos Funcionales (RF)

### Módulo 1: Gestión de Mascotas y Dueños
- **RF-01 (Alta de Propietarios):** El sistema debe registrar propietarios con nombre completo, documento de identidad, dirección física, teléfono, contacto de emergencia y correo electrónico único verificado por expresión regular. *(Prioridad: Alta)*
- **RF-02 (Registro de Mascotas con Builder):** El sistema debe permitir el alta de pacientes asociándolos a un único dueño, seleccionando especie y raza de catálogo, peso actual con validación biológica (0.01 a 999 kg), sexo, alergias y condiciones preexistentes. *(Prioridad: Alta)*
- **RF-03 (Identificación Única):** El sistema debe soportar identificadores unívocos por código de microchip o tatuaje, y enlace opcional a fotografía. *(Prioridad: Media)*
- **RF-04 (Cálculo de Edad Estimada):** El sistema debe permitir registrar la fecha de nacimiento exacta o calcularla automáticamente a partir de la edad estimada ingresada en años y meses. *(Prioridad: Media)*
- **RF-05 (Transferencia de Propiedad):** El sistema debe ejecutar el traspaso formal de una mascota a un nuevo titular legal, guardando una traza histórica auditable del motivo y fecha de transferencia. *(Prioridad: Alta)*
- **RF-06 (Carnet de Salud Consolidado):** El sistema debe generar y exportar el carnet clínico oficial (`carnet_mascota_<id>.txt`) integrando antecedentes, historial de vacunas aplicadas e historial de cirugías registradas. *(Prioridad: Alta)*

### Módulo 2: Personal Veterinario
- **RF-07 (Gestión de Médicos):** El sistema debe registrar veterinarios con tarjeta profesional/licencia única, especialidad médica (Cirugía, Medicina Interna, etc.) y horario laboral. *(Prioridad: Media)*
- **RF-08 (Disponibilidad y Estado):** El sistema debe permitir la baja lógica de profesionales sin perder el historial médico asociado. *(Prioridad: Media)*

### Módulo 3: Servicios Médicos y Citas
- **RF-09 (Agendamiento de Citas):** El sistema debe programar citas asociando paciente, profesional, servicio, fecha y hora. *(Prioridad: Alta)*
- **RF-10 (Control de Solapamiento):** El sistema debe rechazar automáticamente citas que coincidan con la agenda de un veterinario dentro de una ventana de protección médica de 30 minutos. *(Prioridad: Alta)*
- **RF-11 (Ciclo de Vida de Citas - Patrón State):** El sistema debe gestionar las transiciones de estado de una cita: *Programada -> Confirmada -> En Curso -> Finalizada / Cancelada / No Asistió*. *(Prioridad: Media)*

### Módulo 4: Consultas Médicas, Cirugías y Farmacia Clínica
- **RF-12 (Registro de Consulta Médica):** El sistema debe capturar anamnesis, constantes fisiológicas (peso, temperatura), diagnóstico y prescripción médica estructurada (dosis, frecuencia, duración). *(Prioridad: Alta)*
- **RF-13 (Protocolo Quirúrgico):** El sistema debe registrar procedimientos quirúrgicos capturando ayuno/anestesia preoperatoria, descripción de la cirugía, complicaciones, postoperatorio y fecha de control. *(Prioridad: Alta)*
- **RF-14 (Deducción Atómica de Inventario):** El sistema debe descontar automáticamente del inventario los fármacos prescritos y los insumos utilizados en cirugías mediante disparadores relacionales (*triggers*), registrando el movimiento en el Kardex. *(Prioridad: Alta)*
- **RF-15 (Facturación Inmediata al Cierre):** El sistema debe ofrecer la opción de facturar inmediatamente al concluir una consulta o procedimiento, precargando honorarios y medicamentos consumidos. *(Prioridad: Alta)*

### Módulo 5: Inventario, Bodega y Proveedores
- **RF-16 (Control de Existencias):** El sistema debe gestionar insumos clasificándolos por lote, fecha de vencimiento, stock actual, stock mínimo, costo y precio de venta. *(Prioridad: Alta)*
- **RF-17 (Bloqueo Estricto de Vencidos):** El sistema debe impedir la prescripción, venta o uso quirúrgico de cualquier producto con fecha de caducidad cumplida (`fecha_vencimiento <= fecha_actual`). *(Prioridad: Alta)*
- **RF-18 (Auditoría Física y Ajustes):** El sistema debe permitir registrar entradas por compra, salidas por merma y auditorías físicas de estantería calculando discrepancias de stock. *(Prioridad: Media)*
- **RF-19 (Gestión de Proveedores):** El sistema debe registrar y relacionar proveedores comerciales con los suministros de farmacia. *(Prioridad: Media)*

### Módulo 6: Alertas del Sistema y Notificaciones
- **RF-20 (Notificación de Stock Mínimo - Observer):** El sistema debe emitir alertas automáticas e instantáneas cuando un producto alcance o descienda de su nivel mínimo de seguridad. *(Prioridad: Alta)*
- **RF-21 (Alertas de Vencimiento Próximo):** El sistema debe detectar y listar productos que caduquen en los próximos 30 días mediante verificaciones automáticas de base de datos. *(Prioridad: Alta)*
- **RF-22 (Centro de Notificaciones y Contador en Menú):** El sistema debe reflejar dinámicamente en el menú principal la cantidad de notificaciones pendientes de atención (`[!] TIENE X NOTIFICACION(ES) PENDIENTE(S)`). *(Prioridad: Media)*

### Módulo 7: Facturación, Descuentos y Finanzas
- **RF-23 (Emisión de Facturas):** El sistema debe generar facturas con desglose de ítems, subtotal, IVA parametrizable (19%) y total. *(Prioridad: Alta)*
- **RF-24 (Descuentos por Membresía - Strategy):** El sistema debe aplicar automáticamente descuentos según el nivel de fidelización del cliente en el Club de Mascotas (Bronce, Plata, Oro, Platino). *(Prioridad: Alta)*
- **RF-25 (Acumulación de Puntos):** El sistema debe sumar 1 punto al Club de Mascotas por cada unidad monetaria facturada para su posterior canje. *(Prioridad: Media)*
- **RF-26 (Exportación Dual TXT/PDF):** El sistema debe emitir el comprobante fiscal en texto plano legible (`factura_FAC-XXXX.txt`) y simultáneamente en formato digital PDF. *(Prioridad: Media)*

### Módulo 8: Actividades Especiales y Comunitarias
- **RF-27 (Días de Adopción):** El sistema debe listar mascotas aptas para adopción y formalizar la entrega emitiendo el Contrato Legal de Adopción Responsable (TXT y PDF). *(Prioridad: Media)*
- **RF-28 (Jornadas de Vacunación Fast-Track):** El sistema debe proporcionar un flujo continuo de captura ultrarrápida (< 3 segundos) con descuento automático de biológicos para pacientes propios y comunitarios. *(Prioridad: Media)*
- **RF-29 (Canje de Beneficios del Club):** El sistema debe permitir a los clientes debitar sus puntos disponibles a cambio de servicios o productos del catálogo del club. *(Prioridad: Media)*

### Módulo 9: Reportes Gerenciales y Análisis
- **RF-30 (Servicios más Demandados):** El sistema debe procesar con Java Stream API el ranking de servicios médicos más solicitados con porcentaje de recaudación. *(Prioridad: Media)*
- **RF-31 (Rendimiento por Veterinario):** El sistema debe generar métricas de consultas atendidas, cirugías realizadas e ingresos generados por cada profesional. *(Prioridad: Media)*
- **RF-32 (Consolidado Fiscal y Financiero):** El sistema debe consolidar la recaudación mensual con desglose impositivo y exportación a PDF. *(Prioridad: Media)*

### Módulo 10: Asistente y Usabilidad en Consola
- **RF-33 (Diagnóstico de Conexión):** El sistema debe incluir un asistente para probar credenciales MySQL, cambiar puertos y alternar entre modo en línea y fuera de línea sin reiniciar. *(Prioridad: Alta)*
- **RF-34 (Limpieza Manual de Pantalla):** El sistema debe permitir ingresar el comando `clear` (o `limpiar`, `:clear`) en cualquier menú numérico para despejar la terminal y redibujar el menú. *(Prioridad: Baja)*
- **RF-35 (Cancelación Universal de Formularios):** El sistema debe permitir abortar cualquier formulario ingresando `:c`, `:cancel` o `:q` para retornar al menú de forma segura sin guardar cambios incompletos. *(Prioridad: Media)*
- **RF-36 (Paginación de Listados):** El sistema debe paginar tablas extensas permitiendo avanzar (`S`), retroceder (`A`), saltar de página (`P`) y volver (`0`). *(Prioridad: Media)*

---

## 3. Requerimientos No Funcionales (RNF)

- **RNF-01 (Arquitectura y Desacoplamiento):** El sistema debe estructurarse mediante *Package by Feature* y patrón Modelo-Vista-Controlador (MVC), separando la interfaz de consola de las reglas de negocio y del acceso a datos.
- **RNF-02 (Transaccionalidad e Integridad ACID):** Toda operación clínica que afecte stock o facturación debe ejecutarse bajo transacciones atómicas en MySQL (`InnoDB`), impidiendo inconsistencias contables o de bodega.
- **RNF-03 (Seguridad contra Inyección SQL):** El 100% de las consultas de manipulación de datos en JDBC deben implementarse con `PreparedStatement` parametrizados.
- **RNF-04 (Tolerancia a Fallos y Modo Offline):** La aplicación no debe colapsar ante la caída o indisponibilidad del servidor MySQL; debe degradar su servicio a modo offline y habilitar el asistente de diagnóstico.
- **RNF-05 (Auditoría y Bitácora Persistente):** Los eventos del sistema, inicios de sesión, transacciones y errores no controlados deben registrarse en un archivo de bitácora local (`app_veterinaria.log`) y en la tabla de auditoría.
- **RNF-06 (Portabilidad Multiplataforma):** El software debe ser compatible con Java 17 LTS y ejecutarse idénticamente en Linux, macOS y Windows, adaptando comandos de consola según el sistema operativo.
- **RNF-07 (Usabilidad de Interfaz en Consola):** Los menús deben presentar una paleta ANSI estandarizada con compatibilidad con la variable de entorno `NO_COLOR`, recuadros de ancho uniforme (76 columnas) y soporte para saltos multilínea.
- **RNF-08 (Mantenibilidad y SOLID):** El código debe aplicar principios SOLID (Responsabilidad Única, Abierto/Cerrado para estrategias de descuento e Inversión de Dependencias).

---

## 4. Matriz de Trazabilidad y Evolución según Git

A continuación se detalla cómo se fueron abordando e implementando los requerimientos a lo largo del historial de ramas y commits del repositorio:

| Fase / Rama Git | Requerimientos Abordados | Descripción del Trabajo Realizado |
|---|:---:|---|
| **`feat/db-ddl-schema`** | RNF-02, RNF-03 | Creación del esquema relacional DDL (31 tablas en MySQL, PKs, FKs, restricciones). |
| **`feat/db-programmability`** | RF-10, RF-14, RF-20, RNF-02 | Programación de *Triggers* de inventario y stock mínimo, procedimientos almacenados y eventos. |
| **`feat/db-dml-seeds`** | RF-02, RF-07, RF-16 | Inserción de catálogos semilla (especies, razas, estados de citas, insumos base). |
| **`feat/core-config`** | RF-33, RNF-04, RNF-06 | Implementación de `ConexionBD` (Singleton JDBC), `database.properties` y asistente de diagnóstico. |
| **`feat/core-utils-logging`** | RNF-05, RNF-07 | Implementación de `LoggerManager` (`app_veterinaria.log`), excepciones de dominio y utilidades ANSI. |
| **`feat/modulo-duenos`** | RF-01 | Módulo completo de propietarios (validación regex de email y unicidad). |
| **`feat/modulo-mascotas`** | RF-02, RF-03, RF-05 | Modelo `Mascota.Builder`, catálogos dependientes y transferencias. |
| **`feat/modulo-veterinarios`**| RF-07, RF-08 | Gestión de médicos veterinarios, matrículas y disponibilidad. |
| **`feat/modulo-citas-agenda`** | RF-09, RF-10, RF-11 | Agenda médica con prevención de solapamiento (+/- 30 min) y patrón State. |
| **`feat/modulo-inventario`** | RF-16, RF-17, RF-18, RF-19 | Control de existencias, Kardex, gestión de proveedores y bloqueo de productos vencidos. |
| **`feat/modulo-consultas`** | RF-12, RF-13, RF-14 | Consultas generales, cirugías con anestesia/postoperatorio y deducción atómica de insumos. |
| **`feat/modulo-alertas`** | RF-20, RF-21, RNF-08 | Centro de alertas reactivo con patrón Observer conectado a eventos de inventario. |
| **`feat/modulo-facturacion`** | RF-23, RF-24, RF-25, RF-26 | Facturación fiscal con patrón Strategy (Club de Mascotas) y exportación dual TXT y PDF. |
| **`feat/modulo-actividades`** | RF-27, RF-28, RF-29 | Días de adopción (contratos PDF), jornadas de vacunación Fast-Track y fidelización. |
| **`feat/modulo-reportes`** | RF-30, RF-31, RF-32 | Reportes analíticos gerenciales procesados con Java Stream API y exportables a PDF. |
| **`feat/menu-principal`** | RNF-01, RNF-04 | Ensamble general con carga perezosa (*Lazy Loading*) y tolerancia a fallos. |
| **`feat/fix-features`** | RF-04, RF-06, RF-15, RF-22, RF-34, RF-35, RF-36 | **Refactorizaciones finales:**<br>• Opción de edad estimada al registrar mascota.<br>• Historial de vacunas y cirugías en carnet.<br>• Facturación inmediata al cerrar atención clínica.<br>• Delegación definitiva de deducción a disparadores MySQL.<br>• Paginación interactiva (`P`).<br>• Cancelación de formularios con `:c` y limpieza con `clear`.<br>• Indicador de notificaciones pendientes en menú principal. |
| **`feat/documentation`** | RNF-01, RNF-06, RNF-08 | Documentación técnica completa en `README.md` y `docs/REQUERIMIENTOS.md`. |

