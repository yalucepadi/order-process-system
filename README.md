# Order Processing System - HACOM Backend Evaluation

## Descripción

Sistema de procesamiento de pedidos que integra múltiples tecnologías utilizadas en proyectos Telco. El sistema recibe pedidos a través de gRPC, los procesa usando Akka Actors, los almacena en MongoDB y envía notificaciones SMS usando SMPP.

## Tecnologías Integradas

- **Spring Boot 3.2.0** con Java 17
- **Spring WebFlux** - API reactiva
- **Spring Data MongoDB Reactive** - Base de datos reactiva
- **gRPC** - Servicio de creación de pedidos
- **Akka Classic Actors** - Procesamiento asíncrono
- **MongoDB** - Almacenamiento de datos
- **SMPP (Cloudhopper)** - Envío de SMS
- **Log4j2** - Sistema de logging
- **Prometheus** - Métricas con Spring Actuator
- **Gradle** - Gestión de dependencias

## Estructura del Proyecto

```
src/
├── main/
│   ├── java/com/hacom/
│   │   ├── OrderProcessingApplication.java      # Clase principal
│   │   ├── actor/
│   │   │   └── OrderProcessingActor.java        # Actor Akka
│   │   ├── config/
│   │   │   ├── AkkaConfig.java                  # Configuración Akka
│   │   │   ├── MongoConfig.java                 # Configuración MongoDB
│   │   │   └── WebFluxConfig.java               # Configuración WebFlux
│   │   ├── controller/
│   │   │   └── OrderController.java             # API REST
│   │   ├── grpc/
│   │   │   └── OrderGrpcService.java            # Servicio gRPC
│   │   ├── model/
│   │   │   └── Order.java                       # Modelo de datos
│   │   ├── repository/
│   │   │   └── OrderRepository.java             # Repositorio MongoDB
│   │   └── service/
│   │       └── SmsService.java                  # Servicio SMPP
│   ├── proto/
│   │   └── orderRequest.proto                          # Definición gRPC
│   └── resources/
│       ├── application.yml                      # Configuración principal
│       └── log4j2.yml                           # Configuración logging
├── build.gradle                                 # Configuración Gradle
└── README.md                                    # Este archivo
```

## Configuración

### Variables de Configuración (application.yml)

```yaml
app:
  mongodb:
    database: exampleDb
    uri: "mongodb://127.0.0.1:27017"
  api:
    port: 9898
```

### Puertos

- **WebFlux API**: 9898
- **gRPC Server**: 9090
- **MongoDB**: 27017 (por defecto)
- **SMPP**: 2775 (configurable en SmsService)

## Prerequisitos

1. **Java 17** instalado
2. **MongoDB** ejecutándose en `localhost:27017`
3. **Servidor SMPP** (opcional, para pruebas reales de SMS)

## Instalación y Ejecución

### 1. Clonar el repositorio
```bash
git clone <repository-url>
cd orderRequest-processing-system
```

### 2. Compilar el proyecto
```bash
./gradlew build
```

### 3. Ejecutar la aplicación
```bash
./gradlew bootRun
```

## API Endpoints

### REST API (Puerto 9898)

#### 1. Consultar estado de pedido
```http
GET /api/orders/{orderId}/status
```

**Respuesta:**
```json
{
  "orderId": "12345",
  "status": "PROCESSED"
}
```

#### 2. Consultar total de pedidos por rango de fecha
```http
GET /api/orders/count?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z
```

**Respuesta:**
```json
{
  "startDate": "2024-01-01T00:00:00Z",
  "endDate": "2024-12-31T23:59:59Z",
  "totalOrders": 150
}
```

### gRPC Service (Puerto 9090)

#### Crear Pedido
```protobuf
service OrderService {
  rpc CreateOrder(CreateOrderRequest) returns (CreateOrderResponse);
}

message CreateOrderRequest {
  string order_id = 1;
  string customer_id = 2;
  string customer_phone_number = 3;
  repeated string items = 4;
}

message CreateOrderResponse {
  string order_id = 1;
  string status = 2;
}
```

## Métricas y Monitoreo

### Spring Actuator Endpoints

- **Health**: `http://localhost:9898/actuator/health`
- **Prometheus**: `http://localhost:9898/actuator/prometheus`
- **Metrics**: `http://localhost:9898/actuator/metrics`

### Métricas Personalizadas

- `orders.created` - Contador de pedidos creados

## Flujo de Procesamiento

1. **Cliente envía pedido** via gRPC al `OrderGrpcService`
2. **Incrementa contador** de Prometheus
3. **Envía mensaje** al `OrderProcessingActor` (Akka)
4. **Actor procesa pedido**:
    - Crea objeto `Order`
    - Guarda en MongoDB
    - Envía SMS via SMPP
    - Responde al cliente gRPC
5. **Logs** se registran en todas las etapas

## Pruebas

### Usando grpcurl (Cliente gRPC)

```bash
# Instalar grpcurl
go install github.com/fullstorydev/grpcurl/cmd/grpcurl@latest

# Crear pedido
grpcurl -plaintext -d '{
  "order_id": "12345",
  "customer_id": "CUST001",
  "customer_phone_number": "1234567890",
  "items": ["item1", "item2", "item3"]
}' localhost:9090 orderRequest.OrderService/CreateOrder
```

### Usando curl (API REST)

```bash
# Consultar estado de pedido
curl http://localhost:9898/api/orders/12345/status

# Consultar total de pedidos
curl "http://localhost:9898/api/orders/count?startDate=2024-01-01T00:00:00Z&endDate=2024-12-31T23:59:59Z"
```

## Logging

Los logs se guardan en:
- **Consola**: Formato legible para desarrollo
- **Archivo**: `logs/application.log` con rotación diaria

## Notas de Configuración

### MongoDB
- Configuración programática en `MongoConfig.java`
- No usa la configuración automática de Spring Boot

### WebFlux
- Puerto configurado programáticamente en `WebFluxConfig.java`
- Servidor Netty reactivo

### SMPP
- Cliente configurado para pruebas locales
- Requiere servidor SMPP real para funcionalidad completa

## Arquitectura

```
[Cliente] --> [gRPC Service] --> [Akka Actor] --> [MongoDB]
                    |                 |
                    v                 v
              [Prometheus]        [SMPP SMS]
```

## Consideraciones de Producción

1. **SMPP**: Configurar credenciales reales del proveedor
2. **MongoDB**: Configurar autenticación y SSL
3. **Logging**: Ajustar niveles para producción
4. **Métricas**: Configurar alertas en Prometheus
5. **Error Handling**: Implementar circuit breakers
6. **Security**: Agregar autenticación/autorización

## Troubleshooting

### Problemas Comunes

1. **MongoDB no conecta**: Verificar que esté ejecutándose en puerto 27017
2. **Puerto en uso**: Cambiar puertos en `application.yml`
3. **gRPC no responde**: Verificar firewall y puerto 9090
4. **SMPP falla**: Normal si no hay servidor SMPP configurado

### Logs Útiles

```bash
# Ver logs en tiempo real
tail -f logs/application.log

# Filtrar logs de pedidos
grep "Processing orderRequest" logs/application.log
```

## Autor

Desarrollado para evaluación técnica HACOM - Backend Java