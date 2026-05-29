package com.example.myapplication1.data

import com.example.myapplication1.network.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SupabaseRepository {
    private val client = SupabaseClient.client

    suspend fun signUp(email: String, password: String) = withContext(Dispatchers.IO) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signOut() = withContext(Dispatchers.IO) {
        client.auth.signOut()
    }

    suspend fun getCurrentUser() = client.auth.currentUserOrNull()

    suspend fun getGroups(): List<ConnectionGroup> = withContext(Dispatchers.IO) {
        client.postgrest["groups"].select().decodeList<ConnectionGroup>()
    }

    suspend fun getConnections(): List<SshConnection> = withContext(Dispatchers.IO) {
        client.postgrest["connections"].select().decodeList<SshConnection>()
    }

    suspend fun addConnection(connection: SshConnection) = withContext(Dispatchers.IO) {
        client.postgrest["connections"].insert(connection)
    }

    suspend fun addGroup(group: ConnectionGroup) = withContext(Dispatchers.IO) {
        client.postgrest["groups"].insert(group)
    }
}
