-- =====================================================================
--  Salud-AR - Historia Clinica Electronica ambulatoria
--
--  Contenido:
--    1. Creacion de la base de datos
--    2. Tablas (con PK, FK, UNIQUE y CHECK)
--    3. Indices secundarios
--    4. Vistas
--    5. Triggers (inviolabilidad de la HC y baja logica - Ley 26.529)
--    6. Usuario de la aplicacion con privilegios minimos
--    7. Datos de prueba (INSERT) para poblar la base
--    8. Consultas de verificacion (pruebas)


-- =====================================================================
--  1. BASE DE DATOS
-- =====================================================================
DROP DATABASE IF EXISTS saludar;
CREATE DATABASE saludar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE saludar;

-- =====================================================================
--  2. TABLAS
-- =====================================================================

-- ---------- Seguridad y acceso (RF-01 a RF-04) ----------
CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario   VARCHAR(50)  NOT NULL,
    hash_contrasena  VARCHAR(255) NOT NULL,           -- PBKDF2-SHA256 con sal (RF-02)
    rol              VARCHAR(20)  NOT NULL,
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_alta       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso    DATETIME     NULL,
    CONSTRAINT uq_usuario_nombre UNIQUE (nombre_usuario),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('MEDICO', 'ADMINISTRATIVO', 'ADMINISTRADOR'))
) ENGINE=InnoDB COMMENT='Cuentas de acceso al sistema';

CREATE TABLE profesional (
    id_profesional  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT          NOT NULL,
    apellido        VARCHAR(80)  NOT NULL,
    nombre          VARCHAR(80)  NOT NULL,
    matricula       VARCHAR(20)  NOT NULL,
    especialidad    VARCHAR(60)  NULL,
    CONSTRAINT uq_profesional_usuario   UNIQUE (id_usuario),
    CONSTRAINT uq_profesional_matricula UNIQUE (matricula),
    CONSTRAINT fk_profesional_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB COMMENT='Profesionales de la salud (un usuario MEDICO por profesional)';

-- ---------- Pacientes y cobertura (RF-05 a RF-11) ----------
CREATE TABLE obra_social (
    id_obra_social  INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80) NOT NULL,
    codigo          VARCHAR(20) NOT NULL,
    CONSTRAINT uq_obra_social_nombre UNIQUE (nombre),
    CONSTRAINT uq_obra_social_codigo UNIQUE (codigo)
) ENGINE=InnoDB COMMENT='Padron de obras sociales y prepagas';

CREATE TABLE paciente (
    id_paciente  INT AUTO_INCREMENT PRIMARY KEY,
    dni          VARCHAR(8)   NOT NULL,
    apellido     VARCHAR(80)  NOT NULL,
    nombre       VARCHAR(80)  NOT NULL,
    fecha_nac    DATE         NOT NULL,               -- la edad se calcula, no se almacena
    sexo         CHAR(1)      NOT NULL,
    domicilio    VARCHAR(120) NULL,
    telefono     VARCHAR(30)  NULL,
    email        VARCHAR(80)  NULL,
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,  -- baja logica (RF-10, RN-07)
    fecha_alta   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_paciente_dni UNIQUE (dni),                              -- RN-01, RF-07
    CONSTRAINT chk_paciente_dni  CHECK (dni REGEXP '^[0-9]{7,8}$'),     -- RF-08
    CONSTRAINT chk_paciente_sexo CHECK (sexo IN ('F', 'M', 'X'))
) ENGINE=InnoDB COMMENT='Padron de pacientes';

CREATE TABLE cobertura (
    id_cobertura    INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente     INT         NOT NULL,
    id_obra_social  INT         NOT NULL,
    nro_afiliado    VARCHAR(40) NULL,
    plan            VARCHAR(60) NULL,
    CONSTRAINT uq_cobertura_paciente UNIQUE (id_paciente),   -- una cobertura vigente por paciente
    CONSTRAINT fk_cobertura_paciente    FOREIGN KEY (id_paciente)    REFERENCES paciente (id_paciente),
    CONSTRAINT fk_cobertura_obra_social FOREIGN KEY (id_obra_social) REFERENCES obra_social (id_obra_social)
) ENGINE=InnoDB COMMENT='Datos filiatorios de cobertura. Sin fila = paciente particular';

-- ---------- Alergias y antecedentes (RF-18 a RF-20) ----------
CREATE TABLE alergia (
    id_alergia      INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente     INT          NOT NULL,
    sustancia       VARCHAR(80)  NOT NULL,
    tipo_reaccion   VARCHAR(120) NULL,
    severidad       VARCHAR(10)  NOT NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_alergia_severidad CHECK (severidad IN ('LEVE', 'MODERADA', 'GRAVE')),
    CONSTRAINT fk_alergia_paciente FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente)
) ENGINE=InnoDB COMMENT='Alergias del paciente (insumo de la alerta RN-04)';

