# 10. MANUAL DE INSTALACIÓN LOCAL Y REQUISITOS — SIGM-QUINUA

## 1. REQUISITOS PREVIOS DEL SISTEMA

### Requisitos de Hardware:
* **Procesador:** CPU x86_64 Dual Core de 2.0 GHz o superior (Intel Core i3 / AMD Ryzen 3 mínimo).
* **Memoria RAM:** Mínimo 4 GB (se recomiendan 8 GB para ejecución concurrente con PostgreSQL).
* **Almacenamiento en Disco:** Mínimo 2 GB de espacio libre disponible (SSD recomendado).

### Requisitos de Software:
* **Sistema Operativo:** Microsoft Windows 10/11 (64 bits) o Linux Ubuntu Server 22.04 LTS en adelante.
* **Java Development Kit (JDK):** Java 17 LTS (Eclipse Temurin, Amazon Corretto u OpenJDK oficial).
* **Base de Datos:** PostgreSQL versión 15 a 17 instalada y en ejecución.
* **Navegador Web:** Google Chrome 110+, Mozilla Firefox 110+, o Microsoft Edge 110+.

---

## 2. PROCEDIMIENTO DE INSTALACIÓN PASO A PASO (WINDOWS 11)

### Paso 1: Verificación de Java 17
Abra una consola de PowerShell o CMD y verifique la versión activa de Java:
```powershell
java -version
```
*Respuesta esperada:* `openjdk version "17.0.x"` o similar con compatibilidad Java 17.

### Paso 2: Configuración de PostgreSQL 17
1. Asegúrese de que el servicio PostgreSQL se encuentre activo en el puerto `5433` (o `5432` según configuración local).
2. Conéctese mediante `psql` o pgAdmin con el usuario administrador:
```sql
CREATE DATABASE sigm_quinua WITH ENCODING = 'UTF8';
```

### Paso 3: Clonación o Ubicación del Proyecto
Ubíquese en el directorio raíz del repositorio:
```powershell
cd "c:\Users\ASUS\OneDrive\PROYECTO FINAL DE LENGUAJE DE PROGRAMACION MUNICIPALIDAD DISTRITAL DE QUINUA\SIGM-QUINUA"
```

### Paso 4: Verificación de Maven Wrapper
Verifique que el wrapper de Maven responda correctamente:
```powershell
.\mvnw.cmd -version
```

### Paso 5: Compilación del Código Fuente
Compile las clases de producción y prueba:
```powershell
.\mvnw.cmd compile
```
*Resultado esperado:* `BUILD SUCCESS` (compilación de 2,048 clases de código fuente).

### Paso 6: Ejecución de Migraciones de Base de Datos
Las 67 migraciones Flyway se aplicarán automáticamente al arrancar la aplicación, creando el esquema completo y sembrando los datos iniciales de catálogos y usuarios administrativos.

### Paso 7: Ejecución del Servidor Spring Boot
Inicie la aplicación en modo desarrollo:
```powershell
.\mvnw.cmd spring-boot:run
```
*Respuesta en consola:*
```text
Tomcat started on port 8080 (http) with context path '/'
Started SigmQuinuaApplication in X.XX seconds
```

### Paso 8: Acceso al Sistema
Abra su navegador web e ingrese a:
`http://localhost:8080`

---

## 3. SOLUCIÓN A PROBLEMAS FRECUENTES (TROUBLESHOOTING)

| Síntoma de Error | Causa Probable | Solución Verificada |
| :--- | :--- | :--- |
| `Connection refused: localhost:5433` | El servicio PostgreSQL no está iniciado o está en otro puerto. | Verifique que PostgreSQL esté corriendo en el puerto 5433 o configure `DB_PORT=5432` en el archivo de propiedades. |
| `Access denied for user 'postgres'` | Contraseña de base de datos incorrecta. | Ajuste la variable `DB_PASSWORD` en su entorno o en `application.properties`. |
| `Failed to clean project: classes locked` | Un proceso de Java o IDE mantiene tomado un archivo `.class`. | Cierre instancias previas de Java o evite usar `clean` directamente; use `mvnw compile`. |

