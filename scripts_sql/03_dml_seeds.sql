-- Datos semilla para pruebas del sistema Happy Feet
USE happy_feet_veterinaria;

-- Desactivar temporalmente verificacion de claves foraneas para carga limpia
SET FOREIGN_KEY_CHECKS = 0;

-- 1. Especies
INSERT INTO especies (id, nombre, descripcion) VALUES
(1, 'Canino', 'Perros domesticos (Canis lupus familiaris)'),
(2, 'Felino', 'Gatos domesticos (Felis catus)'),
(3, 'Ave', 'Aves domesticas y de compania'),
(4, 'Roedor', 'Pequenos mamiferos (hamsters, cuyos, cobayos)'),
(5, 'Reptil', 'Reptiles domesticos (iguanas, tortugas)'),
(6, 'Conejo', 'Conejos domesticos (Oryctolagus cuniculus)');

-- 2. Razas comunes
INSERT INTO razas (id, especie_id, nombre, caracteristicas) VALUES
(1, 1, 'Labrador Retriever', 'Tamano grande, amigable, energetico'),
(2, 1, 'Golden Retriever', 'Tamano grande, inteligente, leal'),
(3, 1, 'Pastor Aleman', 'Tamano grande, inteligente, guardian'),
(4, 1, 'Bulldog Frances', 'Tamano pequeno, cariñoso, docil'),
(5, 1, 'Chihuahua', 'Tamano miniatura, vivaz, alerta'),
(6, 1, 'Poodle', 'Inteligente, hipoalergenico'),
(7, 1, 'Criollo / Mestizo', 'Cruza de razas caninas'),
(8, 2, 'Siames', 'Pelaje claro con puntos oscuros, vocal y activo'),
(9, 2, 'Persa', 'Pelaje largo y denso, temperamento tranquilo'),
(10, 2, 'Maine Coon', 'Tamano grande, pelaje grueso, sociable'),
(11, 2, 'Bengali', 'Pelaje atigrado tipo leopardo, muy activo'),
(12, 2, 'Comun Europeo', 'Mestizo felino resistente y equilibrado'),
(13, 3, 'Canario', 'Pequeno cantor, plumaje amarillo brillante'),
(14, 3, 'Periquito Australiano', 'Ave sociable y gregaria'),
(15, 3, 'Loro Amazonas', 'Inteligente y longevo'),
(16, 4, 'Hamster Sirio', 'Pequeno, solitario y nocturno'),
(17, 4, 'Cobayo / Cuy', 'Docil, sociable, herbivoro estricto'),
(18, 5, 'Iguana Verde', 'Reptil arboricola tropical'),
(19, 5, 'Tortuga de Orejas Rojas', 'Semi-acuatica resistente'),
(20, 6, 'Conejo Enano Belier', 'Orejas caidas, temperamento pacifico');

-- 3. Tipos de productos
INSERT INTO producto_tipos (id, nombre, descripcion) VALUES
(1, 'Medicamento', 'Farmacos y antibioticos veterinarios'),
(2, 'Vacuna', 'Biologicos para inmunizacion'),
(3, 'Material Quirurgico', 'Material esteril e instrumental descartable'),
(4, 'Insumo General', 'Material de curacion, antisepsia y limpieza'),
(5, 'Alimento Especializado', 'Dietas veterinarias y de prescripcion'),
(6, 'Accesorio Clinico', 'Collares isabelinos, bozales, correas');

-- 4. Tipos de eventos medicos
INSERT INTO evento_tipos (id, nombre, descripcion) VALUES
(1, 'Vacunacion', 'Aplicacion de biologicos y vacunas'),
(2, 'Consulta General', 'Evaluacion medica clinica estandar'),
(3, 'Cirugia', 'Procedimiento quirurgico invasivo o mayor'),
(4, 'Desparasitacion', 'Tratamiento interno o externo contra parasitos'),
(5, 'Control de Peso', 'Monitoreo de peso y condicion corporal'),
(6, 'Examen de Laboratorio', 'Hemogramas, coprologicos y bioquimicas'),
(7, 'Radiografia / Ecografia', 'Estudios de imagenologia diagnostica'),
(8, 'Emergencia', 'Atencion de urgencia vital'),
(9, 'Profilaxis Dental', 'Limpieza y destartraje ultrasonico');

