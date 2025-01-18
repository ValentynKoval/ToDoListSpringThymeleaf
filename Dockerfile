# Укажите базовый образ
FROM openjdk:17-jdk-slim

# Установите рабочую директорию
WORKDIR /app

# Скопируйте собранный JAR-файл в контейнер
COPY target/*.jar app.jar

# Укажите команду запуска
CMD ["java", "-jar", "app.jar"]
