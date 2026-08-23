# exmapleCamel

Proyecto de ejemplo de integración con **Apache Camel 4.20** sobre **Spring Boot 4.0.7** (Java 17). Demuestra varios patrones de integración empresarial (EIP): lectura de ficheros legacy, exposición de API REST, enrutamiento condicional, persistencia JPA, mensajería con ActiveMQ y cacheo en Redis.

## Stack tecnológico

| Componente        | Versión / Detalle                          |
|-------------------|--------------------------------------------|
| Java              | 17                                         |
| Spring Boot       | 4.0.7                                       |
| Apache Camel      | 4.20.0 (spring-boot-starter)               |
| Base de datos     | MySQL (`camel_db`) vía Spring Data JPA     |
| Mensajería        | ActiveMQ 6 (`tcp://localhost:61616`)       |
| Cache             | Redis (`localhost:6379`)                   |
| Formato ficheros  | BeanIO (CSV)                               |
| API REST          | Camel REST + Jetty (puerto `8084`)         |
| Utilidades        | Lombok, Jackson                            |

## Estructura del proyecto

```
src/main/java/com/javacamel/exmapleCamel/
├── ExmapleCamelApplication.java      # Arranque Spring Boot (@EntityScan sobre beans)
├── beans/
│   ├── NameAddress.java              # Entidad JPA (tabla NAME_ADDRESS)
│   ├── OutBoundNameAddress.java      # DTO de salida (name + address concatenada)
│   └── InboundRestProcessingbean.java# Bean de validación (extrae userCity)
├── components/                       # Rutas Camel (RouteBuilder)
│   ├── LegacyFileRoute.java
│   ├── NewRestRoute.java
│   ├── BatchJPAProcessingRoute.java
│   ├── QueueReciveRoute.java
│   ├── RedisRoute.java
│   └── ActivemqConfig.java           # ConnectionFactory de ActiveMQ
└── procesor/
    └── InboundMessageProcessor.java  # Transforma NameAddress -> OutBoundNameAddress

src/main/resources/
├── application.yaml                  # Config de Camel, datasource, redis, jpa
├── inboundMessageBeanIOMapping.xml   # Mapeo BeanIO CSV -> NameAddress
└── nameAddress.sql                   # DDL de la tabla name_address
```

## Rutas Camel (flujos de integración)

### 1. `LegacyFileRoute` — Procesamiento de ficheros CSV
Lee `src/data/input/inputFile.csv`, lo divide por líneas, lo deserializa con BeanIO
a objetos `NameAddress`, y escribe el resultado en `src/data/output/outputFile.csv`
(modo append).

### 2. `NewRestRoute` — API REST con enrutamiento condicional
- Expone `POST /testcamel/nameAddress` (JSON) en el puerto **8084**.
- Valida el mensaje y extrae la cabecera `userCity`.
- **Regla de negocio (choice):**
  - Si `userCity == "AJAX"` → envía a **ActiveMQ** y a **Redis**.
  - En caso contrario → persiste en **BD (JPA)** y envía a **ActiveMQ**.
- Maneja `JMSException` / `ConnectException` de forma controlada.

### 3. `BatchJPAProcessingRoute` — Procesamiento batch programado
Cada 5 segundos (`timer`) consulta todas las filas de `NameAddress`
(namedQuery `fetchAllRows`), las transforma, las escribe al CSV de salida y
finalmente **elimina** cada registro procesado de la BD.

### 4. `QueueReciveRoute` — Consumidor de cola
Escucha la cola `nameaddressqueue` de ActiveMQ y registra los mensajes recibidos.

### 5. `RedisRoute` — Escritura en Redis
Ruta `direct:redisTest` que ejecuta un `SET camel:test = "Hola Redis"`.

## Modelo de datos

Tabla `name_address`:

| Columna       | Tipo         |
|---------------|--------------|
| id            | BIGINT (PK)  |
| name          | VARCHAR(255) |
| house_number  | VARCHAR(255) |
| city          | VARCHAR(255) |
| province      | VARCHAR(255) |
| postal_code   | VARCHAR(255) |

## Requisitos previos

- JDK 17
- MySQL con base de datos `camel_db` (usuario `camel` / clave `camel123`)
- ActiveMQ escuchando en `tcp://localhost:61616` (usuario `admin` / clave `admin`)
- Redis en `localhost:6379`

## Ejecución

```bash
./mvnw spring-boot:run
```

## Consola Hawtio (grafo de las rutas Camel)

El proyecto integra [Hawtio](https://hawt.io) (vía `io.hawt:hawtio-springboot4` + Spring Boot Actuator)
para poder visualizar en el navegador el diagrama de cada ruta Camel, sus contadores
(mensajes procesados, en error, tiempo medio, etc.) y el estado del `CamelContext`, sin
necesidad de herramientas externas de JMX.

1. Arranca la aplicación normalmente:
   ```bash
   ./mvnw spring-boot:run
   ```
2. Abre en el navegador:
   ```
   http://localhost:8081/actuator/hawtio
   ```
3. En el menú lateral entra a **Camel → Routes**. Al seleccionar una ruta (por ejemplo
   `managementNameAddressId`, `toDBId`, `toActiveMQId`, etc.) verás la pestaña
   **Route Diagram** con el grafo visual del flujo (EIPs, choice/otherwise, endpoints)
   y la pestaña **Statistics/Metrics** con las métricas en vivo de esa ruta.

Notas:
- Hawtio/Actuator corre en su **propio puerto (8081)**, separado del puerto principal de la
  app (**8080**, `server.port`) y del REST de Camel (**8084**, `camel-jetty`). Esto se controla
  con `management.server.port` en `application.yaml`; cámbialo al valor que prefieras (o
  quítalo para que Hawtio vuelva a compartir el puerto principal de la app).
- Por ahora la autenticación de Hawtio está desactivada (`hawtio.authenticationEnabled: false`)
  para simplificar el desarrollo local. Si el proyecto se despliega en un entorno compartido,
  añade `io.hawt:hawtio-springboot4-security` y habilita/gestiona el login antes de exponerlo.

## Prueba rápida de la API REST

```bash
curl -X POST http://localhost:8084/testcamel/nameAddress \
  -H "Content-Type: application/json" \
  -d '{"name":"Clem","houseNumber":"80","city":"AJAX","province":"Ontario","postalCode":"L1S"}'
```

- Con `city = "AJAX"` → el mensaje va a ActiveMQ y Redis.
- Con cualquier otra ciudad → se persiste en la BD y se envía a ActiveMQ.

## Notas

- El proyecto es un ejemplo educativo para practicar EIPs con Apache Camel.
- Los tests (`LegacyFileRouteTest`) están comentados y usan `AdviceWith` + `MockEndpoint`
  para simular endpoints de fichero.
