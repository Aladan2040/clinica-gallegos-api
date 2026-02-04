-- Script de inicialización para PostgreSQL
-- Este script se ejecuta automáticamente al crear el contenedor de Docker

-- Crear la base de datos (ya se crea automáticamente por POSTGRES_DB)
-- Conectar a la base de datos
\c clinicagallegos_bd;

-- Crear tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    usuario_id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    telefono VARCHAR(20),
    rol VARCHAR(20) CHECK (rol IN ('PACIENTE', 'ADMIN')) NOT NULL DEFAULT 'PACIENTE',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Crear tabla de servicios
CREATE TABLE IF NOT EXISTS servicios (
    servicio_id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    descripcion TEXT,
    duracion_minutos INTEGER NOT NULL,
    costo DECIMAL(10,2) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- Crear tabla de citas
CREATE TABLE IF NOT EXISTS citas (
    cita_id SERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    servicio_id INTEGER NOT NULL,
    fecha_hora TIMESTAMP NOT NULL,
    estado VARCHAR(20) CHECK (estado IN ('PENDIENTE', 'CONFIRMADA', 'CANCELADA', 'COMPLETADA')) NOT NULL DEFAULT 'PENDIENTE',
    notas TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE,
    FOREIGN KEY (servicio_id) REFERENCES servicios(servicio_id) ON DELETE RESTRICT
);

-- Crear índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_fecha_servicio ON citas(fecha_hora, servicio_id);
CREATE INDEX IF NOT EXISTS idx_usuario_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_citas_usuario ON citas(usuario_id);
CREATE INDEX IF NOT EXISTS idx_citas_estado ON citas(estado);

-- Insertar servicios por defecto
INSERT INTO servicios (nombre, descripcion, duracion_minutos, costo, activo) VALUES
('Limpieza Dental Básica', 'Remoción de placa, sarro y pulido dental.', 45, 50.00, TRUE),
('Control Odontológico General', 'Revisión completa, evaluación de salud bucal y diagnóstico.', 30, 25.00, TRUE),
('Blanqueamiento Dental LED', 'Tratamiento de blanqueamiento profesional con luz LED.', 90, 250.00, TRUE),
('Endodoncia (Tratamiento de Conducto)', 'Tratamiento para salvar un diente dañado o infectado.', 120, 350.00, TRUE),
('Extracción Simple', 'Remoción de piezas dentales sin complicaciones quirúrgicas.', 60, 80.00, TRUE),
('Otros / Servicio No Listado', 'Solicitud especial que requiere revisión por parte del administrador de la clínica.', 30, 0.00, TRUE)
ON CONFLICT (nombre) DO NOTHING;

-- Mensaje de confirmación
SELECT 'Base de datos inicializada correctamente' AS status;
