-- =====================================================================
--  Salud-AR — 02_dml_ejemplos.sql
--  Inserción, consulta y borrado de registros de prueba
--  (ejecutar luego de 01_schema.sql)
-- =====================================================================
USE saludar;

-- ---------------------------------------------------------------------
-- 1) INSERCIÓN de datos de prueba
-- ---------------------------------------------------------------------
INSERT INTO usuario (nombre_usuario, hash_contrasena, rol) VALUES
  ('jperez', '$2a$10$hashDeEjemploBcrypt................', 'MEDICO'),
  ('mruiz',  '$2a$10$otroHashBcrypt.....................', 'MEDICO'),
  ('recepcion1', '$2a$10$hashRecepcion.................', 'ADMINISTRATIVO');

INSERT INTO profesional (id_usuario, nombre, apellido, matricula, especialidad) VALUES
  (1, 'Juan',  'Pérez', 'MP-12345', 'Clínica Médica'),
  (2, 'Laura', 'Ruiz',  'MP-67890', 'Clínica Médica');

INSERT INTO obra_social (nombre, codigo) VALUES
  ('OSDE', 'OSDE'), ('Swiss Medical', 'SWISS'), ('PAMI', 'PAMI');

INSERT INTO paciente (dni, apellido, nombre, fecha_nac, sexo, domicilio, telefono, email) VALUES
  ('28455120', 'Giménez', 'María',  '1979-04-12', 'F', 'Calle 50 N° 1234, La Plata', '221-5551234', 'maria.gimenez@mail.com'),
  ('30987654', 'López',   'Carlos', '1984-11-03', 'M', 'Av. 7 N° 890, La Plata',     '221-5559876', 'carlos.lopez@mail.com');

INSERT INTO cobertura (id_paciente, id_obra_social, nro_afiliado, plan) VALUES
  (1, 1, '210-445512-00', '210'),
  (2, 2, 'SW-778812',     'SMG20');

INSERT INTO alergia (id_paciente, sustancia, tipo_reaccion, severidad) VALUES
  (1, 'Penicilina', 'Erupción cutánea / anafilaxia', 'GRAVE'),
  (1, 'AINE',       'Urticaria',                     'MODERADA');

INSERT INTO cie10 (codigo_cie10, descripcion, capitulo) VALUES
  ('R51',   'Cefalea',                     'Síntomas y signos generales'),
  ('J02.9', 'Faringitis aguda, no especificada', 'Enfermedades del sistema respiratorio'),
  ('Z00.0', 'Examen médico general',       'Factores que influyen en el estado de salud'),
  ('G43.9', 'Migraña, no especificada',    'Enfermedades del sistema nervioso');

-- Consulta con su diagnóstico y medicación (transacción: todo o nada — RNF-10)
START TRANSACTION;
INSERT INTO consulta (id_paciente, id_profesional, fecha_hora, motivo, evaluacion, indicaciones, estado)
VALUES (1, 1, '2026-09-10 10:30:00',
        'Cefalea de 3 días de evolución',
        'Paciente lúcida, afebril. TA 120/80. Sin foco neurológico.',
        'Reposo relativo. Pautas de alarma. Control en 7 días.',
        'CERRADA');
SET @idc = LAST_INSERT_ID();
INSERT INTO diagnostico (id_consulta, codigo_cie10, principal) VALUES (@idc, 'R51', TRUE);
INSERT INTO medicacion (id_consulta, farmaco, dosis, frecuencia, duracion)
VALUES (@idc, 'Paracetamol', '500 mg', 'cada 8 h', '3 días');
INSERT INTO auditoria (id_usuario, entidad, accion) VALUES (1, 'consulta', 'ALTA');
COMMIT;

-- ---------------------------------------------------------------------
-- 2) CONSULTAS (SELECT)
-- ---------------------------------------------------------------------

-- 2.a) Búsqueda de pacientes por apellido (RF-11) — solo activos
SELECT id_paciente, dni, apellido, nombre
FROM   paciente
WHERE  apellido LIKE 'Gim%' AND activo = TRUE
ORDER  BY apellido, nombre;

-- 2.b) Alergias de un paciente (RF-19) — insumo de la alerta
SELECT sustancia, tipo_reaccion, severidad
FROM   alergia
WHERE  id_paciente = 1;

-- 2.c) Historia clínica de un paciente como línea de tiempo (RF-21, RF-22)
SELECT c.fecha_hora,
       CONCAT(pr.apellido, ', ', pr.nombre) AS profesional,
       c.motivo,
       c.estado,
       GROUP_CONCAT(CONCAT(d.codigo_cie10, ' ', ci.descripcion)
                    ORDER BY d.principal DESC SEPARATOR ' | ') AS diagnosticos
FROM   consulta c
       JOIN profesional pr ON pr.id_profesional = c.id_profesional
       LEFT JOIN diagnostico d  ON d.id_consulta = c.id_consulta
       LEFT JOIN cie10 ci       ON ci.codigo_cie10 = d.codigo_cie10
WHERE  c.id_paciente = 1
GROUP  BY c.id_consulta
ORDER  BY c.fecha_hora DESC;

-- 2.d) Búsqueda de códigos CIE-10 por descripción (RF-15)
SELECT codigo_cie10, descripcion
FROM   cie10
WHERE  descripcion LIKE '%cefalea%' OR codigo_cie10 = 'R51';

-- ---------------------------------------------------------------------
-- 3) BORRADO
-- ---------------------------------------------------------------------

-- 3.a) Baja LÓGICA de un paciente (RF-10 / RN-07): NO se elimina físicamente
UPDATE paciente SET activo = FALSE WHERE id_paciente = 2;

-- 3.b) Borrado FÍSICO de datos de prueba, respetando el orden de las FK
DELETE FROM auditoria   WHERE id_usuario   IN (1,2,3);
DELETE FROM medicacion  WHERE id_consulta  IN (SELECT id_consulta FROM consulta WHERE id_paciente IN (1,2));
DELETE FROM diagnostico WHERE id_consulta  IN (SELECT id_consulta FROM consulta WHERE id_paciente IN (1,2));
DELETE FROM consulta    WHERE id_paciente  IN (1,2);
DELETE FROM alergia     WHERE id_paciente  IN (1,2);
DELETE FROM cobertura   WHERE id_paciente  IN (1,2);
DELETE FROM paciente    WHERE id_paciente  IN (1,2);
