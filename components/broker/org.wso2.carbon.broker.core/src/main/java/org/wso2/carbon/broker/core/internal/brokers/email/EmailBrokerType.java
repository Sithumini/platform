/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.broker.core.internal.brokers.email;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.base.BaseConstants;
import org.apache.axis2.transport.mail.MailConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.broker.core.BrokerConfiguration;
import org.wso2.carbon.broker.core.BrokerListener;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.broker.core.BrokerType;
import org.wso2.carbon.broker.core.internal.ds.BrokerServiceValueHolder;
import org.wso2.carbon.broker.core.internal.util.BrokerConstants;
import org.wso2.carbon.databridge.agent.thrift.internal.utils.AgentConstants;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class EmailBrokerType implements BrokerType {

    private static final Log log = LogFactory.getLog(EmailBrokerType.class);

    private ConcurrentHashMap<String, EmailSenderConfiguration> emailSenderConfigurationMap = new ConcurrentHashMap<String, EmailSenderConfiguration>();


    private BrokerTypeDto brokerTypeDto = null;

    private static EmailBrokerType emailBrokerType = new EmailBrokerType();

    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, Integer.MAX_VALUE, AgentConstants.DEFAULT_KEEP_ALIVE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    private EmailBrokerType() {
        this.brokerTypeDto = new BrokerTypeDto();
        this.brokerTypeDto.setName(BrokerConstants.BROKER_TYPE_EMAIL);

        ResourceBundle resourceBundle = ResourceBundle.getBundle(
                "org.wso2.carbon.broker.core.i18n.Resources", Locale.getDefault());

        // set default Subject as a property
        Property factoryInitialProperty = new Property(BrokerConstants.BROKER_CONF_EMAIL_DEFAULT_SUBJECT);
        factoryInitialProperty.setRequired(true);
        factoryInitialProperty.setDisplayName(
                resourceBundle.getString(BrokerConstants.BROKER_CONF_EMAIL_DEFAULT_SUBJECT));
        getBrokerTypeDto().addProperty(factoryInitialProperty);
    }

    public static EmailBrokerType getInstance() {
        return emailBrokerType;
    }

    public String subscribe(String topicName,
                            BrokerListener brokerListener,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration)
            throws BrokerEventProcessingException {
        throw new BrokerEventProcessingException("Can not subscribe to e-mail broker " + brokerConfiguration.getName());
    }

    public void publish(String topicName,
                        Object message,
                        BrokerConfiguration brokerConfiguration)
            throws BrokerEventProcessingException {
        // note here the brokerConfiguration configuration for all the brokers are same hence ignored
        EmailSenderConfiguration emailSenderConfiguration = emailSenderConfigurationMap.get(topicName);
        if (emailSenderConfiguration == null) {
            emailSenderConfiguration = new EmailSenderConfiguration(topicName, brokerConfiguration.getProperties().get(BrokerConstants.BROKER_CONF_EMAIL_DEFAULT_SUBJECT));
            emailSenderConfigurationMap.putIfAbsent(topicName, emailSenderConfiguration);
        }

        String[] emailIds = emailSenderConfiguration.getEmailIds();
        if (emailIds != null) {
            for (String email : emailIds) {
                threadPoolExecutor.submit(new EmailSender(email, emailSenderConfiguration.getSubject(), (String) message));
            }
        }
    }

    @Override
    public void testConnection(BrokerConfiguration brokerConfiguration) throws BrokerEventProcessingException {
        //no test
    }

    public BrokerTypeDto getBrokerTypeDto() {
        return brokerTypeDto;
    }

    public void unsubscribe(String topicName,
                            BrokerConfiguration brokerConfiguration,
                            AxisConfiguration axisConfiguration, String subscriptionId)
            throws BrokerEventProcessingException {


    }

    class EmailSender implements Runnable {
        String to;
        String subject;
        String body;

        EmailSender(String to, String subject, String body) {
            this.to = to;
            this.subject = subject;
            this.body = body;
        }

        @Override
        public void run() {
            Map<String, String> headerMap = new HashMap<String, String>();
            headerMap.put(MailConstants.MAIL_HEADER_SUBJECT, subject);
            OMElement payload = OMAbstractFactory.getOMFactory().createOMElement(
                    BaseConstants.DEFAULT_TEXT_WRAPPER, null);
            payload.setText(body);

            try {
                ServiceClient serviceClient;
                ConfigurationContext configContext = BrokerServiceValueHolder.getConfigurationContextService().getClientConfigContext();
                if (configContext != null) {
                    serviceClient = new ServiceClient(configContext, null);
                } else {
                    serviceClient = new ServiceClient();
                }
                Options options = new Options();
                options.setProperty(Constants.Configuration.ENABLE_REST, Constants.VALUE_TRUE);
                options.setProperty(MessageContext.TRANSPORT_HEADERS, headerMap);
                options.setProperty(MailConstants.TRANSPORT_MAIL_FORMAT,
                                    MailConstants.TRANSPORT_FORMAT_TEXT);
                options.setTo(new EndpointReference("mailto:" + to));
                serviceClient.setOptions(options);
                serviceClient.fireAndForget(payload);
                log.debug("Sending confirmation mail to " + to);
            } catch (AxisFault e) {
                String msg = "Error in delivering the message, " +
                             "subject: " + subject + ", to: " + to + ".";
                log.error(msg);
            }
        }
    }

    private class EmailSenderConfiguration {


        private String subject;
        private String[] emailIds;

        private EmailSenderConfiguration(String topic, String defaultSubject) {
            String[] emailIdsAndSubject = topic.split("/");
            if (defaultSubject != null) {
                subject = defaultSubject;
            } else {
                subject = "";
            }
            String emailIdString = null;
            if (emailIdsAndSubject.length == 2) {
                subject = emailIdsAndSubject[1];
                emailIdString = emailIdsAndSubject[0];
            } else if (emailIdsAndSubject.length == 1) {
                emailIdString = emailIdsAndSubject[0];
                log.info("Subject for the topic '" + topic + "' is empty");
            } else {
                log.error(topic + " doesn't constants E-mail ids hence no message will be sent via topic '" + topic + "'");
            }
            emailIds = null;
            if (emailIdString != null) {
                emailIds = emailIdString.replaceAll(" ", "").split(",");
            }
        }

        public String getSubject() {
            return subject;
        }

        public String[] getEmailIds() {
            return emailIds;
        }
    }
}
