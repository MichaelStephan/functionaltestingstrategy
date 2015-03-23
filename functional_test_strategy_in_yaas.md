#Functional Test Strategy in YaaS

## Introduction
This document summarizes the functional test strategy applied by YaaS teams. The overarching goal of the strategy is to guarantee the delivery of functional correct services while keeping test costs minimal.

In order to understand this document various test types need to be defined:
 
***Unit testing:*** is a software testing method by which individual units of source code, sets of one or more computer program modules together with associated control data, usage procedures, and operating procedures, are tested to determine whether they are fit for use. Intuitively, one can view a unit as the smallest testable part of an application. Unit tests are short code fragments created by programmers or occasionally by white box testers during the development process. Substitutes such as method stubs, mock objects, fakes, and test harnesses can be used to assist testing a module/ service in isolation
 
***Acceptance testing:*** is a term used in agile software development methodologies referring to the functional testing of a user story by the software development team during the implementation phase. The product owner specifies scenarios to test when a user story has been correctly implemented. A story can have one or many acceptance tests, whatever it takes to ensure the functionality works. Acceptance tests are black-box system tests. Each acceptance test represents some expected result from the service
 
***Contract testing:*** most services have dependencies to other services to fulfill their functionality. An interface contract test guarantees that dependant services do not change their contract which would result in service malfunction  

***Smoke testing:*** is non-exhaustive software testing, ascertaining that the most crucial functions of a program work, but not bothering with finer details

All test types will be illustrated in examples.

## Goals

### Team indepedence
The independance of teams is one of the YaaS success factors. By applying traditional integration tests requiring dependant services to be operational at any time is a clear violation of this rule as such a services is most probably maintained by a different team, it may be subject to slow, and unreliable networks, and maybe unreliable itself. The goal of this test strategy is to make teams as independent from each other, during the entire software development lifecycle. This can only be achieved by testing as much as possible locally, detached from any network.

### Business continuity for consumers
A new service release should avoid introducing changes which will break fuctionalities of consumers by all means. The YaaS functional testing strategy focuses on help identifying any changes impacting the consumer interfaces.
This document covers only minor versions changes and does not deal with major version. In order to guarantee that new minor versions do not introduce any breaking changes into a service, previous test suites need to be re-run against the most recent service version. 

![acceptancetestsuites](./images/acceptancetestsuites.tiff "Acceptance test suites")

Example: a team has already implemented two minor version of its service, v1.1 and v1.2. The team is currently working on another version v1.3. When building the most recent version the continous integration environment (CI) automatically re-runs all the existing acceptance test suites (v1.1. and v1.2) against the newest service implementation 1.3. In case the build breaks the team needs to investigate why the old test suites aren't compatible anymore with the new implementation:

* was it forgotten to introduce a new major version?
* was it a bug?


## The anatomy of a microservice
Each microservice is composed out of following components:

* Rest API: the REST contract exposed by the service (called by consumers via http)
* Business logic: the actual implementation of the service logic (called by resources layer or business logic)
* Data access logic: mechanism to dispatch to either the integration logic or data source for fetching data objects
* Integration logic: mechanism for interaction with other services (called by business logic) 
* Data sources: mechaism for persisting data (called by business logic)

![anatomy](./images/anatomy.tiff "Anatomy of a microservice")


## Examples
In the following sections the authors will summarize each test type and show how each test type shall be implemented in the context of a microservice. Besides the implementation examples the authors also describe the business goals per test type. 

![exampleservices](./images/exampleservices.tiff "Example services")

A traditional commerce use-case consisting of a product, price and product details service will be used to illustrate the concepts.


### Unit testing


#### REST API testing
The REST API in a microservice is in YaaS well defined by its RAML definition file. The interface exposed by the REST API needs to be completely tested in regards of:

* compliance to RAML definition 
* correctness of functionality in positive scenarios
* correctness of functionality in negative scenarios

In order to achieve a high test coverage with minimum effort the business logic layer beneath the REST API needs to be mocked. In addition it is required that a unit test can spawn a test server with mocks injected. The tests run real http requests against the test server.

![unittesting](./images/unittesting_restapi.tiff "Unit testing of a microservice - REST API")