CREATE TABLE antecedente (
    id_antecedente  INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente     INT          NOT NULL,
    tipo            VARCHAR(12)  NOT NULL,
    descripcion     VARCHAR(255) NOT NULL,
    fecha           DATE         NULL,
    CONSTRAINT chk_antecedente_tipo CHECK (tipo IN ('PERSONAL', 'FAMILIAR', 'QUIRURGICO', 'OTRO')),
    CONSTRAINT fk_antecedente_paciente FOREIGN KEY (id_paciente) REFERENCES paciente (id_paciente)
) ENGINE=InnoDB COMMENT='Antecedentes personales, familiares y quirurgicos';

-- ---------- Catalogo CIE-10 (RN-03) ----------
CREATE TABLE cie10 (
    codigo_cie10  VARCHAR(10)  PRIMARY KEY,
    descripcion   VARCHAR(255) NOT NULL,
    capitulo      VARCHAR(100) NULL
) ENGINE=InnoDB COMMENT='Clasificacion Internacional de Enfermedades, 10a revision (subconjunto)';

-- ---------- Consulta y detalle clinico (RF-12 a RF-17) ----------
CREATE TABLE consulta (
    id_consulta              INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente              INT          NOT NULL,
    id_profesional           INT          NOT NULL,
    fecha_hora               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo                   VARCHAR(255) NOT NULL,
    evaluacion               TEXT         NULL,
    indicaciones             TEXT         NULL,
    estado                   VARCHAR(12)  NOT NULL DEFAULT 'BORRADOR',
    id_consulta_rectificada  INT          NULL,   -- asiento que rectifica a otra consulta (RN-05)
    CONSTRAINT chk_consulta_estado CHECK (estado IN ('BORRADOR', 'CERRADA', 'RECTIFICADA')),
    CONSTRAINT fk_consulta_paciente    FOREIGN KEY (id_paciente)    REFERENCES paciente (id_paciente),
    CONSTRAINT fk_consulta_profesional FOREIGN KEY (id_profesional) REFERENCES profesional (id_profesional),
    CONSTRAINT fk_consulta_rectificada FOREIGN KEY (id_consulta_rectificada) REFERENCES consulta (id_consulta)
) ENGINE=InnoDB COMMENT='Evolucion clinica. CERRADA = inviolable (Ley 26.529)';

CREATE TABLE diagnostico (
    id_diagnostico  INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta     INT         NOT NULL,
    codigo_cie10    VARCHAR(10) NOT NULL,
    principal       BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT uq_diagnostico UNIQUE (id_consulta, codigo_cie10),
    CONSTRAINT fk_diagnostico_consulta FOREIGN KEY (id_consulta)  REFERENCES consulta (id_consulta),
    CONSTRAINT fk_diagnostico_cie10    FOREIGN KEY (codigo_cie10) REFERENCES cie10 (codigo_cie10)
) ENGINE=InnoDB COMMENT='Diagnosticos codificados de cada consulta (RF-14)';

CREATE TABLE medicacion (
    id_medicacion  INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta    INT         NOT NULL,
    farmaco        VARCHAR(80) NOT NULL,
    dosis          VARCHAR(40) NULL,
    frecuencia     VARCHAR(40) NULL,
    duracion       VARCHAR(40) NULL,
    CONSTRAINT fk_medicacion_consulta FOREIGN KEY (id_consulta) REFERENCES consulta (id_consulta)
) ENGINE=InnoDB COMMENT='Medicacion indicada en la consulta (RF-16)';

-- ---------- Sala de espera con triage ----------
CREATE TABLE ingreso_sala (
    id_ingreso           INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente          INT         NOT NULL,
    prioridad            VARCHAR(5)  NOT NULL,
    estado               VARCHAR(12) NOT NULL DEFAULT 'EN_ESPERA',
    fecha_hora_ingreso   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_hora_fin       DATETIME    NULL,
    id_consulta          INT         NULL,     -- consulta que cerro el ingreso
    id_usuario_registro  INT         NOT NULL,
    CONSTRAINT chk_ingreso_prioridad CHECK (prioridad IN ('ALTA', 'MEDIA', 'BAJA')),
    CONSTRAINT chk_ingreso_estado    CHECK (estado IN ('EN_ESPERA', 'EN_ATENCION', 'FINALIZADO')),
    CONSTRAINT fk_ingreso_paciente FOREIGN KEY (id_paciente)         REFERENCES paciente (id_paciente),
    CONSTRAINT fk_ingreso_consulta FOREIGN KEY (id_consulta)         REFERENCES consulta (id_consulta),
    CONSTRAINT fk_ingreso_usuario  FOREIGN KEY (id_usuario_registro) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB COMMENT='Ingresos a la sala de espera con prioridad de triage';

-- ---------- Auditoria y trazabilidad (RF-23) ----------
CREATE TABLE auditoria (
    id_auditoria  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario    INT          NULL,      -- NULL: evento sin sesion (p. ej. login fallido)
    entidad       VARCHAR(40)  NOT NULL,
    id_registro   INT          NULL,
    accion        VARCHAR(30)  NOT NULL,
    detalle       VARCHAR(255) NULL,
    fecha_hora    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_auditoria_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario)
) ENGINE=InnoDB COMMENT='Quien, que y cuando de cada operacion sensible';

