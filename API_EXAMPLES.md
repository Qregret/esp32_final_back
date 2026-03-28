# SmartLab API Examples

Base URL:

```text
http://localhost:8080
```

## 1. Dashboard overview

```http
GET /api/dashboard/overview
```

Example response:

```json
{
  "serverTime": "2026-03-24T10:15:30",
  "auth": {
    "authEventId": 1,
    "flowState": "success",
    "authResult": "granted",
    "similarity": 98.5,
    "cameraStatus": "recognized",
    "statusText": "Auth granted. Starting seat linkage."
  },
  "currentAuth": {
    "authEventId": 1,
    "flowState": "success",
    "authResult": "granted",
    "similarity": 98.5
  },
  "seats": [],
  "currentSeats": [],
  "environment": {
    "id": 8,
    "sensorDeviceId": 4,
    "temperatureC": 25.9,
    "humidityPercent": 43.5
  },
  "latestEnvironment": {
    "id": 8,
    "sensorDeviceId": 4,
    "temperatureC": 25.9,
    "humidityPercent": 43.5
  },
  "logs": [],
  "recentLogs": []
}
```

## 2. Seat power on

```http
POST /api/seats/1/power-on
Content-Type: application/json
```

```json
{
  "userId": 2,
  "source": "manual_control",
  "remark": "operator power on"
}
```

## 3. Seat power off

```http
POST /api/seats/1/power-off
Content-Type: application/json
```

```json
{
  "userId": 2,
  "source": "manual_control",
  "remark": "operator power off"
}
```

## 4. Start seat session

```http
POST /api/seat-sessions/start
Content-Type: application/json
```

```json
{
  "seatId": 2,
  "userId": 2,
  "sessionSource": "manual"
}
```

## 5. Finish seat session

```http
POST /api/seat-sessions/3/finish
Content-Type: application/json
```

```json
{
  "actionSource": "manual",
  "remark": "user checkout"
}
```

## 6. Create auth event directly

```http
POST /api/auth-events
Content-Type: application/json
```

```json
{
  "gatewayDeviceId": 1,
  "cameraDeviceId": 2,
  "rfidDeviceId": 3,
  "seatId": 2,
  "rfidUid": "4A B2 19 CF",
  "similarity": 98.5,
  "flowState": "success",
  "authResult": "granted",
  "cameraStatus": "recognized",
  "statusText": "Auth granted",
  "snapshotUrl": "/snapshots/auth_0001.jpg",
  "sessionSource": "auth_event"
}
```

## 7. Auth workflow split requests

### RFID scan

```http
POST /api/auth-events/rfid-scan
Content-Type: application/json
```

```json
{
  "gatewayDeviceId": 1,
  "rfidDeviceId": 3,
  "seatId": 2,
  "rfidUid": "4A B2 19 CF"
}
```

### Camera upload

```http
POST /api/auth-events/camera-upload
Content-Type: application/json
```

```json
{
  "authEventId": 1,
  "cameraDeviceId": 2,
  "snapshotUrl": "/snapshots/auth_0001.jpg",
  "cameraStatus": "image_uploaded"
}
```

### AI result

```http
POST /api/auth-events/ai-result
Content-Type: application/json
```

```json
{
  "authEventId": 1,
  "similarity": 98.5,
  "cameraStatus": "recognized"
}
```

### Finalize

```http
POST /api/auth-events/finalize
Content-Type: application/json
```

```json
{
  "authEventId": 1,
  "seatId": 2,
  "rfidUid": "4A B2 19 CF",
  "similarity": 98.5,
  "flowState": "success",
  "authResult": "granted"
}
```

## 8. Environment reading report

```http
POST /api/environment/readings
Content-Type: application/json
```

```json
{
  "sensorDeviceId": 4,
  "temperatureC": 25.9,
  "humidityPercent": 43.5,
  "tempGauge": 65,
  "humidityGauge": 44,
  "batteryPercent": 91,
  "source": "mqtt"
}
```

## 9. Recent logs

```http
GET /api/system-logs/recent?limit=20
```

## 10. Current seats

```http
GET /api/seats/current
```

## 11. Latest environment

```http
GET /api/environment/latest
```

## 12. SSE subscription

```http
GET /api/stream/events
Accept: text/event-stream
```

Event names currently include:

```text
auth-event-updated
seat-powered-on
seat-powered-off
seat-session-started
seat-session-finished
environment-updated
```

## 13. WebSocket subscription

```text
GET /ws/stream/events
```

The server sends JSON frames like:

```json
{
  "eventType": "seat-powered-on",
  "payload": {
    "id": 2,
    "seatCode": "02",
    "powerStatus": "on",
    "seatStatus": "occupied"
  },
  "timestamp": "2026-03-28T15:10:00"
}
```

## 14. MQTT integration

Frontend still reads data from the existing HTTP API. MQTT is only used as the device uplink.

Default topics:

```text
smartlab/environment/readings
smartlab/seats/state
```

### Environment payload

Publish to `smartlab/environment/readings`:

```json
{
  "sensorDeviceId": 4,
  "temperatureC": 25.9,
  "humidityPercent": 43.5,
  "tempGauge": 65,
  "humidityGauge": 44,
  "batteryPercent": 91,
  "source": "esp32c3"
}
```

### Seat state payload

Publish to `smartlab/seats/state`:

```json
{
  "deviceCode": "esp32c3-seat-node-01",
  "source": "esp32c3",
  "remark": "button update",
  "seats": [
    { "seatCode": "02", "occupied": true },
    { "seatCode": "03", "occupied": false },
    { "seatCode": "04", "occupied": true },
    { "seatCode": "05", "occupied": false }
  ]
}
```

Seat items also accept:

```json
{ "seatId": 2, "powerOn": true }
```
