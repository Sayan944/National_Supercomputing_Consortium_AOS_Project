# National Supercomputing Consortium (NSC)
### Distributed Mutual Exclusion using Raymond's Algorithm

A Java-based distributed systems project that demonstrates distributed mutual exclusion among multiple universities using **Raymond's Tree-Based Token Algorithm** and global-state recording using the **Chandy–Lamport Distributed Snapshot Algorithm**.

The project uses real TCP socket communication, with each university running as an independent Java process.

## 1. Features

- Distributed mutual exclusion using Raymond's algorithm.
- Real TCP socket communication between university nodes.
- Configurable university names, ports and network topology.
- Dynamic token transfer and request queue management.
- Physical network tree visualization.
- Dynamic logical holder tree visualization.
- Distributed snapshot implementation using Chandy–Lamport algorithm.
- Command-line interface for each university.

## 2. Technologies Used

- Java
- TCP Socket Programming
- Multithreading
- Java Collections Framework
- DataInputStream and DataOutputStream

## 3. Project Structure

```text
AOS_Project_2026_NSC/
│
├── config/          # Network configuration
├── messaging/       # Message types and serialization
├── socket/          # TCP communication
├── raymond/         # Raymond's algorithm
├── university/      # University node management
├── visualization/   # Physical and logical trees
├── snapshot/        # Distributed snapshot
│
├── Main.java
├── raymond.conf
├── README.md


```

## 4. Network Configuration

The `raymond.conf` file defines the number of universities, root node, initial token holder, ports and physical network edges.

Sample configuration:

```text
5 // total universities that makes up the tree
0 // root of the tree
0 // initial token holder of the tree
CU 5001
BU 5002
KU 5003
JU 5004
JNU 5005
0 1 
0 2
1 3
1 4
```

Sample physical topology:

```text
          CU
         /  \
       BU    KU
      /  \
     JU   JNU
```

The configuration can be modified to change the network topology and initial token holder.

## 5. How to Run

**Prerequisites:** JDK installed with `java` and `javac` available in PATH.

### Step 1: Clone the repository

```bash
git clone <YOUR_REPOSITORY_URL>
cd AOS_Project_2026_NSC
```

### Step 2: Compile the project

```cmd
mkdir out
```

```cmd
javac -d out Main.java config\*.java messaging\*.java socket\*.java raymond\*.java university\*.java visualization\*.java snapshot\*.java
```

### Step 3: Start university processes

Open five separate Command Prompt windows and run one command in each:

```cmd
java -cp out Main CU
```

```cmd
java -cp out Main BU
```

```cmd
java -cp out Main KU
```

```cmd
java -cp out Main JU
```

```cmd
java -cp out Main JNU
```

Each university operates as a separate JVM process and communicates with its physical neighbours through TCP sockets.

## 6. Available Commands

| Command | Function |
|---|---|
| `request` | Request access to the critical section |
| `release` | Release the critical section |
| `status` | Display the current node status |
| `tree` | Display physical and logical trees |
| `snapshot` | Initiate a distributed snapshot |
| `help` | Display available commands |
| `exit` | Exit the university process |

## 7. Algorithms

**Raymond's Tree-Based Mutual Exclusion**

Uses a unique token and logical holder tree to coordinate access to the critical section. Requests are queued and the token is transferred automatically when pending requests exist.

**Chandy–Lamport Distributed Snapshot**

Uses MARKER messages to record local states and channel states and collect a global snapshot of the distributed system.

## 8. Testing

The project was tested with five independent university processes running on the same machine.

Tests performed:

- Successful compilation and startup of all five nodes.
- TCP connection establishment.
- Physical and logical tree visualization.
- Token requests from different universities.
- Critical-section entry and release.
- Multiple pending requests and automatic token transfer.
- Distributed snapshot collection.

**Testing observations:**
- Raymond's mutual exclusion and queued token transfers worked successfully in the tested scenarios.
- A snapshot initiated by CU successfully collected all five university reports, although one test showed a delay of approximately three minutes.
- Snapshot initiation from universities other than CU remains an improvement area.

## 9. Future Improvements

- Support snapshot initiation from every university.
- Improve snapshot collection performance.
- Enhance fault tolerance and connection recovery.
- Add graphical real-time network visualization.
- Perform extensive simultaneous-request and multi-machine testing.

## 10. Academic Project

**Course:** Advanced Operating Systems (AOS)

**Project:** National Supercomputing Consortium – Distributed Mutual Exclusion System

**Language:** Java

**Core Algorithms:** Raymond's Algorithm and Chandy–Lamport Distributed Snapshot Algorithm.

Developed for academic learning and demonstration of distributed systems concepts.