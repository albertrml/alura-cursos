package br.com.alura.orgs.model.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.com.alura.orgs.model.entity.Account

@Dao
interface AccountDAO {

    @Insert (onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(account: Account)

    @Query("SELECT username FROM account")
    suspend fun getAllUsernames(): List<String>

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM account WHERE username = :username AND password = :password")
    suspend fun authenticate(username: String, password: String): Account?

    @Query("SELECT EXISTS (SELECT * FROM account WHERE username = :username)")
    suspend fun isUsernameExist(username: String): Boolean

}