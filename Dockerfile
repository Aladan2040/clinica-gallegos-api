# --- ETAPA 1: CONSTRUCCIÓN (BUILD) ---
# Usamos Maven con Java 21 para compilar
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiamos el archivo de configuración y descargamos dependencias
COPY pom.xml .
RUN mvn dependency:go-offline

# Copiamos el código fuente y compilamos (saltando tests para agilizar)
COPY src ./src
RUN mvn clean package -DskipTests

# --- ETAPA 2: EJECUCIÓN (RUNTIME) ---
# Usamos una imagen ligera de Java 21 (JRE) para correr la app
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copiamos el .jar generado en la etapa anterior
COPY --from=build /app/target/*.jar app.jar

# Exponemos el puerto 8080
EXPOSE 8080

# Comando de arranque
ENTRYPOINT ["java", "-jar", "app.jar"]