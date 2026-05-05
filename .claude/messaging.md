# Messaging (RabbitMQ)

## Rules

- Messaging = adapter
- Use events, not commands when possible

---

## Flow

1. Upload file
2. Send message
3. Worker processes

---

## Message Content

- importId
- fileLocation

---

## Reliability

- Retry mechanism
- Dead letter queue