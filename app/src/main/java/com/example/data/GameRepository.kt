package com.example.data

import kotlinx.coroutines.flow.Flow

class GameRepository(private val dao: GameDao) {
    val playerStateFlow: Flow<PlayerStateEntity?> = dao.getPlayerState()
    val claimedTasksFlow: Flow<List<ClaimedTaskEntity>> = dao.getClaimedTasks()
    val withdrawalsFlow: Flow<List<WithdrawalRecordEntity>> = dao.getWithdrawals()

    suspend fun ensureInitialized(): PlayerStateEntity {
        val existing = dao.getPlayerStateOnce()
        if (existing != null) return existing
        val initial = PlayerStateEntity()
        dao.upsertPlayerState(initial)
        return initial
    }

    suspend fun updateState(transform: (PlayerStateEntity) -> PlayerStateEntity) {
        val current = ensureInitialized()
        dao.upsertPlayerState(transform(current))
    }

    suspend fun markTaskClaimed(taskId: String, rewardAmount: Double) {
        dao.insertClaimedTask(ClaimedTaskEntity(taskId = taskId))
        updateState { state ->
            state.copy(balance = state.balance + rewardAmount)
        }
    }

    suspend fun recordWithdrawal(
        amount: Double,
        method: String,
        accountNumber: String,
        accountName: String
    ) {
        dao.insertWithdrawal(
            WithdrawalRecordEntity(
                amount = amount,
                method = method,
                accountNumber = accountNumber,
                accountName = accountName,
                status = "PENDING"
            )
        )
        updateState { state ->
            state.copy(balance = (state.balance - amount).coerceAtLeast(0.0))
        }
    }
}