-- 5. Estados de citas
INSERT INTO cita_estados (id, nombre, descripcion) VALUES
(1, 'Programada', 'Cita agendada pendiente de atencion'),
(2, 'Confirmada', 'Cita reconfirmada por el cliente'),
(3, 'En Curso', 'Paciente en atencion medica'),
(4, 'Finalizada', 'Consulta completada satisfactoriamente'),
(5, 'Cancelada', 'Cita cancelada con registro de motivo'),
(6, 'No Asistio', 'El dueno y la mascota no se presentaron');

-- 6. Proveedores
INSERT INTO proveedores (id, nombre_empresa, ruc_nit, contacto_nombre, telefono, email, direccion, sitio_web, activo) VALUES
(1, 'Distribuidora Zoosanitaria del Norte SAS', '900123456-1', 'Carlos Mendoza', '3104567890', 'ventas@zoosanitaria.com', 'Avenida Principal #45-12, Bogota', 'https://zoosanitaria.com', TRUE),
(2, 'Laboratorios BioVeterinarios Andinos', '900234567-2', 'Patricia Herrera', '3157891234', 'pedidos@biovetandinos.com', 'Calle Industrial #12-34, Medellin', 'https://biovetandinos.com', TRUE),
(3, 'Farmaceutica Animal Panamericana', '900345678-3', 'Fernando Gomez', '3209876543', 'contacto@farmaanimal.com', 'Carrera 7 #100-20, Cali', 'https://farmaanimal.com', TRUE),
(4, 'Suministros Medico-Quirurgicos VetMed', '900456789-4', 'Lucia Rodriguez', '3006549870', 'ventas@vetmedsupply.com', 'Parque Tecnologico Lote 8, Barranquilla', 'https://vetmedsupply.com', TRUE);

-- 7. Personal veterinario
INSERT INTO veterinarios (id, tipo_documento, numero_documento, nombre_completo, licencia_profesional, especialidad, telefono, email, fecha_contratacion, activo) VALUES
(1, 'CC', '1015234890', 'Dra. Valentina Morales Sanchez', 'MPV-2018-0941', 'Medicina Interna y Diagnostico', '3112345678', 'valentina.morales@happyfeet.com', '2022-01-15', TRUE),
(2, 'CC', '1020456789', 'Dr. Mateo Restrepo Cifuentes', 'MPV-2016-0422', 'Cirugia y Traumatologia Veterinaria', '3123456789', 'mateo.restrepo@happyfeet.com', '2021-06-01', TRUE),
(3, 'CC', '1035678901', 'Dra. Camila Vargas Ortiz', 'MPV-2020-1123', 'Dermatologia y Alergologia', '3134567890', 'camila.vargas@happyfeet.com', '2023-03-10', TRUE),
(4, 'CC', '1048901234', 'Dr. Nicolas Benitez Silva', 'MPV-2019-0755', 'Anestesiologia y Cuidados Criticos', '3145678901', 'nicolas.benitez@happyfeet.com', '2022-08-20', TRUE);

-- 8. Clientes y Duenos
INSERT INTO duenos (id, tipo_documento, numero_documento, nombre_completo, direccion, telefono, email, contacto_emergencia, fecha_registro, activo) VALUES
(1, 'CC', '1052345678', 'Andres Felipe Castano Morales', 'Calle 127 #48-20, Bogota', '3161112233', 'andres.castano@gmail.com', 'Hermana: 3169998877', '2023-01-10 10:00:00', TRUE),
(2, 'CC', '1063456789', 'Mariana Rivas Cardona', 'Carrera 15 #85-30, Bogota', '3172223344', 'mariana.rivas@hotmail.com', 'Mama: 3178887766', '2023-02-14 11:30:00', TRUE),
(3, 'CC', '1074567890', 'Diego Alejandro Quintero', 'Transversal 23 #102-15, Bogota', '3183334455', 'diego.quintero@yahoo.com', 'Esposa: 3187776655', '2023-03-05 09:15:00', TRUE),
(4, 'CC', '1085678901', 'Laura Sofia Gutierrez Pena', 'Diagonal 68 #30-45, Bogota', '3194445566', 'laura.gutierrez@outlook.com', 'Padre: 3196665544', '2023-04-20 14:00:00', TRUE),
(5, 'CC', '1096789012', 'Esteban Jose Mejia Velez', 'Calle 53 #13-80, Bogota', '3205556677', 'esteban.mejia@gmail.com', 'Hermano: 3204443322', '2023-05-18 16:45:00', TRUE);

