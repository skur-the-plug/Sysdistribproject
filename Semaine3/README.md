# Semaine 3 — Horloges Logiques (Lamport + Vector Clock)

## What’s implemented
- **Lamport clock**: increment on send/event, update on receive: `max(local, received) + 1`
- **Vector clock**: increment own component on send/event, merge on receive then increment own component
- Every message carries:
  - payload
  - senderId
  - lamportTs
  - vectorClock[]

## Folder structure
- `src/main/java/semaine3/` contains all Java sources.

## Default topology
If you do not provide a config file, it runs **3 nodes** on localhost:
- Node 0: 127.0.0.1:5000
- Node 1: 127.0.0.1:5001
- Node 2: 127.0.0.1:5002

## Optional config file
Create a text file (example `nodes.txt`) with:
```
127.0.0.1 5000
127.0.0.1 5001
127.0.0.1 5002
```

## Compile (from Semaine3 folder)
```
mkdir -p out
javac -d out src/main/java/semaine3/*.java
```

## Run (open 3 terminals)
Terminal 1:
```
java -cp out semaine3.Main 0
```
Terminal 2:
```
java -cp out semaine3.Main 1
```
Terminal 3:
```
java -cp out semaine3.Main 2
```

## Commands
Inside each node terminal:
- `send <destId> <message>`
- `broadcast <message>`
- `clocks`
- `quit`

Example:
```
send 1 hello
send 2 hi
broadcast test
clocks
```

## Notes for defense
- On each **SEND**, clocks update and are attached to the message.
- On each **RECEIVE**, clocks merge/update before the message is processed.
- Logs show both received timestamps and the node’s updated clocks.
