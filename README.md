# Salud-AR — Historia Clínica Electrónica ambulatoria

Prototipo operacional desarrollado para el Seminario de Práctica (2026).
Aplicación de escritorio en **Java 21 + Swing** con persistencia en **MySQL 8** (JDBC).

## Requisitos

- JDK 21
- MySQL Server 8.0
- Apache Maven 3.9 (o un IDE que lo incluya: IntelliJ IDEA / NetBeans)

## 1. Crear la base de datos

El script `db/saludar.sql` crea la base `saludar` completa: tablas, índices, vistas,
triggers, el usuario de la aplicación y los datos de prueba. **Elimina y vuelve a crear
la base `saludar`** si ya existe. Se ejecuta como `root`:

```bash
mysql -u root -p < db/saludar.sql
```

También puede abrirse y ejecutarse completo desde MySQL Workbench.

## 2. Compilar y ejecutar

```bash
mvn package
java -jar target/salud-ar.jar
```

`mvn package` compila y genera `target/salud-ar.jar`, que ya incluye el
driver de MySQL.

## Usuarios de prueba

| Usuario         | Contraseña         | Rol            |
|-----------------|--------------------|----------------|
| `administrador` | `administrador123` | ADMINISTRADOR  |
| `recepcion`     | `recepcion123`     | ADMINISTRATIVO |
| `medico`        | `medico123`        | MEDICO (Dr. Juan Pérez) |
| `medico2`       | `medico123`        | MEDICO (Dra. Laura Ruiz) |

Solo los usuarios MEDICO pueden registrar consultas y alergias.

## Configuración de la conexión

Por defecto la aplicación se conecta a `localhost:3306/saludar` con el usuario
`saludar_app` que crea el script. Para usar otra base o credenciales, copiar
`config/db.properties.example` como `config/db.properties` y editarlo.

## Arquitectura

La interfaz gráfica sigue el patrón **MVC** y el acceso a datos el patrón **DAO**:

- **Modelo:** `modelo`, `servicio` y `persistencia` (entidades, reglas de negocio y DAO sobre MySQL).
- **Vista:** `ui`, las pantallas Swing. Cada una implementa una interfaz de vista y no contiene lógica.
- **Controlador:** `controlador`, que recibe los eventos de la vista, invoca al modelo y decide qué mostrar.

## Estructura

```
db/saludar.sql                      script completo de la base de datos
src/main/java/saludar/
  app/          puntos de entrada (MainSwing, Main) y armado del MVC
  config/       ConexionBD (Singleton) y configuración
  controlador/  controladores MVC e interfaces de vista
  excepciones/  excepciones de negocio y de persistencia
  modelo/       entidades del dominio
  persistencia/ interfaces DAO y fábrica abstracta
    mysql/      implementación JDBC (DAOBaseMySQL abstracta + DAO concretos)
  seguridad/    hash de contraseñas (PBKDF2) y sesión
  servicio/     lógica de negocio
  ui/           vistas Swing
  util/         ordenación y búsqueda sobre arreglos
```
