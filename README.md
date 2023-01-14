# Fullstack Book Review App

[![](https://img.shields.io/badge/Spring%20Boot%20Version-2.7.7-orange)](/pom.xml)
[![](https://img.shields.io/badge/Java%20Version-17-orange)](/pom.xml)
[![](https://img.shields.io/badge/Testcontainers%20Version-1.17.6-orange)](https://www.testcontainers.org/)
[![](https://img.shields.io/badge/LocalStack%20Version-1.3.1-orange)](https://localstack.cloud/)
[![](https://img.shields.io/badge/Keycloak%20Version-20.0.3-orange)](https://www.keycloak.org/)
[![](https://img.shields.io/badge/PostgreSQL%20Version-15.1-orange)](https://www.postgresql.org/)


## Introduction
Developed a fully tested book review application where users can add new books and write or see reviews posted by
different users. The aim of the project is to master testing techniques for effectively carrying out unit,
integration, and end-to-end testing following a test driven development (TDD) approach.

The project gave me the opportunity to explore testing recipes for the different layers of an application such as
database, messaging, and HTTP communication. Furthermore, I got to work with the different testing slices provided by Spring
and create a custom application context to carry out integration tests with the help of LocalStack. Finally, Testcontainers
formed a critical part of this project because it allowed me to spawn up the necessary infrastructure needed like database,
and AWS SQS support to perform integration tests.

## Application Architecture
- Keycloack (Open source identity and access management solution) to secure parts of the frontend and backend.
- PostgreSQL (RDBMS) to store data in a relational database.
- Amazon SQS (Simple Queuing Service) for asynchronous message processing.
- Spring Boot backend with Java
- Dependency on a remote REST API for fetching book information.
- Single page application frontend with React and Typescript

<p align="center">
    <img src="assets/images/book-review-app-diagram.png" alt="Book Review Application Architecture">
</p>

## Local Project Setup

### Mandatory Requirements
* Java 17
```
❯ java --version
openjdk 17.0.5 2022-10-18
OpenJDK Runtime Environment Temurin-17.0.5+8 (build 17.0.5+8)
OpenJDK 64-Bit Server VM Temurin-17.0.5+8 (build 17.0.5+8, mixed mode)
```
* Docker Engine and Docker Compose
```
❯ docker version
Client:
 Cloud integration: v1.0.29
 Version:           20.10.21
 API version:       1.41
 Go version:        go1.18.7
 Git commit:        baeda1f
 Built:             Tue Oct 25 18:01:18 2022
 OS/Arch:           darwin/arm64
 Context:           default
 Experimental:      true

Server: Docker Desktop 4.15.0 (93002)
 Engine:
  Version:          20.10.21
  API version:      1.41 (minimum version 1.12)
  Go version:       go1.18.7
  Git commit:       3056208
  Built:            Tue Oct 25 17:59:41 2022
  OS/Arch:          linux/arm64
  Experimental:     false
 containerd:
  Version:          1.6.10
  GitCommit:        770bd0108c32f3fb5c73ae1264f7e503fe7b2661
 runc:
  Version:          1.1.4
  GitCommit:        v1.1.4-0-g5fd4c4d
 docker-init:
  Version:          0.19.0
  GitCommit:        de40ad0
```

### Optional Requirements:
* Maven >= 3.8 (the project includes the Maven Wrapper to run the project)
```
❯ mvn --version
Apache Maven 3.8.7 (b89d5959fcde851dcb1c8946a785a163f14e1e29)
Maven home: /opt/homebrew/Cellar/maven/3.8.7/libexec
Java version: 17.0.5, vendor: Eclipse Adoptium, runtime: /Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home
Default locale: en_US, platform encoding: UTF-8
OS name: "mac os x", version: "13.1", arch: "aarch64", family: "mac"
```


### Running the Project Locally
1. Make sure Docker is running
2. Start required infrastructure to run application with`docker-compose up`
3. Run the application with `./mvnw spring-boot:run`
4. Access http://localhost:8080 for the application frontend
5. (Optional) access http://localhost:8888/auth for the Keycloak admin interface

Default Application Users
* duke (password `dukeduke`)
* mike (password `mikemike`)