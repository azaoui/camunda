package io.poc.process.delegate;

import io.poc.process.ExternalTaskConfiguration;
import io.poc.process.model.Person;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.task.ExternalTask;
import org.camunda.bpm.client.task.ExternalTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Async("createExternalTaskClient")
@Component
public class CustomerService {


    @Autowired
    private ExternalTaskConfiguration externalTaskConfiguration;

    private static final Logger LOG = LoggerFactory.getLogger(ExternalTaskConfiguration.class);

    @PostConstruct
    public void checkUsers() throws Exception {

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

    private synchronized void externalTaskHandler(ExternalTask externalTask, ExternalTaskService externalTaskService) {
        boolean success = false;
        Integer retryCount = externalTask.getRetries();
        LOG.info("Should implement the logic here");
        Person person = new Person("Paul", "paul@noreply.com");
        //https://groups.google.com/forum/#!topic/camunda-bpm-users/tmFZ9rYObzE
        //should include the spin plugin to be able serialise object
       /*ObjectValue invoiceValue = Variables
                .objectValue(person)
                .serializationDataFormat("application/json")
                .create();*/
        Map<String, Object> variables = new HashMap<>();
        //variables.put("invoice", invoiceValue);
        variables.put("xxxx", "12222222222");
        externalTaskService.complete(externalTask, variables);

        // it's possible to get variable from the process
        Object contract = externalTask.getVariable("contract");
        //and handle failure
        // externalTaskService.handleFailure(externalTask, "NO_CONTRACT", "No contract information present", 0, 0);


    }

}
