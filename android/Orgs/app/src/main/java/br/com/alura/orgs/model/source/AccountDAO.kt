package br.com.alura.orgs.model.source

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import br.com.alura.orgs.model.entity.Account

@Dao
interface AccountDAO {

    @Insert
    suspend fun create(account: Account)

    @Query("SELECT * FROM account")
    suspend fun read(): List<Account>

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM account WHERE username = :username AND password = :password")
    suspend fun authenticate(username: String, password: String): Account?

    @Query("SELECT EXISTS (SELECT * FROM account WHERE username = :username)")
    suspend fun usernameExists(username: String): Boolean

}