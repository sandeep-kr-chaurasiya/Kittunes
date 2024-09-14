import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kittunes.Api_Data.Data

class SharedViewModel : ViewModel() {

    private val _currentSong = MutableLiveData<Data?>()
    val currentSong: LiveData<Data?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> get() = _currentPosition

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
    }
}