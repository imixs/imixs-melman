# Imixs-Melman

[![Join the chat at https://gitter.im/imixs/imixs-workflow](https://badges.gitter.im/imixs/imixs-workflow.svg)](https://gitter.im/imixs/imixs-workflow?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/imixs/imixs-melman.svg?branch=master)](https://travis-ci.org/imixs/imixs-melman)
[![License](https://img.shields.io/badge/license-GPL-blue.svg)](https://github.com/imixs/imixs-melman/blob/master/LICENSE)

The goal of this project is to provide components to interact with the Imixs-Workflow Rest API. These components are agnostic from an Imixs-Workflow Implementation and can be used in a microservice architecture. The components are based on Java JAX-RS and JAX-B.

## Installation

Using Maven add the following dependencies to your project:

```xml
	<!-- JEE Dependencies -->
	<dependency>
		<groupId>jakarta.platform</groupId>
		<artifactId>jakarta.jakartaee-api</artifactId>
		<version>10.0.0</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>org.imixs.workflow</groupId>
		<artifactId>imixs-melman</artifactId>
		<version>2.1.0</version>
		<scope>test</scope>
	</dependency>
```

Imixs-Melman is based on Jax-RS 2.0. So you may need to add the missing Java dependencies.
For jUnit test you can add the following dependencies to test Jax-RS 2.0

```xml
	<!-- JAX-RS 2.0 Test dependencies -->
	<dependency>
		<groupId>org.glassfish.jaxb</groupId>
		<artifactId>jaxb-runtime</artifactId>
		<version>3.0.0</version>
	</dependency>
	<dependency>
		<groupId>jakarta.xml.bind</groupId>
		<artifactId>jakarta.xml.bind-api</artifactId>
		<version>3.0.0</version>
	</dependency>
	<dependency>
		<groupId>org.glassfish.jersey.core</groupId>
		<artifactId>jersey-client</artifactId>
		<version>2.27</version>
	</dependency>
	<dependency>
		<groupId>org.apache.cxf</groupId>
		<artifactId>cxf-rt-rs-client</artifactId>
		<version>3.0.15</version>
	</dependency>
```

# Examples

These are examples how to use Imixs-Melman. For details see also the [Imixs-Workflow Rest-API](https://www.imixs.org/doc/restapi/index.html).

## Initalization & Authentication

Each request against the Imixs-Workflow engine must be authenticated. For that reason Imixs-Melman provides some AuthenticatonRequestFilter.

```java
// Init the workflowClient with a basis URL
WorkflowClient workflowCLient = new WorkflowClient("http://localhost:8080/office-rest/");
// Create a basic authenticator
BasicAuthenticator basicAuth = new BasicAuthenticator("admin", "adminadmin");
// register the authenticator
workflowCLient.registerClientRequestFilter(basicAuth);
...
```

## Get a Workitem by $UniqueID

```java
....
// get document by UniqueID
ItemCollection document = workflowCLient.getWorkitem("f3357f0b-20de-40ca-8aa1-4b9f43759c0b");
```

## Get a Resultlist of workitems

Imixs-Melman provides a set of getter methods to receive a list of workitems.

### Get tasklist by Creator

```java
// get task list by creator with maximum 5 elements.
List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin");
```

### Get tasklist by Owner

```java
List<ItemCollection> documents=workflowCLient.getTaskListByOwner("admin",5,0, null);
```

### How to restrict the Result Set

Per default all WorkItems are returned with all available items. To restrict the returned WorkItem data to only a subset of items the property 'items' need to be specified:

```java
// get task list by creator with maximum 100 elements with subset of items
workflowCLient.setItems("$processid,$modelversion,txtworkflowsummary");
List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin");
```

Also the page size and page index can be limited:

```java
workflowCLient.setPageSize(100);
workflowCLient.setPageIndex(0);
List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin");
```

## Create a Workitem

To create a workitem, an ItemCollection have to be created first:

```java
ItemCollection workitem=new ItemCollection();
workitem.model("1.0.0").task(1000).event(10);
workitem.replaceItemValue("type", "workitem");
// add some data..
workitem.replaceItemValue("_subject","This is some test data....");
// process workitem
workitem=workflowCLient.processWorkitem(workitem);
String unqiueID=workitem.getUniqueID();
```

## Get Workflow Events by a Workitem

To get all valid workflow events for an existing process instance:

```java
// load worktiem
ItemCollection workitem = workflowCLient.getWorkitem(uniqueID.get(), null);
// load event list
List<ItemCollection> events = workflowCLient.getWorkflowEventsByWorkitem(workitem);
```

## Error Handling

The Melman Rest Client throws a _RestAPIException_ in case an API error occurred. The Imixs _RestAPIException_ inherits form the Imixs _WorkflowException_ and provides methods to evaluate the error context and error code. See the following example:

```java
...
try {
	workitem = workflowCLient.processWorkitem(workitem);
} catch (RestAPIException e) {
	// evaluate exception
	Assert.assertEquals("MODEL_ERROR", e.getErrorCode());
	Assert.assertEquals("WorkflowClient", e.getErrorContext());
	logger.info("ErrorMessage=" + e.getMessage());
}
...
```

# Insecure SSL Connections

The initialization of a secure JAX-RS / HTTPs connection can fail caused by the lack of a certificate in Java's keystore. A certificate import into the java keystore fixes the problem in most cases. But for development or for system tests, however, a certificate verification is not required and can be omitted. To accept insecure SSL connections the environment variable _IMIXS_REST_CLIENT_INSECURE_ can be set to 'true':

    IMIXS_REST_CLIENT_INSECURE=true

In this mode, the Imixs-Rest Client will install a custom SSL TrustManager that accepts insecure SSL connections.

**Note:** This feature should only be used in dev and test environments!
