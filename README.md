# Zookeeper-users-management

## Introduction

A simple Angular, Java, Zookeeper project. It shows and manages zookeeper nodes(as users), track statuses like online status, feed etc.

### Features

* User logging in and out

* Online status tracker: Users can see current online status from other users.

* Realtime communication: Zookeeper uses watcher mechanisms in combination with websocket to automatically update user online status.

* Dynamicall user adition and deletion: Users can add another users and delete them.

* Messaging: Users have a shared chat where they can interact with each other.

* Simple user interface: Users can easily track other users acitivity, zookeeper nodes, etc.

* Horizontall scalability: When a new server instance is added to the cluster, it gets the latest data and starts serving requests.

* Data consistency: All update/write requests will be forwarded to the leader, and then the leader will broadcast data to all active servers.

* Data can be read from any of the replicas without any inconsistencies.

* All servers in the cluster store ClusterInfo — Who is the leader and lists of /all_nodes, /live_nodes in the cluster.

* Dynamicall connection to servers: In a event of server failure, if some frontend instance was connected to that server, it automatically searches for next available server.
