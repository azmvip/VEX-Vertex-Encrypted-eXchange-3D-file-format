package com.example.data.repository

import com.example.data.db.MockRuleDao
import com.example.data.db.RequestDao
import com.example.data.db.SessionDao
import com.example.data.model.InterceptedRequestEntity
import com.example.data.model.MockRuleEntity
import com.example.data.model.SavedSessionEntity
import kotlinx.coroutines.flow.Flow

class RequestRepository(
    private val requestDao: RequestDao,
    private val mockRuleDao: MockRuleDao,
    private val sessionDao: SessionDao
) {
    val allRequests: Flow<List<InterceptedRequestEntity>> = requestDao.getAllRequests()
    val allRules: Flow<List<MockRuleEntity>> = mockRuleDao.getAllRules()
    val allSessions: Flow<List<SavedSessionEntity>> = sessionDao.getAllSessions()
    val bookmarkedRequests: Flow<List<InterceptedRequestEntity>> = requestDao.getBookmarkedRequests()

    fun getRequestsForSession(sessionId: String): Flow<List<InterceptedRequestEntity>> {
        return requestDao.getRequestsForSession(sessionId)
    }

    suspend fun insertRequest(request: InterceptedRequestEntity): Long {
        return requestDao.insertRequest(request)
    }

    suspend fun updateRequest(request: InterceptedRequestEntity) {
        requestDao.updateRequest(request)
    }

    suspend fun toggleBookmark(id: Long, isBookmarked: Boolean) {
        requestDao.setBookmarked(id, isBookmarked)
    }

    suspend fun deleteRequest(id: Long) {
        requestDao.deleteById(id)
    }

    suspend fun clearAllRequests() {
        requestDao.clearAll()
    }

    suspend fun getActiveRulesSync(): List<MockRuleEntity> {
        return mockRuleDao.getActiveRulesSync()
    }

    suspend fun insertRule(rule: MockRuleEntity): Long {
        return mockRuleDao.insertRule(rule)
    }

    suspend fun updateRule(rule: MockRuleEntity) {
        mockRuleDao.updateRule(rule)
    }

    suspend fun toggleRule(id: Long, isEnabled: Boolean) {
        mockRuleDao.setRuleEnabled(id, isEnabled)
    }

    suspend fun deleteRule(id: Long) {
        mockRuleDao.deleteRule(id)
    }

    suspend fun saveSession(session: SavedSessionEntity) {
        sessionDao.insertSession(session)
    }

    suspend fun deleteSession(id: String) {
        sessionDao.deleteSession(id)
    }
}
