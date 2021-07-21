package com.ragavan.randomusers.viewmodel

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.ragavan.randomusers.common.Resource
import kotlinx.coroutines.Dispatchers
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.ragavan.randomusers.model.Results
import com.ragavan.randomusers.repositories.RandomUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@ExperimentalPagingApi
@HiltViewModel
class UserDetailsViewModel  @Inject constructor(private val repository: RandomUserRepository): ViewModel() {

    fun getUserDetails(search: String) = liveData(Dispatchers.IO) {
        val trendingResponse = repository.getUserDetails(search)
        emit(trendingResponse)
    }

    fun getWeatherReport(coordinates:String) = liveData(Dispatchers.IO) {
        val weatherReport = repository.getWeatherReport(coordinates)
        emit(weatherReport)
    }

}