# Укажите базовый образ
FROM openjdk:17-jdk-slim

# Установите рабочую директорию
WORKDIR /app

# Скопируйте собранный JAR-файл в контейнер
COPY target/ToDoList-0.0.1-SNAPSHOT.jar app.jar

# Укажите команду запуска
CMD ["java", "-jar", "app.jar"]
