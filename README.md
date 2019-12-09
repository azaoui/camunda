## External task pattern:

 ![pattern](/doc/1.png)

Process Engine: Creation of an external task instance

External Worker: Fetch and lock external tasks

External Worker & Process Engine: Complete external task instance

 


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