-- 9. Pacientes y Mascotas
INSERT INTO mascotas (id, dueno_id, nombre, especie_id, raza_id, fecha_nacimiento, sexo, peso_actual, microchip, tatuaje, foto_url, alergias, condiciones_preexistentes, fecha_registro, activo) VALUES
(1, 1, 'Max', 1, 1, '2021-04-10', 'Macho', 32.50, 'MC-981098101', NULL, 'https://cdn.happyfeet.com/fotos/max_labrador.jpg', 'Ninguna conocida', 'Cirugia de rodilla previa superada', '2023-01-10 10:30:00', TRUE),
(2, 1, 'Luna', 2, 8, '2022-08-15', 'Hembra', 4.10, 'MC-981098102', NULL, 'https://cdn.happyfeet.com/fotos/luna_siamesa.jpg', 'Alergica al polen estacional', 'Ninguna', '2023-01-12 15:00:00', TRUE),
(3, 2, 'Rocky', 1, 4, '2020-11-20', 'Macho', 12.80, 'MC-981098103', 'TAT-BOG-401', 'https://cdn.happyfeet.com/fotos/rocky_frenchie.jpg', 'Sensibilidad a la proteina de pollo', 'Sindrome braquiocefalico leve', '2023-02-14 12:00:00', TRUE),
(4, 3, 'Simba', 2, 10, '2019-06-01', 'Macho', 7.90, 'MC-981098104', NULL, 'https://cdn.happyfeet.com/fotos/simba_mainecoon.jpg', 'Ninguna', 'Control preventivo renal anual', '2023-03-05 09:45:00', TRUE),
(5, 4, 'Toby', 1, 7, '2023-02-01', 'Macho', 18.20, 'MC-981098105', NULL, 'https://cdn.happyfeet.com/fotos/toby_mestizo.jpg', 'Ninguna', 'Rescatado sano y desparasitado', '2023-04-20 14:30:00', TRUE),
(6, 5, 'Kira', 1, 3, '2022-01-25', 'Hembra', 29.40, 'MC-981098106', 'TAT-BOG-890', 'https://cdn.happyfeet.com/fotos/kira_pastor.jpg', 'Sensibilidad cutanea a pulgas', 'Ninguna', '2023-05-18 17:00:00', TRUE);

-- 10. Servicios de la clinica
INSERT INTO servicios (id, nombre, descripcion, categoria, precio_base, duracion_estimada_minutos, activo) VALUES
(1, 'Consulta Medica General', 'Evaluacion clinica completa, peso, constantes fisiologicas y diagnostico', 'Consulta', 35.00, 30, TRUE),
(2, 'Consulta Medica de Urgencia', 'Atencion prioritaria por emergencia clinica o traumatismo', 'Consulta', 55.00, 45, TRUE),
(3, 'Vacunacion Antirrabica Canina/Felina', 'Inmunizacion antirrabica obligatoria con expedicion de certificado', 'Inmunizacion', 22.00, 15, TRUE),
(4, 'Vacunacion Polivalente Canina DHPP', 'Inmunizacion contra distemper, hepatitis, parvovirus y parainfluenza', 'Inmunizacion', 30.00, 20, TRUE),
(5, 'Desparasitacion Interna y Externa', 'Administracion de antiparasitario oral y pipeta segun peso', 'Medicina Preventiva', 18.00, 15, TRUE),
(6, 'Profilaxis Dental con Ultrasonido', 'Limpieza profunda de sarro, pulido dental y antisepsia bucal', 'Odontologia', 85.00, 60, TRUE),
(7, 'Esterilizacion Quirurgica (Osh / Orquiectomia)', 'Cirugia bajo anestesia general inhalatoria con monitoreo constante', 'Cirugia', 140.00, 90, TRUE),
(8, 'Radiografia Simple de Control', 'Toma de placa radiografica digital en 2 proyecciones', 'Imagenologia', 50.00, 30, TRUE);

