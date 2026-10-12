package com.petedev.wavemusic.domain.data.model.home

import com.petedev.wavemusic.domain.data.model.home.chart.Chart
import com.petedev.wavemusic.domain.data.model.mood.Mood
import com.petedev.wavemusic.domain.utils.Resource

data class HomeResponse(
    val homeItem: Resource<ArrayList<HomeItem>>,
    val exploreMood: Resource<Mood>,
    val exploreChart: Resource<Chart>,
)