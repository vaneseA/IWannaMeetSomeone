package com.example.iwannameetsomeone.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    lateinit var myLikeList : LiveData<List<UserDataModel>>

    fun getAllInterestCoinData() = viewModelScope.launch {

//        val coinList = dbRepository.getAllInterestCoinData().asLiveData()
//        selectedCoinList = coinList

    }
}