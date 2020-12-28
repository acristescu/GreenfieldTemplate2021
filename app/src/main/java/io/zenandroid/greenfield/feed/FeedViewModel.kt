package io.zenandroid.greenfield.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.zenandroid.greenfield.data.model.Image
import io.zenandroid.greenfield.feed.FeedAction.*
import io.zenandroid.greenfield.repository.FlickrRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FeedViewModel(
        private val repository: FlickrRepository
) : ViewModel() {

    private val _state = MutableStateFlow(FeedState())

    val state: StateFlow<FeedState> = _state

    init {
        fetchImages(null)
    }

    fun fetchImages(tags: String?) {
        _state.value = _state.value.copy(loading = true)
        viewModelScope.launch {
            val response = repository.getImageList(tags)
            _state.value = _state.value.copy(
                    loading = false,
                    images = sortImages(response.items)
            )
        }
    }

    suspend fun sortImages(list: List<Image>?) =
            withContext(Dispatchers.Default) {
                list?.sortedBy {
                    when (_state.value.criterion) {
                        SortCriterion.TAKEN -> it.dateTaken
                        SortCriterion.PUBLISHED -> it.published
                    }
                }
            }

    fun onEvent(event: FeedAction) {
        when(event) {
            SearchButtonPressed -> _state.value = state.value.copy(
                    searchText = "",
                    searchBoxVisible = true
            )
            is SearchTextChanged -> _state.value = state.value.copy(
                    searchText = event.text
            )
            SearchCloseButtonPressed -> _state.value = state.value.copy(
                    searchBoxVisible = state.value.searchText.isNotEmpty(),
                    searchText = ""
            )
            SearchComplete -> {
                if(_state.value.searchText.isNotBlank()) {
                    val currentState = state.value
                    fetchImages(currentState.searchText.replace("\\s+".toRegex(), ","))
                    _state.value = currentState.copy(
                            loading = true,
                            searchBoxVisible = false,
                            tags = currentState.searchText,
                    )
                }
            }
            ChangeFiltering -> _state.value = state.value.copy(
                    sortDialogVisible = true
            )
            DismissFilterDialog -> _state.value = state.value.copy(
                    sortDialogVisible = false
            )
            is FeedAction.SortCriterion -> {
                if(event.newCriterion != state.value.criterion) {
                    fetchImages(state.value.searchText.replace("\\s+".toRegex(), ","))
                }
                _state.value = state.value.copy(
                        sortDialogVisible = false,
                        criterion = event.newCriterion
                )
            }
            is Browse, is Save, is Share -> { } // This is handled by the activity
            is Error -> _state.value = state.value.copy(
                errorMessage = event.errorMessage
            )
            DismissError -> _state.value = state.value.copy(
                errorMessage = null
            )
        }
    }
}