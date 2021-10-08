#
# Build the base DHIS2 image
#

FROM maven:3.8.1-jdk-11-slim as build

ARG IDENTIFIER=unknown
LABEL identifier=${IDENTIFIER}

WORKDIR /src

# TODO remove duplicates :joy: I think I have too many poms :)
# leverage docker cache by first getting all dependencies
COPY ./dhis-2/pom.xml ./pom.xml
COPY ./dhis-2/dhis-api/pom.xml ./dhis-api/pom.xml
COPY ./dhis-2/dhis-web-api/pom.xml ./dhis-web-api/pom.xml
COPY ./dhis-2/dhis-web-api-test/pom.xml ./dhis-web-api-test/pom.xml
COPY ./dhis-2/dhis-web-embedded-jetty/pom.xml ./dhis-web-embedded-jetty/pom.xml

COPY ./dhis-2/dhis-services/pom.xml ./dhis-services/pom.xml
COPY ./dhis-2/dhis-services/dhis-service-schema           ./dhis-services/dhis-service-schema
COPY ./dhis-2/dhis-services/dhis-service-setting          ./dhis-services/dhis-service-setting
COPY ./dhis-2/dhis-services/dhis-service-acl              ./dhis-services/dhis-service-acl
COPY ./dhis-2/dhis-services/dhis-service-audit-consumer   ./dhis-services/dhis-service-audit-consumer
COPY ./dhis-2/dhis-services/dhis-service-node             ./dhis-services/dhis-service-node
COPY ./dhis-2/dhis-services/dhis-service-field-filtering  ./dhis-services/dhis-service-field-filtering
COPY ./dhis-2/dhis-services/dhis-service-core             ./dhis-services/dhis-service-core
COPY ./dhis-2/dhis-services/dhis-service-validation       ./dhis-services/dhis-service-validation
COPY ./dhis-2/dhis-services/dhis-service-program-rule     ./dhis-services/dhis-service-program-rule
COPY ./dhis-2/dhis-services/dhis-service-administration   ./dhis-services/dhis-service-administration
COPY ./dhis-2/dhis-services/dhis-service-dxf2             ./dhis-services/dhis-service-dxf2
COPY ./dhis-2/dhis-services/dhis-service-analytics        ./dhis-services/dhis-service-analytics
COPY ./dhis-2/dhis-services/dhis-service-tracker          ./dhis-services/dhis-service-tracker
COPY ./dhis-2/dhis-services/dhis-service-reporting        ./dhis-services/dhis-service-reporting
COPY ./dhis-2/dhis-services/dhis-service-schema           ./dhis-services/dhis-service-schema
COPY ./dhis-2/dhis-services/dhis-service-setting          ./dhis-services/dhis-service-setting
COPY ./dhis-2/dhis-services/dhis-service-acl              ./dhis-services/dhis-service-acl
COPY ./dhis-2/dhis-services/dhis-service-audit-consumer   ./dhis-services/dhis-service-audit-consumer
COPY ./dhis-2/dhis-services/dhis-service-node             ./dhis-services/dhis-service-node
COPY ./dhis-2/dhis-services/dhis-service-field-filtering  ./dhis-services/dhis-service-field-filtering
COPY ./dhis-2/dhis-services/dhis-service-core             ./dhis-services/dhis-service-core
COPY ./dhis-2/dhis-services/dhis-service-validation       ./dhis-services/dhis-service-validation
COPY ./dhis-2/dhis-services/dhis-service-program-rule     ./dhis-services/dhis-service-program-rule
COPY ./dhis-2/dhis-services/dhis-service-administration   ./dhis-services/dhis-service-administration
COPY ./dhis-2/dhis-services/dhis-service-dxf2             ./dhis-services/dhis-service-dxf2
COPY ./dhis-2/dhis-services/dhis-service-analytics        ./dhis-services/dhis-service-analytics
COPY ./dhis-2/dhis-services/dhis-service-tracker          ./dhis-services/dhis-service-tracker
COPY ./dhis-2/dhis-services/dhis-service-reporting        ./dhis-services/dhis-service-reporting

