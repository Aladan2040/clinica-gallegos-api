create database clinicaGallegos;
use clinicaGallegos;

create table usuarios(
	usuario_id int auto_increment primary key,
    nombre varchar(100) not null,
    apellido varchar(100) not null,
    email varchar(150) unique not null,
    contrasena_hash varchar(255) not null,
    telefono varchar(20),
    rol enum('PACIENTE','ADMIN') not null default 'PACIENTE',
    fecha_registro timestamp default current_timestamp
);

create table servicios(
	servicio_id int auto_increment primary key,
    nombre varchar(100) unique not null,
    descripcion text,
    duracion_minutos int not null,
    costo decimal(10,2) not null,
    activo boolean not null default true
);

create table citas(
	cita_id int auto_increment primary key,
    usuario_id int not null,
    servicio_id int not null,
	fecha_hora datetime not null,
    estado enum('Pendiente','Confirmada','Cancelada','Completada') not null default 'Pendiente',
    notas text,
    fecha_creacion timestamp default current_timestamp,
    foreign key(usuario_id) references usuarios(usuario_id) on delete cascade,
    foreign key(servicio_id) references servicios(servicio_id) on delete restrict,
    INDEX idx_fecha_servicio (fecha_hora, servicio_id)
);

-- Inserts para la tabla Servicios

INSERT INTO Servicios (nombre, descripcion, duracion_minutos, costo, activo) VALUES
('Limpieza Dental Básica', 'Remoción de placa, sarro y pulido dental.', 45, 50.00, TRUE),
('Control Odontológico General', 'Revisión completa, evaluación de salud bucal y diagnóstico.', 30, 25.00, TRUE),
('Blanqueamiento Dental LED', 'Tratamiento de blanqueamiento profesional con luz LED.', 90, 250.00, TRUE),
('Endodoncia (Tratamiento de Conducto)', 'Tratamiento para salvar un diente dañado o infectado.', 120, 350.00, TRUE),
('Extracción Simple', 'Remoción de piezas dentales sin complicaciones quirúrgicas.', 60, 80.00, TRUE),
('Otros / Servicio No Listado', 'Solicitud especial que requiere revisión por parte del administrador de la clínica.', 30, 0.00, TRUE);

select * from citas;
select * from servicios;
select * from usuarios

DELETE FROM Servicios 
WHERE servicio_id = 10;