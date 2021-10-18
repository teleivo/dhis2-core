# each target (debian & alpine) receives its own default
# so building each target works without providing it
ARG DEBIAN_TOMCAT_IMAGE=tomcat:8.5-jdk8-openjdk-slim
ARG ALPINE_TOMCAT_IMAGE=tomcat:8.5.34-jre8-alpine
# build war in Docker by default but provide a way to bake in a pre-built war
# from outside of Docker. Passing build-arg WAR_SOURCE=local will use the war
# at ./docker/artifacts
ARG WAR_SOURCE=build

FROM maven:3.6.3-jdk-8-slim as build

ARG IDENTIFIER=unknown
LABEL identifier=${IDENTIFIER}

# needed to clone DHIS2 apps
RUN apt-get update && \
    apt-get install --no-install-recommends -y git

WORKDIR /src

# NB: web-apps build uses `git rev-parse` to tag the build, so just copy over the whole tree for now
COPY dhis-2 .

# TODO: We should be able to achieve much faster incremental builds and cached dependencies using
RUN mvn clean install -f pom.xml -Dmaven.test.skip=true --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
RUN mvn clean install -U -f dhis-web/pom.xml -Dmaven.test.skip=true --batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn

RUN cp dhis-web/dhis-web-portal/target/dhis.war /dhis.war

FROM alpine:latest as local

COPY ./docker/artifacts/dhis.war /dhis.war

FROM $WAR_SOURCE as war

FROM alpine:latest as base

COPY --from=war /dhis.war /srv/dhis2/dhis.war

FROM $DEBIAN_TOMCAT_IMAGE as debian

ARG IDENTIFIER=unknown
LABEL identifier=${IDENTIFIER}

ENV WAIT_FOR_DB_CONTAINER=""

ENV DHIS2_HOME=/DHIS2_home

RUN rm -rf /usr/local/tomcat/webapps/* && \
    mkdir /usr/local/tomcat/webapps/ROOT && \
    mkdir $DHIS2_HOME && \
    adduser --system --disabled-password --group tomcat && \
    echo 'tomcat' >> /etc/cron.deny && \
    echo 'tomcat' >> /etc/at.deny

RUN apt-get update && \
    apt-get install --no-install-recommends -y \
        util-linux \
        bash \
        unzip \
        fontconfig

COPY ./docker/shared/wait-for-it.sh /usr/local/bin/
COPY ./docker/tomcat-debian/docker-entrypoint.sh /usr/local/bin/

RUN chmod +rx /usr/local/bin/docker-entrypoint.sh && \
    chmod +rx /usr/local/bin/wait-for-it.sh

COPY ./docker/shared/server.xml /usr/local/tomcat/conf
COPY --from=war /dhis.war /usr/local/tomcat/webapps/ROOT.war

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD ["catalina.sh", "run"]


FROM $ALPINE_TOMCAT_IMAGE as alpine

ARG IDENTIFIER=unknown
LABEL identifier=${IDENTIFIER}

ENV WAIT_FOR_DB_CONTAINER=""

ENV DHIS2_HOME=/DHIS2_home

RUN rm -rf /usr/local/tomcat/webapps/* && \
    mkdir /usr/local/tomcat/webapps/ROOT && \
    mkdir $DHIS2_HOME && \
    addgroup -S tomcat && \
    addgroup root tomcat && \
    adduser -S -D -G tomcat tomcat && \
    echo 'tomcat' >> /etc/cron.deny && \
    echo 'tomcat' >> /etc/at.deny

RUN apk add --update --no-cache \
        bash  \
        su-exec \
        fontconfig \
        ttf-dejavu

COPY ./docker/shared/wait-for-it.sh /usr/local/bin/
COPY ./docker/tomcat-alpine/docker-entrypoint.sh /usr/local/bin/

RUN chmod +rx /usr/local/bin/docker-entrypoint.sh && \
    chmod +rx /usr/local/bin/wait-for-it.sh

COPY ./docker/shared/server.xml /usr/local/tomcat/conf
COPY --from=war /dhis.war /usr/local/tomcat/webapps/ROOT.war

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD ["catalina.sh", "run"]

# This ensures we are building debian by default
FROM debian
