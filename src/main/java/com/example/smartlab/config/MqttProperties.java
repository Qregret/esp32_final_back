package com.example.smartlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.mqtt")
public class MqttProperties {

    private boolean enabled;
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "smartlab-backend";
    private String username;
    private String password;
    private boolean cleanSession = true;
    private boolean automaticReconnect = true;
    private int connectionTimeoutSeconds = 10;
    private int keepAliveSeconds = 20;
    private int qos = 1;
    private long reconnectDelayMs = 15000;
    private String environmentTopic = "smartlab/environment/readings";
    private String seatTopic = "smartlab/seats/state";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isCleanSession() {
        return cleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        this.cleanSession = cleanSession;
    }

    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    public void setAutomaticReconnect(boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }

    public int getKeepAliveSeconds() {
        return keepAliveSeconds;
    }

    public void setKeepAliveSeconds(int keepAliveSeconds) {
        this.keepAliveSeconds = keepAliveSeconds;
    }

    public int getQos() {
        return qos;
    }

    public void setQos(int qos) {
        this.qos = qos;
    }

    public long getReconnectDelayMs() {
        return reconnectDelayMs;
    }

    public void setReconnectDelayMs(long reconnectDelayMs) {
        this.reconnectDelayMs = reconnectDelayMs;
    }

    public String getEnvironmentTopic() {
        return environmentTopic;
    }

    public void setEnvironmentTopic(String environmentTopic) {
        this.environmentTopic = environmentTopic;
    }

    public String getSeatTopic() {
        return seatTopic;
    }

    public void setSeatTopic(String seatTopic) {
        this.seatTopic = seatTopic;
    }
}
