# 16. GESTIÓN DE RIESGOS Y LIMITACIONES TÉCNICAS — SIGM-QUINUA

## 1. LIMITACIONES TÉCNICAS IDENTIFICADAS

1. **Conectividad a Internet en la Zona de Quinua:**  
   El distrito de Quinua cuenta con variaciones en el servicio de banda ancha. Por este motivo, el sistema fue concebido bajo arquitectura monolítica capaz de operar en la red local municipal (Intranet / LAN) sin requerir acceso continuo a Internet para sus funciones operativas internas.
2. **Dependencia de Infraestructura Local:**  
   Al operar en servidor local on-premise en el Palacio Municipal de Quinua, se requiere un sistema de alimentación ininterrumpida (UPS) de al menos 2 kVA para prevenir apagados abruptos que puedan dañar la base de datos PostgreSQL durante cortes de energía eléctrica.
3. **Ausencia de Pasarela de Pago Bancario en Línea:**  
   Los pagos en los portales del ciudadano y contribuyente requieren el registro manual de vouchers bancarios o la atención presencial en ventanilla de caja, dado que la entidad no cuenta actualmente con contrato activo con pasarelas comerciales (e.g., Niubiz, Izipay).

---

## 2. MATRIZ DE RIESGOS RESIDUALES Y PLANES DE MITIGACIÓN

| Identificador | Descripción del Riesgo | Probabilidad | Impacto | Estrategia de Mitigación |
| :---: | :--- | :---: | :---: | :--- |
| **RSK-01** | Saturación de espacio en disco por acumulación de PDFs de expedientes | Media | Alto | Implementación de script semanal de verificación de almacenamiento y política de compresión PDF estándar. |
| **RSK-02** | Concurrencia alta en fechas límites de pago tributario (vencimiento de predial) | Media | Medio | Ajuste del pool HikariCP a 20 conexiones y configuración de optimizaciones de consulta e índices en PostgreSQL. |
| **RSK-03** | Errores humanos en cuadre de caja por anulación indebida de recibos | Baja | Alto | Restricción de permisos: solo administradores pueden anular recibos tributarios con justificación obligatoria y auditoría. |
| **RSK-04** | Vulnerabilidades por software desactualizado | Baja | Medio | Uso de Java 17 LTS y dependencias Spring Boot consolidadas sin dependencias externas CDN. |

