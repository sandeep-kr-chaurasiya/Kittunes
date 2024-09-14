import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kittunes.Api_Data.Data

class SharedViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<Data?>()
    val currentSong: LiveData<Data?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    // Set current song
    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
    }

    // Set current position of the song
    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }

    // Set play/pause state
    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
    }
}