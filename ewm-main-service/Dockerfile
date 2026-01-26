FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY . ../
RUN mvn -q -f ../pom.xml -pl ewm-main-service -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/../ewm-main-service/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]


