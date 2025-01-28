FROM maven:3.8.5-openjdk-17

RUN mkdir job4j_descsrv

WORKDIR job4j_descsrv

COPY . .

RUN mvn package -Dmaven.test.skip=true

CMD ["mvn", "liquibase:update", "-Pdocker"]

CMD ["java", "-jar", "target/descsrv.jar"]