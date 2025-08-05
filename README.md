# Order Process System

Microservicio para la gestión y procesamiento de órdenes, desarrollado con **Spring Boot 3**, **Spring WebFlux**, **gRPC**, **Akka Classic Actors**, **MongoDB Reactivo**, **SMPP** y **Micrometer + Prometheus**.

El sistema soporta entrada tanto por **REST** como por **gRPC**, procesa de forma asíncrona con **Akka**, persiste en MongoDB, envía notificaciones SMS vía SMPP y expone métricas para monitoreo.

---

## 🚀 Tecnologías Usadas
- **Spring Boot 3.2.0**
- **Spring WebFlux** (programación reactiva)
- **Spring Data MongoDB Reactivo**
- **Spring Boot Actuator**
- **gRPC con Protobuf**
- **Akka Classic Actors**
- **Cloudhopper SMPP**
- **Micrometer + Prometheus**
- **SLF4J + Log4j2**

---

## 📂 Estructura del Proyecto

```
src/main/java/com/hacom/order_process_system
│
├── config/               # Configuración de Akka, Mongo, Métricas y WebFlux
├── controller/           # Controladores REST
├── grpc/                 # Implementaciones de servicios gRPC
├── actor/                # Actores de Akka para procesamiento concurrente
├── repository/           # Repositorios MongoDB reactivos
├── service/              # Servicios y lógica de negocio
│   └── proxy/sms/        # Implementación de servicio SMPP
└── model/                # Modelos y DTOs
```

## 📌 Endpoints REST

### Obtener estado de una orden
```http
GET /api/orders/{orderId}/status
```
**Respuesta:**
```json
{
    "code": "200",
    "status": 200,
    "comment": "Order found successfully",
    "data": {
        "orderId": "ORDER-003",
        "status": "PROCESSED",
        "timestamp": "2025-08-05T03:54:23.552Z"
    }
}
```

### Contar órdenes por rango de fechas
```http
GET /api/orders/count?startDate=2024-08-01T00:00:00Z&endDate=2025-08-04T03:56:04.474Z
```
**Respuesta:**
```json
{
    "code": "200",
    "status": 200,
    "comment": "Order count retrieved successfully",
    "data": {
        "totalOrders": 2,
        "startDate": "2024-08-01T00:00:00Z",
        "endDate": "2025-08-04T03:56:04.474Z"
    }
}
```

---

## 📌 Servicio gRPC

### Definición en `order.proto`
```proto
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

**Puerto gRPC:** `9090` (configurable)

---

## 📊 Métricas y Monitoreo

- **Endpoint Prometheus:**  
  ```
  GET /actuator/prometheus
  ```
- **Métricas personalizadas:**
  - `orders.created` → Órdenes creadas vía gRPC.
  - `orders.processed` → Órdenes procesadas exitosamente.

Ejemplo en Prometheus:
```
# HELP orders_received_total Number of orders received via gRPC
# TYPE orders_received_total counter
orders_received_total 120.0
```

---

## 💬 Notificaciones SMS

- Implementadas en `SmsServiceImpl` usando **Cloudhopper SMPP**.
- Configuración de host, puerto y credenciales SMPP en `init()`.
- Envía mensaje de confirmación al cliente una vez procesada la orden.

---

## ▶️ Ejecución

### Compilar
```bash
./gradlew clean build
```



### Probar gRPC
Usar [grpcurl](https://github.com/fullstorydev/grpcurl):
```bash
grpcurl -plaintext -d "{\"order_id\": \"ORDER-001\", \"customer_id\": \"CUSTOMER-123\", \"customer_phone_number\": \"1234567890\", \"items\": [\"Producto A\", \"Producto B\"]}" localhost:9090 order.OrderService/CreateOrder
```

---

## 📈 Arquitectura

1. **REST o gRPC** recibe la orden.
2. **gRPC** envía mensaje a **OrderProcessingActor**.
3. Actor guarda en **MongoDB** y envía **SMS** simulado.
4. Métricas registradas en **Prometheus**.


