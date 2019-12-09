package io.poc.process;

import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.ExternalTaskClientBuilder;
import org.camunda.bpm.client.interceptor.ClientRequestInterceptor;
import org.camunda.bpm.client.interceptor.auth.BasicAuthProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExternalTaskConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalTaskConfiguration.class);

    @Value("${app.engine.api}")
    private String engineUrl;
    @Value("${app.engine.userName}")
    private String engineUserName;
    @Value("${app.engine.password}")
    private String enginePassword;

    public ExternalTaskClient createExternalTaskClient() {

        ClientRequestInterceptor interceptor = createClientInterceptor(engineUserName, enginePassword);
        ExternalTaskClientBuilder externalTaskClientBuilder = ExternalTaskClient.create();
        externalTaskClientBuilder.baseUrl(engineUrl);
        externalTaskClientBuilder.lockDuration(1000).addInterceptor(interceptor);
        ExternalTaskClient externalTaskClient = externalTaskClientBuilder.build();
        return externalTaskClient;

    }


    private ClientRequestInterceptor createClientInterceptor(String engineUserName, String enginePassword) {

        ClientRequestInterceptor basicAuth = (ClientRequestInterceptor) new BasicAuthProvider(engineUserName, enginePassword);
        return basicAuth;
    }


}
