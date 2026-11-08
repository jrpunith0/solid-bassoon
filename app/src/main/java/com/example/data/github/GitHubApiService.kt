package com.example.data.github

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface GitHubApiService {

    @GET("user")
    suspend fun getAuthenticatedUser(
        @Header("Authorization") authHeader: String
    ): Response<GitHubUser>

    @GET("user/repos")
    suspend fun getAuthenticatedRepos(
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 50,
        @Header("Authorization") authHeader: String
    ): Response<List<GitHubRepo>>

    @GET("users/{username}")
    suspend fun getUserProfile(
        @Path("username") username: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<GitHubUser>

    @GET("users/{username}/repos")
    suspend fun getUserRepos(
        @Path("username") username: String,
        @Query("sort") sort: String = "updated",
        @Query("per_page") perPage: Int = 30,
        @Header("Authorization") authHeader: String? = null
    ): Response<List<GitHubRepo>>

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Header("Authorization") authHeader: String? = null
    ): Response<GitHubRepo>

    @GET("repos/{owner}/{repo}/contents")
    suspend fun getRepositoryRootContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("ref") ref: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): Response<List<GitHubContentItem>>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getRepositoryPathContents(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String,
        @Query("ref") ref: String? = null,
        @Header("Authorization") authHeader: String? = null
    ): Response<List<GitHubContentItem>>

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun commitFile(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path") path: String,
        @Body body: GitHubFileCommitRequest,
        @Header("Authorization") authHeader: String
    ): Response<GitHubCommitResponse>

    companion object {
        private const val BASE_URL = "https://api.github.com/"

        fun create(): GitHubApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .addHeader("Accept", "application/vnd.github.v3+json")
                        .addHeader("User-Agent", "CreatorStudio-AndroidApp")
                        .build()
                    chain.proceed(req)
                }
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder()
                .add(KotlinJsonAdapterFactory())
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(GitHubApiService::class.java)
        }
    }
}