-- =====================================================================
--  3. INDICES SECUNDARIOS (RNF-05 y RNF-06: busquedas < 2-3 s)
-- =====================================================================
CREATE INDEX idx_paciente_apellido     ON paciente (apellido, nombre);
CREATE INDEX idx_paciente_activo       ON paciente (activo);
CREATE INDEX idx_cobertura_afiliado    ON cobertura (nro_afiliado);
CREATE INDEX idx_alergia_paciente      ON alergia (id_paciente);
CREATE INDEX idx_antecedente_paciente  ON antecedente (id_paciente);
CREATE INDEX idx_consulta_paciente     ON consulta (id_paciente, fecha_hora);
CREATE INDEX idx_consulta_profesional  ON consulta (id_profesional, fecha_hora);
CREATE INDEX idx_ingreso_estado        ON ingreso_sala (estado, prioridad, fecha_hora_ingreso);
CREATE INDEX idx_auditoria_fecha       ON auditoria (fecha_hora);
CREATE FULLTEXT INDEX idx_cie10_descripcion ON cie10 (descripcion);

-- =====================================================================
--  4. VISTAS
-- =====================================================================

-- Paciente con su cobertura y la edad calculada
CREATE VIEW v_paciente AS
SELECT p.id_paciente, p.dni, p.apellido, p.nombre, p.fecha_nac,
       TIMESTAMPDIFF(YEAR, p.fecha_nac, CURDATE()) AS edad,
       p.sexo, p.domicilio, p.telefono, p.email, p.activo,
       os.id_obra_social, COALESCE(os.nombre, 'Particular') AS obra_social,
       c.nro_afiliado, c.plan
FROM   paciente p
       LEFT JOIN cobertura c    ON c.id_paciente = p.id_paciente
       LEFT JOIN obra_social os ON os.id_obra_social = c.id_obra_social;

-- Historia clinica: una fila por consulta con su diagnostico principal
CREATE VIEW v_historia_clinica AS
SELECT c.id_consulta, c.id_paciente, p.dni,
       CONCAT(p.apellido, ', ', p.nombre)   AS paciente,
       c.fecha_hora,
       CONCAT(pr.apellido, ', ', pr.nombre) AS profesional,
       c.motivo, c.estado, c.id_consulta_rectificada,
       d.codigo_cie10 AS cie10_principal, ci.descripcion AS diagnostico_principal
FROM   consulta c
       JOIN paciente p     ON p.id_paciente = c.id_paciente
       JOIN profesional pr ON pr.id_profesional = c.id_profesional
       LEFT JOIN diagnostico d ON d.id_consulta = c.id_consulta AND d.principal = TRUE
       LEFT JOIN cie10 ci      ON ci.codigo_cie10 = d.codigo_cie10;

-- Sala de espera: ingresos activos con su tiempo de espera
CREATE VIEW v_sala_espera AS
SELECT i.id_ingreso, i.id_paciente, p.dni,
       CONCAT(p.apellido, ', ', p.nombre) AS paciente,
       TIMESTAMPDIFF(YEAR, p.fecha_nac, CURDATE()) AS edad,
       i.prioridad, i.estado, i.fecha_hora_ingreso,
       TIMESTAMPDIFF(MINUTE, i.fecha_hora_ingreso, NOW()) AS minutos_espera
FROM   ingreso_sala i
       JOIN paciente p ON p.id_paciente = i.id_paciente
WHERE  i.estado IN ('EN_ESPERA', 'EN_ATENCION');

-- =====================================================================
--  5. TRIGGERS - reglas de negocio garantizadas por el motor
-- =====================================================================
DELIMITER $$

-- RN-05 / RF-17: una consulta CERRADA no se modifica. Unica excepcion:
-- marcarla como RECTIFICADA cuando se registra un asiento que la rectifica.
CREATE TRIGGER trg_consulta_inviolable
BEFORE UPDATE ON consulta
FOR EACH ROW
BEGIN
    IF OLD.estado IN ('CERRADA', 'RECTIFICADA') THEN
        IF NOT (OLD.estado = 'CERRADA' AND NEW.estado = 'RECTIFICADA'
                AND NEW.id_paciente    =   OLD.id_paciente
                AND NEW.id_profesional =   OLD.id_profesional
                AND NEW.fecha_hora     =   OLD.fecha_hora
                AND NEW.motivo         <=> OLD.motivo
                AND NEW.evaluacion     <=> OLD.evaluacion
                AND NEW.indicaciones   <=> OLD.indicaciones) THEN
            SIGNAL SQLSTATE '45000'
                SET MESSAGE_TEXT = 'La consulta esta CERRADA y no puede modificarse (Ley 26.529).';
        END IF;
    END IF;
END$$

-- La historia clinica no admite borrado fisico
CREATE TRIGGER trg_consulta_sin_borrado
BEFORE DELETE ON consulta
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Las consultas no se eliminan: la historia clinica es inviolable (Ley 26.529).';
END$$

