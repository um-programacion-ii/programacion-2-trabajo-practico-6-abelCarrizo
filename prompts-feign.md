# Prompts de Feign

## Prompt 1: Que es Feign?

### Prompt Utilizado:
```
Puedes hablarme sobre Feign? Que es? Para que sirve? Donde puedo obtener mas informacion sobre esto?
```

### Respuesta Recibida:
```
¡buena pregunta! 👇

### ¿Qué es Feign y para qué sirve?

* **Feign (OpenFeign)** es un **cliente HTTP declarativo**: en lugar de escribir código para construir solicitudes HTTP, definís una **interfaz Java** con anotaciones (por ej. `@GetMapping`, `@PostMapping`) y Feign genera la implementación que llama a la API remota. Soporta **encoders/decoders** y manejo de errores personalizable. ([GitHub][1])
* **Spring Cloud OpenFeign** integra OpenFeign con Spring Boot: autoconfigura el cliente, permite usar **anotaciones de Spring MVC** en la interfaz y los mismos **HttpMessageConverters** que usa Spring Web. ([Home][2])

### ¿Cuándo conviene usarlo?

* Cuando querés **llamadas REST entre microservicios** con el mínimo de “plomería” (interfaces + anotaciones).
* Cuando te sirve **configurarlo por propiedades** (timeouts, logging, retries) sin tocar mucho código. ([Home][3])

### ¿Alternativas modernas en Spring?

* **HTTP Interface / `@HttpExchange`** (Spring Framework): también declarativo, crea proxies de interfaces; puede usar RestClient o WebClient por debajo.
* **RestClient**: cliente sincrónico, fluido, sucesor de RestTemplate para casos no declarativos. ([Home][4])

---

## Dónde leer más (recomendado)

1. **Referencia oficial – Spring Cloud OpenFeign** (qué es, cómo usarlo, ejemplos). ([Home][2])
2. **Página del proyecto – Spring Cloud OpenFeign** (visión general, características). ([Home][5])
3. **Repositorio oficial – OpenFeign** (overview, filosofía, versiones). ([GitHub][1])
4. **Propiedades de configuración** (timeouts, loggerLevel, etc.). ([Home][3])
5. (Opcional) **Tutorial introductorio** para repasar conceptos con ejemplos. ([Baeldung on Kotlin][6])

Si querés, te marco secciones clave en la doc para leer primero (timeouts y manejo de errores) y cómo mapear códigos HTTP a excepciones con `ErrorDecoder`.

[1]: https://github.com/OpenFeign/feign?utm_source=chatgpt.com "OpenFeign/feign: Feign makes writing java http clients easier"
[2]: https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/?utm_source=chatgpt.com "Spring Cloud OpenFeign"
[3]: https://docs.spring.io/spring-cloud-openfeign/reference/configprops.html?utm_source=chatgpt.com "Configuration Properties :: Spring Cloud Openfeign"
[4]: https://docs.spring.io/spring-framework/reference/web/webflux-http-interface-client.html?utm_source=chatgpt.com "HTTP Interface Client :: Spring Framework"
[5]: https://spring.io/projects/spring-cloud-openfeign?utm_source=chatgpt.com "Spring Cloud OpenFeign"
[6]: https://www.baeldung.com/spring-cloud-openfeign?utm_source=chatgpt.com "Introduction to Spring Cloud OpenFeign"
```

### Modificaciones Realizadas:
No se realizaron cambios.

### Explicación del Prompt:
Entender que es Feign.

### Aprendizajes Obtenidos:
Entendí sobre Feign

---

## Prompt 2: Utilizar Feign

### Prompt Utilizado:
```

Cómo puedo implementar Feign Client para comunicación con el microservicio de datos:
   - `DataServiceClient` interface
   - Configuración de Feign
   - Manejo de errores y timeouts
```

### Respuesta Recibida:
~~~text
Acá tenés **lo justo y necesario** para la implementar: **Feign Client + configuración + manejo de errores y timeouts** en `business-service`.

---

