package com.example.dictionary.ui.main

import com.example.dictionary.model.data.AppState
import com.example.dictionary.model.data.DataModel
import com.example.dictionary.model.repository.Repository

class MainInteractor(
    private val remoteRepository: Repository<List<DataModel>>,
    private val localRepository: Repository<List<DataModel>>
) : Interactor<AppState> {
    override suspend fun getData(word: String, fromRemoteSource: Boolean): AppState {
        return AppState.Success(
            if (fromRemoteSource) {
                remoteRepository
            } else {
                localRepository
            }.getData(word)
        )
    }
}