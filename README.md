# Imixs-Melman

The goal of this project is to provide components to interact with the Imixs-Workflow Rest API. These components are agnostic from an Imixs-Workflow Implementation and can be used in a microservice architecture. The components are based on Java JAX-RS and JAX-B. 

## Installation

Using Maven add the following dependencies to your project:

	<!-- JEE Dependencies -->
	<dependency>
		<groupId>javax</groupId>
		<artifactId>javaee-api</artifactId>
		<version>7.0</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>org.imixs.workflow</groupId>
		<artifactId>imixs-melman</artifactId>
		<version>1.0.0</version>
		<scope>test</scope>
	</dependency>

Imixs-Melman is based on Jax-RS 2.0. So you may need to add the missing Java dependencies. 
For jUnit test you can add the following dependencys to test Jax-RS 2.0

	<!-- JAX-RS 2.0 Test dependencies -->
	<dependency>
		<groupId>org.glassfish.jersey.core</groupId>
		<artifactId>jersey-client</artifactId>
		<version>2.27</version>
		<scope>test</scope>
	</dependency>
		
	<dependency>
		<groupId>org.apache.cxf</groupId>
		<artifactId>cxf-rt-rs-client</artifactId>
		<version>3.0.15</version>
		<scope>test</scope>
	</dependency>

	
	
# Examples
	
These are examples how to use Imixs-Melman. For details see also the [Imixs-Workflow Rest-API](https://www.imixs.org/doc/restapi/index.html). 


## Initalization & Authentication

Each request against the Imixs-Workflow engine must be authenticated. For that reason Imixs-Melman provides some AuthenticatonRequestFilter. 

	// Init the workflowClient with a basis URL
	WorkflowClient workflowCLient = new WorkflowClient("http://localhost:8080/office-rest/");
	// Create a basic authenticator
	BasicAuthenticator basicAuth = new BasicAuthenticator("admin", "adminadmin");
	// register the authenticator
	workflowCLient.registerClientRequestFilter(basicAuth);
	...


## Get a Workitem by $UniqueID

	....
	// get document by UniqueID
	ItemCollection document = workflowCLient.getDocumentCustom("f3357f0b-20de-40ca-8aa1-4b9f43759c0b", null);

## Get a Resultlist of workitems

Imixs-Melman provides a set of getter methods to receive a list of workitems. 
Each method expects the parameters PageSize, PageIndex and an optional item list: 

	List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin",5,0, null);

	
### Get tasklist by Creator

		
	// get task list by creator with maximum 5 elements.
	List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin",5,0, null);

to restrict the resultlist to only a subset of items use the 'items' param:

	// get task list by creator with maximum 100 elements with subset of items
	List<ItemCollection> documents=workflowCLient.getTaskListByCreator("admin",100,0, "$processid,$modelversion,txtworkflowsummary");

	
### Get tasklist by Owner

	
	List<ItemCollection> documents=workflowCLient.getTaskListByOwner("admin",5,0, null);


## Create a Workitem

To create a workitem, an ItemCollection have to be created first:

	ItemCollection workitem=new ItemCollection();
	workitem.replaceItemValue("type", "workitem");
	workitem.replaceItemValue(WorkflowKernel.MODELVERSION, "1.0.0");
	workitem.replaceItemValue(WorkflowKernel.PROCESSID,1000);
	workitem.replaceItemValue(WorkflowKernel.ACTIVITYID,10);
	// add some data..
	workitem.replaceItemValue("_subject","This is some test data....");
	// process workitem
	workitem=workflowCLient.processWorkitem(workitem);
	String unqiueID=workitem.getUniqueID();
		
	