### 1) Habilitar Feign

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/BusinessServiceApplication.java
package com.example.businessservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.businessservice.client")
public class BusinessServiceApplication {
  public static void main(String[] args) {
    SpringApplication.run(BusinessServiceApplication.class, args);
  }
}
```

---

### 2) Feign Client (interfaz)

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/client/DataServiceClient.java
package com.example.businessservice.client;

import com.example.businessservice.dto.CategoriaDTO;
import com.example.businessservice.dto.InventarioDTO;
import com.example.businessservice.dto.ProductoDTO;
import com.example.businessservice.dto.ProductoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(
    name = "data-service",
    url = "${data.service.url}",
    configuration = com.example.businessservice.config.FeignClientConfig.class
)
public interface DataServiceClient {

  @GetMapping("/data/productos")
  List<ProductoDTO> obtenerTodosLosProductos();

  @GetMapping("/data/productos/{id}")
  ProductoDTO obtenerProductoPorId(@PathVariable Long id);

  @PostMapping("/data/productos")
  ProductoDTO crearProducto(@RequestBody ProductoRequest request);

  @PutMapping("/data/productos/{id}")
  ProductoDTO actualizarProducto(@PathVariable Long id, @RequestBody ProductoRequest request);

  @DeleteMapping("/data/productos/{id}")
  void eliminarProducto(@PathVariable Long id);

  @GetMapping("/data/productos/categoria/{nombre}")
  List<ProductoDTO> obtenerProductosPorCategoria(@PathVariable String nombre);

  @GetMapping("/data/categorias")
  List<CategoriaDTO> obtenerTodasLasCategorias();

  @GetMapping("/data/inventario/stock-bajo")
  List<InventarioDTO> obtenerProductosConStockBajo();
}
```

> Si tu data-service expone `/api/...` en lugar de `/data/...`, cambiá los paths y listo.

---

### 3) Configuración de Feign (timeouts, logging, decoder)

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/config/FeignClientConfig.java
package com.example.businessservice.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
  @Bean
  public ErrorDecoder errorDecoder() {
    return new SimpleErrorDecoder();
  }
}
```

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/config/SimpleErrorDecoder.java
package com.example.businessservice.config;

import com.example.businessservice.error.MicroserviceCommunicationException;
import com.example.businessservice.error.ProductoNoEncontradoException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class SimpleErrorDecoder implements ErrorDecoder {
  private final ErrorDecoder defaultDecoder = new Default();

  @Override
  public Exception decode(String methodKey, Response response) {
    int status = response.status();
    return switch (status) {
      case 404 -> new ProductoNoEncontradoException("Recurso no encontrado en data-service");
      case 400, 409 -> new MicroserviceCommunicationException("Error del cliente al invocar data-service");
      case 502, 503, 504 -> new MicroserviceCommunicationException("data-service no disponible");
      default -> defaultDecoder.decode(methodKey, response);
    };
  }
}
```

---

### 4) Excepciones mínimas (dominio)

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/error/MicroserviceCommunicationException.java
package com.example.businessservice.error;

public class MicroserviceCommunicationException extends RuntimeException {
  public MicroserviceCommunicationException(String message) { super(message); }
  public MicroserviceCommunicationException(String message, Throwable cause) { super(message, cause); }
}
```

```java
// microservices-system/business-service/src/main/java/com/example/businessservice/error/ProductoNoEncontradoException.java
package com.example.businessservice.error;

public class ProductoNoEncontradoException extends RuntimeException {
  public ProductoNoEncontradoException(String message) { super(message); }
}
```

---

### 5) Timeouts y URL (application.yml)

```yaml
# microservices-system/business-service/src/main/resources/application.yml
server:
  port: 8082

data:
  service:
    url: http://localhost:8081  # URL base del data-service

spring:
  cloud:
    openfeign:
      client:
        config:
          default:
            connectTimeout: 2000     # ms
            readTimeout: 3000        # ms
            loggerLevel: BASIC       # NONE|BASIC|HEADERS|FULL
            decode404: false
            retryer: default         # (opcional) para desactivarlo: "never"
          data-service:
            connectTimeout: 2000
            readTimeout: 3000
            loggerLevel: BASIC
```

---

> Con esto ya cumplís la consigna:
>
> * **DataServiceClient** ✅
> * **Configuración Feign (habilitación + ErrorDecoder)** ✅
> * **Manejo de errores y timeouts (YAML + decoder)** ✅

Si ya tenés los DTOs placeholder, esto **compila y queda listo** para usar desde tus servicios de negocio. ¿Avanzamos al siguiente paso cuando quieras?

~~~

### Modificaciones Realizadas:
Implementar Feign en el microservicio de datos.

### Explicación del Prompt:
Desconocimiento del uso de Feign

### Aprendizajes Obtenidos:
Entendi como se implementa Feign
