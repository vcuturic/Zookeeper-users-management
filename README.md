# Zookeeper-users-management

## Introduction

A simple Angular, Java, Zookeeper project. It shows and manages zookeeper nodes(as users), track statuses like online status, feed etc.

### Features

* **<ins>User Logging In and Out</ins>**: Users can log in and out seamlessly.

* **<ins>Online Status Tracker</ins>**: Users can see the current online status of other users.

* **<ins>Real-time Communication</ins>**: Zookeeper uses watcher mechanisms combined with WebSocket to automatically update user online status.

* **<ins>Dynamic User Addition and Deletion</ins>**: Users can add and delete other users dynamically.

* **<ins>Messaging</ins>**: Users have access to a shared chat where they can interact with each other.

* **<ins>Simple User Interface</ins>**: Users can easily track other users' activities, Zookeeper nodes, and more.

* **<ins>Horizontal Scalability</ins>**: When a new server instance is added to the cluster, it retrieves the latest data and starts serving requests.

* **<ins>Data Consistency</ins>**: All update/write requests are forwarded to the leader, who then broadcasts the data to all active servers.

* **<ins>Reliable Data Reads</ins>**: Data can be read from any of the replicas without inconsistencies.

* **<ins>Cluster Information Storage</ins>**: All servers in the cluster store ClusterInfo, including the leader and lists of /all_nodes and /live_nodes in the cluster.

* **<ins>Dynamic Server Connection</ins>**: In the event of a server failure, if a frontend instance was connected to that server, it automatically searches for the next available server.

