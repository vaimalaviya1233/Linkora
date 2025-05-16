package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.data.local.dao.SnapshotDao
import com.sakethh.linkora.domain.model.Snapshot
import com.sakethh.linkora.domain.repository.local.SnapshotRepo

class SnapshotRepoImpl(private val snapshotDao: SnapshotDao) : SnapshotRepo {
    override suspend fun getASnapshot(id: Long): Snapshot {
        return snapshotDao.getASnapshot(id)
    }

    override suspend fun addASnapshot(snapshot: Snapshot): Long {
        return snapshotDao.addASnapshot(snapshot)
    }

    override suspend fun deleteASnapshot(id: Long) {
        snapshotDao.deleteASnapshot(id)
    }
}