-- 11. Inventario inicial de medicamentos, vacunas e insumos
INSERT INTO inventario (id, codigo_barras, nombre_producto, tipo_id, descripcion, fabricante, proveedor_id, cantidad_stock, stock_minimo, unidad_medida, precio_compra, precio_venta, lote, fecha_vencimiento, requiere_receta, activo) VALUES
(1, '770999001001', 'Amoxicilina + Acido Clavulanico 500mg', 1, 'Antibiotico de amplio espectro para caninos y felinos', 'Zoetis Pharma', 1, 45, 10, 'caja x 20 tab', 18.50, 32.00, 'AMX-2026-A', DATE_ADD(CURDATE(), INTERVAL 180 DAY), TRUE, TRUE),
(2, '770999001002', 'Meloxicam 0.5% Gotas 10ml', 1, 'Antiinflamatorio no esteroideo analgesico', 'Boehringer Ingelheim', 2, 30, 8, 'frasco gotero', 12.00, 24.50, 'MLX-2026-B', DATE_ADD(CURDATE(), INTERVAL 240 DAY), TRUE, TRUE),
(3, '770999001003', 'Cefalexina 600mg Tabletas', 1, 'Antibiotico de primera eleccion para infecciones de piel', 'BioVeterinarios Andinos', 2, 28, 10, 'caja x 14 tab', 15.00, 28.00, 'CFX-2026-C', DATE_ADD(CURDATE(), INTERVAL 210 DAY), TRUE, TRUE),
(4, '770999001004', 'Vacuna Antirrabica Canina / Felina', 2, 'Biologico inactivado contra el virus de la rabia', 'Virbac Salud Animal', 1, 60, 15, 'vial monodosis', 6.50, 15.00, 'VRB-RAB-2026', DATE_ADD(CURDATE(), INTERVAL 300 DAY), FALSE, TRUE),
(5, '770999001005', 'Vacuna Polivalente Canina DHPP+L', 2, 'Inmunizacion quintuple canina con leptospira', 'Zoetis Pharma', 1, 40, 12, 'vial monodosis', 11.00, 25.00, 'DHPP-2026-X', DATE_ADD(CURDATE(), INTERVAL 270 DAY), FALSE, TRUE),
(6, '770999001006', 'Jeringas Desechables 3ml con Aguja 21G', 4, 'Jeringa esteril de 3 piezas para aplicacion parenteral', 'VetMed Supply', 4, 250, 50, 'unidad', 0.20, 0.80, 'JER-3ML-2028', DATE_ADD(CURDATE(), INTERVAL 720 DAY), FALSE, TRUE),
(7, '770999001007', 'Gasas Quirurgicas Esteriles 10x10 cm', 4, 'Paquete x 5 gasas tejidas de algodon puro esteril', 'VetMed Supply', 4, 180, 40, 'paquete', 0.50, 1.50, 'GAS-EST-2028', DATE_ADD(CURDATE(), INTERVAL 600 DAY), FALSE, TRUE),
(8, '770999001008', 'Sutura Absorbible Vycril 3-0 con Aguja', 3, 'Hilo de poliglactina absorbible para cirugia de tejidos', 'Ethicon Vet', 4, 3, 5, 'sobre individual', 4.50, 12.00, 'SUT-VYC-LOW', DATE_ADD(CURDATE(), INTERVAL 365 DAY), FALSE, TRUE),
(9, '770999001009', 'Amoxicilina Suspension Pediatrica 250mg', 1, 'Lote proximo a vencer para test de alertas clinicas', 'Laboratorios Andinos', 2, 12, 5, 'frasco 60ml', 8.00, 16.00, 'AMX-EXP-01', DATE_ADD(CURDATE(), INTERVAL 18 DAY), TRUE, TRUE),
(10, '770999001010', 'Vacuna Triple Felina RCP', 2, 'Lote en ventana de caducidad cercana', 'Virbac Animal', 1, 15, 6, 'vial monodosis', 9.50, 22.00, 'FEL-EXP-02', DATE_ADD(CURDATE(), INTERVAL 25 DAY), FALSE, TRUE);

