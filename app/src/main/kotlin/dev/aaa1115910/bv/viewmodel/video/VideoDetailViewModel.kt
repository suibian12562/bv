package dev.aaa1115910.bv.viewmodel.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dev.aaa1115910.biliapi.entity.video.VideoDetail
import dev.aaa1115910.biliapi.repositories.VideoDetailRepository
import dev.aaa1115910.bv.entity.carddata.VideoCardData
import dev.aaa1115910.bv.util.Prefs
import dev.aaa1115910.bv.util.fInfo
import dev.aaa1115910.bv.util.swapList
import io.github.oshai.kotlinlogging.KotlinLogging

class VideoDetailViewModel(
    private val videoDetailRepository: VideoDetailRepository
) : ViewModel() {
    private val logger = KotlinLogging.logger { }
    var state by mutableStateOf(VideoInfoState.Loading)
    var videoDetail: VideoDetail? by mutableStateOf(null)

    var relatedVideos = mutableStateListOf<VideoCardData>()

    suspend fun loadDetail(aid: Int) {
        logger.fInfo { "Load detail: [avid=$aid, preferApiType=${Prefs.apiType.name}]" }
        state = VideoInfoState.Loading
        runCatching {
            videoDetail = videoDetailRepository.getVideoDetail(
                aid = aid,
                preferApiType = Prefs.apiType
            )
        }.onFailure {
            state = VideoInfoState.Error
            logger.fInfo { "Load video av$aid failed: ${it.stackTraceToString()}" }
        }.onSuccess {
            state = VideoInfoState.Success
            logger.fInfo { "Load video av$aid success" }

            updateRelatedVideos()
        }.getOrThrow()
    }

    suspend fun loadDetailOnlyUpdateHistory(aid: Int) {
        logger.fInfo { "Load detail only update history: [avid=$aid, preferApiType=${Prefs.apiType.name}]" }
        runCatching {
            videoDetail?.history = videoDetailRepository.getVideoDetail(
                aid = aid,
                preferApiType = Prefs.apiType
            ).history
        }.onFailure {
            logger.fInfo { "Load video av$aid only update history failed: ${it.stackTraceToString()}" }
        }.onSuccess {
            logger.fInfo { "Load video av$aid only update history success: ${videoDetail?.history}" }
        }
    }

    private fun updateRelatedVideos() {
        logger.fInfo { "Start update relate video" }
        val relateVideoCardDataList = videoDetail?.relatedVideos?.map {
            VideoCardData(
                avid = it.aid,
                title = it.title,
                cover = it.cover,
                upName = it.author?.name ?: "",
                time = it.duration * 1000L,
                play = it.view,
                danmaku = it.danmaku,
                jumpToSeason = it.jumpToSeason,
                epId = it.epid
            )
        } ?: emptyList()
        relatedVideos.swapList(relateVideoCardDataList)
        logger.fInfo { "Update ${relateVideoCardDataList.size} relate videos" }
    }
}

enum class VideoInfoState {
    Loading,
    Success,
    Error
}