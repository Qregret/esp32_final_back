package com.example.smartlab.service.impl;

import com.example.smartlab.config.MqttProperties;
import com.example.smartlab.dto.EnvironmentReadingCreateRequest;
import com.example.smartlab.dto.SeatStateSyncRequest;
import com.example.smartlab.service.EnvironmentOperationService;
import com.example.smartlab.service.SeatOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class MqttInboundService {

    private static final Logger log = LoggerFactory.getLogger(MqttInboundService.class);

    private final MqttProperties properties;
    private final EnvironmentOperationService environmentOperationService;
    private final SeatOperationService seatOperationService;
    private final ObjectMapper objectMapper;

    private volatile MqttClient client;

    public MqttInboundService(MqttProperties properties,
                              EnvironmentOperationService environmentOperationService,
                              SeatOperationService seatOperationService,
                              ObjectMapper objectMapper) {
        this.properties = properties;
        this.environmentOperationService = environmentOperationService;
        this.seatOperationService = seatOperationService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void init() {
        connectIfNeeded();
    }

    @Scheduled(fixedDelayString = "${app.mqtt.reconnect-delay-ms:15000}")
    public void maintainConnection() {
        connectIfNeeded();
    }

    @PreDestroy
    public void shutdown() {
        closeClient();
    }

    private synchronized void connectIfNeeded() {
        if (!properties.isEnabled()) {
            return;
        }
        if (client != null && client.isConnected()) {
            return;
        }
        try {
            closeClient();
            client = new MqttClient(properties.getBrokerUrl(), properties.getClientId(), new MemoryPersistence());
            client.setCallback(new BackendMqttCallback());
            client.connect(buildConnectOptions());
            subscribeTopics();
            log.info("MQTT connected to {}", properties.getBrokerUrl());
        } catch (MqttException exception) {
            log.warn("MQTT connect failed: {}", exception.getMessage());
        }
    }

    private MqttConnectOptions buildConnectOptions() {
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(properties.isCleanSession());
        options.setAutomaticReconnect(properties.isAutomaticReconnect());
        options.setConnectionTimeout(properties.getConnectionTimeoutSeconds());
        options.setKeepAliveInterval(properties.getKeepAliveSeconds());
        if (properties.getUsername() != null && !properties.getUsername().isBlank()) {
            options.setUserName(properties.getUsername());
        }
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            options.setPassword(properties.getPassword().toCharArray());
        }
        return options;
    }

    private synchronized void subscribeTopics() throws MqttException {
        if (client == null || !client.isConnected()) {
            return;
        }
        client.subscribe(properties.getEnvironmentTopic(), properties.getQos());
        client.subscribe(properties.getSeatTopic(), properties.getQos());
        log.info("MQTT subscribed: {}, {}", properties.getEnvironmentTopic(), properties.getSeatTopic());
    }

    private void handleMessage(String topic, String payload) {
        try {
            if (properties.getEnvironmentTopic().equals(topic)) {
                EnvironmentReadingCreateRequest request =
                        objectMapper.readValue(payload, EnvironmentReadingCreateRequest.class);
                if (request.getSource() == null || request.getSource().isBlank()) {
                    request.setSource("mqtt");
                }
                environmentOperationService.createReading(request);
                log.info("MQTT environment reading consumed");
                return;
            }

            if (properties.getSeatTopic().equals(topic)) {
                SeatStateSyncRequest request = objectMapper.readValue(payload, SeatStateSyncRequest.class);
                if (request.getSource() == null || request.getSource().isBlank()) {
                    request.setSource("mqtt");
                }
                seatOperationService.syncStates(request);
                log.info("MQTT seat state consumed");
                return;
            }

            log.warn("MQTT message ignored, unknown topic={}", topic);
        } catch (Exception exception) {
            log.error("MQTT message handling failed, topic={}, payload={}", topic, payload, exception);
        }
    }

    private synchronized void closeClient() {
        if (client == null) {
            return;
        }
        try {
            if (client.isConnected()) {
                client.disconnect();
            }
            client.close();
        } catch (MqttException exception) {
            log.debug("MQTT client close failed: {}", exception.getMessage());
        } finally {
            client = null;
        }
    }

    private class BackendMqttCallback implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            if (reconnect) {
                try {
                    subscribeTopics();
                } catch (MqttException exception) {
                    log.warn("MQTT resubscribe failed: {}", exception.getMessage());
                }
            }
            log.info("MQTT connect complete, reconnect={}, uri={}", reconnect, serverURI);
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT connection lost: {}", cause == null ? "unknown" : cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            handleMessage(topic, new String(message.getPayload(), StandardCharsets.UTF_8));
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            // Backend acts as subscriber only.
        }
    }
}
