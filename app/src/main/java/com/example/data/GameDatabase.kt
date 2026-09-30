package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM player_state WHERE id = 1")
    fun getPlayerState(): Flow<PlayerStateEntity?>

    @Query("SELECT * FROM player_state WHERE id = 1")
    suspend fun getPlayerStateOnce(): PlayerStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPlayerState(state: PlayerStateEntity)

    @Query("SELECT * FROM claimed_tasks")
    fun getClaimedTasks(): Flow<List<ClaimedTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClaimedTask(task: ClaimedTaskEntity)

    @Query("SELECT * FROM withdrawal_records ORDER BY timestamp DESC")
    fun getWithdrawals(): Flow<List<WithdrawalRecordEntity>>

    @Query("SELECT * FROM withdrawal_records WHERE id = :id LIMIT 1")
    suspend fun getWithdrawalById(id: Int): WithdrawalRecordEntity?

    @Query("SELECT * FROM withdrawal_records ORDER BY timestamp DESC")
    suspend fun getWithdrawalsOnce(): List<WithdrawalRecordEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWithdrawal(record: WithdrawalRecordEntity)

    @Query("UPDATE withdrawal_records SET status = :newStatus WHERE id = :id")
    suspend fun updateWithdrawalStatus(id: Int, newStatus: String)

    @Query("DELETE FROM withdrawal_records WHERE id = :id")
    suspend fun deleteWithdrawal(id: Int)
}

@Database(
    entities = [
        PlayerStateEntity::class,
        ClaimedTaskEntity::class,
        WithdrawalRecordEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "cash_arrows_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
