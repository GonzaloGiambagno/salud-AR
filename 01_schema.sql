-- =====================================================================
--  Salud-AR — Historia Clínica Electrónica
--  01_schema.sql — Creación de la base de datos, tablas e índices
--  Motor: MySQL 8  ·  InnoDB  ·  utf8mb4
--  Autor: Giambagno, Gonzalo
-- =====================================================================

DROP DATABASE IF EXISTS saludar;
CREATE DATABASE saludar CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE saludar;

-- ---------- Seguridad ----------
CREATE TABLE usuario (
    id_usuario       INT AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario   VARCHAR(50)  NOT NULL UNIQUE,
    hash_contrasena  VARCHAR(255) NOT NULL,
    rol              VARCHAR(20)  NOT NULL,           -- MEDICO | ADMINISTRATIVO | ADMINISTRADOR
    activo           BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT chk_usuario_rol
        CHECK (rol IN ('MEDICO','ADMINISTRATIVO','ADMINISTRADOR'))
) ENGINE=InnoDB;

CREATE TABLE profesional (
    id_profesional  INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario      INT NOT NULL UNIQUE,
    nombre          VARCHAR(80) NOT NULL,
    apellido        VARCHAR(80) NOT NULL,
    matricula       VARCHAR(20) NOT NULL UNIQUE,
    especialidad    VARCHAR(60),
    CONSTRAINT fk_prof_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- ---------- Pacientes y cobertura ----------
CREATE TABLE obra_social (
    id_obra_social  INT AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(80) NOT NULL,
    codigo          VARCHAR(20) UNIQUE
) ENGINE=InnoDB;

CREATE TABLE paciente (
    id_paciente  INT AUTO_INCREMENT PRIMARY KEY,
    dni          VARCHAR(15) NOT NULL UNIQUE,
    apellido     VARCHAR(80) NOT NULL,
    nombre       VARCHAR(80) NOT NULL,
    fecha_nac    DATE,
    sexo         CHAR(1),
    domicilio    VARCHAR(120),
    telefono     VARCHAR(30),
    email        VARCHAR(80),
    activo       BOOLEAN NOT NULL DEFAULT TRUE          -- baja lógica (Ley 26.529)
) ENGINE=InnoDB;

CREATE TABLE cobertura (
    id_cobertura    INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente     INT NOT NULL,
    id_obra_social  INT NOT NULL,
    nro_afiliado    VARCHAR(40),
    plan            VARCHAR(60),
    CONSTRAINT fk_cob_paciente   FOREIGN KEY (id_paciente)    REFERENCES paciente(id_paciente),
    CONSTRAINT fk_cob_obrasocial FOREIGN KEY (id_obra_social) REFERENCES obra_social(id_obra_social)
) ENGINE=InnoDB;

-- ---------- Antecedentes y alergias ----------
CREATE TABLE alergia (
    id_alergia     INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente    INT NOT NULL,
    sustancia      VARCHAR(80) NOT NULL,
    tipo_reaccion  VARCHAR(80),
    severidad      VARCHAR(20),
    CONSTRAINT fk_alergia_paciente FOREIGN KEY (id_paciente)
        REFERENCES paciente(id_paciente)
) ENGINE=InnoDB;

CREATE TABLE antecedente (
    id_antecedente INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente    INT NOT NULL,
    tipo           VARCHAR(30),
    descripcion    VARCHAR(255),
    fecha          DATE,
    CONSTRAINT fk_ant_paciente FOREIGN KEY (id_paciente)
        REFERENCES paciente(id_paciente)
) ENGINE=InnoDB;

-- ---------- Catálogo CIE-10 ----------
CREATE TABLE cie10 (
    codigo_cie10 VARCHAR(10) PRIMARY KEY,
    descripcion  VARCHAR(255) NOT NULL,
    capitulo     VARCHAR(80)
) ENGINE=InnoDB;

-- ---------- Consulta y detalle clínico ----------
CREATE TABLE consulta (
    id_consulta     INT AUTO_INCREMENT PRIMARY KEY,
    id_paciente     INT NOT NULL,
    id_profesional  INT NOT NULL,
    fecha_hora      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo          VARCHAR(255),
    evaluacion      TEXT,
    indicaciones    TEXT,
    estado          VARCHAR(15) NOT NULL DEFAULT 'BORRADOR',  -- BORRADOR | CERRADA | RECTIFICADA
    CONSTRAINT fk_cons_paciente    FOREIGN KEY (id_paciente)    REFERENCES paciente(id_paciente),
    CONSTRAINT fk_cons_profesional FOREIGN KEY (id_profesional) REFERENCES profesional(id_profesional),
    CONSTRAINT chk_cons_estado
        CHECK (estado IN ('BORRADOR','CERRADA','RECTIFICADA'))
) ENGINE=InnoDB;

CREATE TABLE diagnostico (
    id_diagnostico INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta    INT NOT NULL,
    codigo_cie10   VARCHAR(10) NOT NULL,
    principal      BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_diag_consulta FOREIGN KEY (id_consulta)  REFERENCES consulta(id_consulta),
    CONSTRAINT fk_diag_cie10    FOREIGN KEY (codigo_cie10) REFERENCES cie10(codigo_cie10)
) ENGINE=InnoDB;

CREATE TABLE medicacion (
    id_medicacion INT AUTO_INCREMENT PRIMARY KEY,
    id_consulta   INT NOT NULL,
    farmaco       VARCHAR(80) NOT NULL,
    dosis         VARCHAR(40),
    frecuencia    VARCHAR(40),
    duracion      VARCHAR(40),
    CONSTRAINT fk_med_consulta FOREIGN KEY (id_consulta)
        REFERENCES consulta(id_consulta)
) ENGINE=InnoDB;

-- ---------- Auditoría (trazabilidad — RF-23) ----------
CREATE TABLE auditoria (
    id_auditoria INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario   INT NOT NULL,
    entidad      VARCHAR(40) NOT NULL,
    accion       VARCHAR(20) NOT NULL,
    fecha_hora   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aud_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuario(id_usuario)
) ENGINE=InnoDB;

-- =====================================================================
--  Índices secundarios (soportan RNF-05 y RNF-06: búsquedas < 2-3 s)
-- =====================================================================
CREATE INDEX idx_paciente_apellido   ON paciente(apellido);
CREATE INDEX idx_paciente_activo      ON paciente(activo);
CREATE INDEX idx_consulta_paciente    ON consulta(id_paciente, fecha_hora);
CREATE INDEX idx_diagnostico_consulta ON diagnostico(id_consulta);
CREATE INDEX idx_alergia_paciente     ON alergia(id_paciente);
CREATE INDEX idx_cobertura_paciente   ON cobertura(id_paciente);
CREATE FULLTEXT INDEX idx_cie10_desc  ON cie10(descripcion);
