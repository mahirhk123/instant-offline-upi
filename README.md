# 🚀 Instant Offline UPI — Demo

A Spring Boot backend that demonstrates **offline UPI payments routed through a mesh network**.

Imagine you're in a basement with zero connectivity. You send ₹200 to your friend. Your phone encrypts the payment and broadcasts it to nearby phones. The packet hops device-to-device until someone walks outside, gets internet, and uploads it to the backend. The backend decrypts, validates, and settles the transaction.

This project simulates that entire system — including the mesh network — on a single machine.

---

# 📚 Table of Contents

- What this demo proves  
- How to run  
- Demo flow (step-by-step)  
- Architecture  
- Core problems & solutions  
- Tech stack  
- Project structure  
- API reference  
- What’s NOT production-ready  
- Limitations  
- Troubleshooting  

---

# ✅ What This Demo Proves

This system demonstrates three critical guarantees:

### 🔐 1. Secure transmission through untrusted devices
Intermediate devices **cannot read or modify** the payment.

👉 Achieved using:
- RSA (key encryption)
- AES-GCM (data encryption + tamper detection)

---

### 🔁 2. Exactly-once processing (Idempotency)

Even if the same packet reaches backend multiple times:

👉 It is processed **only once**

---

### 🛡️ 3. Replay & tamper protection

- Old packets → rejected  
- Modified packets → rejected  

---

# 🚀 How to Run

## Prerequisites

- Java 17+

---

## Run application

### Windows

mvnw.cmd spring-boot:run


### Mac/Linux

./mvnw spring-boot:run


---

## Open dashboard


http://localhost:8080/


---

## Stop server


Ctrl + C


---

# 🧪 Demo Flow (Step-by-Step)

## Step 1 — Send Payment

Fill:
- sender  
- receiver  
- amount  
- PIN  

Click:

📤 Inject into Mesh


### Backend:
- Creates PaymentInstruction  
- Encrypts using RSA + AES  
- Wraps into MeshPacket  
- Injects into device  

---

## Step 2 — Gossip (Packet Spread)

Click:

🔄 Run Gossip Round (2–3 times)


Packet spreads:


Alice → Stranger → Stranger → Bridge


---

## Step 3 — Bridge Upload

Click:

📡 Bridges Upload to Backend


Backend pipeline:

Hash packet
Check idempotency
Decrypt
Validate timestamp
Settle transaction


---

## Step 4 — Duplicate Protection

Click again:


📡 Bridges Upload


Result:


DUPLICATE_DROPPED


---

# 🏗️ Architecture


Sender (offline)
↓
Encrypt (RSA + AES)
↓
Mesh Devices (gossip)
↓
Bridge Node (internet)
↓
Spring Boot Backend
↓
Settlement + Database


---

# 🔥 Core Problems & Solutions

## Problem 1: Untrusted Devices

👉 Intermediate phones carry your data

### Solution:
Hybrid Encryption

- AES → encrypt data  
- RSA → encrypt AES key  
- AES-GCM → ensures integrity  

---

## Problem 2: Duplicate Transactions

👉 Same packet arrives multiple times

### Solution:
Idempotency using hash

seen.putIfAbsent(packetHash, now);


✔ Only first request is processed  
✔ Others are dropped  

---

## Problem 3: Replay Attacks

👉 Old packet reused

### Solution:
- timestamp validation  
- unique nonce  

---

# ⚙️ Tech Stack

- Java 17  
- Spring Boot 3  
- Spring Data JPA  
- H2 Database  
- Thymeleaf  
- REST APIs  

---

# 📂 Project Structure


src/main/java/com/instantupi/offline/
├── controller/
├── service/
├── crypto/
├── entity/
├── repository/
├── dto/
└── config/


---

# 📊 API Reference

| Method | Endpoint | Description |
|------|--------|------------|
| GET | `/` | Dashboard UI |
| GET | `/api/accounts` | View accounts |
| GET | `/api/transactions` | View transactions |
| POST | `/api/demo/send` | Create payment |
| POST | `/api/mesh/gossip` | Spread packets |
| POST | `/api/mesh/flush` | Upload to backend |
| POST | `/api/mesh/reset` | Reset system |
| POST | `/api/bridge/ingest` | Process packet |

---

# ⚠️ What’s NOT Production-Ready

| Demo | Production |
|------|-----------|
| H2 DB | PostgreSQL |
| In-memory cache | Redis |
| Generated RSA key | HSM / KMS |
| Simulated mesh | Real Bluetooth |
| No authentication | Secure auth (JWT / mTLS) |

---

# ⚠️ Honest Limitations

- Receiver cannot verify balance offline  
- Double spending possible before settlement  
- Bluetooth complexity not implemented  
- This is a **simulation system**  

---

# 🧠 Key Learnings

- Distributed system design  
- Secure communication  
- Idempotent APIs  
- Concurrency handling  
- Payment system architecture  

---

# 🚀 Future Improvements

- JWT Authentication  
- Redis integration  
- Cloud deployment  
- Real Bluetooth communication  

---

# ⭐ Final Thoughts

This project demonstrates a **real-world backend system design** combining:

- Security  
- Distributed systems  
- Payment processing  

---

# 🛠 Troubleshooting

### Java not found
Install JDK 17

---

### Port already in use
Change in application.properties:

server.port=8081


---

### First run slow
Dependencies downloading (~2–3 minutes)