COPY ./dhis-2/dhis-support/pom.xml ./dhis-support/pom.xml
COPY ./dhis-2/dhis-support/dhis-support-test-json         ./dhis-support/dhis-support-test-json
COPY ./dhis-2/dhis-support/dhis-support-commons           ./dhis-support/dhis-support-commons
COPY ./dhis-2/dhis-support/dhis-support-db-migration      ./dhis-support/dhis-support-db-migration
COPY ./dhis-2/dhis-support/dhis-support-test              ./dhis-support/dhis-support-test
COPY ./dhis-2/dhis-support/dhis-support-external          ./dhis-support/dhis-support-external
COPY ./dhis-2/dhis-support/dhis-support-hibernate         ./dhis-support/dhis-support-hibernate
COPY ./dhis-2/dhis-support/dhis-support-audit             ./dhis-support/dhis-support-audit
COPY ./dhis-2/dhis-support/dhis-support-system            ./dhis-support/dhis-support-system
COPY ./dhis-2/dhis-support/dhis-support-jdbc              ./dhis-support/dhis-support-jdbc
COPY ./dhis-2/dhis-support/dhis-support-expression-parser ./dhis-support/dhis-support-expression-parser
COPY ./dhis-2/dhis-support/dhis-support-artemis           ./dhis-support/dhis-support-artemis
COPY ./dhis-2/dhis-support/dhis-support-cache-invalidation ./dhis-support/dhis-support-cache-invalidation
COPY ./dhis-2/dhis-support/dhis-support-test-json         ./dhis-support/dhis-support-test-json
COPY ./dhis-2/dhis-support/dhis-support-commons           ./dhis-support/dhis-support-commons
COPY ./dhis-2/dhis-support/dhis-support-db-migration      ./dhis-support/dhis-support-db-migration
COPY ./dhis-2/dhis-support/dhis-support-test              ./dhis-support/dhis-support-test
COPY ./dhis-2/dhis-support/dhis-support-external          ./dhis-support/dhis-support-external
COPY ./dhis-2/dhis-support/dhis-support-hibernate         ./dhis-support/dhis-support-hibernate
COPY ./dhis-2/dhis-support/dhis-support-audit             ./dhis-support/dhis-support-audit
COPY ./dhis-2/dhis-support/dhis-support-system            ./dhis-support/dhis-support-system
COPY ./dhis-2/dhis-support/dhis-support-jdbc              ./dhis-support/dhis-support-jdbc
COPY ./dhis-2/dhis-support/dhis-support-expression-parser ./dhis-support/dhis-support-expression-parser
COPY ./dhis-2/dhis-support/dhis-support-artemis           ./dhis-support/dhis-support-artemis

# TODO clean up comments
# --non-recursive solves the following issue
# [ERROR] Failed to execute goal on project dhis-support-commons: Could not resolve dependencies for project org.hisp.dhis:dhis-support-commons:jar:2.38-SNAPSHOT: Could not find artifact org.hisp.dhis:dhis-api:jar:2.38-SNAPSHOT in ossrh (https://oss.sonatype.org/content/repositories/snapshots) -> [Help 1]
RUN mvn dependency:go-offline --batch-mode --non-recursive

# TODO for whatever reason when building below it will still download jars. do
# I have an issue with paths. I did not take care ;)
# combine with dockerignore
# TODO can we narrow down what we copy?
# NB: web-apps build uses `git rev-parse` to tag the build, so just copy over the whole tree for now
COPY . .

# TODO: We should be able to achieve much faster incremental builds and cached dependencies using
RUN mvn clean install -Pdev -Pjdk11 -f dhis-2/pom.xml -DskipTests -pl -dhis-web-embedded-jetty
RUN mvn clean install -Pdev -Pjdk11 -U -f dhis-2/dhis-web/pom.xml -DskipTests

RUN cp dhis-2/dhis-web/dhis-web-portal/target/dhis.war /dhis.war && \
    cd / && \
    sha256sum dhis.war > /sha256sum.txt && \
    md5sum dhis.war > /md5sum.txt

#
# Slim final image that has the build artifacts at /srv/dhis2
#

FROM alpine:latest
COPY --from=build /dhis.war /srv/dhis2/dhis.war
COPY --from=build /sha256sum.txt /srv/dhis2/sha256sum.txt
COPY --from=build /md5sum.txt /srv/dhis2/md5sum.txt