-- 12. Citas medicas iniciales
INSERT INTO citas (id, mascota_id, veterinario_id, fecha_hora, motivo, estado, notas, fecha_cancelacion, motivo_cancelacion, fecha_creacion) VALUES
(1, 1, 1, '2026-10-15 09:00:00', 'Revision general y refuerzo de esquema vacunal', 'Finalizada', 'Paciente en excelente condicion corporal, vacunas aplicadas con exito', NULL, NULL, '2026-10-10 08:30:00'),
(2, 3, 2, '2026-10-18 10:30:00', 'Dermatitis persistente en pliegues faciales', 'Finalizada', 'Se prescribe lavado con clorhexidina y antibioterapia oral', NULL, NULL, '2026-10-12 11:00:00'),
(3, 4, 1, '2026-10-22 14:00:00', 'Esterilizacion programada electiva', 'Finalizada', 'Procedimiento quirurgico completado sin incidentes', NULL, NULL, '2026-10-15 16:00:00'),
(4, 2, 3, DATE_ADD(NOW(), INTERVAL 2 DAY), 'Control dermatologico y revision de oidos', 'Programada', 'Cita agendada para control de seguimiento', NULL, NULL, NOW()),
(5, 6, 2, DATE_ADD(NOW(), INTERVAL 4 DAY), 'Valoracion de renguera en pata posterior derecha', 'Confirmada', 'Confirmado por cliente via telefonica', NULL, NULL, NOW()),
(6, 5, 1, '2026-10-20 16:00:00', 'Revision por posible ingestion de cuerpo extrano', 'Cancelada', 'Cliente cancelo por motivos de viaje imprevisto', '2026-10-19 18:20:00', 'Viaje de urgencia fuera de la ciudad', '2026-10-18 09:00:00');

-- 13. Consultas medicas completadas
INSERT INTO consultas_medicas (id, cita_id, mascota_id, veterinario_id, fecha_hora, motivo_visita, sintomas, diagnostico, tratamiento_indicaciones, recomendaciones, peso_registrado, temperatura, costo_consulta) VALUES
(1, 1, 1, 1, '2026-10-15 09:15:00', 'Revision general y vacunas anuales', 'Ninguno, paciente asintomatico y energico', 'Paciente clinicamente sano apto para inmunizacion', 'Aplicacion de vacuna DHPP y desparasitacion', 'Mantener hidratacion y evitar ejercicio vigoroso por 24h', 32.50, 38.6, 35.00),
(2, 2, 3, 2, '2026-10-18 10:45:00', 'Dermatitis en pliegues faciales', 'Prurito moderado, eritema en pliegues nasales y olor caracteristico', 'Pioderma superficial por sobrecrecimiento bacteriano', 'Cefalexina 600mg cada 12h x 10 dias y limpieza con gasa esteril', 'Secar bien los pliegues luego del bano o ingesta de agua', 12.80, 38.9, 35.00),
(3, 3, 4, 1, '2026-10-22 14:15:00', 'Preoperatorio para esterilizacion', 'Asintomatico', 'Apto para quirofano con analisis de sangre normales', 'Canalizacion con jeringa y sedacion previa', 'Ayuno estricto de solidos de 8 horas postcirugia', 7.90, 38.4, 35.00);

-- 14. Procedimientos especiales
INSERT INTO procedimientos_especiales (id, consulta_id, mascota_id, veterinario_id, tipo_procedimiento, nombre_procedimiento, fecha_programada, fecha_ejecucion, duracion_estimada_minutos, estado, notas_preoperatorias, notas_procedimiento, notas_postoperatorias, seguimiento_postoperatorio, proximo_control, costo_base) VALUES
(1, 3, 4, 2, 'Quirurgico', 'Orquiectomia Felina Bilateral', '2026-10-22 15:00:00', '2026-10-22 15:45:00', 45, 'Finalizado', 'Anestesia tiletamina-zolazepam inducida sin complicaciones', 'Incicion escrotal bilateral, ligadura de conductos espermaticos y hemostasia completa', 'Recuperacion anestesica suave en jaula termica', 'Uso de collar isabelino obligatorio durante 7 dias', '2026-10-29', 140.00),
(2, NULL, 6, 2, 'Odontologico', 'Profilaxis Dental con Destartraje Ultrasonico', DATE_ADD(NOW(), INTERVAL 7 DAY), NULL, 60, 'Programado', 'Paciente requerira ayuno de 8 horas y sedacion profunda', 'Limpieza ultrasonica de piezas dentales caninas', NULL, 'Monitoreo de tolerancia bucal postdestartraje', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 85.00);

