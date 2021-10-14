# each target (debian & alpine) receives its own default
# so building each target works without providing it
ARG DEBIAN_TOMCAT_IMAGE=tomcat:8.5-jdk8-openjdk-slim
ARG ALPINE_TOMCAT_IMAGE=tomcat:8.5.34-jre8-alpine
# build war in Docker by default but provide a way to bake in a pre-built war
# from outside of Docker. Passing build-arg WAR_SOURCE=local will use the war
# at ./docker/artifacts
ARG WAR_SOURCE=build

FROM maven:3.8.1-jdk-11-slim as build

ARG IDENTIFIER=unknown
LABEL identifier=${IDENTIFIER}

# needed to clone DHIS2 apps
RUN apt-get update && \
    apt-get install --no-install-recommends -y git

WORKDIR /src

# leverage docker cache by first getting all dependencies
COPY ./dhis-2/pom.xml ./pom.xml
COPY ./dhis-2/dhis-api/pom.xml ./dhis-api/pom.xml
COPY ./dhis-2/dhis-services/pom.xml ./dhis-services/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-acl/pom.xml ./dhis-services/dhis-service-acl/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-administration/pom.xml ./dhis-services/dhis-service-administration/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-analytics/pom.xml ./dhis-services/dhis-service-analytics/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-audit-consumer/pom.xml ./dhis-services/dhis-service-audit-consumer/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-core/pom.xml ./dhis-services/dhis-service-core/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-dxf2/pom.xml ./dhis-services/dhis-service-dxf2/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-field-filtering/pom.xml ./dhis-services/dhis-service-field-filtering/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-node/pom.xml ./dhis-services/dhis-service-node/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-program-rule/pom.xml ./dhis-services/dhis-service-program-rule/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-reporting/pom.xml ./dhis-services/dhis-service-reporting/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-schema/pom.xml ./dhis-services/dhis-service-schema/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-setting/pom.xml ./dhis-services/dhis-service-setting/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-tracker/pom.xml ./dhis-services/dhis-service-tracker/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-validation/pom.xml ./dhis-services/dhis-service-validation/pom.xml
COPY ./dhis-2/dhis-support/pom.xml ./dhis-support/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-artemis/pom.xml ./dhis-support/dhis-support-artemis/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-audit/pom.xml ./dhis-support/dhis-support-audit/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-cache-invalidation/pom.xml ./dhis-support/dhis-support-cache-invalidation/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-commons/pom.xml ./dhis-support/dhis-support-commons/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-db-migration/pom.xml ./dhis-support/dhis-support-db-migration/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-expression-parser/pom.xml ./dhis-support/dhis-support-expression-parser/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-external/pom.xml ./dhis-support/dhis-support-external/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-hibernate/pom.xml ./dhis-support/dhis-support-hibernate/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-jdbc/pom.xml ./dhis-support/dhis-support-jdbc/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-system/pom.xml ./dhis-support/dhis-support-system/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-test/pom.xml ./dhis-support/dhis-support-test/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-test-json/pom.xml ./dhis-support/dhis-support-test-json/pom.xml
COPY ./dhis-2/dhis-web-api/pom.xml ./dhis-web-api/pom.xml
# TODO is the dhis-web-api-test project needed for this docker image? If not
# ignore it in the dockerignore and remove it from here
COPY ./dhis-2/dhis-web-api-test/pom.xml ./dhis-web-api-test/pom.xml
COPY ./dhis-2/dhis-web-embedded-jetty/pom.xml ./dhis-web-embedded-jetty/pom.xml

FROM build as another 

# does it make sense to fetch all dependencies of the modules? Will it resolve
# the dependencies of their submodule?
RUN mvn dependency:go-offline --batch-mode -pl dhis-api
# does not seem to have an effect on it not being found by the subsequent
# install
RUN mvn --offline install -DskipTests -pl dhis-api

# I cannot do the root pom go-offline since dhis-support-commons needs the org.hisp.dhis:dhis-api:jar
# RUN mvn dependency:go-offline --batch-mode

RUN mvn dependency:go-offline --batch-mode -pl dhis-services
RUN mvn dependency:go-offline --batch-mode -pl dhis-support
RUN mvn dependency:go-offline --batch-mode -pl dhis-web-api
# this might not be needed for this image since its related to tests?
RUN mvn dependency:go-offline --batch-mode -pl dhis-web-api-test
# would also be interested if the embedded jetty is needed
RUN mvn dependency:go-offline --batch-mode -pl dhis-web-embedded-jetty

# TODO this one is failing
# RUN mvn dependency:go-offline --batch-mode -f dhis-support/pom.xml -pl dhis-support-commons
RUN mvn install -DskipTests -f dhis-support/pom.xml -pl dhis-support-commons
# dhis-support-commons needs
# the dhis-api, so we could only run mvn dependency:go-offline
# with --non-recursive but that does not get all dependencies as the mvn
# install later on still downloads tons of dependencies
# do we need to handle these interdependencies differently? like moving them
# after the mvn dependency:go-offline or is there another way? Like first
# installing the dhis-api?
# NB: web-apps build uses `git rev-parse` to tag the build, so just copy over the whole tree for now
COPY dhis-2 .

# TODO: We should be able to achieve much faster incremental builds and cached dependencies using
RUN mvn --offline clean install -Pdev -Pjdk11 -f pom.xml -DskipTests -pl -dhis-web-embedded-jetty
RUN mvn --offline clean install -Pdev -Pjdk11 -U -f dhis-web/pom.xml -DskipTests

RUN cp dhis-web/dhis-web-portal/target/dhis.war /dhis.war && \
    cd / && \
    sha256sum dhis.war > /sha256sum.txt && \
    md5sum dhis.war > /md5sum.txt


FROM alpine:latest as local

COPY ./docker/artifacts/dhis.war /dhis.war
COPY ./docker/artifacts/sha256sum.txt /sha256sum.txt
COPY ./docker/artifacts/md5sum.txt /md5sum.txt

FROM $WAR_SOURCE as war


FROM alpine:latest as base

COPY --from=war /dhis.war /srv/dhis2/dhis.war
COPY --from=war /sha256sum.txt /srv/dhis2/sha256sum.txt
COPY --from=war /md5sum.txt /srv/dhis2/md5sum.txt

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

COPY ./docker/docker-image-resources/wait-for-it.sh /usr/local/bin/
COPY ./docker/docker-image-resources/debian-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +rx /usr/local/bin/docker-entrypoint.sh && \
    chmod +rx /usr/local/bin/wait-for-it.sh

COPY ./docker/docker-image-resources/server.xml /usr/local/tomcat/conf
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

COPY ./docker/docker-image-resources/wait-for-it.sh /usr/local/bin/
COPY ./docker/docker-image-resources/alpine-entrypoint.sh /usr/local/bin/docker-entrypoint.sh

RUN chmod +rx /usr/local/bin/docker-entrypoint.sh && \
    chmod +rx /usr/local/bin/wait-for-it.sh

COPY ./docker/docker-image-resources/server.xml /usr/local/tomcat/conf
COPY --from=war /dhis.war /usr/local/tomcat/webapps/ROOT.war

ENTRYPOINT ["/usr/local/bin/docker-entrypoint.sh"]

CMD ["catalina.sh", "run"]

# This ensures we are building debian by default
FROM debian
