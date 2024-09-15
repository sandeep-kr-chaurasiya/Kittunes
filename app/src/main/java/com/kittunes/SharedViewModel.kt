import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.kittunes.Api_Data.Data

class SharedViewModel(context: Context) : ViewModel() {

    private val prefs: SharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
    private val gson = Gson() // For JSON serialization/deserialization

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

    fun setCurrentSong(song: Data?) {
        _currentSong.value = song
        saveSongToPreferences(song)
    }

    fun setCurrentPosition(position: Int) {
        _currentPosition.value = position
        prefs.edit().putInt("currentPosition", position).apply()
    }

    fun setPlayingState(isPlaying: Boolean) {
        _isPlaying.value = isPlaying
        prefs.edit().putBoolean("isPlaying", isPlaying).apply()
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
            e.printStackTrace()  // Log or handle the error
            null
        }
    }
}