#Functional Test Strategy in YaaS (DRAFT v0.1)

## Introduction
This document summarizes the functional test strategy applied by YaaS teams. The overarching goal of the strategy is to guarantee the delivery of functional correct services while keeping test costs minimal.

In order to understand this document various test types need to be defined:
 
***Unit testing:*** is a software testing method by which individual units of source code, sets of one or more computer program modules together with associated control data, usage procedures, and operating procedures, are tested to determine whether they are fit for use. Intuitively, one can view a unit as the smallest testable part of an application. Unit tests are short code fragments created by programmers or occasionally by white box testers during the development process. Substitutes such as method stubs, mock objects, fakes, and test harnesses can be used to assist testing a module in isolation
 
***Acceptance testing:*** is a term used in agile software development methodologies referring to the functional testing of a user story by the software development team during the implementation phase. The product owner specifies scenarios to test when a user story has been correctly implemented. A story can have one or many acceptance tests, whatever it takes to ensure the functionality works. Acceptance tests are black-box system tests. Each acceptance test represents some expected result from the service
 
***Contract testing:*** most services have dependencies to other services to fulfill their functionality. An interface contract test guarantees that dependent services do not change their contract which would result in service malfunction

***Smoke testing:*** is non-exhaustive software testing, ascertaining that the most crucial functions of a program work, but not bothering with finer details

All test types will be illustrated in examples.

## Goals

### Team independence
The independence of teams is one of the YaaS success factors. Applying traditional integration tests requires dependent services to be operational at any time which is a clear violation of this rule as such services are most probably maintained by a different team, are may be subject to slow, and unreliable networks, and maybe unreliable themselves. The goal of this test strategy is to make teams as independent from each other, during the entire software development lifecycle. This can only be achieved by testing as much as possible locally, detached from any network.

### Business continuity for consumers
A new minor service version release must not introduce breaking changes. The YaaS functional testing strategy focuses on help identifying any changes impacting the consumer interfaces so that accidental errors can be detected before impacting real consumers and stop their services being available or functional correct.

In order to guarantee that new minor versions do not introduce any breaking changes into a service, previous test suites need to be re-run against the most recent service version. 

![acceptancetestsuites](./images/acceptancetestsuites.tiff "Acceptance test suites")

Example: a team has already implemented two minor version of its service, v1.1 and v1.2. The team is currently working on another version v1.3. When building the most recent version the continuous integration environment (CI) automatically re-runs all the existing acceptance test suites (v1.1. and v1.2) against the newest service implementation 1.3. In case the build breaks the team needs to investigate why the old test suites aren't compatible anymore with the new implementation:

* was it forgotten to introduce a new major version?
* was it a bug?

## The anatomy of a microservice
Each microservice is composed out of following components:

* Rest API: the REST contract exposed by the service (called by consumers via http)
* Business logic: the actual implementation of the service logic (called by resources layer or business logic)
* Data access logic: mechanism to dispatch to either the integration logic or data source for fetching data objects
* Integration logic: mechanism for interaction with other services (called by business logic) 
* Data sources: mechanism for persisting data (called by business logic)

![anatomy](./images/anatomy.tiff "Anatomy of a microservice")

## Examples
In the following sections the authors will summarize each test type and show how each test type shall be implemented in the context of a microservice. Besides the implementation examples the authors also describe the business goals per test type. 

![exampleservices](./images/exampleservices.tiff "Example services")

A traditional commerce use-case consisting of a product, price and product details service will be used to illustrate the concepts.

### Unit testing

#### REST API testing
The REST API in a microservice is in YaaS well defined by its RAML definition file. The interface exposed by the REST API needs to be completely tested in regards of:

* compliance to RAML definition for positive/ negative scenarios
* correctness of functionality for positive scenarios (this test does not check the correct functionality of the business logic)
* correctness of functionality for negative scenarios

