package com.lifelink.lifelink.data

import android.util.Log
import com.lifelink.lifelink.api.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class BloodRequestRepository {

    suspend fun createRequest(request: BloodRequest): Result<Unit> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to insert request into Supabase.")
            SupabaseClient.client.postgrest["blood_requests"].insert(request)
            Log.i("BloodRequestRepo_Debug", "Successfully created blood request.")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Supabase insert failed for blood request.", e)
            Result.failure(e)
        }
    }

    suspend fun getPendingRequests(currentUserId: String): Result<List<BloodRequest>> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to fetch pending requests not made by user: $currentUserId")
            val requests = SupabaseClient.client.postgrest["blood_requests"]
                .select {
                    filter {

                        neq("requester_id", currentUserId)

                        eq("status", "Pending")
                    }
                }
                .decodeList<BloodRequest>()
            Log.i("BloodRequestRepo_Debug", "Successfully fetched ${requests.size} pending requests not by user: $currentUserId")
            Result.success(requests)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Failed to fetch pending requests not by user: $currentUserId", e)
            Result.failure(e)
        }
    }
    suspend fun getRequestsIAccepted(donorId: String): Result<List<BloodRequest>> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to fetch requests accepted by donor: $donorId")
            val requests: List<BloodRequest> = SupabaseClient.client.postgrest["blood_requests"]
                .select {
                    filter {
                        eq("donor_id", donorId)
                    }
                }
                .decodeList<BloodRequest>()
            Log.i("BloodRequestRepo_Debug", "Successfully fetched ${requests.size} requests accepted by donor: $donorId")
            Result.success(requests)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Failed to fetch requests accepted by donor: $donorId", e)
            Result.failure(e)
        }
    }

    suspend fun acceptRequest(requestId: String, donorId: String): Result<Unit> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to accept request ID: $requestId by donor ID: $donorId")
            SupabaseClient.client.postgrest["blood_requests"]
                .update({
                    set("status", "Accepted")
                    set("donor_id", donorId)
                }) {
                    filter { eq("id", requestId) }
                }
            Log.i("BloodRequestRepo_Debug", "Successfully accepted request ID: $requestId by donor ID: $donorId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Failed to accept request ID: $requestId by donor ID: $donorId", e)
            Result.failure(e)
        }
    }
    suspend fun getProfile(userId: String): Result<Profile?> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to fetch profile for user ID: $userId")
            val profile: Profile? = SupabaseClient.client.postgrest["profiles"]
                .select {
                    filter { eq("id", userId) }
                }
                .decodeSingleOrNull<Profile>()
            Log.i("BloodRequestRepo_Debug", "Successfully fetched profile for user ID: $userId. Profile found: ${profile != null}")
            Result.success(profile)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Failed to fetch profile for user ID: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun getMyActiveRequest(userId: String): Result<BloodRequest?> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to fetch active request for user ID: $userId")
            val request: BloodRequest? = SupabaseClient.client.postgrest["blood_requests"]
                .select {
                    filter {
                        eq("requester_id", userId)
                    }
                }
                .decodeSingleOrNull<BloodRequest>()
            Log.i("BloodRequestRepo_Debug", "Successfully fetched active request for user ID: $userId. Request found: ${request != null}")
            Result.success(request)
        } catch (e: Exception) {
            Log.e("BloodRequestRepo_Debug", "Failed to fetch active request for user ID: $userId", e)
            Result.failure(e)
        }
    }

    suspend fun deleteMyRequest(userId: String): Result<Unit> {
        return try {
            Log.d("BloodRequestRepo_Debug", "Attempting to delete request for user ID: $userId")
            // Find the 'blood_requests' table and delete from it
            SupabaseClient.client.postgrest["blood_requests"]
                .delete {
                    filter {
                        // Only delete the row where the requester_id matches the user's ID
                        eq("requester_id", userId)
                    }
                }
            Log.i("BloodRequestRepo_Debug", "Successfully deleted request for user ID: $userId")
            // If no errors, report success
            Result.success(Unit)
        } catch (e: Exception) {
            // If there's an error, report failure
            Log.e("BloodRequestRepo_Debug", "Failed to delete request for user ID: $userId", e)
            Result.failure(e)
        }
    }


}