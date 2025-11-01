FROM openjdk:21

WORKDIR /usrapp/bin

ENV PORT=33025

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["java", "-cp", "./classes:./dependency/*", "edu.aygo.taller3aygo.Taller3AygoApplication"]
