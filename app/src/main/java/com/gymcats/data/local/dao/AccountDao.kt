package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymcats.data.local.entities.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    fun getAccount(accountId: Long): Flow<Account?>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountNow(accountId: Long): Account?

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): Account?

    @Query("SELECT * FROM accounts WHERE phone = :phone LIMIT 1")
    suspend fun getByPhone(phone: String): Account?

    @Query("SELECT COUNT(*) FROM accounts")
    suspend fun countAccounts(): Int

    @Query("UPDATE accounts SET password = :password WHERE id = :accountId")
    suspend fun updatePassword(accountId: Long, password: String)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertAccount(account: Account): Long
}
