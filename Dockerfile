FROM gradle:jdk23 as builder
WORKDIR /home/gradle/project
COPY . .
RUN gradle shadowJar

FROM amazoncorretto:23
COPY --from=builder /home/gradle/project/build/libs/bankdata-0.1-all.jar /app/application.jar
WORKDIR /app
CMD ["java", "-jar", "application.jar"]