The given visualization shows the test subject highlighted in red. Test doubles are marked with magenta. Blue layers are not relevant for this test type. An implementation of the given test type can be found at [link](https://github.com/MichaelStephan/functionaltestingstrategy/tree/master/sample/productservice/src/test/java/api). What can be seen when looking into the example is the separation of the actual test double and service initialization and the actual expecations in the test implementation. With the given approach it is easy to implemented the goal of business continuity for consumers.


#### Business logic testing
The correctness of business logic needs to be tested in regards of:

* correctness of functionality in positive scenarios
* correctness of functionality in negative scenarios

![unittesting](./images/unittesting_businesslogic.tiff "Unit testing of a microservice - business logic")

The test of the business logic requires the data access logic to be mocked still the tests are executed as traditional unit tests are, no test server is required.


#### Data access logic testing

A microservice has at most two dependency types, other microservices or infrastructure components (e.g. database). The data access logic shields the business logic from the technical details of underlying implementations and therefore it needs to be subject to traditional unit tests as well.

In general following scenarions are subject to tests:

* correctness of functionality in positive scenarios
* correctness of functionality in negative scenarios

![unittesting](./images/unittesting_dataaccesslogic.tiff "Unit testing of a microservice - data access logic")


#### Data access logic testing - data sources
In case a microservice communicates directly with a infrastructure component it needs to be spawnable in a unit test. For JVM based infrastructure components this is normally not a problem, for other infrastructure components tools like docker may be used. At [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productservice/src/test/java/dao/impl/CassandraProductDaoTest.java) the authors show how a cassandra database could be embedded into the JVM process executing the actual unit tests.


#### Data access logic testing - integration logic
The integration logic for interacting with other services needs to be technically and functionally tested. Technical tests cover edge cases like:

* remote service not accessible
* slow communication when interacting with remote service
* ... 

In order to simulate the given scenarios an http mocking tool is required. An example could be found at [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productdetailsservice/src/test/java/dao/impl/PriceServiceDaoImplTechnicalTest.java).

For serving data during functional test we will ask teams to use [pact jvm](https://github.com/DiUS/pact-jvm) for producer service mocking. As can be seen in the given example a remote serivce can be mocked by defining the response it returns on a specific request. What can be seen in addition, the actual mocked response definition also contains data type rules, e.g. stringMatcher("currency", "[A-Z]{3}". The rule defines that the currency field in a given response needs to consist of exactly three capital letters.

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

An example is available at [link](https://github.com/MichaelStephan/functionaltestingstrategy/blob/master/sample/productdetailsservice/src/test/java/dao/impl/GivenProductIdAsArgumentToGetPricesThenReturnProductPriceTest.java). When the unit tests are executed pact jvm will run all tests and spawn mock services if applicable. During test execution pact files will be generated. Those files can be re-used as will be described in the contract testing section. 

## Contract testing
As mentioned in the *Data access logic testing - integration logic* section each time a functional data access logic test is executed a pact file is generated and made available in a central pact repository. From there the pact files are available for further usage, e.g. a pact compliance test against a given product could be run if the producer is somehow modified. In addition automated tests could be run periodically as well. 

![contracttestingstrat](./images/contracttestingstrat.tiff "Contract testing")

The goal of the automated contract tests is to protect any consumers from unforseen non-compatible producer interface changes. The benfit of the given process is that the pact files are automatically generated and 
no team has to do additiona work except for maintaining its unit tests.  


## Acceptance testing
Each user story has a well defined list of acceptance criterias:

* GET on /sites/\{code\}/service returns a list of configured service providers 
* POST on on /sites/\{code\}/service creates a new service provider
	* if there is a service provider with the given id, 409 is returned
* ...

Each single item of the list needs to be tested automatically. In contradiction to the REST API testing  it is forbidden to mock any components in the actual microservice, it is only allowed to mock remote producer services. In case a service requires infrastructure components these need to be run embedded in the test. Again the rule applies that acceptance tests need to be runnable independant from any other services or infrastructure.

![acceptancetesting](./images/acceptancetesting.tiff "Acceptance testing of a microservice")

As with REST API tests the acceptance tests require a test server to be running. For the actual acceptance test the test simulates a real user interacting with the server. Subject to test is the service's functional correctness and behavior in case of user error.

As with the REST API testing it needs to be guaranteed that a new minor version of a service does not introduce any breaking changes into its interface. Therefor it is mandatory that former minor versions' acceptance tests are re-run against the most recent version. This is inline with with the business continuity for consumers goal.


## Smoke testing
Tools like the robot framework or SOAPUI may be used to simulate real user journeys on the real stage/ production services. Smoke tests cover only happy paths and don't test an erroneous scenarios. Each team is asked to keep the amount of smoke tests to a minimum, e.g. one simple test per service resource.


## Guidelines

### Naming conventions



