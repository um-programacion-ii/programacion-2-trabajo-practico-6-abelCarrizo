# Prompts de Microservicios

## Prompt 1: Estructura del proyecto

### Prompt Utilizado:
```
Puedo crear una estructura de microservicios en donde tenga un archivo de configuracion global de pom?
Debes tomar en cuenta la estructura del proyecto que te compartiré a continuacion y las tecnologias asociadas
a cada microservicio.

1. Crear la estructura de proyectos:

microservices-system/
├── data-service/          # Microservicio de datos
├── business-service/      # Microservicio de negocio
├── docker-compose.yml     # Configuración de bases de datos
└── README.md


2. Configurar el microservicio de datos (`data-service`):
   - `spring-boot-starter-data-jpa`
   - `spring-boot-starter-web`
   - `spring-boot-starter-test`
   - `h2` (para desarrollo)
   - `mysql-connector-java`
   - `postgresql`

3. Configurar el microservicio de negocio (`business-service`):
   - `spring-boot-starter-web`
   - `spring-cloud-starter-openfeign`
   - `spring-boot-starter-test`
   - `spring-cloud-dependencies`
```

### Respuesta Recibida:
~~~text
¡Muy buena observación! 👌

Sí, **podés tener un proyecto padre (multi-module)** como el que escribiste, y es una práctica bastante común en entornos de microservicios cuando querés:

* **Centralizar versiones** de Spring Boot, Spring Cloud, JUnit, Lombok, etc.
* **Compartir dependencias comunes** (ej: Lombok, utilidades internas).
* Compilar todos los microservicios juntos con un simple `mvn clean install`.

---

## ✅ Cómo funcionaría tu setup

Tu `microservices-system/` tendría el `pom.xml` **padre** (packaging `pom`) y dos módulos:

```
microservices-system/
├── pom.xml                # POM padre (el que pusiste)
├── data-service/          # Módulo hijo
│   └── pom.xml
├── business-service/      # Módulo hijo
│   └── pom.xml
└── docker-compose.yml
```

---

