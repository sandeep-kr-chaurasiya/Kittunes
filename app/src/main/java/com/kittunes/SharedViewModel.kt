import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.kittunes.Api_Data.Data

class SharedViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _songList = MutableLiveData<MutableList<Data>>(mutableListOf())
    val songList: LiveData<MutableList<Data>> get() = _songList

    private val _currentSong = MutableLiveData<Data?>().apply {
        value = getStoredSong()
    }
    val currentSong: LiveData<Data?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Int>().apply {
        value = prefs.getInt("currentPosition", 0)
    }
    val currentPosition: LiveData<Int> get() = _currentPosition

    private val _isPlaying = MutableLiveData<Boolean>().apply {
        value = prefs.getBoolean("isPlaying", false)
    }
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    init {
        // Ensure song queue is persisted even if it was loaded from prefs
        saveQueueToPreferences(_songList.value ?: listOf())
    }

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
        saveSongToPreferences(song)
    }

    fun addSongToQueue(song: Data) {
        val list = _songList.value ?: mutableListOf()
        if (!list.contains(song)) {
            list.add(song)
            _songList.value = list
            saveQueueToPreferences(list)
        }
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
        prefs.edit().putInt("currentPosition", position).apply()
    }

    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        prefs.edit().putBoolean("isPlaying", isPlaying).apply()
    }

    fun playNextSong() {
        val list = _songList.value ?: return
        val currentIndex = list.indexOf(_currentSong.value)
        if (currentIndex != -1 && currentIndex < list.size - 1) {
            val nextSong = list[currentIndex + 1]
            setCurrentSong(nextSong)
        }
    }

    fun playPreviousSong() {
        val list = _songList.value ?: return
        val currentIndex = list.indexOf(_currentSong.value)
        if (currentIndex > 0) {
            val previousSong = list[currentIndex - 1]
            setCurrentSong(previousSong)
        }
    }

    private fun saveSongToPreferences(song: Data?) {
        val editor = prefs.edit()
        if (song != null) {
            val songJson = gson.toJson(song)
            editor.putString("currentSong", songJson)
        } else {
            editor.remove("currentSong")
        }
        editor.apply()
    }

    private fun getStoredSong(): Data? {
        val songJson = prefs.getString("currentSong", null) ?: return null
        return try {
            gson.fromJson(songJson, Data::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun saveQueueToPreferences(queue: List<Data>) {
        val editor = prefs.edit()
        val queueJson = gson.toJson(queue)
        editor.putString("songQueue", queueJson)
        editor.apply()
    }

    private fun getStoredQueue(): List<Data>? {
        val queueJson = prefs.getString("songQueue", null) ?: return null
        return try {
            gson.fromJson(queueJson, Array<Data>::class.java)?.toList()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}