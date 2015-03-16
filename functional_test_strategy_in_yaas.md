#Functional Test Strategy in YaaS

## Introduction
This document summarizes the functional test strategy applied by YaaS teams. The overarching goal of the strategy is to guarantee the delivery of functional correct services while keeping test costs minimal.

In order to understand this document various test types need to be defined:
 
***Unit testing:*** is a software testing method by which individual units of source code, sets of one or more computer program modules together with associated control data, usage procedures, and operating procedures, are tested to determine whether they are fit for use. Intuitively, one can view a unit as the smallest testable part of an application. Unit tests are short code fragments created by programmers or occasionally by white box testers during the development process. It is also known as component testing. Substitutes such as method stubs, mock objects, fakes, and test harnesses can be used to assist testing a module in isolation
 
***Acceptance testing:*** is a term used in agile software development methodologies referring to the functional testing of a user story by the software development team during the implementation phase. The product owner specifies scenarios to test when a user story has been correctly implemented. A story can have one or many acceptance tests, whatever it takes to ensure the functionality works. Acceptance tests are black-box system tests. Each acceptance test represents some expected result from the service
 
***Contract testing:*** most services have dependencies to other services to fulfill their functionality. An interface contract test guarantees that dependant services do not change their contract which would result in service malfunction  

***Smoke testing:*** is non-exhaustive software testing, ascertaining that the most crucial functions of a program work, but not bothering with finer details

All test types will be illustrated in examples.


## Indepencene vs traditional testing
The independance of teams is one of the YaaS success factors. By applying traditional integration tests requiring dependant services to be operational at any time is a clear violation of this rule as such a services is most probably maintained by a different team, it may be subject to slow, and unreliable networks, and maybe unreliable itself. The goal of this test strategy is to make teams as independent from each other, during the entire software development lifecycle. This can only be achieved by testing as much as possible locally, detached from any network.

## Guarantee business continuity for consumers
TODO


## The anatonomy of a microservice
Each microservice is composed out of following components:

* Rest API: the REST contract exposed by the service (called by consumers via http)
* Business logic: the actual implementation of the service logic (called by resources layer or business logic)
* Data access logic: mechanism to dispatch to either the integration logic or data source for fetching data objects
* Integration logic: mechanism for interaction with other services (called by business logic) 
* Data sources: mechaism for persisting data (called by business logic)

![anatomy](./images/anatomy.tiff "Anatomy of a microservice")



## Unit testing
![unittesting](./images/unittesting_restapi.tiff "Unit testing of a microservice - REST API")

![unittesting](./images/unittesting_businesslogic.tiff "Unit testing of a microservice - business logic")

![unittesting](./images/unittesting_dataaccesslogic.tiff "Unit testing of a microservice - data access logic")



## Acceptance testing

![acceptancetesting](./images/acceptancetesting.tiff "Acceptance testing of a microservice")

In order to guarantee that new minor versions do not introduce any breaking changes into a service, previous acceptance test suites need to be re-run against the most recent service version. 

![acceptancetestsuites](./images/acceptancetestsuites.tiff "Acceptance test suites")

Example: a team has already implemented two minor version of its service, v1.1 and v1.2. The team is currently working on another version v1.3. When building the most recent version the continous integration environment (CI) automatically re-runs all the existing acceptance test suites (v1.1. and v1.2 )against the newest service implementation 1.3. In case the build breaks the team needs to investigate why the old test suites aren't compatible anymore with the new implementation:

* was it forgotten to introduce a new major version?
* was it a bug?





## Contact testing