-- RN-07: la baja de un paciente es logica (columna activo)
CREATE TRIGGER trg_paciente_sin_borrado
BEFORE DELETE ON paciente
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Los pacientes no se eliminan: use la baja logica (activo = FALSE).';
END$$

-- Diagnosticos y medicacion solo se agregan mientras la consulta es BORRADOR
CREATE TRIGGER trg_diagnostico_consulta_abierta
BEFORE INSERT ON diagnostico
FOR EACH ROW
BEGIN
    IF (SELECT estado FROM consulta WHERE id_consulta = NEW.id_consulta) <> 'BORRADOR' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No se pueden agregar diagnosticos a una consulta cerrada.';
    END IF;
END$$

CREATE TRIGGER trg_medicacion_consulta_abierta
BEFORE INSERT ON medicacion
FOR EACH ROW
BEGIN
    IF (SELECT estado FROM consulta WHERE id_consulta = NEW.id_consulta) <> 'BORRADOR' THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'No se puede agregar medicacion a una consulta cerrada.';
    END IF;
END$$

DELIMITER ;

-- =====================================================================
--  6. USUARIO DE LA APLICACION (principio de minimo privilegio)
--  La aplicacion se conecta con este usuario: puede leer, insertar y
--  actualizar, pero NO borrar datos clinicos ni alterar la estructura.
--  Unica excepcion: borrar la fila de cobertura cuando un paciente pasa
--  a ser particular (dato administrativo, no forma parte de la HC).
-- =====================================================================
DROP USER IF EXISTS 'saludar_app'@'localhost';
CREATE USER 'saludar_app'@'localhost' IDENTIFIED BY 'SaludAR#2026';
GRANT SELECT, INSERT, UPDATE ON saludar.* TO 'saludar_app'@'localhost';
GRANT DELETE ON saludar.cobertura TO 'saludar_app'@'localhost';
FLUSH PRIVILEGES;

-- =====================================================================
--  7. DATOS DE PRUEBA
--  Usuarios de prueba (contrasenia en texto plano SOLO a fines de la demo):
--    administrador / administrador123  (ADMINISTRADOR)
--    medico        / medico123         (MEDICO - Dr. Juan Perez)
--    medico2       / medico123         (MEDICO - Dra. Laura Ruiz)
--    recepcion     / recepcion123      (ADMINISTRATIVO)
-- =====================================================================

INSERT INTO usuario (id_usuario, nombre_usuario, hash_contrasena, rol) VALUES
 (1, 'administrador', 'pbkdf2_sha256$120000$2/0Yrq9zQlPermaePT62dw==$rOoDelYz6FQjT+zoHhal5N1oGtdLNJOybzTleQrdXRA=', 'ADMINISTRADOR'),
 (2, 'medico',        'pbkdf2_sha256$120000$R76ib8SzgzaCcYzvRZT9lA==$sI91vu4UeEHaRRMqCBjoIPTSTZUzPT44TMqCUADqYb4=', 'MEDICO'),
 (3, 'medico2',       'pbkdf2_sha256$120000$WxYZwA0Rg8z8q5lZ4MBE+w==$uIf+GKJY8xm0uJHumcb2YXCVZVjgEbJd8oguX9AZnEE=', 'MEDICO'),
 (4, 'recepcion',     'pbkdf2_sha256$120000$t6xKljKbyoqASmvmk7peuw==$UtY6/XS7ppp5r89LatRZeLITSELZhq6piNsTJ+y5spw=', 'ADMINISTRATIVO');

INSERT INTO profesional (id_profesional, id_usuario, apellido, nombre, matricula, especialidad) VALUES
 (1, 2, 'Pérez', 'Juan',  'MP-12345', 'Clínica Médica'),
 (2, 3, 'Ruiz',  'Laura', 'MP-67890', 'Medicina Familiar');

INSERT INTO obra_social (id_obra_social, nombre, codigo) VALUES
 (1, 'OSDE',          'OSDE'),
 (2, 'Swiss Medical', 'SWISS'),
 (3, 'PAMI',          'PAMI'),
 (4, 'IOMA',          'IOMA'),
 (5, 'Galeno',        'GALENO'),
 (6, 'OSECAC',        'OSECAC'),
 (7, 'Medicus',       'MEDICUS');

