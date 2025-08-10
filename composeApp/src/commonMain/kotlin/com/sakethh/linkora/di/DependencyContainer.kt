package com.sakethh.linkora.di

import com.sakethh.linkora.network.Network
import com.sakethh.linkora.network.repository.NetworkRepoImpl
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.data.ExportDataRepoImpl
import com.sakethh.linkora.data.ImportDataRepoImpl
import com.sakethh.linkora.data.LocalizationRepoImpl
import com.sakethh.linkora.data.local.repository.LocalFoldersRepoImpl
import com.sakethh.linkora.data.local.repository.LocalLinksRepoImpl
import com.sakethh.linkora.data.local.repository.LocalMultiActionRepoImpl
import com.sakethh.linkora.data.local.repository.LocalPanelsRepoImpl
import com.sakethh.linkora.data.local.repository.PendingSyncQueueRepoImpl
import com.sakethh.linkora.data.local.repository.PreferencesImpl
import com.sakethh.linkora.data.local.repository.SnapshotRepoImpl
import com.sakethh.linkora.data.remote.repository.GitHubReleasesRepoImpl
import com.sakethh.linkora.data.remote.repository.RemoteFoldersRepoImpl
import com.sakethh.linkora.data.remote.repository.RemoteLinksRepoImpl
import com.sakethh.linkora.data.remote.repository.RemoteMultiActionRepoImpl
import com.sakethh.linkora.data.remote.repository.RemotePanelsRepoImpl
import com.sakethh.linkora.data.remote.repository.RemoteSyncRepoImpl

object DependencyContainer {

    val preferencesRepo by lazy {
        PreferencesImpl(LinkoraSDKProvider.getInstance().dataStore)
    }

    val localizationRepo by lazy {
        LocalizationRepoImpl(
            standardClient = Network.standardClient, localizationServerURL = {
                AppPreferences.localizationServerURL.value
            }, localizationDao = LinkoraSDKProvider.getInstance().localDatabase.localizationDao
        )
    }

    val networkRepo by lazy {
        NetworkRepoImpl(syncServerClient = {
            Network.getSyncServerClient()
        })
    }

    val remoteFoldersRepo by lazy {
        RemoteFoldersRepoImpl(
            syncServerClient = {
                Network.getSyncServerClient()
            },
            baseUrl = { AppPreferences.serverBaseUrl.value },
            authToken = { AppPreferences.serverSecurityToken.value },
        )
    }

    val remoteSyncRepo by lazy {
        RemoteSyncRepoImpl(
            localFoldersRepo = localFoldersRepo,
            localLinksRepo = localLinksRepo,
            localPanelsRepo = localPanelsRepo,
            authToken = {
                AppPreferences.serverSecurityToken.value
            },
            baseUrl = {
                AppPreferences.serverBaseUrl.value
            },
            pendingSyncQueueRepo = pendingSyncQueueRepo,
            remoteFoldersRepo = remoteFoldersRepo,
            remoteLinksRepo = remoteLinksRepo,
            remotePanelsRepo = remotePanelsRepo,
            preferencesRepository = preferencesRepo,
            localMultiActionRepo = localMultiActionRepo,
            remoteMultiActionRepo = remoteMultiActionRepo,
            linksDao = LinkoraSDKProvider.getInstance().localDatabase.linksDao,
            foldersDao = LinkoraSDKProvider.getInstance().localDatabase.foldersDao,
            websocketScheme = {
                AppPreferences.WEB_SOCKET_SCHEME
            },
        )
    }
    val pendingSyncQueueRepo by lazy {
        PendingSyncQueueRepoImpl(LinkoraSDKProvider.getInstance().localDatabase.pendingSyncQueueDao)
    }
    val localFoldersRepo by lazy {
        LocalFoldersRepoImpl(
            foldersDao = LinkoraSDKProvider.getInstance().localDatabase.foldersDao,
            remoteFoldersRepo = remoteFoldersRepo,
            localLinksRepo = localLinksRepo,
            localPanelsRepo = localPanelsRepo,
            pendingSyncQueueRepo = pendingSyncQueueRepo,
            preferencesRepository = preferencesRepo
        )
    }

    val localLinksRepo by lazy {
        LocalLinksRepoImpl(
            linksDao = LinkoraSDKProvider.getInstance().localDatabase.linksDao,
            primaryUserAgent = {
                AppPreferences.primaryJsoupUserAgent.value
            },
            syncServerClient = {
                Network.getSyncServerClient()
            },
            remoteLinksRepo = remoteLinksRepo,
            foldersDao = LinkoraSDKProvider.getInstance().localDatabase.foldersDao,
            pendingSyncQueueRepo = pendingSyncQueueRepo,
            preferencesRepository = preferencesRepo,
            standardClient = Network.standardClient
        )
    }

    val remoteLinksRepo by lazy {
        RemoteLinksRepoImpl(syncServerClient = { Network.getSyncServerClient() }, baseUrl = {
            AppPreferences.serverBaseUrl.value
        }, authToken = {
            AppPreferences.serverSecurityToken.value
        })
    }

    val gitHubReleasesRepo by lazy {
        GitHubReleasesRepoImpl(standardClient = Network.standardClient)
    }

    val remotePanelsRepo by lazy {
        RemotePanelsRepoImpl(syncServerClient = { Network.getSyncServerClient() }, baseUrl = {
            AppPreferences.serverBaseUrl.value
        }, authToken = {
            AppPreferences.serverSecurityToken.value
        })
    }

    val localPanelsRepo by lazy {
        LocalPanelsRepoImpl(
            panelsDao = LinkoraSDKProvider.getInstance().localDatabase.panelsDao,
            remotePanelsRepo = remotePanelsRepo,
            foldersDao = LinkoraSDKProvider.getInstance().localDatabase.foldersDao,
            pendingSyncQueueRepo = pendingSyncQueueRepo,
            preferencesRepository = preferencesRepo
        )
    }

    val exportDataRepo by lazy {
        ExportDataRepoImpl(localLinksRepo, localFoldersRepo, localPanelsRepo)
    }

    val importDataRepo by lazy {
        ImportDataRepoImpl(
            localLinksRepo, localFoldersRepo, localPanelsRepo, canPushToServer = {
                AppPreferences.canPushToServer()
            }, remoteSyncRepo = remoteSyncRepo
        )
    }

    private val remoteMultiActionRepo by lazy {
        RemoteMultiActionRepoImpl(syncServerClient = { Network.getSyncServerClient() }, baseUrl = {
            AppPreferences.serverBaseUrl.value
        }, authToken = {
            AppPreferences.serverSecurityToken.value
        })
    }

    val localMultiActionRepo by lazy {
        LocalMultiActionRepoImpl(
            linksDao = LinkoraSDKProvider.getInstance().localDatabase.linksDao,
            foldersDao = LinkoraSDKProvider.getInstance().localDatabase.foldersDao,
            preferencesRepository = preferencesRepo,
            remoteMultiActionRepo = remoteMultiActionRepo,
            pendingSyncQueueRepo = pendingSyncQueueRepo,
            localFoldersRepo = localFoldersRepo
        )
    }

    val snapshotRepo by lazy {
        SnapshotRepoImpl(snapshotDao = LinkoraSDKProvider.getInstance().localDatabase.snapshotDao)
    }
}