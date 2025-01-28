FROM maven:3.8.5-openjdk-17

RUN mkdir job4j_mocksrv

WORKDIR job4j_mocksrv

COPY . .

RUN mvn package -Dmaven.test.skip=true

CMD ["mvn", "liquibase:update", "-Pdocker"]

CMD ["java", "-jar", "target/mocksrv.jar"]