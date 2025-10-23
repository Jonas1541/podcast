# ---- Estágio de Build ----
FROM gradle:8.5.0-jdk21 AS build
WORKDIR /app

# Copia primeiro apenas arquivos de configuração (melhora cache do Docker)
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew

# Baixa dependências antes do código (melhora cache de build)
RUN ./gradlew dependencies --no-daemon || true

# Agora copia o restante do código-fonte
COPY src ./src

# Builda o jar sem rodar testes (mais rápido e adequado pra produção)
RUN ./gradlew bootJar -x test --no-daemon


# ---- Estágio de Execução ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar do estágio anterior
COPY --from=build /app/build/libs/*.jar app.jar

# Expõe a porta padrão
EXPOSE 8080

# Define perfil padrão (pode ser sobrescrito no Render)
ENV SPRING_PROFILES_ACTIVE=prod

# Inicia o app
ENTRYPOINT ["java", "-jar", "app.jar"]
