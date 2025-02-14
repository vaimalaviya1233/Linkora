Starting Linkora v0.11.0, the app supports connection to [sync-server](https://github.com/LinkoraApp/sync-server) for syncing your data (or just to store your data on cloud).

### 1. Setup server and its database

[sync-server](https://github.com/LinkoraApp/sync-server) is a self-hostable solution that connects to an SQL-based database of your choice for storing and syncing data.

Go through the [README of [`sync-server`](https://github.com/LinkoraApp/sync-server)](https://github.com/LinkoraApp/sync-server/blob/master/README.md) to learn how to set up the server, then continue with the further instructions here.

### 2. Connecting to the server from the app

Now, follow the steps using the images below:
- Navigate to **Settings -> Data**.
- **Step 1 (Image 1)**: Click on `Connect to a Linkora Server`.
- **Step 2 (Image 2, 3, 4)**: Provide the required values and click `Test Server Availability`.
- **Step 3 (Image 5)**: If the server is reachable, you'll see a few more options to configure how the app should interact with the server. Click on `Use This Connection`, and Linkora will handle everything else.
- **Step 4 (Image 6)**:
    - A new option appears to initiate a manual sync in case Linkora couldn't connect to the server on app launch.
    - By clicking `Manage Connected Server`, you can view the sync type, edit the connection, or delete it, which will remove the link between the app and server.

| Image 1                                         | Image 2                                         | Image 3                                         |
|-------------------------------------------------|-------------------------------------------------|-------------------------------------------------|
| ![](/assets/docs/server_connection_setup/1.png) | ![](/assets/docs/server_connection_setup/2.png) | ![](/assets/docs/server_connection_setup/3.png) |

| Image 4                                          | Image 5                                         | Image 6                                         |
|--------------------------------------------------|-------------------------------------------------|-------------------------------------------------|
| ![](/assets/docs/server_connection_setup/3_.png) | ![](/assets/docs/server_connection_setup/4.png) | ![](/assets/docs/server_connection_setup/5.png) |

If anything isn't working as expected, feel free to open an issue.
