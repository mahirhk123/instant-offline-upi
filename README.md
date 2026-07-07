# 💳 Instant Offline UPI Payment System

> A secure offline UPI payment simulation built using **Spring Boot**, **React**, and **MySQL** with **Bluetooth Mesh Network Simulation**, **Hybrid Encryption**, and **Idempotent Transaction Processing**.

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen" />
  <img src="https://img.shields.io/badge/React-19-blue" />
  <img src="https://img.shields.io/badge/MySQL-8-blue" />
  <img src="https://img.shields.io/badge/Vite-Latest-purple" />
  <img src="https://img.shields.io/badge/Status-Completed-success" />
</p>

---

# 📖 Project Overview

Instant Offline UPI Payment System is a full-stack application that demonstrates how secure digital payments can be performed even when internet connectivity is unavailable.

Instead of directly communicating with the banking server, payment requests are encrypted and converted into secure packets that travel through a simulated Bluetooth Mesh Network. Once a bridge device with internet connectivity receives the packet, it forwards it to the backend for verification, decryption, and transaction settlement.

This project simulates the future possibility of offline digital payments while emphasizing security, reliability, and distributed communication.

---

# ✨ Features

- 🔐 Hybrid Encryption (RSA + AES)
- 📡 Bluetooth Mesh Network Simulation
- 📦 Secure Packet-Based Payment Transfer
- 🔁 Gossip Protocol for Packet Propagation
- 🌐 Bridge Node for Internet Connectivity
- 💰 Account Balance Management
- 📊 Interactive Dashboard
- 📈 Live Transaction Ledger
- 📋 Activity Log
- 📶 Network Statistics
- 🔄 Mesh Reset & Gossip Simulation
- 🛡️ Idempotent Transaction Processing
- ⚡ RESTful APIs
- 📱 Responsive User Interface

---

# 🏗️ System Architecture

```
               +----------------------+
               |   React Dashboard    |
               +----------+-----------+
                          |
                     REST APIs
                          |
               +----------v-----------+
               |   Spring Boot API    |
               +----------+-----------+
                          |
             -----------------------------
             |                           |
      Payment Processing          Mesh Simulation
             |                           |
      Transaction Service        Gossip Protocol
             |                           |
         MySQL Database         Virtual Devices
```

---

# ⚙️ Technology Stack

## Backend

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Hibernate
- Maven

## Frontend

- React
- Vite
- Axios
- React Icons
- CSS3

## Database

- MySQL

## Security

- RSA-2048
- AES-256-GCM
- Hybrid Encryption

---

# 📂 Project Structure

```
Instant-Offline-UPI
│
├── backend
│   ├── src
│   ├── pom.xml
│   └── ...
│
├── frontend
│   ├── public
│   ├── src
│   │
│   ├── components
│   ├── pages
│   ├── services
│   └── styles
│
├── README.md
└── .gitignore
```

---

# 🔄 Offline Payment Workflow

```
User

   │

   ▼

Enter Payment Details

   │

   ▼

Packet Creation

   │

   ▼

AES Encryption

   │

   ▼

RSA Encrypt AES Key

   │

   ▼

Inject Packet into Mesh

   │

   ▼

Bluetooth Mesh Gossip

   │

   ▼

Bridge Device

   │

   ▼

Backend Processing

   │

   ▼

Decrypt Packet

   │

   ▼

Verify Transaction

   │

   ▼

Update Database

   │

   ▼

Settlement Complete
```

---

# 📊 Dashboard Features

- Dashboard Summary Cards
- Network Statistics
- Mesh Device Monitoring
- Payment Injection Form
- Account Balance Table
- Transaction Ledger
- Activity Log

---

# 🗄️ Database Tables

### Account

| Field | Type |
|-------|------|
| VPA | String |
| Holder Name | String |
| Balance | Decimal |
| Version | Long |

---

### Transaction

| Field | Type |
|-------|------|
| ID | Long |
| Sender VPA | String |
| Receiver VPA | String |
| Amount | Decimal |
| Status | String |
| Timestamp | DateTime |

---

# 🔐 Security Features

- Hybrid Encryption
- RSA Key Exchange
- AES Payload Encryption
- Idempotent Packet Processing
- Optimistic Locking
- Secure Transaction Settlement

---

# 🚀 REST API Endpoints

| Method | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/accounts` | Fetch all accounts |
| GET | `/api/transactions` | Latest transactions |
| GET | `/api/transactions/count` | Total transaction count |
| POST | `/api/demo/send` | Inject payment packet |
| GET | `/api/mesh/state` | Mesh network state |
| POST | `/api/mesh/gossip` | Run gossip protocol |
| POST | `/api/mesh/flush` | Upload packets via bridge |
| POST | `/api/mesh/reset` | Reset mesh network |

---

# 💻 Installation

## Clone Repository

```bash
git clone https://github.com/mahirhk123/Instant-Offline-UPI.git
```

---

## Backend

```bash
cd backend

mvn clean install

mvn spring-boot:run
```

Backend runs on

```
http://localhost:8080
```

---

## Frontend

```bash
cd frontend

npm install

npm run dev
```

Frontend runs on

```
http://localhost:5173
```

---

# 📸 Screenshots

> Add screenshots here.

Example:

```
screenshots/

dashboard.png

mesh-network.png

transactions.png

mobile-view.png
```

---

# 🎯 Future Enhancements

- Real Bluetooth Communication
- NFC Integration
- QR Code Based Offline Payments
- Digital Signature Verification
- JWT Authentication
- Multi-Bridge Synchronization
- Redis Packet Cache
- Docker Deployment
- Kubernetes Deployment
- Cloud Hosting

---

# 👨‍💻 Developer

**Mahir Hussain**

B.Tech Computer Science & Engineering


GitHub:
https://github.com/mahirhk123

---

# 📄 License

This project is developed for educational and academic purposes.

---

# ⭐ If you found this project useful, please consider giving it a star.