INSERT INTO cie10 (codigo_cie10, descripcion, capitulo) VALUES
 ('A09',   'Diarrea y gastroenteritis de presunto origen infeccioso',  'I - Enfermedades infecciosas y parasitarias'),
 ('B34.9', 'Infección viral, no especificada',                         'I - Enfermedades infecciosas y parasitarias'),
 ('E11.9', 'Diabetes mellitus tipo 2, sin complicaciones',             'IV - Enfermedades endocrinas, nutricionales y metabólicas'),
 ('E78.5', 'Hiperlipidemia, no especificada',                          'IV - Enfermedades endocrinas, nutricionales y metabólicas'),
 ('F41.1', 'Trastorno de ansiedad generalizada',                       'V - Trastornos mentales y del comportamiento'),
 ('G43.9', 'Migraña, no especificada',                                 'VI - Enfermedades del sistema nervioso'),
 ('H10.9', 'Conjuntivitis, no especificada',                           'VII - Enfermedades del ojo y sus anexos'),
 ('I10',   'Hipertensión esencial (primaria)',                         'IX - Enfermedades del sistema circulatorio'),
 ('J00',   'Rinofaringitis aguda (resfriado común)',                   'X - Enfermedades del sistema respiratorio'),
 ('J02.9', 'Faringitis aguda, no especificada',                        'X - Enfermedades del sistema respiratorio'),
 ('J18.9', 'Neumonía, no especificada',                                'X - Enfermedades del sistema respiratorio'),
 ('J20.9', 'Bronquitis aguda, no especificada',                        'X - Enfermedades del sistema respiratorio'),
 ('J45.9', 'Asma, no especificada',                                    'X - Enfermedades del sistema respiratorio'),
 ('K21.9', 'Enfermedad del reflujo gastroesofágico sin esofagitis',    'XI - Enfermedades del sistema digestivo'),
 ('K29.7', 'Gastritis, no especificada',                               'XI - Enfermedades del sistema digestivo'),
 ('L50.9', 'Urticaria, no especificada',                               'XII - Enfermedades de la piel y del tejido subcutáneo'),
 ('M54.5', 'Lumbago no especificado',                                  'XIII - Enfermedades del sistema osteomuscular'),
 ('N39.0', 'Infección de vías urinarias, sitio no especificado',       'XIV - Enfermedades del sistema genitourinario'),
 ('R07.4', 'Dolor en el pecho, no especificado',                       'XVIII - Síntomas, signos y hallazgos anormales'),
 ('R10.4', 'Otros dolores abdominales y los no especificados',         'XVIII - Síntomas, signos y hallazgos anormales'),
 ('R50.9', 'Fiebre, no especificada',                                  'XVIII - Síntomas, signos y hallazgos anormales'),
 ('R51',   'Cefalea',                                                  'XVIII - Síntomas, signos y hallazgos anormales'),
 ('S93.4', 'Esguince y torcedura del tobillo',                         'XIX - Traumatismos y envenenamientos'),
 ('T78.4', 'Alergia no especificada',                                  'XIX - Traumatismos y envenenamientos'),
 ('Z00.0', 'Examen médico general',                                    'XXI - Factores que influyen en el estado de salud');

INSERT INTO paciente (id_paciente, dni, apellido, nombre, fecha_nac, sexo, domicilio, telefono, email, activo) VALUES
 ( 1, '28455120', 'Giménez',   'María',     '1979-04-12', 'F', 'Calle 50 N° 1234, La Plata',     '221-5551234', 'maria.gimenez@mail.com',    TRUE),
 ( 2, '30987654', 'López',     'Carlos',    '1984-11-03', 'M', 'Av. 7 N° 890, La Plata',         '221-5559876', 'carlos.lopez@mail.com',     TRUE),
 ( 3, '25111222', 'Acosta',    'Ana',       '1972-01-20', 'F', 'Calle 12 N° 455, La Plata',      '221-4210033', 'ana.acosta@mail.com',       TRUE),
 ( 4, '35123987', 'Fernández', 'Martín',    '1990-06-15', 'M', 'Calle 60 N° 712, La Plata',      '221-6011122', 'martin.fernandez@mail.com', TRUE),
 ( 5, '42345678', 'Romero',    'Sofía',     '2001-02-28', 'F', 'Diag. 74 N° 1501, La Plata',     '221-6334455', 'sofia.romero@mail.com',     TRUE),
 ( 6, '20456789', 'Sosa',      'Jorge',     '1965-09-09', 'M', 'Calle 1 N° 230, Berisso',        '221-4612290', NULL,                        TRUE),
 ( 7, '38765432', 'Benítez',   'Lucía',     '1995-12-01', 'F', 'Calle 44 N° 980, La Plata',      '221-5098761', 'lucia.benitez@mail.com',    TRUE),
 ( 8, '47890123', 'Díaz',      'Tomás',     '2010-03-22', 'M', 'Calle 19 N° 1120, La Plata',     '221-5872301', NULL,                        TRUE),
 ( 9, '40123987', 'Torres',    'Valentina', '1997-08-30', 'F', 'Calle 8 N° 615, La Plata',       '221-6125544', 'vale.torres@mail.com',      TRUE),
 (10, '18234567', 'Medina',    'Ricardo',   '1958-05-17', 'M', 'Av. Montevideo 340, Berisso',    '221-4645512', NULL,                        TRUE),
 (11, '33456123', 'Castro',    'Paula',     '1988-10-10', 'F', 'Calle 66 N° 1890, La Plata',     '221-5440987', 'paula.castro@mail.com',     TRUE),
 (12, '29876543', 'Ruiz',      'Esteban',   '1982-07-04', 'M', 'Calle 3 N° 77, Ensenada',        '221-4698800', 'esteban.ruiz@mail.com',     FALSE);  -- baja logica

