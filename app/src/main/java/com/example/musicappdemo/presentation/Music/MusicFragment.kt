package com.example.musicappdemo.presentation.Music

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.example.musicappdemo.R
import com.example.musicappdemo.databinding.FragmentMusicBinding
import com.example.musicappdemo.domain.Model.AudioModel
import kotlinx.coroutines.cancel
import kotlin.math.max

class MusicFragment : Fragment(R.layout.fragment_music) {
    private val viewModel: MusicViewModel by viewModels()
    private lateinit var binding: FragmentMusicBinding
    private lateinit var list: ArrayList<AudioModel>
    var maxPosition: Float = 0f
    var currSongIndex: Int? = 0
    var playingSong: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMusicBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list = viewModel.audioList as ArrayList<AudioModel>

        viewModel.maxDuration.observe(viewLifecycleOwner, Observer { success ->
            maxPosition = success.toFloat()
        })

        context?.let { viewModel.fetchSongsFromStorage(it) }

        binding.composeView.setContent {
            val state = rememberLazyListState()
            val visibleItems = state.visibleItems(50f)
                .map { list[it.index] }

            if (playingSong && !visibleItems.contains(list[currSongIndex!!])) {
                viewModel.stopSong()
                playingSong = false
            }

            Column(
                modifier = Modifier
                    .padding(15.dp, 15.dp, 15.dp, 15.dp)
            ) {

                progressBar()

                LazyColumn(
                    modifier = Modifier,
                    state = state

                ) {

                    itemsIndexed(list) { index, song ->
                        SongsListItem(
                            index = index
                        )
                    }
                }
            }
        }

    }

    fun LazyListState.visibleItems(itemVisiblePercentThreshold: Float) =
        layoutInfo
            .visibleItemsInfo
            .filter {
                visibilityPercent(it) >= itemVisiblePercentThreshold
            }

    fun LazyListState.visibilityPercent(info: LazyListItemInfo): Float {
        val cutTop = max(0, layoutInfo.viewportStartOffset - info.offset)
        val cutBottom = max(0, info.offset + info.size - layoutInfo.viewportEndOffset)

        return max(0f, 100f - (cutTop + cutBottom) * 100f / info.size)
    }


    @Composable
    fun SongsListItem(
        index: Int
    ) {
        Card(
            shape = RoundedCornerShape(15.dp),
            elevation = 10.dp,
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp, 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            )

            {

                Column() {

                    Text(
                        text = list[index].getaName(),
                        fontSize = 14.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = list[index].getaAlbum(),
                        fontSize = 12.sp,
                        color = Color.Gray,
                    )
                }

                Button(
                    onClick = {
                        playingSong = true
                        currSongIndex = index
                        viewModel.stopSong()
                        context?.let { viewModel.playSong(it, index) }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "Play",
                        color = Color.White,
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }

    @Composable
    fun progressBar() {

        var sliderPosition by remember { mutableStateOf(0f) }

        viewModel.currentDur.observe(viewLifecycleOwner, Observer { success ->
            sliderPosition = success.toFloat()
        })

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it },
            valueRange = 0f..maxPosition,
            steps = 0,
            colors = SliderDefaults.colors(
                thumbColor = Color.Red,
                activeTrackColor = Color.Black,
                inactiveTrackColor = Color.Gray
            )
        )
    }

    override fun onPause() {
        lifecycleScope.cancel()
        super.onPause()
    }

}