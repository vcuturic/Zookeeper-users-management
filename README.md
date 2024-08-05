# Zookeeper-users-management

## Introduction

A simple Angular, Java, Zookeeper project. It shows and manages zookeeper nodes(as users), track statuses like online status, feed etc.

### Features

* **__User Logging In and Out__**: Users can log in and out seamlessly.

* **__Online Status Tracker__**: Users can see the current online status of other users.

* **__Real-time Communication__**: Zookeeper uses watcher mechanisms combined with WebSocket to automatically update user online status.

* **__Dynamic User Addition and Deletion__**: Users can add and delete other users dynamically.

* **__Messaging__**: Users have access to a shared chat where they can interact with each other.

* **__Simple User Interface__**: Users can easily track other users' activities, Zookeeper nodes, and more.

* **__Horizontal Scalability__**: When a new server instance is added to the cluster, it retrieves the latest data and starts serving requests.

* **__Data Consistency__**: All update/write requests are forwarded to the leader, who then broadcasts the data to all active servers.

* **__Reliable Data Reads__**: Data can be read from any of the replicas without inconsistencies.

* **__Cluster Information Storage__**: All servers in the cluster store ClusterInfo, including the leader and lists of /all_nodes and /live_nodes in the cluster.

* **__Dynamic Server Connection__**: In the event of a server failure, if a frontend instance was connected to that server, it automatically searches for the next available server.
