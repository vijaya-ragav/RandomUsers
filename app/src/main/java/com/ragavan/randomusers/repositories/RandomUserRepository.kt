package com.ragavan.randomusers.repositories

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ragavan.randomusers.common.Resource
import com.ragavan.randomusers.common.ResponseHandler
import com.ragavan.randomusers.common.Status
import com.ragavan.randomusers.database.RandomUserDao
import com.ragavan.randomusers.database.UserDatabase
import com.ragavan.randomusers.model.Coordinates
import com.ragavan.randomusers.model.RandomUserModel
import com.ragavan.randomusers.model.Results
import com.ragavan.randomusers.model.WeatherReportModel
import com.ragavan.randomusers.network.ApiRequest
import com.ragavan.randomusers.network.RetrofitCall
import com.ragavan.randomusers.repositories.UserListMediator.Companion.DEFAULT_PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
class RandomUserRepository @Inject constructor(private val userDatabase: UserDatabase,private val userDao: RandomUserDao, private val responseHandler: ResponseHandler){

    //suspend keyword to perform operation in different thread without blocking the main thread
    // in this class network call and database query are performed by this suspend keyword
    suspend fun getWeatherReport(coordinates: String): Resource<WeatherReportModel> {
        val client: ApiRequest = RetrofitCall.retrofitClient
        return try {
            val response = client.getWeather(RetrofitCall.WEATHER_URL,"b98bcb7d1738474586865420212007",coordinates,"no")
            if (responseHandler.handleSuccess(response).status == Status.SUCCESS) {
            }
            responseHandler.handleSuccess(response)
        } catch (e: Exception) {
            responseHandler.handleException(e)
        }
    }

    fun getDefaultPageConfig(): PagingConfig {
        return PagingConfig(pageSize = DEFAULT_PAGE_SIZE, enablePlaceholders = true)
    }

    fun getUserListFlowDb(pagingConfig: PagingConfig = getDefaultPageConfig()): Flow<PagingData<Results>> {

        val pagingSourceFactory = { userDao.getAllUsers() }
        return Pager(
            config = pagingConfig,
            pagingSourceFactory = pagingSourceFactory,
            remoteMediator = UserListMediator(RetrofitCall.retrofitClient,userDatabase)
        ).flow
    }

    suspend fun getSearchRepo(search: String): List<Results> {
        return userDao.getSearchResult(search)
    }

    suspend fun getUserDetails(search: String): Results {
        return userDao.getUserDetails(search)
    }

    private suspend fun getCount(): Int {
        return userDao.getCount()
    }

    suspend fun isDbContainValue(): Boolean {
        try {
            if (getCount() > 0) {
                return true
            }
        } catch (e: Exception) {
            return false
        }
        return false
    }

}