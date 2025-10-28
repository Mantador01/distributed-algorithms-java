# ⚙️ Distributed Algorithms – Java Simulation

This repository contains implementations and experiments for the **Distributed Algorithms** course completed during the **Master 1 in Computer Science** program at **Université Claude Bernard Lyon 1**.

The goal of these labs is to simulate **fundamental distributed protocols** — including leader election, spanning tree construction, failure detection, and coordination — using **Java**.  
Each node operates independently, communicating through message passing, and the experiments analyze communication cost, synchronization, and fault-tolerance behavior.

---

## 🧠 Overview

Distributed algorithms are key to coordination in decentralized systems where no global knowledge exists.  
These labs explore several classic algorithms in asynchronous or partially synchronous environments:

| Theme | Protocol | Concept Demonstrated |
|--------|-----------|----------------------|
| TP2 | Leader Election (Ring topology) | Unique ID propagation and election convergence |
| TP3 | Failure Detection (Heartbeat-based) | Reliable detection vs. false suspicions |
| TP4 | Diffusion and Coordination | Spanning tree and message propagation |
| TP5 | Fault-tolerant coordination | Node failures and recovery (Itai-Rodeh algorithm) |

Each experiment measures the **number of messages**, **convergence time**, and **correctness** of the protocol.

---

## 🧩 Project Structure

| File | Description |
|------|--------------|
| `NoeudArbreCouvrant.java` | Node implementing a **Spanning Tree** algorithm for diffusion and message aggregation. |
| `NoeudDiffusion.java` | Node that performs **broadcast** or **diffusion** to all reachable nodes. |
| `NoeudCoordinateur.java` | Elects and manages the **coordinator node** in distributed systems. |
| `NoeudFaillible.java` | Models a **fault-prone node** that can crash and recover. |
| `NoeudDetecteur.java` | Implements a **failure detector** based on periodic heartbeats (HB). |
| `NoeudItaiRodeh.java` | Implements the **Itai–Rodeh randomized leader election** algorithm. |
| `NoeudITAII.java` | Variant of the Itai-Rodeh algorithm for synchronous and asynchronous settings. |
| `NoeudTest.java` | Entry point for running distributed simulations and collecting statistics. |
| `NoteTP2.txt` | Theoretical background and experimental results for the **leader election** problem. |
| `NoteTP3.txt` | Observations for the **heartbeat-based failure detector** and latency effects. |

---

## 🧮 TP2 – Leader Election (Ring Topology)

### Description
Implements a **unidirectional ring election algorithm** where each node starts as a candidate, sends its own ID, and relays any larger ID it receives.  
Eventually, the node with the **maximum ID** receives its own message and declares itself the leader.

### Hypotheses
- Unique, totally ordered node identifiers  
- Reliable FIFO channels  
- No process or link failures  
- Partial asynchrony (bounded but variable delays)

### Behavior
- Each node initially sends its ID to its successor  
- Nodes forward only IDs greater than their own  
- The process terminates when the largest ID completes a full cycle  

### Experimental Results (100 runs)
| Metric | Mean | Min | Max |
|---------|------|-----|-----|
| Messages exchanged | 28.45 | 21 | 40 |

**Observation:**  
More initial candidates → more messages exchanged.  
Example trends:  
- 2 candidates → ~24.8 messages  
- 5 candidates → ~29.3 messages  
- 8 candidates → ~32.0 messages  

➡️ The algorithm scales linearly with the number of candidates, reflecting increasing message overhead.

---

## 🧩 TP3 – Failure Detection via Heartbeats

### Description
Simulates a **heartbeat-based failure detector** where nodes periodically send “heartbeat” (HB) messages to indicate liveness.  
Nodes suspect a peer after a fixed timeout without receiving a heartbeat.

### Hypotheses
- Each node has a unique ID and known neighbor  
- Asynchronous network (variable delays)  
- No message loss but unpredictable delay up to 5 ticks

### Observations
- In **fully synchronous mode**, no false suspicions occur: messages always arrive on time.  
- In **partially asynchronous mode**, delayed messages trigger **temporary false suspicions** (oscillation between “trusted” and “suspected”).

**Phenomenon:**  
A node can be wrongly suspected as failed if a heartbeat arrives late, then reinstated when the message finally arrives.  
This simulates real-world distributed uncertainty (distinguishing delay vs. crash).

---

## 🧩 Additional Algorithms

### 🌳 Spanning Tree (Arbre Couvrant)
Constructs a minimal message tree connecting all processes.  
Used as the base for **diffusion** (broadcast) and **convergecast** operations.

### 📡 Diffusion (Broadcast)
Each node forwards a message to its children using the spanning tree.  
Ensures every node receives the broadcast exactly once.

### 🧩 Itai–Rodeh Leader Election
A **probabilistic leader election algorithm** for asynchronous systems where nodes randomly delay transmissions to reduce collisions.  
Guarantees eventual convergence even under uncertain timing.

---

## 📊 Experimental Insights

- Message complexity grows linearly with the number of initial candidates.  
- Heartbeat-based detection demonstrates trade-offs between **speed** and **accuracy**.  
- Randomized leader election improves resilience to delays but increases average runtime.  
- Spanning tree diffusion ensures reliability and avoids message redundancy.  

---

## 🧰 Technologies Used

| Category | Tools |
|-----------|--------|
| Language | Java 17+ |
| IDE | IntelliJ IDEA / Eclipse |
| Simulation | Multi-threaded Java processes |
| Data Analysis | CSV, manual stats, textual logs |
| OS | Linux / Windows |

---

## 🚀 How to Run

### Compile
```bash
javac *.java
```

### Run an example
```bash
java NoeudTest
```
This launches a distributed simulation with configurable topology and number of nodes.

---

## 👨‍💻 Author

**Alexandre COTTIER**  
Master’s student in Computer Science – *Image, Développement et Technologie 3D (ID3D)*  
Université Claude Bernard Lyon 1  

📍 Lyon, France  
🔗 [GitHub](https://github.com/Mantador01) · [LinkedIn](https://www.linkedin.com/in/alexandre-cottier-72ab20227/)

---

## 📜 License

This repository is provided for **academic and educational use**.  
You may reuse and adapt the algorithms with proper attribution.

---

> *“In distributed systems, agreement is not about speed — it’s about surviving uncertainty.”*