In order to achieve a high test coverage with minimum effort the business logic layer beneath the REST API needs to be mocked. In addition it is required that a unit test can spawn a test server with mocks injected. The tests needs to use http for communication with the actual test server.

![unittesting](./images/unittesting_restapi.tiff "Unit testing of a microservice - REST API")

The given visualization shows the test subject highlighted in red. Test doubles are marked with purple. Blue layers are not relevant for this test type. An implementation of the given test type can be found at [link](https://github.com/MichaelStephan/functionaltestingstrategy/tree/master/sample/productservice/src/test/java/api). What can be seen when looking into the example is the separation of the actual test double and service initialization and the actual expectations in the test implementation. With the given approach it is easy implement the goal of business continuity for consumers.

##### Change
This is fundamental change in the way tests are executed now. It requires that all team follow a layered implementation style. In addition it requires that a test server can be spawned from source code. Finally tests need to be structured in a way that test server behavior setup and test expectations are separated.

##### Level of freedom
Teams may decide to limit the amount of REST API tests to only "correctness of functionality for negative scenarios" and "compliance to RAML definition for negative scenarios" only. This requires that all happy paths are covered by acceptance tests. 


#### Business logic testing
The correctness of business logic needs to be tested in regards of:

* correctness of functionality for positive scenarios
* correctness of functionality for negative scenarios

![unittesting](./images/unittesting_businesslogic.tiff "Unit testing of a microservice - business logic")

The test of the business logic requires the data access logic to be mocked still the tests are executed as traditional unit tests are, no test server is required.

##### Change:
No changes are expected as teams already do proper unit testing.

##### Level of freedom
Teams can limit their business logic tests only to "correctness of functionality for negative scenarios" but need to cover the positive scenarios in the acceptance tests. 


#### Data access logic testing
A microservice has dependencies to at most two types of systems, other microservices or infrastructure components (e.g. database). The data access logic shields the business logic from the technical details of underlying implementations and therefore it needs to be subject to traditional unit tests as well.

In general following scenarios are subject to tests:

* correctness of functionality in positive scenarios
* correctness of functionality in negative scenarios

![unittesting](./images/unittesting_dataaccesslogic.tiff "Unit testing of a microservice - data access logic")

#### Data access logic testing - data sources
In case a microservice communicates directly with a infrastructure component, the latter needs to be spawned in a unit test. For JVM based infrastructure components this is normally not a problem, for other infrastructure components tools like docker may be used. At [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productservice/src/test/java/dao/impl/CassandraProductDaoTest.java) the authors show how a cassandra database could be embedded into the JVM process executing the actual unit tests.

##### Change
Most of the teams are not impacted as they don't interact with infrastructure components directly. Some teams still are required to do so and already use embedded infrastructure components, e.g. for Kafka.

#### Data access logic testing - integration logic
The integration logic for interacting with other services needs to be technically and functionally tested. Technical tests cover edge cases like:

* remote service not accessible
* slow communication when interacting with remote service
* ... 

In order to simulate the given scenarios an http mocking tool is required. An example could be found at [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productdetailsservice/src/test/java/dao/impl/PriceServiceDaoImplTechnicalTest.java).

For serving data during functional test we suggest teams to use [pact jvm](https://github.com/DiUS/pact-jvm) for producer service mocking. Teams are allowed to use other technologies which create need to be able to create pact definitions during test execution and store them into the pact repository.
As can be seen in the given example a remote service can be mocked by defining the response it returns on a specific request. What can be seen in addition, the actual mocked response definition also contains data type rules, e.g. stringMatcher("currency", "[A-Z]{3}". The rule defines that the currency field in a given response needs to consist of exactly three capital letters.

```
return builder.uponReceiving("a request for price")
	.path("/priceservice/products/" + productId + "/price")
	.method("GET")
    .willRespondWith()
    .headers(headers)
    .status(200)
    .body(new PactDslJsonBody().guid("id", expectedId).stringMatcher("currency", "[A-Z]{3}", expectedCurrency).numberType("value", 99.90).asBody()).toFragment();
    }
```

An example is available at [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productdetailsservice/src/test/java/dao/impl/GivenProductIdAsArgumentToGetPricesThenReturnProductPriceTest.java). When the unit tests are executed pact jvm will run all tests and spawn mock services. During test execution pact files will be generated. Those files can be re-used as will be described in the contract testing section. 

```
Change: most teams don't test do tests on technical integration logic (e.g. test for timeouts). In additional teams will be asked to use pact-jvm for mocking or equivalent technology.
```

## Contract testing
As mentioned in the *Data access logic testing - integration logic* section each time a functional data access logic test is executed a pact file is generated and made available in a central pact repository. From there the pact files are available for further usage, e.g. a pact compliance test against a given producer could be run if the producer is somehow modified. In addition automated tests could be run periodically as well.

![contracttestingstrat](./images/contracttestingstrat.tiff "Contract testing")

The goal of the automated contract tests is to protect any consumers from unforeseen non-compatible producer interface changes. The benefit of the given process is that the pact files are automatically generated and no team has to do additional work except for maintaining its unit tests.

##### Change
A team needs to take responsiblity of the pact repository and the CI pipelines need to be harmonized to leverage the central repository.

## Acceptance testing
Each user story has a well defined list of acceptance criterias:

* GET on /sites/\{code\}/service returns a list of configured service providers 
* POST on on /sites/\{code\}/service creates a new service provider
	* if there is a service provider with the given id, 409 is returned
* ...

Each single item of the list needs to be tested automatically. In contradiction to the REST API testing it is forbidden to mock any components in the actual microservice. It is only allowed to mock remote producer services. In case a service requires infrastructure components these need to be run embedded in the test. Again the rule applies that acceptance tests need to be runnable independent from any other services or infrastructure.

![acceptancetesting](./images/acceptancetesting.tiff "Acceptance testing of a microservice")

As with REST API tests the acceptance tests require an embedded test server to be running. For the actual acceptance test the test simulates a real user interacting with the server. Subject to test is the service's functional correctness and behavior in case of user input error. If no already tested during REST API testing the compliance with the RAML definition needs also be tested.

As with the REST API testing it needs to be guaranteed that a new minor version of a service does not introduce any breaking changes into its interface. Therefor it is mandatory that former minor versions' acceptance tests are re-run against the most recent version. This is inline with with the business continuity for consumers goal.

##### Change
Same as in section REST API testing.

## Smoke testing
Tools like the robot framework or SOAPUI may be used to simulate real user journeys on the real stage/ production services. Smoke tests cover only happy paths and don't test an erroneous scenarios. Each team is asked to keep the amount of smoke tests to a minimum, e.g. one simple test per service resource.


# Delivery
The authors of this guide are aware of the fact that all teams implemented their services differently, some follow a clean layered architecture as described above others don't. Still the concepts can be mapped to any kind of implementation patterns.  

| Tasks        | Responsible           | Due to  |
| ------------- |:-------------:|:-----:|
| Communicate test strategy to teams      | Angela | 02.04.2015 |
| Periodically execute smoke testing (aka existing integration tests) in stage and production)      | all teams | TBD |
| Create acceptance tests and embed into CI process      | all teams      |   TBD |
| Create REST API tests and embed into CI process | all teams      |    TBD |
| Create data access logic tests - data sources and embed into CI process | all teams      |    TBD |
| Create data access logic tests - integration logic and embed into CI process | all teams      |    TBD |
| Setup pact repository | Idefix      |    TBD |
| Setup CI process to publish pact contracts into pact repository | all teams      |    TBD |
| Periodically execute contract tests | Idefix      |    TBD |


# TODOs
* How to enfore immutability of REST API tests and acceptance tests
* Implications of test failures (e.g. stop CI process)
* How to monitor team's compliance with test strategy
* How to incorporate proxy into local tests (e.g. docker)
* How to incorporate automated raml compliance checks into local tests (tool by marek koniew)
