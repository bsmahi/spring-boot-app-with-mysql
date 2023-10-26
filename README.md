# Spring Boot Rest API with MySql

We will build a Spring Boot JPA Rest CRUD API for a Course application in that:

- Each Course has id, title, description, published status
- All the Api's help to perform create, retrieve, update, delete Courses
- We have also defined custom query method to retrieve details based on the title and published

## Technologies Used

- Java 17
- Spring Boot 3.1.x
- Spring Modules Covered: Spring Boot Web, Spring Data Jpa, Spring Actuator, OpenAPI
- Database: MySQL
- Build Tool: Maven

We will develop a Course application by creating a Spring Boot JPA Rest CRUD API.

- Each Course has id, title, description, published status.
- Apis help to create, retrieve, update, delete Tutorials.
- Apis also support custom finder methods such as find by published status or by title.

These are APIs that we need to provide:

| HTTP Method |                    Urls                    |                    Description                    |
|:-----------:|:------------------------------------------:|:-------------------------------------------------:|
|    POST     |                /api/courses                |                 Create New Course                 |
|     GET     |                /api/courses                |                Get All the Courses                |
|     GET     |              /api/courses/:id              |             Retrieve a Course by :id              |
|     PUT     |              /api/courses/:id              |              Update a Course by :id               |
|   DELETE    |              /api/courses/:id              |              Delete a Course by :id               |
|   DELETE    |                /api/courses                |              Delete All the Courses               |
|     GET     | /api/courses/courses-title?title=[keyword] | Retrieve all Courses which title contains keyword |

## Project Folder Structure

![Image](./images/projectstructure.png "Project Structure")

Let me explain it briefly

- `Course.java` data model class corresponds to entity and table courses
- `CourseRepository.java` the interface extends JpaRepository for CRUD methods and custom finder methods. It will be
  autowired in CourseController
- `CourseController.java` the class where we will define all endpoints as a presentation layer
- Configuration for Spring Datasource, JPA & Hibernate in `application.properties`
- `data.sql` to load initial data when the application starts up
- `pom.xml` contains all the dependencies required for this application

## Create Spring Boot Project