-- Paciente 9 (Torres) no tiene fila en cobertura: es particular
INSERT INTO cobertura (id_paciente, id_obra_social, nro_afiliado, plan) VALUES
 ( 1, 1, '210-445512-00', '210'),
 ( 2, 2, 'SW-778812',     'SMG20'),
 ( 3, 3, 'PAMI-9001',     NULL),
 ( 4, 4, 'IO-3512398',    'Integral'),
 ( 5, 5, 'GA-778341',     'Oro'),
 ( 6, 3, 'PAMI-4471',     NULL),
 ( 7, 6, 'OSE-552190',    NULL),
 ( 8, 1, '210-889120-02', '210'),
 (10, 3, 'PAMI-1182',     NULL),
 (11, 2, 'SW-341209',     'SMG30'),
 (12, 7, 'MED-90871',     'Azul');

INSERT INTO alergia (id_paciente, sustancia, tipo_reaccion, severidad) VALUES
 (1, 'Penicilina',  'Anafilaxia',             'GRAVE'),
 (1, 'AINE',        'Urticaria',              'MODERADA'),
 (6, 'Látex',       'Dermatitis de contacto', 'LEVE'),
 (7, 'Mariscos',    'Edema de glotis',        'GRAVE'),
 (8, 'Amoxicilina', 'Erupción cutánea',       'MODERADA');

INSERT INTO antecedente (id_paciente, tipo, descripcion, fecha) VALUES
 ( 1, 'PERSONAL',   'Migraña crónica',                          '2015-01-01'),
 ( 6, 'PERSONAL',   'Hipertensión arterial en tratamiento',     '2012-05-01'),
 ( 6, 'FAMILIAR',   'Padre con diabetes tipo 2',                NULL),
 (10, 'QUIRURGICO', 'Apendicectomía',                           '1990-03-12'),
 (10, 'PERSONAL',   'Diabetes mellitus tipo 2',                 '2008-09-01'),
 ( 7, 'PERSONAL',   'Infecciones urinarias a repetición',       '2024-02-01');

-- Consultas historicas. Se insertan en BORRADOR, se les agregan diagnosticos
-- y medicacion, y luego se CIERRAN (los triggers impiden hacerlo al reves).
START TRANSACTION;

INSERT INTO consulta (id_consulta, id_paciente, id_profesional, fecha_hora, motivo, evaluacion, indicaciones) VALUES
 ( 1, 1, 1, '2026-03-10 09:30:00', 'Control anual',
      'Paciente en buen estado general. TA 118/76. Examen físico sin particularidades.',
      'Laboratorio de rutina. Control en 12 meses.'),
 ( 2, 1, 1, '2026-09-10 10:30:00', 'Cefalea de 3 días de evolución',
      'Lúcida, afebril. TA 120/80. Sin foco neurológico. Antecedente de migraña.',
      'Reposo relativo. Pautas de alarma. Control en 7 días.'),
 ( 3, 2, 2, '2026-08-02 11:00:00', 'Dolor lumbar tras esfuerzo',
      'Contractura paravertebral lumbar. Lasègue negativo.',
      'Calor local. Evitar esfuerzos. Kinesiología si persiste.'),
 ( 4, 6, 1, '2026-07-15 08:45:00', 'Control de hipertensión',
      'TA 135/85. Buena adherencia al tratamiento.',
      'Continuar medicación. Dieta hiposódica. Control en 3 meses.'),
 ( 5, 10, 1, '2026-06-20 10:00:00', 'Control de diabetes',
      'Glucemia en ayunas 132 mg/dl. HbA1c 7,1 %. Colesterol LDL elevado.',
      'Continuar metformina. Plan alimentario. Control en 3 meses.'),
 ( 6, 5, 2, '2026-10-05 16:20:00', 'Odinofagia y fiebre',
      'Fauces congestivas, sin exudado. T 38,2 °C.',
      'Hidratación. Antitérmicos. Consultar si persiste más de 72 h.'),
 ( 7, 8, 2, '2026-10-20 17:00:00', 'Tos y fiebre de 2 días',
      'Roncus aislados. Saturación 97 %. Alergia a amoxicilina registrada.',
      'Hidratación. Control en 48 h. Pautas de alarma respiratoria.'),
 ( 8, 7, 1, '2026-09-28 12:15:00', 'Ardor miccional',
      'Disuria y polaquiuria. Puño-percusión negativa.',
      'Abundante líquido. Urocultivo de control.'),
 ( 9, 4, 2, '2026-10-30 09:10:00', 'Dolor de tobillo derecho tras caída',
      'Edema y dolor en ligamento lateral externo. Sin signos de fractura.',
      'Reposo, hielo, compresión y elevación. Control en 7 días.'),
 (10, 7, 1, '2026-09-28 13:00:00', 'Rectificación de la consulta #8: duración del tratamiento',
      'Se corrige la duración del tratamiento indicado en la consulta #8.',
      'Completar 7 días de tratamiento.');

UPDATE consulta SET id_consulta_rectificada = 8 WHERE id_consulta = 10;