## 📄 Ejemplo de POM Padre (tu propuesta, ajustado)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.mycompany.app</groupId>
  <artifactId>microservices-system</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>pom</packaging>

  <name>microservices-system</name>

  <modules>
    <module>business-service</module>
    <module>data-service</module>
  </modules>

  <properties>
    <java.version>21</java.version>
    <spring.boot.version>3.5.0</spring.boot.version>
    <spring.cloud.version>2025.0.0</spring.cloud.version>
  </properties>

  <dependencyManagement>
    <dependencies>
      <!-- Spring Boot BOM -->
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-dependencies</artifactId>
        <version>${spring.boot.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>

      <!-- Spring Cloud BOM -->
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring.cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
    </dependencies>
  </dependencyManagement>

  <dependencies>
    <!-- Dependencias globales que querés para todos los módulos -->
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <optional>true</optional>
    </dependency>
  </dependencies>
</project>
```

---

## 📄 Ejemplo de POM hijo (data-service)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.mycompany.app</groupId>
    <artifactId>microservices-system</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <artifactId>data-service</artifactId>
  <name>data-service</name>
  <packaging>jar</packaging>

  <dependencies>
    <!-- Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Bases de datos -->
    <dependency>
      <groupId>com.h2database</groupId>
      <artifactId>h2</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <scope>runtime</scope>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## 📄 Ejemplo de POM hijo (business-service)

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>com.mycompany.app</groupId>
    <artifactId>microservices-system</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <artifactId>business-service</artifactId>
  <name>business-service</name>
  <packaging>jar</packaging>

  <dependencies>
    <!-- Spring Boot -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- OpenFeign -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- Test -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

---

## 🚀 Conclusión

* ✔️ Sí, podés usar un **POM padre multi-módulo**.
* ✔️ Los hijos heredan **versiones y dependencias gestionadas**, evitando duplicación.
* ✔️ Podés seguir generando cada microservicio con **Spring Initializr**, pero ajustando el `pom.xml` para que use tu **padre** en vez del `spring-boot-starter-parent`.

👉 La diferencia es que ahora **tu proyecto padre importa el BOM de Spring Boot/Cloud**, y cada microservicio hijo solo declara las dependencias.

---

¿Querés que te arme ya el **comando de Spring Initializr con `curl`/`wget`** para generar cada microservicio (`data-service` y `business-service`) apuntando a tu `pom` padre, listo para usar?

~~~

### Modificaciones Realizadas:
Se creó un pom.xml en la raiz del proyecto.

### Explicación del Prompt:
Verificar si se podía realizar una configuracion global que llegue a muchos microservicios.

### Aprendizajes Obtenidos:
Entendí que se puede realizar una configuracion global para un proyecto de microservicios.

---

## Prompt 2: Configuracion pom.xml

### Prompt Utilizado:
```
Enseñame como adaptar los pom.xml que se encuentran en los microservicios generados con Spring Initializr 
```

### Respuesta Recibida:
~~~text
Perfecto 🚀, lo que querés hacer es reemplazar el `pom.xml` que te generó **Spring Initializr** por uno que herede de tu **pom padre (`microservices-system`)**, en vez del `spring-boot-starter-parent`.

Vamos paso a paso:

---

### 🔴 Problema del archivo generado por Spring Initializr

Spring Initializr siempre mete este bloque como padre:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.6</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>
```

Eso **entra en conflicto** con tu `pom.xml` padre porque vos ya definiste la gestión de dependencias en `microservices-system`.

---

### ✅ Solución

El `pom.xml` de tu **hijo `data-service`** debería quedar así:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>

    <!-- Hereda del POM padre -->
    <parent>
        <groupId>com.mycompany.app</groupId>
        <artifactId>microservices-system</artifactId>
        <version>1.0-SNAPSHOT</version>
    </parent>

    <artifactId>data-service</artifactId>
    <name>data-service</name>
    <description>Microservicio de datos</description>

    <dependencies>
        <!-- Spring Boot -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Bases de datos -->
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <!-- Spring Boot Plugin -->
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>

            <!-- Maven Compiler Plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
        </plugins>
    </build>

</project>
```

El mismo proceso se repite con el  **hijo `business-service`** debería quedar así:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <!-- Hereda del POM padre -->
  <parent>
    <groupId>com.mycompany.app</groupId>
    <artifactId>microservices-system</artifactId>
    <version>1.0-SNAPSHOT</version>
  </parent>

  <artifactId>business-service</artifactId>
  <name>business-service</name>
  <description>Microservicio de negocio</description>

  <dependencies>
    <!-- Spring Boot Web -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- OpenFeign (Spring Cloud) -->
    <dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <!-- Spring Boot Plugin -->
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>

      <!-- Compiler Plugin -->
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <configuration>
          <source>${java.version}</source>
          <target>${java.version}</target>
        </configuration>
      </plugin>
    </plugins>
  </build>

</project>
```
---

### 📌 Notas importantes

1. **Eliminamos**:

    * `<groupId>` y `<version>` en el hijo → ya los hereda del padre.
    * El `spring-boot-starter-parent`.

2. **No es necesario fijar versión de plugins ni dependencias**
   porque eso ya lo gestiona tu **padre** con:

   ```xml
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-dependencies</artifactId>
               <version>${spring.boot.version}</version>
               <type>pom</type>
               <scope>import</scope>
           </dependency>
   ```

3. En el plugin `maven-compiler-plugin` usé:

   ```xml
   <source>${java.version}</source>
   <target>${java.version}</target>
   ```

   porque ya definiste `<java.version>21</java.version>` en el padre.

---

👉 Con esto, cuando corras en la raíz:

```bash
mvn clean install
```

Se van a compilar **padre + business-service + data-service** correctamente.

---

¿Querés que te arme también el `pom.xml` base de **business-service** siguiendo el mismo esquema, así ya tenés el paquete completo armado?

~~~

### Modificaciones Realizadas:
Se creo un pom.xml padre, luego se crearon los microservicios con Spring Intializr y se adaptaron los pom.xml del proyecto segun la recomendacion de chat gpt.

### Explicación del Prompt:
Verificar si se podía realizar una configuracion global que llegue a muchos microservicios.

### Aprendizajes Obtenidos:
Aprendí a crear una configuracion global en un proyecto de microservicios.
