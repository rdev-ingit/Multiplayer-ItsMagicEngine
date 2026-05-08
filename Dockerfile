FROM eclipse-temurin:17-jdk-alpine

WORKDIR /app

COPY GameServer.java .

RUN javac GameServer.java

EXPOSE 5050

CMD ["java", "GameServer"]
