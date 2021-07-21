package com.ragavan.randomusers.repositories

import android.util.Log
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.ragavan.randomusers.database.RemoteKeys
import com.ragavan.randomusers.database.UserDatabase
import com.ragavan.randomusers.model.Results
import com.ragavan.randomusers.network.ApiRequest
import retrofit2.HttpException
import java.io.IOException
import java.io.InvalidObjectException

@ExperimentalPagingApi
class UserListMediator(private val apiRequest: ApiRequest, private val userDatabase: UserDatabase?) :
    RemoteMediator<Int, Results>() {

    override suspend fun load(
        loadType: LoadType, state: PagingState<Int, Results>
    ): MediatorResult {
        val pageKeyData = getKeyPageData(loadType, state)
        val page = when (pageKeyData) {
            is MediatorResult.Success -> {
                return pageKeyData
            }
            else -> {
                pageKeyData as Int
            }
        }
        try {
            val response = apiRequest.getRandomUsers(page, state.config.pageSize,"abc")
            val isEndOfList = response.results.isEmpty()
            userDatabase?.withTransaction {
                // clear all tables in the database
                if (loadType == LoadType.REFRESH) {
                    userDatabase.remoteKeysDao().clearRemoteKeys()
                    userDatabase.getUserDao().clearAllUsers()
                }
                val prevKey = if (page == DEFAULT_PAGE_INDEX) null else page - 1
                val nextKey = if (isEndOfList) null else page + 1
                val keys = response.results.map {
                    RemoteKeys(repoId = it.email, prevKey = prevKey, nextKey = nextKey)
                }
                userDatabase.remoteKeysDao().insertAll(keys)
                userDatabase.getUserDao().update(response.results)
            }
            return MediatorResult.Success(endOfPaginationReached = isEndOfList)
        } catch (exception: IOException) {
            return MediatorResult.Error(exception)
        } catch (exception: HttpException) {
            return MediatorResult.Error(exception)
        }
    }

    /**
     * this returns the page key or the final end of list success result
     */
    suspend fun getKeyPageData(loadType: LoadType, state: PagingState<Int, Results>): Any? {
        return when (loadType) {
            LoadType.REFRESH -> {
                val remoteKeys = getClosestRemoteKey(state)
                remoteKeys?.nextKey?.minus(1) ?: DEFAULT_PAGE_INDEX
            }
            LoadType.APPEND -> {
                val remoteKeys = getLastRemoteKey(state)
                    ?: throw InvalidObjectException("Remote key should not be null for $loadType")
                remoteKeys.nextKey
            }
            LoadType.PREPEND -> {
                val remoteKeys = getFirstRemoteKey(state)
                //end of list condition reached
                remoteKeys?.prevKey ?: return MediatorResult.Success(endOfPaginationReached = true)
                remoteKeys.prevKey
            }
        }
    }

    /**
     * get the last remote key inserted which had the data
     */
    private suspend fun getLastRemoteKey(state: PagingState<Int, Results>): RemoteKeys? {
        return state.pages
            .lastOrNull { it.data.isNotEmpty() }
            ?.data?.lastOrNull()
            ?.let { result -> userDatabase?.remoteKeysDao()?.remoteKeysId(result.email) }
    }

    /**
     * get the first remote key inserted which had the data
     */
    private suspend fun getFirstRemoteKey(state: PagingState<Int, Results>): RemoteKeys? {
        Log.e("instance of remote:", userDatabase?.remoteKeysDao().toString())
        Log.e("instance of remote1:", state.pages.firstOrNull() { it.data.isNotEmpty() }.toString())
        Log.e("instance of remote2:", state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull().toString())
        Log.e("Nothing but data :",
            state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { Log.e("internal:",
                it.email
            ) }
                .toString()
        )
        Log.e("Nothing but data :",
            state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull()?.let { userDatabase?.remoteKeysDao()?.remoteKeysId(it.email)}
                .toString()
        )

        val remoteKeys:RemoteKeys? = state.pages.firstOrNull() { it.data.isNotEmpty() }?.data?.firstOrNull()?.
        let { userDatabase?.remoteKeysDao()?.remoteKeysId(it.email)}
        return remoteKeys
    }

    /**
     * get the closest remote key inserted which had the data
     */
    private suspend fun getClosestRemoteKey(state: PagingState<Int, Results>): RemoteKeys? {
        return state.anchorPosition?.let { position ->
            state.closestItemToPosition(position)?.email?.let { repoId ->
                userDatabase?.remoteKeysDao()?.remoteKeysId(repoId)
            }
        }
    }

    companion object {
        const val DEFAULT_PAGE_INDEX = 1
        const val DEFAULT_PAGE_SIZE = 25
    }

}