-- 15. Prescripciones medicas
INSERT INTO prescripciones (id, consulta_id, procedimiento_id, producto_id, cantidad, dosis, frecuencia, duracion_dias, instrucciones, fecha_prescripcion) VALUES
(1, 2, NULL, 3, 2, '1 tableta de 600mg', 'Cada 12 horas', 10, 'Administrar junto con el alimento solido de la manana y noche', '2026-10-18 11:00:00'),
(2, 3, 1, 2, 1, '5 gotas orales', 'Cada 24 horas', 4, 'Analgesia postoperatoria directa en mucosa oral', '2026-10-22 16:00:00');

-- 16. Insumos utilizados en procedimientos
INSERT INTO procedimiento_insumos (id, procedimiento_id, producto_id, cantidad_utilizada, fecha_uso, observaciones) VALUES
(1, 1, 6, 2, '2026-10-22 15:10:00', 'Jeringas para induccion anestesica y fluidos'),
(2, 1, 7, 3, '2026-10-22 15:20:00', 'Gasas esteriles para hemostasia escrotal');

-- 17. Facturas y detalle comercial
INSERT INTO facturas (id, numero_factura, dueno_id, fecha_emision, subtotal, descuento, impuesto, total, metodo_pago, estado, observaciones) VALUES
(1, 'FAC-202610-001', 1, '2026-10-15 10:00:00', 60.00, 0.00, 11.40, 71.40, 'Tarjeta', 'Pagada', 'Facturacion de consulta general y vacuna polivalente canina'),
(2, 'FAC-202610-002', 2, '2026-10-18 11:30:00', 91.00, 4.55, 16.43, 102.88, 'Transferencia', 'Pagada', 'Descuento 5% socio Plata aplicado a servicios y farmacia'),
(3, 'FAC-202610-003', 3, '2026-10-22 17:00:00', 199.50, 19.95, 34.11, 213.66, 'Efectivo', 'Pagada', 'Descuento 10% socio Oro aplicado en cirugia y medicamentos');

INSERT INTO factura_detalles (id, factura_id, tipo_item, producto_id, servicio_id, referencia_id, descripcion, cantidad, precio_unitario, subtotal) VALUES
(1, 1, 'Servicio', NULL, 1, 1, 'Consulta Medica General', 1, 35.00, 35.00),
(2, 1, 'Producto', 5, NULL, 5, 'Vacuna Polivalente Canina DHPP+L', 1, 25.00, 25.00),
(3, 2, 'Servicio', NULL, 1, 2, 'Consulta Medica General', 1, 35.00, 35.00),
(4, 2, 'Producto', 3, NULL, 3, 'Cefalexina 600mg Tabletas', 2, 28.00, 56.00),
(5, 3, 'Servicio', NULL, 7, 1, 'Esterilizacion Quirurgica (Orquiectomia Felina)', 1, 140.00, 140.00),
(6, 3, 'Servicio', NULL, 1, 3, 'Consulta Preoperatoria', 1, 35.00, 35.00),
(7, 3, 'Producto', 2, NULL, 2, 'Meloxicam 0.5% Gotas 10ml', 1, 24.50, 24.50);

-- 18. Historial medico unificado
INSERT INTO historial_medico (id, mascota_id, fecha_evento, evento_tipo_id, descripcion, diagnostico, tratamiento_recomendado, veterinario_id, consulta_id, procedimiento_id) VALUES
(1, 1, '2026-10-15', 1, 'Aplicacion de refuerzo anual de vacuna polivalente canina', 'Paciente sano sin contraindicaciones', 'Vacuna DHPP aplicada', 1, 1, NULL),
(2, 3, '2026-10-18', 2, 'Consulta dermatologica por prurito y eritema facial', 'Pioderma en pliegues faciales', 'Cefalexina oral y curacion local', 2, 2, NULL),
(3, 4, '2026-10-22', 3, 'Orquiectomia bilateral completada bajo anestesia general', 'Esterilizacion exitosa', 'Analgesia con Meloxicam y reposo', 2, 3, 1);

