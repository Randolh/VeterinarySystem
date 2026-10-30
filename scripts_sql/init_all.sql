-- Inicializacion completa de la base de datos Happy Feet
-- Ejecucion en terminal: mysql -u root -p < scripts_sql/init_all.sql

SELECT '1. Creando tablas y estructura relacional (01_ddl_schema.sql)' AS estado;
SOURCE scripts_sql/01_ddl_schema.sql;

SELECT '2. Configurando triggers, procedimientos, funciones y vistas (02_programacion_bd.sql)' AS estado;
SOURCE scripts_sql/02_programacion_bd.sql;

SELECT '3. Insertando datos iniciales y semillas de prueba (03_dml_seeds.sql)' AS estado;
SOURCE scripts_sql/03_dml_seeds.sql;

SELECT 'Base de datos happy_feet_veterinaria inicializada con exito' AS resultado;

-- Verificacion de alertas generadas
SELECT id, tipo_alerta, nivel_severidad, mensaje, fecha_generacion 
FROM alertas_sistema;

-- Verificacion de triggers activos
SELECT TRIGGER_NAME AS trigger_nombre, EVENT_MANIPULATION AS evento, EVENT_OBJECT_TABLE AS tabla
FROM information_schema.TRIGGERS
WHERE TRIGGER_SCHEMA = 'happy_feet_veterinaria';