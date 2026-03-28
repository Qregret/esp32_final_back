package com.example.smartlab.service.impl;

import com.example.smartlab.config.MqttProperties;
import com.example.smartlab.entity.IotSeat;
import com.example.smartlab.service.MqttDeviceCommandService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
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
public class MqttDeviceCommandServiceImpl implements MqttDeviceCommandService {

    private static final Logger log = LoggerFactory.getLogger(MqttDeviceCommandServiceImpl.class);

    private final MqttProperties properties;

    private volatile MqttClient client;

    public MqttDeviceCommandServiceImpl(MqttProperties properties) {
        this.properties = properties;
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

    @Override
    public synchronized void publishSeatPowerCommand(IotSeat seat, boolean powerOn, String source, String remark) {
        if (!properties.isEnabled() || seat == null) {
            return;
        }
        connectIfNeeded();
        if (client == null || !client.isConnected()) {
            log.warn("MQTT seat command skipped: publisher not connected");
            return;
        }

        String seatCode = normalizeSeatCode(seat.getSeatCode());
        if (seatCode.isBlank()) {
            log.warn("MQTT seat command skipped: empty normalized seat code for seatId={}", seat.getId());
            return;
        }

        String topic = properties.getSeatCommandTopicPrefix() + "/" + seatCode;
        String payload = powerOn ? "on" : "off";

        try {
            MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
            message.setQos(properties.getQos());
            client.publish(topic, message);
            log.info("MQTT seat command published: topic={}, payload={}, source={}, remark={}",
                    topic, payload, defaultText(source, "backend"), defaultText(remark, ""));
        } catch (MqttException exception) {
            log.warn("MQTT seat command publish failed: {}", exception.getMessage());
        }
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
            client = new MqttClient(
                    properties.getBrokerUrl(),
                    properties.getClientId() + "-publisher",
                    new MemoryPersistence()
            );
            client.setCallback(new PublisherCallback());
            client.connect(buildConnectOptions());
            log.info("MQTT publisher connected to {}", properties.getBrokerUrl());
        } catch (MqttException exception) {
            log.warn("MQTT publisher connect failed: {}", exception.getMessage());
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
            log.debug("MQTT publisher close failed: {}", exception.getMessage());
        } finally {
            client = null;
        }
    }

    private String normalizeSeatCode(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("seat", "")
                .replace("-", "")
                .replace("_", "")
                .replace(" ", "");
        if (normalized.matches("\\d+")) {
            return String.format(Locale.ROOT, "%02d", Integer.parseInt(normalized));
        }
        return normalized;
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private class PublisherCallback implements MqttCallbackExtended {

        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            log.info("MQTT publisher connect complete, reconnect={}, uri={}", reconnect, serverURI);
        }

        @Override
        public void connectionLost(Throwable cause) {
            log.warn("MQTT publisher connection lost: {}", cause == null ? "unknown" : cause.getMessage());
        }

        @Override
        public void messageArrived(String topic, MqttMessage message) {
            // Publisher does not consume messages.
        }

        @Override
        public void deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken token) {
            // No-op.
        }
    }
}
