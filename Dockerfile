FROM ubuntu:22.04

ENV JAVA_HOME /opt/jdk
ENV PATH $JAVA_HOME/bin:$PATH

RUN apt-get update -y

ADD "https://github.com/CRaC/openjdk-builds/releases/download/17-crac%2B2/jdk17-crac+2.tar.gz" $JAVA_HOME/openjdk.tar.gz
RUN tar --extract --file $JAVA_HOME/openjdk.tar.gz --directory "$JAVA_HOME" --strip-components 1; rm $JAVA_HOME/openjdk.tar.gz;

COPY build/libs/crac4-17.0.0.jar /opt/app/crac4-17.0.0.jar
CMD ["java", "-XX:CRaCCheckpointTo=/opt/crac-files/", "-jar", "/opt/app/crac4-17.0.0.jar"]
