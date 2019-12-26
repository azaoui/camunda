## External task pattern:

 ![pattern](/doc/1.png)

Process Engine: Creation of an external task instance

External Worker: Fetch and lock external tasks

External Worker & Process Engine: Complete external task instance


External task in camunda is a pattern where workers are independent of the process engine and receive work items by polling the process engine’s APIs. 


## Polling Definition : 
Client has to perform multiple recurring requests until an information is available.

The client makes requests periodically, and the server sends data if there is a response. In case there is no data to be sent by the server, an empty response is returned. The following diagram shows how continuous polling works:


 

#### Problem : 
This approach can obviously be expensive in terms of resources especially if the the polling is too frequently. 

#### How Camunda external task reduce this impact with the external task pattern : 

Long polling significantly reduces the number of requests and enables using resources more efficiently on both the server and the client side.

 


#### External task sequence :
 

External task must declare a topic:

 ![topic](/doc/2.png)

External task workers subscribe to the topic.

Worker complete the task and notify the process engine.

process engine mark the external task as completed.

 


#### External task java client:

doc : https://github.com/camunda/camunda-external-task-client-java ‑ 

To use the external task in the project we should add this dependency to the project (out of the box of the engine): 
```
<dependency>
  <groupId>org.camunda.bpm</groupId>
  <artifactId>camunda-external-task-client</artifactId>
  <version>${version}</version>
</dependency>
 ```

Create External task client : 

 
```
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
....
ClientRequestInterceptor interceptor = createClientInterceptor(engineUserName, enginePassword);
        ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();
        externalTaskClientBuilder.baseUrl(engineUrl);
        externalTaskClientBuilder.lockDuration(1000).addInterceptor(interceptor);
        ExternalTaskClient externalTaskClient = externalTaskClientBuilder.build();
        return externalTaskClient;
 ClientRequestInterceptor :  used to intercept the request that will be send to process engine in order to add the needed authorization (basic auth in our case) 

private ClientRequestInterceptor createClientInterceptor(String engineUserName, String enginePassword) {
//admin;admin 
        ClientRequestInterceptor basicAuth = (ClientRequestInterceptor) new BasicAuthProvider(engineUserName, enginePassword);
        return basicAuth;
    }
``` 
    
* baseUrl: the link to the remote camunda engine rest api .

* lockDuration : in milliseconds to lock the external tasks .

 

Client subscription and handler :  

 ![handler](/doc/3.png)
 
The client should subscribe to a topic defined in the remote engine

The client fetch and lock continuously for newly appearing External Tasks provided by the Workflow Engine.

The client handle the task.

the client complete the task.

ExternalTaskClient client = externalTaskConfiguration.createExternalTaskClient();
        client.subscribe("checkusers")
                .lockDuration(60000).handler(

                (externalTask, externalTaskService) -> {
                    LOG.info("Task received: " + externalTask.getId());
                    try {
                        externalTaskHandler(externalTask, externalTaskService);

                    } catch (Exception e) {
                        LOG.error("An error occured", e);
                    }

                }
        ).open();
    }
NB: 

The client could receive variable from the process engine:

 

externalTask.getVariable("varName");
The Client when complete the task can send variable to the engine : 

 

 Map<String, Object> variables = new HashMap<>();
externalTaskService.complete(externalTask, variables);



How to create a JavaScript based worker: 

Create a package.json file : 
 
```json
{
  "name": "poc-camunda-external-task-worker",
  "version": "1.0.0",
  "main": "worker.js",
  "author": "POC"
  }
```  
Where : 

lists the packages your project depends on.

specifies versions of a package that your project can use using semantic versioning rules.

makes your build reproducible, and therefore easier to share with other developers.

 


Install camunda-external-task-client-js : 

```
npm install  camunda-external-task-client-js --save
```
This command will install the  camunda-external-task-client-js  with all dependency and add this lib to the package json file:

```json
{
  "name": "poc-camunda-external-task-worker",
  "version": "1.0.0",
  "main": "worker.js",
  "author": "poc ",
  "license": "Apache License 2.0",
  "dependencies": {
    "camunda-external-task-client-js": "^1.3.0"
  }
}
```


Create worker.js : 
 
```javascript
/**
 * 
 * Sample external task worker
 */
 
const {Client, BasicAuthInterceptor,logger, Variables} = require("camunda-external-task-client-js");

const basicAuthentication = new BasicAuthInterceptor({
    username: "admin",
    password: "admin"
  }); 

// configuration for the Client:
// - 'baseUrl': url to the Workflow Engine
// - 'logger': utility to automatically log important events
// -interceptors : interceptors to add custom information to request header in our example its basicAuthentication for 
const config = {baseUrl: "https://localhost:8080/rest", interceptors: basicAuthentication, use: logger,asyncResponseTimeout:5000,maxTasks:1};


// create a Client instance with custom configuration
const client = new Client(config);

client.subscribe("checkAvailability", async function ({task, taskService}) {
    //await taskService.extendLock(task, 5);

    const processVariables = new Variables();
    // randomize true or false for "isAvailble" variable
    const isAvailble = Math.random() >= 0.5;

      // add the variable to the collection
      processVariables.set("isAvailble", isAvailble);

    console.log("......finishing up 'checkAvailability' topic work");

    // complete the task in Camunda Engine via the client API
    await taskService.complete(task, processVariables);

});
 ```
 
 

Start the worker : 

 
```
node worker.js
```

Polling tasks from the engine works by performing a fetch & lock operation of tasks that have subscriptions. It then calls the handler registered to each task.

Polling is done periodically based on the interval configuration. 

 


Modeling the external task and deploy to process engine:
Let’s assume that we have a sample process that will use an external task to make a decision : Take direction to  A or B : 


Check availability : is an external task that will be executed in an external worker (in our case the nodejs client).

Check availability:  Will return a Boolean variable #{isAvailble} to decide going on direction A or B.

Prepare the bpmn model : 
We should give a unique topic name to the external task , that will be used by Clients to subscribe


Configure the ExclusiveGateway : 


Deploy the project to running  Camunda engine 