For pre-initialized project using Spring Initializer, please
click [here](https://start.spring.io/#!type=maven-project&language=java&platformVersion=3.1.5&packaging=jar&jvmVersion=17&groupId=com.springapp&artifactId=spring-boot-app-with-mysql&name=spring-boot-app-with-mysql&description=Demo%20project%20for%20Spring%20Boot&packageName=com.springapp.spring-boot-app-with-mysql&dependencies=web,data-jpa,lombok,mysql,actuator)

## Configure Spring Datasource, JPA, Hibernate

```properties

spring.jpa.defer-datasource-initialization=true
# FOR RUNNING THROUGH STAND-LONE APPLICATION
spring.datasource.url=jdbc:mysql://localhost:3306/course-database?useSSL=false&allowPublicKeyRetrieval=true
# FOR RUNNING USING DOCKER CONTAINER UNCOMMENT WHILE DOCKERIZING APPLICATION
#spring.datasource.url=jdbc:mysql://mysql:3306/course-database?useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=dummypassword
# OPTIONAL SINCE SB 3.1.x
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
# Hibernate ddl auto (create, create-drop, validate, update)
spring.jpa.hibernate.ddl-auto=update
#Turn Statistics on
spring.jpa.properties.hibernate.generate_statistics=true
logging.level.org.hibernate.stat=debug
# Show all queries
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
# For any script-based initialization, i.e. inserting data via data.sql or creating schema via schema.sql we need to set the below property:
# UNCOMMENT AFTER THE INITIALIZATION OF DATA IS DONE
#spring.sql.init.mode=always
```

## Define Course Entity

Our Data model is Course with four fields: id, title, description, published.
In model package, we define Course class.

```java
package com.springapp.springbootappwithmysql.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "courses")
@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "published")
    private boolean published;
}
```

- `@Entity` annotation indicates that the class is a persistent Java class
- `@Table` annotation provides the table that maps this entity
- `@Id` annotation is for the primary key
- `@GeneratedValue` annotation is used to define generation strategy for the primary key. `GenerationType.AUTO` means
  Auto Increment field
- `@Column` annotation is used to define the column in database that maps annotated field

## CourseRepository

Let's create a repository interface to interact with database operations

In _repository_ folder, create `CourseRepository` interface that `extends JpaRepository`

```java
package com.springapp.springbootappwithmysql.repository;

import com.springapp.springbootappwithmysql.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByTitleContaining(String title);

}
```

Since Spring is providing boilerplate/implementation code for the `findAll(), findById(), save(), delete()`
and `deleteById()` through `JpaRepository` interface

We can also define the custom methods:

- `findByTitleContaining()`: returns all Courses which title contains input title

## CourseController

Let's create CourseController class define the all the endpoints

```java
package com.springapp.springbootappwithmysql.controller;

import com.springapp.springbootappwithmysql.exception.CourseNotFoundException;
import com.springapp.springbootappwithmysql.model.Course;
import com.springapp.springbootappwithmysql.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseService service;

    public CourseController(CourseService service) {
        this.service = service;
    }

    // http://localhost:8080/api/courses/
    @GetMapping
    @Operation(summary = "Find All Course Details")
    public ResponseEntity<List<Course>> getAllCourses() {
        Optional<List<Course>> courses = service.findAll();

        return courses.map(courseDetails -> new ResponseEntity<>(courseDetails, HttpStatus.OK))
                .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));
    }

    // http://localhost:8080/api/courses/course-titles?title=boot
    @GetMapping("/course-titles")
    @Operation(summary = "Find courses By title")
    public ResponseEntity<List<Course>> getAllCoursesBasedOnTitle(@RequestParam String title) {
        Optional<List<Course>> courses = service.findByTitleContaining(title);

        return courses.map(courseDetails -> new ResponseEntity<>(courseDetails, HttpStatus.OK))
                .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));
    }

    // http://localhost:8080/api/courses/1
    @GetMapping("/{id}")
    @Operation(summary = "Find Course By Id")
    public ResponseEntity<Course> getCourseById(@PathVariable("id") long id) {
        Optional<Course> course = service.findById(id);

        return course.map(courseOne -> new ResponseEntity<>(courseOne, HttpStatus.OK))
                .orElseThrow(() -> new CourseNotFoundException("No Courses are available.."));

    }

    // http://localhost:8080/api/courses
    @PostMapping
    @Operation(summary = "Create a New Course")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Optional<Course> newCourse = service.createCourse(course);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newCourse.get().getId())
                .toUri();

        return ResponseEntity.created(location)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Course By Id")
    public ResponseEntity<Optional<Course>> updateCourse(@PathVariable("id") long id,
                                                         @RequestBody Course course) {
        var courseData = service.findById(id);

        if (courseData.isPresent()) {
            Course updateCourse = courseData.get();
            updateCourse.setTitle(course.getTitle());
            updateCourse.setDescription(course.getDescription());
            updateCourse.setPublished(course.isPublished());
            return new ResponseEntity<>(service.createCourse(updateCourse), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete All Courses")
    public ResponseEntity<HttpStatus> deleteAllCourses() {
        service.deleteAllCourses();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Course By Id")
    public ResponseEntity<HttpStatus> deleteCourseById(@PathVariable("id") long id) {
        service.deleteCourseById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
```

## Run & Test

Run Spring Boot application with command: `mvn spring-boot:run`

> Get All courses: http://localhost:8080/api/courses/

> Get A Single Course: http://localhost:8080/api/courses/1

> Get All course based on the title: http://localhost:8080/api/courses/course-titles?title=boot

> Create a new course: http://localhost:8080/api/courses

**Sample POST Request**

```json
{
  "title": "test title",
  "description": "testdescription",
  "published": false
}
```

> Delete All courses: http://localhost:8080/api/courses/

> Delete A Single Course: http://localhost:8080/api/courses/1

> Update A Single Course: http://localhost:8080/api/courses/1

**Sample PUT Request**

```json
{
  "title": "Updated test title",
  "description": "Updated test description",
  "published": true
}
```

## Create Dockerfile for Spring Boot App

Create .Dockerfile in the root folder

```properties
FROM maven:3.8.5-openjdk-17
WORKDIR /spring-boot-app-with-mysql
COPY . .
RUN mvn clean install -DskipTests
CMD mvn spring-boot:run
```

- FROM: install the image of the Maven – JDK version.
- WORKDIR: path of the working directory.
- COPY: copy all the files inside the project directory to the container.
- RUN: execute a command-line inside the container: mvn clean install -DskipTests to install the dependencies
  in `pom.xml`.
- CMD: run script `mvn spring-boot:run` after the image is built.

## Write Docker Compose configurations

On the root of the project directory, we'll create thecompose.yml file.

Follow version 3 syntax defined by Docker:

```yaml
version: '3.8'

services:
  course-app:
  mysql:

```

`version`: Docker Compose file format version will be used.
`services`: individual services in isolated containers.

Our application has two services: `course-app` (Spring Boot) and `mysql` (MySQL database).

Here go with the complete docker compose file

```yaml
version: '3.8'

services:
  course-app:
    image: spring-boot-app-with-mysql-image
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - $SPRING_LOCAL_PORT:$SPRING_DOCKER_PORT
    depends_on:
      - mysql
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:$MYSQLDB_DOCKER_PORT/$MYSQLDB_DATABASE?useSSL=false&allowPublicKeyRetrieval=true
      SPRING_DATASOURCE_USERNAME: $MYSQLDB_USER
      SPRING_DATASOURCE_PASSWORD: $MYSQLDB_PASSWORD
  mysql:
    image: mysql:8-oracle
    env_file: ./.env
    environment:
      MYSQL_ROOT_PASSWORD: $MYSQLDB_ROOT_PASSWORD
      MYSQL_DATABASE: $MYSQLDB_DATABASE
    ports:
      - $MYSQLDB_LOCAL_PORT:$MYSQLDB_DOCKER_PORT
```

## Components of Docker Compose File

- course-app:
    - `image`: final image name
    - `build`: configuration options that are applied at build time that we defined in the _Dockerfile_ with relative
      path
    - `environment`: environmental variables that Spring Boot application uses
    - `ports` : Inbound and outbound ports
    - `depends_on`: dependency order, `mysql` is started before app
- mysql:
    - `image`: mysql image pull it from docker hub registry, if it is not available in the locally
    - `env_file`: specify our .env path that we will create later
    - `environment`: Specify the environment variables
    - `ports`: Inbound and outbound ports

## Docker Compose Environment variables

In the service configuration, we utilized environmental variables specified within the `.env` file.
Now we will create it.

_.env_

```properties
MYSQLDB_USER=root
MYSQLDB_ROOT_PASSWORD=dummypassword
MYSQLDB_DATABASE=course-database
MYSQLDB_LOCAL_PORT=3306
MYSQLDB_DOCKER_PORT=3306
SPRING_LOCAL_PORT=8080
SPRING_DOCKER_PORT=8080
```

## Run the Spring Boot microservice with Docker Compose

**NOTE: Before executing the Docker Compose command, it is imperative to initiate the Docker Desktop software.**

To spin up the containers for both course-app and mysql database, execute the docker compose command given below.

> docker compose up

Docker will pull the MySQL and Maven images (if our machine does not have it before).

The services can be run on the background with command:

> docker compose up -d

```logsyaml
puneethsai@Puneeths-MacBook-Pro spring-boot-app-with-mysql % docker compose up -d
[+] Running 12/2
 ! course-app Warning                                                                                                                                                                                3.9s 
 ✔ mysql 10 layers [⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿]      0B/0B      Pulled                                                                                                                                             115.0s 
[+] Building 61.1s (7/8)                                                                                                                                                             docker:desktop-linux
 => [course-app internal] load .dockerignore                                                                                                                                                         0.0s
 => => transferring context: 680B                                                                                                                                                                    0.0s
 => [course-app internal] load build definition from Dockerfile                                                                                                                                      0.1s
 => => transferring dockerfile: 171B                                                                                                                                                                 0.0s
 => [course-app internal] load metadata for docker.io/library/maven:3.8.5-openjdk-17                                                                                                                 2.7s
 => [course-app 1/4] FROM docker.io/library/maven:3.8.5-openjdk-17@sha256:3a9c30b3af6278a8ae0007d3a3bf00fff80ec3ed7ae4eb9bfa1772853101549b                                                           0.0s
 => [course-app internal] load build context                                                                                                                                                         0.1s
 => => transferring context: 18.49kB                                                                                                                                                                 0.1s
 => CACHED [course-app 2/4] WORKDIR /spring-boot-app-with-mysql                                                                                                                                      0.0s
 => [course-app 3/4] COPY . .                                                                                                                                                                        0.3s
 => [course-app 4/4] RUN mvn clean install -DskipTests                                                                                                                                              90.1s
 => [course-app] exporting to image                                                                                                                                                                  1.7s
 => => exporting layers                                                                                                                                                                              1.6s
 => => writing image sha256:8d81a4f06e3260805d7d6abb9cb17eea11c1e954fc09b6de2d823b78856a315e                                                                                                         0.0s 
 => => naming to docker.io/library/spring-boot-app-with-mysql-image                                                                                                                                  0.0s 
[+] Running 3/3                                                                                                                                                                                           
 ✔ Network spring-boot-app-with-mysql_default         Created                                                                                                                                        0.7s 
 ✔ Container spring-boot-app-with-mysql-mysql-1       Started                                                                                                                                        0.3s 
 ✔ Container spring-boot-app-with-mysql-course-app-1  Started                                                                                                                                        0.1s 
```

_**NOTE: EVEN THOUGH APPLICATION SAYING IT IS STARTED WHEN YOU ACCESS IF IT IS NOT WORKING, PLEASE EXECUTE DOCKER
COMPOSE UP AGAIN**_
> docker compose up -d

```logs
[+] Building 0.0s (0/0)                                                                                                                                                              docker:desktop-linux
[+] Running 2/2
 ✔ Container spring-boot-app-with-mysql-mysql-1       Running                                                                                                                                        0.0s 
 ✔ Container spring-boot-app-with-mysql-course-app-1  Started                                                                                                                                        0.0s
```

## Containers Running in Docker Desktop

![Image](./images/dockerdesktop.png "Docker Desktop")

## Run & Test

Using OpenAPI Documentation, we'll be able to access all the operations, please access the below URL

> http://localhost:8080/swagger-ui/index.html

![Image](./images/swagger.png "Swagger OpenAPI")