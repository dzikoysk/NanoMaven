---
id: docker
title: Docker
sidebar_label: Docker
---

Reposilite defines two types of builds:
* tag-based - build is triggered per a new release, recommended for production environments
* nightly - build is triggered for each commit on master branch, it might be useful for testers and developers

## Installation

First of all, you have to pull the image from [DockerHub](https://hub.docker.com/r/dzikoysk/reposilite):

```shell-session
// released builds, e.g. 3.0.0-SNAPSHOT
$ docker pull dzikoysk/reposilite:3.0.0-SNAPSHOT

// nightly builds
$ docker pull dzikoysk/reposilite:nightly
```

Then, 
just run the image in interactive mode 
*(to support [interactive CLI](install#interactive-cli))*:

```console
$ docker run -it -v reposilite-data:/app/data -p 80:80 dzikoysk/reposilite:nightly
```

### Data persistence
Reposilite stores data in `/app/data` directory. 
Since 2.7.0, 
Reposilite marks this path as externally mounted volume by default.
To use named volume which can be reused by Docker,
run docker with `-v` parameter:

```console
$ docker run -it -v reposilite-data:/app/data -p 80:80 dzikoysk/reposilite
```

### Custom properties

You can also pass custom configuration values using the environment variables:

```shell-session
$ docker run -e JAVA_OPTS='-Xmx128M -Dreposilite.port=8080' -p 8080:80 dzikoysk/reposilite
```

You can find list of configuration properties in [configuration](configuration) chapter.

### Custom parameters
To pass custom parameters described in [installation#properties](install#properties), use `REPOSILITE_OPTS` variable:

```shell-session
$ docker run -e REPOSILITE_OPTS='--config=/app/data/custom.cdn' -p 80:80 dzikoysk/reposilite
```

**Note:** Default Dockerfile changes Reposilite working directory to `/app/data/` and marks it as volume.
It is not recommended to modify this property.

### External configuration
You can mount external configuration file using the `--mount` and `--config` parameter.
Before that, you have to make sure the the configuration file already exists on Docker host. 
To launch Reposilite with a custom configuration, we have to mount proper file:

```console
$ docker run -it \
  --mount type=bind,source=/etc/reposilite/reposilite.cdn,target=/app/reposilite-host.cdn \
  -e REPOSILITE_OPTS='--config=/app/reposilite-host.cdn' \
  -v reposilite-data:/app/data
  -p 80:80
  dzikoysk/reposilite
```

As you can see, it is not best solution and as long as possible, 
you should override default configuration values using the [custom properties](#custom-properties).

### ARM-based systems
To run Reposilite under docker on an ARM-based system, you'll have to build the image yourself, since the current pipeline doesn't support building multi-arch images, only building for X86 at the moment.

The following Dockerfile should work:

```
# Build stage
FROM maven:3.6.3-openjdk-15-slim AS build
COPY ./ /app/
RUN mvn -f /app/pom.xml clean package

# Build-time metadata stage
ARG BUILD_DATE
ARG VCS_REF
ARG VERSION
LABEL org.label-schema.build-date=$BUILD_DATE \
      org.label-schema.name="Reposilite" \
      org.label-schema.description="Lightweight repository management software dedicated for the Maven artifacts" \
      org.label-schema.url="https://reposilite.com/" \
      org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vcs-url="https://github.com/dzikoysk/reposilite" \
      org.label-schema.vendor="dzikoysk" \
      org.label-schema.version=$VERSION \
      org.label-schema.schema-version="1.0"

# Run stage
FROM adoptopenjdk:15.0.2_7-jre-hotspot
WORKDIR /app
RUN rm -rf /var/lib/apt/lists/* && mkdir -p /app/data
VOLUME /app/data
COPY --from=build /app/reposilite-backend/target/reposilite*.jar reposilite.jar
ENTRYPOINT exec java $JAVA_OPTS -jar reposilite.jar -wd=/app/data $REPOSILITE_OPTS
```

Save the file locally as `Dockerfile` and build it:

```console
$ docker build . -t reposilite-arm
```

To run, just follow the instructions in this page, replacing `dzikoysk/reposilite` by the tag that you have chosen to build the image (`reposilite-arm` in the example).