INSERT INTO diagnostico (id_consulta, codigo_cie10, principal) VALUES
 ( 1, 'Z00.0', TRUE),
 ( 2, 'R51',   TRUE),  ( 2, 'G43.9', FALSE),
 ( 3, 'M54.5', TRUE),
 ( 4, 'I10',   TRUE),
 ( 5, 'E11.9', TRUE),  ( 5, 'E78.5', FALSE),
 ( 6, 'J02.9', TRUE),  ( 6, 'R50.9', FALSE),
 ( 7, 'J20.9', TRUE),
 ( 8, 'N39.0', TRUE),
 ( 9, 'S93.4', TRUE),
 (10, 'N39.0', TRUE);

INSERT INTO medicacion (id_consulta, farmaco, dosis, frecuencia, duracion) VALUES
 ( 2, 'Paracetamol',     '500 mg',           'cada 8 h',  '3 días'),
 ( 3, 'Ibuprofeno',      '400 mg',           'cada 8 h',  '5 días'),
 ( 4, 'Enalapril',       '10 mg',            'cada 12 h', 'crónico'),
 ( 5, 'Metformina',      '850 mg',           'cada 12 h', 'crónico'),
 ( 6, 'Ibuprofeno',      '400 mg',           'cada 8 h',  '5 días'),
 ( 7, 'Paracetamol',     '160 mg/5 ml (7,5 ml)', 'cada 8 h', '3 días'),
 ( 8, 'Nitrofurantoína', '100 mg',           'cada 6 h',  '5 días'),
 ( 9, 'Ibuprofeno',      '600 mg',           'cada 12 h', '5 días'),
 (10, 'Nitrofurantoína', '100 mg',           'cada 6 h',  '7 días');

-- Cierre (firma) de todas las consultas: a partir de aqui son inviolables
UPDATE consulta SET estado = 'CERRADA' WHERE id_consulta BETWEEN 1 AND 10;

-- La consulta #8 queda marcada como RECTIFICADA por la #10 (unica
-- transicion permitida por el trigger sobre una consulta cerrada)
UPDATE consulta SET estado = 'RECTIFICADA' WHERE id_consulta = 8;

COMMIT;

-- Ingresos a la sala de espera: tres historicos (ya atendidos) y tres
-- activos, con horas relativas al momento de ejecutar el script
INSERT INTO ingreso_sala (id_paciente, prioridad, estado, fecha_hora_ingreso, fecha_hora_fin, id_consulta, id_usuario_registro) VALUES
 (5, 'MEDIA', 'FINALIZADO', '2026-10-05 15:50:00', '2026-10-05 16:40:00', 6, 4),
 (8, 'MEDIA', 'FINALIZADO', '2026-10-20 16:35:00', '2026-10-20 17:20:00', 7, 4),
 (4, 'BAJA',  'FINALIZADO', '2026-10-30 08:20:00', '2026-10-30 09:30:00', 9, 4),
 (2, 'BAJA',  'EN_ESPERA',  NOW() - INTERVAL 25 MINUTE, NULL, NULL, 4),
 (1, 'ALTA',  'EN_ESPERA',  NOW() - INTERVAL 12 MINUTE, NULL, NULL, 4),
 (3, 'MEDIA', 'EN_ESPERA',  NOW() - INTERVAL 4 MINUTE,  NULL, NULL, 4);

INSERT INTO auditoria (id_usuario, entidad, id_registro, accion, detalle, fecha_hora) VALUES
 (4, 'paciente',     1, 'ALTA',        'Alta de paciente DNI 28455120',            '2026-03-10 09:05:00'),
 (2, 'consulta',     1, 'CIERRE',      'Consulta cerrada - Dx Z00.0',              '2026-03-10 09:50:00'),
 (2, 'consulta',     2, 'CIERRE',      'Consulta cerrada - Dx R51',                '2026-09-10 10:55:00'),
 (2, 'consulta',    10, 'RECTIFICACION','Rectifica la consulta #8',                '2026-09-28 13:05:00'),
 (4, 'paciente',    12, 'BAJA_LOGICA', 'Baja lógica de paciente DNI 29876543',     '2026-10-01 11:00:00'),
 (3, 'consulta',     9, 'CIERRE',      'Consulta cerrada - Dx S93.4',              '2026-10-30 09:30:00');

-- =====================================================================
--  8. CONSULTAS DE VERIFICACION (pruebas)
--  Cada consulta indica el requerimiento que verifica.
-- =====================================================================

-- P01 - Padron de pacientes activos con cobertura y edad (RF-05, RF-06, RF-10)
SELECT dni, apellido, nombre, edad, obra_social, nro_afiliado
FROM   v_paciente
WHERE  activo = TRUE
ORDER  BY apellido, nombre;

-- P02 - Busqueda por apellido, DNI o numero de afiliado (RF-11)
SELECT dni, apellido, nombre, obra_social, nro_afiliado
FROM   v_paciente
WHERE  activo = TRUE
  AND (apellido LIKE 'Gim%' OR dni = '30987654' OR nro_afiliado = 'PAMI-9001');

-- P03 - Alergias de un paciente: insumo de la alerta (RF-19, RN-04)
SELECT p.dni, CONCAT(p.apellido, ', ', p.nombre) AS paciente,
       a.sustancia, a.tipo_reaccion, a.severidad
