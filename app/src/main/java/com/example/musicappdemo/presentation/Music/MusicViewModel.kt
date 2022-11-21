package com.example.musicappdemo.presentation.Music

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicappdemo.domain.Model.AudioModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList

class MusicViewModel : ViewModel() {

    var musicPlayer: MediaPlayer? = null
    val maxDuration = MutableLiveData<Int>()
    val currentDur = MutableLiveData<Int>()
    val audioList: MutableList<AudioModel> = ArrayList()

    @RequiresApi(Build.VERSION_CODES.O)
    fun fetchSongsFromStorage(context: Context) {
        val uri: Uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.AudioColumns.DATA,
            MediaStore.Audio.AudioColumns.TITLE,
            MediaStore.Audio.AudioColumns.ALBUM,
            MediaStore.Audio.ArtistColumns.ARTIST
        )

        val c = context.contentResolver.query(
            uri,
            projection,
            null, // Put your device folder / file location here.
            null
        );

        if (c != null) {
            while (c.moveToNext()) {
                val audioModel =
                    AudioModel()
                val path: String = c.getString(0)
                val name: String = c.getString(1)
                val album: String = c.getString(2)
                val artist: String = c.getString(3)
                audioModel.setaName(name)
                audioModel.setaAlbum(album)
                audioModel.setaArtist(artist)
                audioModel.setaPath(path)

                audioList.add(audioModel)
            }
            c.close()
        }
    }

    fun playSong(applicationContext: Context, index: Int) {
        var song = audioList[index].getaPath()

        val songUri: Uri = Uri.parse(song)

        musicPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            try {
                setDataSource(applicationContext, songUri)
                prepare()
                start()
            }
            catch (E:Exception){

            }

        }
        if (musicPlayer != null) {
            viewModelScope.launch {
                try {
                    val present: Int = musicPlayer?.currentPosition!!
                    val max: Int = musicPlayer?.duration!!
                    while (present < max!!) {
                        maxDuration.postValue(musicPlayer?.duration)
                        currentDur.postValue(musicPlayer?.currentPosition)
                        delay(1000)
                    }
                } catch (E: Exception) {
                    maxDuration.postValue(0)
                    currentDur.postValue(0)
                }
            }
        }
        musicPlayer!!.setLooping(true);
    }

    fun stopSong() {
        try {
            musicPlayer?.stop()
            musicPlayer?.release()
        } catch (E: Exception) {
        }
    }
}