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
class UserListViewModel
@Inject constructor(private val repository: RandomUserRepository): ViewModel() {

    /*
    * ObservableBoolean() methods are used to easily observe data for changes.
    *  You don't have to worry about refreshing the UI
    * when the underlying data source changes
    */
    var noConnection: ObservableBoolean = ObservableBoolean()
    var searchEnabled: ObservableBoolean = ObservableBoolean()
    var noResult: ObservableBoolean = ObservableBoolean()


    fun getSearchResponse(search: String) = liveData(Dispatchers.IO) {
        val trendingResponse = repository.getSearchRepo(search)
        emit(trendingResponse)
    }

    fun getDatabaseCount() = liveData(Dispatchers.IO) {
        val trendingResponse = repository.isDbContainValue()
        emit(trendingResponse)
    }

    fun getWeatherReport(coordinates:String) = liveData(Dispatchers.IO) {
        val weatherReport = repository.getWeatherReport(coordinates)
        emit(weatherReport)
    }


    fun getUserList(): Flow<PagingData<Results>> {
        return repository.getUserListFlowDb().cachedIn(viewModelScope)
    }

}

@BindingAdapter("trueVisibility")
fun setVisibility(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("falseVisibility")
fun makeVisibility(view: View, visible: Boolean) {
    view.visibility = if (visible) View.GONE else View.VISIBLE
}