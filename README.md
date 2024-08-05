# Zookeeper-users-management

## Introduction

A simple Angular, Java, Zookeeper project. It shows and manages zookeeper nodes(as users), track statuses like online status, feed etc.

### Features

* **User Logging In and Out**: Users can log in and out seamlessly.

* **Online Status Tracker**: Users can see the current online status of other users.

* **Real-time Communication**: Zookeeper uses watcher mechanisms combined with WebSocket to automatically update user online status.

* **Dynamic User Addition and Deletion**: Users can add and delete other users dynamically.

* **Messaging**: Users have access to a shared chat where they can interact with each other.

* **Simple User Interface**: Users can easily track other users' activities, Zookeeper nodes, and more.

* **Horizontal Scalability**: When a new server instance is added to the cluster, it retrieves the latest data and starts serving requests.

* **Data Consistency**: All update/write requests are forwarded to the leader, who then broadcasts the data to all active servers.

* **Reliable Data Reads**: Data can be read from any of the replicas without inconsistencies.

* **Cluster Information Storage**: All servers in the cluster store ClusterInfo, including the leader and lists of /all_nodes and /live_nodes in the cluster.

* **Dynamic Server Connection**: In the event of a server failure, if a frontend instance was connected to that server, it automatically searches for the next available server.