FROM   alergia a
       JOIN paciente p ON p.id_paciente = a.id_paciente
WHERE  p.dni = '28455120'
ORDER  BY FIELD(a.severidad, 'GRAVE', 'MODERADA', 'LEVE');

-- P04 - Historia clinica como linea de tiempo con todos sus diagnosticos (RF-21, RF-22)
SELECT c.id_consulta, c.fecha_hora,
       CONCAT(pr.apellido, ', ', pr.nombre) AS profesional,
       c.motivo, c.estado,
       GROUP_CONCAT(CONCAT(d.codigo_cie10, ' ', ci.descripcion)
                    ORDER BY d.principal DESC SEPARATOR ' | ') AS diagnosticos
FROM   consulta c
       JOIN profesional pr     ON pr.id_profesional = c.id_profesional
       LEFT JOIN diagnostico d ON d.id_consulta = c.id_consulta
       LEFT JOIN cie10 ci      ON ci.codigo_cie10 = d.codigo_cie10
WHERE  c.id_paciente = 1
GROUP  BY c.id_consulta
ORDER  BY c.fecha_hora DESC;

-- P05 - Medicacion indicada en las consultas de un paciente (RF-16)
SELECT c.id_consulta, DATE(c.fecha_hora) AS fecha,
       m.farmaco, m.dosis, m.frecuencia, m.duracion
FROM   medicacion m
       JOIN consulta c ON c.id_consulta = m.id_consulta
WHERE  c.id_paciente = 7
ORDER  BY c.fecha_hora;

-- P06 - Consultas rectificadas y su rectificacion (RN-05)
SELECT o.id_consulta AS consulta_original, o.estado AS estado_original,
       r.id_consulta AS rectificada_por, r.fecha_hora, r.motivo
FROM   consulta r
       JOIN consulta o ON o.id_consulta = r.id_consulta_rectificada;

-- P07 - Sala de espera en orden de atencion: prioridad y llegada (triage)
SELECT id_ingreso, paciente, edad, prioridad, minutos_espera
FROM   v_sala_espera
ORDER  BY FIELD(prioridad, 'ALTA', 'MEDIA', 'BAJA'), fecha_hora_ingreso;

-- P08 - Busqueda en el catalogo CIE-10 por codigo o descripcion (RF-15)
SELECT codigo_cie10, descripcion
FROM   cie10
WHERE  codigo_cie10 LIKE 'J%' OR descripcion LIKE '%fiebre%'
ORDER  BY codigo_cie10;

-- P09 - Diagnosticos principales mas frecuentes (explotacion de datos codificados)
SELECT d.codigo_cie10, ci.descripcion, COUNT(*) AS cantidad
FROM   diagnostico d
       JOIN cie10 ci ON ci.codigo_cie10 = d.codigo_cie10
WHERE  d.principal = TRUE
GROUP  BY d.codigo_cie10, ci.descripcion
ORDER  BY cantidad DESC, d.codigo_cie10
LIMIT  5;

-- P10 - Consultas por profesional
SELECT CONCAT(pr.apellido, ', ', pr.nombre) AS profesional, pr.especialidad,
       COUNT(c.id_consulta) AS consultas
FROM   profesional pr
       LEFT JOIN consulta c ON c.id_profesional = pr.id_profesional
GROUP  BY pr.id_profesional
ORDER  BY consultas DESC;

-- P11 - Registro de auditoria: quien, que y cuando (RF-23)
SELECT a.fecha_hora, COALESCE(u.nombre_usuario, '(sin sesion)') AS usuario,
       a.accion, a.entidad, a.id_registro, a.detalle
FROM   auditoria a
       LEFT JOIN usuario u ON u.id_usuario = a.id_usuario
ORDER  BY a.fecha_hora DESC;

-- P12 - Actualizacion de registros dentro de una transaccion que se
-- deshace al final, para no alterar los datos de prueba (RF-09, RNF-10)
START TRANSACTION;
UPDATE paciente  SET telefono = '221-5550000'  WHERE dni = '30987654';
UPDATE cobertura SET plan = 'SMG30'            WHERE id_paciente = 2;
SELECT dni, apellido, telefono, obra_social, plan FROM v_paciente WHERE dni = '30987654';
ROLLBACK;

-- P13 - Pruebas de las reglas garantizadas por triggers. Se dejan
-- comentadas porque, al ejecutarlas, el motor responde con el error
-- esperado (SQLSTATE 45000) e interrumpiria el script:
-- UPDATE consulta SET motivo = 'cambio' WHERE id_consulta = 1;
--   -> ERROR 1644: La consulta esta CERRADA y no puede modificarse (Ley 26.529).
-- DELETE FROM paciente WHERE id_paciente = 12;
--   -> ERROR 1644: Los pacientes no se eliminan: use la baja logica (activo = FALSE).
-- INSERT INTO diagnostico (id_consulta, codigo_cie10) VALUES (1, 'R51');
--   -> ERROR 1644: No se pueden agregar diagnosticos a una consulta cerrada.
