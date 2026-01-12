# Projet Systèmes Distribués — Ordonnancement Causal

## 📌 Description
Ce projet est une implémentation pédagogique d’un **mini-système distribué** en Java.
Il met en œuvre la communication réseau entre plusieurs nœuds **sans mémoire partagée**, en garantissant la **livraison causale des messages** à l’aide des **horloges logiques de Lamport** et des **horloges vectorielles**.

Le projet est structuré par étapes (Semaine 1 à 5), mais certaines fonctionnalités avancées
(multicast causal, buffer, CLI) sont déjà intégrées dans le dossier **Semaine3**.

---

## 🎯 Objectifs pédagogiques
- Comprendre le fonctionnement d’un système distribué
- Implémenter une communication réseau via **TCP sockets**
- Manipuler le **multithreading** sans exclusion mutuelle complexe
- Implémenter des **horloges logiques**
- Garantir l’**ordre causal** des messages
- Concevoir un **buffer causal** pour les messages arrivés trop tôt

---

## 🧠 Concepts clés abordés
- Systèmes distribués (absence de mémoire partagée)
- Communication réseau TCP
- Threads Java
- Horloge de Lamport
- Horloge vectorielle (Vector Clock)
- Ordre partiel et causalité
- Multicast causal simplifié

---

## 🏗️ Architecture générale
- Plusieurs **nœuds indépendants** (processus Java)
- Communication **point-à-point TCP**
- Chaque nœud contient :
  - un serveur TCP (réception)
  - des clients TCP (envoi)
  - plusieurs threads (réception, envoi, traitement)
- Ordonnancement causal assuré par **Vector Clock**
- Messages non livrables stockés dans un **buffer causal**

---

## 📁 Structure du projet
Sysdistribproject/
│
├── Semaine1/
│ └── Communication réseau (sockets TCP)
│
├── Semaine2/
│ └── Threads + BlockingQueue
│
├── Semaine3/
│ └── Horloges logiques + Multicast causal
│
│ ├── Node.java # Cœur du système (clocks, buffer, broadcast)
│ ├── Message.java # Structure des messages
│ ├── LamportClock.java # Horloge de Lamport
│ ├── VectorClock.java # Horloge vectorielle
│ ├── NetServer.java # Serveur TCP
│ ├── NetClient.java # Client TCP
│ ├── ReceiverThread.java # Thread de réception
│ ├── SenderThread.java # Interface CLI
│ ├── Main.java # Lancement d’un nœud
│ └── Config.java # Configuration des nœuds
│
└── README.md


## ⏱️ Fonctionnement global
1. Chaque nœud démarre un serveur TCP
2. Les messages sont envoyés avec :
   - un timestamp Lamport
   - un Vector Clock
3. À la réception :
   - les horloges sont mises à jour
   - le message est livré **ou** placé dans le buffer
4. Un thread tente périodiquement de vider le buffer
5. La livraison respecte strictement l’ordre causal

---

## 📬 Règle de livraison causale (`canDeliver`)
Un message `m` envoyé par le nœud `s` est livrable si :

- `VCm[s] == VClocal[s] + 1`
- Pour tout `i ≠ s` : `VCm[i] ≤ VClocal[i]`

Sinon, le message est placé dans le buffer.

---

## 💻 Interface CLI
Le projet inclut une interface console permettant de tester le système :

Commandes disponibles :
- `send <destId> <message>`
- `broadcast <message>`
- `clocks`
- `quit`

---

## ▶️ Exécution
1. Compiler le projet
2. Lancer plusieurs instances avec des identifiants différents :
```bash
java Main 0
java Main 1
java Main 2
