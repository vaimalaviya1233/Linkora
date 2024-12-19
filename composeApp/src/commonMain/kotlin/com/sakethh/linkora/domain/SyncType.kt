package com.sakethh.linkora.domain

enum class SyncType {
    ClientToServer {
        override fun asUIString(): String {
            return "Client To Server"
        }

        override fun description(): String {
            return "Client changes are sent to the server, but client is not updated with server changes."
        }
    },
    ServerToClient {
        override fun asUIString(): String {
            return "Server To Client"
        }

        override fun description(): String {
            return "Server changes are sent to the client, but server is not updated with client changes."
        }
    },
    TwoWay {
        override fun asUIString(): String {
            return "Two-Way Sync"
        }

        override fun description(): String {
            return "Changes are sent both ways: client updates the server, and server updates the client."
        }
    };

    abstract fun asUIString(): String
    abstract fun description(): String
}