-- 19. Mascotas para adopcion y formalizacion
INSERT INTO mascotas_adopcion (id, mascota_id, historia, temperamento, necesidades_especiales, fecha_ingreso, estado, fecha_adopcion, dueno_adoptante_id, foto_adicional_url) VALUES
(1, 5, 'Rescatado de una zona rural vulnerable. Muy agradecido y cariñoso', 'Sociable con personas y otros perros, activo y juguetón', 'Requiere espacio para pasear y ejercicio diario', '2026-03-01', 'Adoptada', '2026-04-20', 4, 'https://cdn.happyfeet.com/adopciones/toby_perfil.jpg'),
(2, 6, 'Entregada por dueno anterior por motivo de mudanza al exterior', 'Muy obediente, entrenada en comandos basicos y protectora', 'Alimentacion hipoalergenica por sensibilidad en piel', '2026-05-01', 'Disponible', NULL, NULL, 'https://cdn.happyfeet.com/adopciones/kira_perfil.jpg');

INSERT INTO adopciones (id, mascota_adopcion_id, dueno_id, fecha_adopcion, contrato_texto, condiciones_especiales, seguimiento_requerido, fecha_primer_seguimiento) VALUES
(1, 1, 4, '2026-04-20', 'Contrato formal de adopcion responsable. El adoptante se compromete a brindar atencion veterinaria, alimentacion y bienestar.', 'Compromiso de paseos diarios y envio de fotos mensuales', TRUE, '2026-05-20');

-- 20. Jornadas de vacunacion comunitaria
INSERT INTO jornadas_vacunacion (id, nombre, fecha, hora_inicio, hora_fin, ubicacion, descripcion, capacidad_maxima, estado) VALUES
(1, 'Jornada Barrial de Vacunacion Antirrabica Norte', '2026-09-20', '08:00:00', '14:00:00', 'Parque Central Los Cedros, Bogota', 'Campana comunitaria gratuita de inmunizacion contra la rabia para caninos y felinos', 150, 'Finalizada'),
(2, 'Mega Jornada de Vacunacion y Salud Animal 2026', DATE_ADD(CURDATE(), INTERVAL 15 DAY), '08:30:00', '16:00:00', 'Plaza Simon Bolivar, Bogota', 'Campana masiva con vacunacion, desparasitacion y valoracion clinica rapida', 300, 'Planificada');

-- 21. Registros en jornada de vacunacion
INSERT INTO registro_jornada_vacunacion (id, jornada_id, mascota_id, dueno_id, nombre_mascota_temporal, especie_id, es_callejero_comunitario, nombre_responsable_temporal, telefono_responsable_temporal, vacuna_id, veterinario_id, fecha_hora, lote_vacuna, proxima_dosis, observaciones) VALUES
(1, 1, 1, 1, NULL, 1, FALSE, NULL, NULL, 4, 1, '2026-09-20 09:10:00', 'VRB-RAB-2026', '2027-09-20', 'Paciente regular atendido en carpa principal'),
(2, 1, NULL, NULL, 'Negrito del Parque', 1, TRUE, 'Dona Rosa Martinez (Vecina del parque)', '3159988776', 4, 1, '2026-09-20 10:25:00', 'VRB-RAB-2026', '2027-09-20', 'Perro comunitario cuidado colectivamente por la comunidad'),
(3, 1, NULL, 2, 'Michi de Barrio', 2, FALSE, 'Mariana Rivas Cardona', '3172223344', 4, 3, '2026-09-20 11:40:00', 'VRB-RAB-2026', '2027-09-20', 'Registro rapido en jornada comunitaria');

