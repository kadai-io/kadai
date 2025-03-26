
<img src="./docs/images/logo.svg" height="21"> KADAI - The open source task management library
=================================================

[![CI](https://github.com/kadai-io/kadai/workflows/CI/badge.svg)](https://github.com/kadai-io/kadai/actions?query=workflow%3ACI)
[![License](http://img.shields.io/:license-apache-blue.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
[![Contributors](https://img.shields.io/github/contributors/kadai-io/kadai.svg)](https://github.com/kadai-io/kadai/graphs/contributors)

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=kadai-io_kadai&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=kadai-io_kadai)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=kadai-io_kadai&metric=coverage)](https://sonarcloud.io/summary/new_code?id=kadai-io_kadai)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=kadai-io_kadai&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=kadai-io_kadai)
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=kadai-io_kadai&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=kadai-io_kadai)
[![Vulnerabilities](https://sonarcloud.io/api/project_badges/measure?project=kadai-io_kadai&metric=vulnerabilities)](https://sonarcloud.io/summary/new_code?id=kadai-io_kadai)

![](docs/images/kadai-cli-light.gif#gh-light-mode-only)
![](docs/images/kadai-cli-dark.gif#gh-dark-mode-only)

KADAI is a task management component open source library. It can be embedded into your application
or be operated standalone if appropriate. Beside the basic task management functionalities,
KADAI adds workbaskets and classifications to control and monitor a large amount of Tasks within
a larger organization.


* Web Site: http://kadai.io/
* Demo Environment: https://kadai-io.azurewebsites.net/kadai
* Issue Tracker: https://github.com/kadai-io/kadai/issues
* License: Apache License, Version 2.0  https://www.apache.org/licenses/LICENSE-2.0

---
_We're not aware of all installations of our Open Source project. However, we love_

* _listening to your feedback,_
* _discussing possible use cases with you,_
* _aligning the roadmap to your needs!_

📨 _Feel free to [contact](#contact) us if you need consulting support._

---

# Table of Contents

* ✨ [Overview](#Overview)
    * [Tasks](#Tasks)
    * [Workbasket](#workbasket)
    * [Classification](#classification)
    * [Routing](#routing)
    * [Prioritization](#prioritization)
* 🧫 [Components](#components)
    * [KADAI Lib](#Kadai-lib)
    * [KADAI REST API](#Kadai-rest-api)
    * [KADAI workplace](#Kadai-workplace)
    * [KADAI admin](#Kadai-admin)
    * [KADAI monitor](#Kadai-monitor)
* 🚀 [Getting Started](#getting-started)
    * [Requirements](#requirements)
    * [Wrapper Application](#wrapper-application)
        * [Spring Boot Example](#spring-boot-example)
        * [EJB Example](#ejb-example)
* ⚙️ [Customize Behaviour](#customize-behaviour)
* 📚 [Releases](#releases)
* 🖼️ [Demo](#demo)
* 📨 [Contact](#contact)

# ✨Overview

## TASKS

Tasks are the main entity of KADAI. Each Task has its describing attributes like priority and due
date.

Furthermore each Task has a state.

And a Task holds a reference to the system and business object, it is associated with. This is
important since is meant to be a standalone component.

All Tasks are placed in a Workbasket to control and direct the handling of the Tasks.

```mermaid
---
config:
  look: neo
  theme: neo
---
stateDiagram-v2
    [*] --> READY: create()
    READY --> CLAIMED: claim()
    READY --> nonFinalEndStates: forceComplete() | cancel()
    READY --> finalEndStates: terminate()

    CLAIMED --> READY_FOR_REVIEW: requestReview()
    CLAIMED --> READY: transfer() | cancelClaim()
    CLAIMED --> nonFinalEndStates: complete() | cancel()
    CLAIMED --> finalEndStates: terminate()

    READY_FOR_REVIEW --> IN_REVIEW: claim()
    READY_FOR_REVIEW --> nonFinalEndStates: forceComplete() | cancel()
    READY_FOR_REVIEW --> finalEndStates: terminate()

    IN_REVIEW --> READY_FOR_REVIEW: transfer() | cancelClaim()
    IN_REVIEW --> nonFinalEndStates: forceComplete() | cancel()
    IN_REVIEW --> finalEndStates: terminate()

    nonFinalEndStates --> CLAIMED: reopen()
    nonFinalEndStates --> [*]
    finalEndStates --> [*]
    
    nonFinalEndStates: Non-final endstates
    state nonFinalEndStates {
        COMPLETED
        CANCELLED
    }

    finalEndStates: Final endstates
    state finalEndStates {
        TERMINATED
    }
```

## WORKBASKETS

Workbaskets are the main structure to distribute the Tasks to the available users. There are
personal, group/team and topic Workbaskets.

Workbaskets indicate the responsibility for a Task. The concepts of Workbaskets allow to
differentiate between the permissions or the skills required to complete specific Tasks and the
determination who should complete certain Tasks. This can diverge a lot in a larger organization.

<p align="center">
  <img src="./docs/images/workbaskets.svg" />
</p>

## CLASSIFICATIONS

Classifications allow to identify the type of a Task. The Task derives some major attributes from
the Classification, such as the service level and the priority.

Classifications can be configured by a responsible business administrator to control the handling
of the Tasks.

## Routing

Kadai offers various routing and distribution functions to assign incoming Tasks to the correct
Workbaskets, or to redistribute them between Workbaskets.

## Prioritization

Dynamic prioritization functions allow you to control the processing sequence of
Tasks at any time. This ensures that the most important Tasks are always addressed first.

<p align="center">
  <img src="docs/images/prioritizing.svg" />
</p>

# 🧫Components

<p align="center">
  <img src="./docs/images/components.svg" />
</p>

It splits up into five components:

* KADAI Lib
    * The Java library providing the Task management functions
* KADAI REST API
    * REST API to allow remote access to the KADAI system.
* KADAI workplace
    * Angular based web application to work on the Tasks
    * Sample Tasklist application you can use if appropriate
* KADAI admin
    * Angular based web application to configure the system
    * Supports Workbaskets and Classifications
* KADAI monitor
    * Angular based web application to monitor the status of the entire Task pool
    * Provides views and statistics about the conformance with defined services levels
    * Shows the workload of your organization and the individual teams

# 🚀Getting Started

As KADAI is meant to be integrated in the development environment and process of your organisation, you have to create
your own small integration project as a wrapper and starting point for your customisations.

We currently provide examples how to run KADAI as a Spring Boot Application.

If you are only interested in how KADAI looks and feel, you can try our [Demo Environment](#demo) instead.

## Requirements

[![Java](https://img.shields.io/badge/Java-17+-%23ED8B00.svg?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

Runtime Environment at least Java 17

[![H2](https://img.shields.io/badge/H2-003B28?style=flat&logo=h2&logoColor=white)](https://www.h2database.com/html/main.html)
[![Postgres](https://img.shields.io/badge/Postgres-%23316192.svg?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![DB2](https://img.shields.io/badge/IBM-DB2-008000?logo=IBMDB2&logoColor=fff)](https://www.ibm.com/db2)

Supported Databases:

* H2 (We test with 2.1.214*)
* Postgres (We test with 14.7*)
* DB2 (We test with 11.5.6*)

_* other versions of the named databases should work also, but haven't been tested_

## Spring Boot Example

[![Static Badge](https://img.shields.io/badge/example-spring_boot-green?logo=spring&logoColor=white)](https://github.com/kadai-io/kadai/tree/master/rest/kadai-rest-spring-example-boot)

We use the h2 database in this example.

See `rest/kadai-rest-spring-example-boot` and it dependencies

# ⚙️Customize Behaviour

[![Oracle](https://custom-icon-badges.demolab.com/badge/SPI-F80000?logo=oracle&logoColor=fff)](https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html)

KADAI allows to customize and modify it’s behaviour through the use of dedicated Service
Provider Interfaces (SPI). Each SPI defines an interface that can be implemented by custom code.
This is a common approach for Java developers to extend their applications. You can find out more
about the background and the details in the Java documentation:
https://docs.oracle.com/javase/tutorial/sound/SPI-intro.html

In order to use an SPI within KADAI, you must

* create a class that implements the relevant interface.
* place that class into the classpath of your application
* provide a control file with full name of the SPI
  (e. g. io.kadai.spi.task.api.CreateTaskPreprocessor) in the subdirectory META-INF/services of
  the classpath. This control file must contain the fully qualified classname (including the
  package) of the class that implements the relevant interface. This control file is used by the
  ServiceLoader to load the custom class at runtime. The control file may contain multiple classes
  has implement the interface. Each implementation should be declared in a new line.

  All implementations will be used consecutively in the declaration order of the control file.

If you provide one or multiple implementations according to the description above, KADAI will invoke the implementations
at a specific point. The Javadoc of each SPI describes the conditions for the implementation to be executed.

Currently, KADAI provides the following SPIs:

* [`io.kadai.spi.history.api.KadaiHistory`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/history/api/KadaiHistory.java)
* [`io.kadai.spi.priority.api.PriorityServiceProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/priority/api/PriorityServiceProvider.java)
* [`io.kadai.spi.routing.api.TaskRoutingProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/routing/api/TaskRoutingProvider.java)
* [`io.kadai.spi.task.api.AfterRequestChangesProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/AfterRequestChangesProvider.java)
* [`io.kadai.spi.task.api.AfterRequestReviewProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/AfterRequestReviewProvider.java)
* [`io.kadai.spi.task.api.BeforeRequestChangesProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/BeforeRequestChangesProvider.java)
* [`io.kadai.spi.task.api.BeforeRequestReviewProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/BeforeRequestReviewProvider.java)
* [`io.kadai.spi.task.api.CreateTaskPreprocessor`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/CreateTaskPreprocessor.java)
* [`io.kadai.spi.task.api.ReviewRequiredProvider`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/task/api/ReviewRequiredProvider.java)
* [`io.kadai.spi.user.api.RefreshUserPostprocessor`](https://github.com/kadai-io/kadai/blob/master/lib/kadai-core/src/main/java/io/kadai/spi/user/api/RefreshUserPostprocessor.java)

# 📚Releases
[![Maven Central](https://img.shields.io/maven-central/v/io.kadai/kadai-core.svg)](https://central.sonatype.com/artifact/io.kadai/kadai-core)
[![Maven Central Snapshots](https://img.shields.io/badge/maven--central--snapshots-v9.3.1--SNAPSHOT-blue?link=https%3A%2F%2Fcentral.sonatype.com%2Fservice%2Frest%2Frepository%2Fbrowse%2Fmaven-snapshots%2Fio%2Fkadai%2F)](https://central.sonatype.com/service/rest/repository/browse/maven-snapshots/io/kadai/)

The list of [releases](https://github.com/kadai-io/kadai/releases) contains a detailed changelog.

We use [Semantic Versioning](https://semver.org/).

# 🖼️Demo
[![Static Badge](https://img.shields.io/badge/demo-azure-blue?link=https%3A%2F%2Fkadai-io.azurewebsites.net%2Fkadai&&logo=angular&logoColor=white)](https://kadai-io.azurewebsites.net/kadai/)

Our focus is mainly directed to the backend, but we maintain a demo frontend in Angular, check it out by clicking on the badge!

# 📨Contact

If you have any questions or ideas feel free to create an [issue](https://github.com/kadai-io/kadai/issues) or contact us
via [GitHub Discussions](https://github.com/kadai-io/kadai/discussions).

We love listening to your feedback, and of course also discussing the project roadmap and possible use cases with you!

This open source project is being developed by [envite consulting GmbH](https://www.envite.de/) 
with the support of the open source community.

---
[![envite consulting GmbH](docs/images/envite-black.png)](https://envite.de/)
---
