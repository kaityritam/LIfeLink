package com.lifelink.lifelink.data

import com.lifelink.lifelink.api.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
class UserRepository {

    suspend fun registerUser(
        email: String,
        password: String,
        name: String,
        phone: String,
        bloodGroup: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Create auth user with metadata that the trigger will use
            SupabaseClient.client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
                // Add metadata for the PostgreSQL trigger
                data = buildJsonObject {
                    put("name", name)
                    put("phone", phone)
                    put("blood_group", bloodGroup)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            // Check if it's a "user already exists" error
            if (e.message?.contains("already registered", ignoreCase = true) == true) {
                Result.success(Unit) // Consider this success since user exists
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getUserProfile(userId: String): Result<User?> = withContext(Dispatchers.IO) {
        return@withContext try {
            println("DEBUG: Current auth user: ${SupabaseClient.client.auth.currentUserOrNull()}")
            println("DEBUG: Current session: ${SupabaseClient.client.auth.currentSessionOrNull()}")

            // Refresh session first
            SupabaseClient.client.auth.refreshCurrentSession()

            val session = SupabaseClient.client.auth.currentSessionOrNull()
            if (session != null) {
                println("DEBUG: Session user ID: ${session.user?.id}")
                println("DEBUG: Requested user ID: $userId")

                val profile = SupabaseClient.client.postgrest["profiles"]
                    .select {
                        filter {
                            eq("id", userId)
                        }
                    }
                    .decodeSingleOrNull<User>()
                println("DEBUG: Profile result: $profile")
                Result.success(profile)
            } else {
                println("DEBUG: No active session")
                Result.success(null)
            }
        } catch (e: Exception) {
            println("DEBUG: Detailed error: ${e.stackTraceToString()}")
            Result.failure(e)
        }
    }



    // In UserRepository.kt
    suspend fun getCurrentUserId(): String? = withContext(Dispatchers.IO) {
        return@withContext try {
            SupabaseClient.client.auth.currentUserOrNull()?.id
        } catch (e: Exception) {
            null
        }
    }


    suspend fun loginUser(email: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            SupabaseClient.client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            SupabaseClient.client.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}