-- 22. Club de mascotas frecuentes
INSERT INTO club_mascotas (id, dueno_id, puntos_acumulados, puntos_canjeados, puntos_disponibles, nivel, fecha_inscripcion, activo) VALUES
(1, 1, 120, 0, 120, 'Bronce', '2023-01-15', TRUE),
(2, 2, 450, 100, 350, 'Plata', '2023-02-20', TRUE),
(3, 3, 890, 200, 690, 'Oro', '2023-03-10', TRUE);

-- 23. Beneficios del club
INSERT INTO beneficios_club (id, nombre, descripcion, nivel_requerido, puntos_necesarios, tipo_beneficio, valor_beneficio, activo) VALUES
(1, 'Descuento 5% en Farmacia', 'Descuento del 5% aplicable al total de farmacia', 'Plata', 100, 'Descuento', 5.00, TRUE),
(2, 'Descuento 10% Integral', 'Descuento del 10% en servicios medicos y productos', 'Oro', 200, 'Descuento', 10.00, TRUE),
(3, 'Corte de Unas y Limpieza de Oidos Gratis', 'Procedimiento de aseo e higiene sin costo', 'Bronce', 80, 'Servicio Gratis', 15.00, TRUE),
(4, 'Profilaxis Dental Gratuita', 'Servicio completo de profilaxis dental ultrasonica sin costo', 'Platino', 500, 'Servicio Gratis', 85.00, TRUE);

-- 24. Transacciones de puntos
INSERT INTO transacciones_puntos (id, club_mascotas_id, factura_id, puntos, tipo, fecha, descripcion, saldo_anterior, saldo_nuevo) VALUES
(1, 1, 1, 120, 'Ganados', '2026-10-15 10:05:00', 'Puntos por compra Factura FAC-202610-001', 0, 120),
(2, 2, 2, 180, 'Ganados', '2026-10-18 11:35:00', 'Puntos por compra Factura FAC-202610-002', 270, 450),
(3, 2, NULL, 100, 'Canjeados', '2026-10-19 14:00:00', 'Canje de descuento 5% en farmacia', 450, 350),
(4, 3, 3, 290, 'Ganados', '2026-10-22 17:05:00', 'Puntos por cirugia Factura FAC-202610-003', 600, 890),
(5, 3, NULL, 200, 'Canjeados', '2026-10-23 09:30:00', 'Canje de descuento 10% integral', 890, 690);

-- 25. Canjes de beneficios
INSERT INTO canjes_beneficios (id, club_mascotas_id, beneficio_id, fecha_canje, puntos_canjeados, estado, fecha_expiracion, factura_id) VALUES
(1, 2, 1, '2026-10-19 14:00:00', 100, 'Aplicado', '2026-11-19', 2),
(2, 3, 2, '2026-10-23 09:30:00', 200, 'Aplicado', '2026-11-23', 3);

-- 26. Historial de transferencias de propiedad
INSERT INTO historial_transferencias_mascota (id, mascota_id, dueno_anterior_id, dueno_nuevo_id, fecha_transferencia, motivo) VALUES
(1, 5, 5, 4, '2026-04-20 15:00:00', 'Adopcion formal y cambio de tutor legal a favor de Laura Gutierrez');

-- 27. Alertas iniciales generadas
INSERT INTO alertas_sistema (id, tipo_alerta, nivel_severidad, referencia_tabla, referencia_id, mensaje, fecha_generacion, leida, fecha_atencion) VALUES
(1, 'STOCK_MINIMO', 'Advertencia', 'inventario', 8, 'Stock bajo para producto: Sutura Absorbible Vycril 3-0 con Aguja (Stock actual: 3, Minimo: 5)', NOW(), FALSE, NULL),
(2, 'PRODUCTO_POR_VENCER', 'Advertencia', 'inventario', 9, 'Producto proximo a vencer: Amoxicilina Suspension Pediatrica 250mg (Lote: AMX-EXP-01)', NOW(), FALSE, NULL),
(3, 'PRODUCTO_POR_VENCER', 'Advertencia', 'inventario', 10, 'Producto proximo a vencer: Vacuna Triple Felina RCP (Lote: FEL-EXP-02)', NOW(), FALSE, NULL);

-- Reactivar verificacion de claves foraneas
SET FOREIGN_KEY_CHECKS = 1;
