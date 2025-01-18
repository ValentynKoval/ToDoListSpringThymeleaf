# Устанавливаем базовый образ Java
FROM eclipse-temurin:17-jdk-jammy

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем файл JAR
COPY target/*.jar app.jar

# Устанавливаем команду для запуска приложения
ENTRYPOINT ["java", "-jar", "app.jar"]
