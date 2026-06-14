package com.maneo.app.feature.blocker.repository

import com.maneo.app.feature.blocker.domain.Prayer

class PrayerRepository(prayersLoader: () -> List<Prayer>) {

    val prayers: List<Prayer> by lazy { prayersLoader() }
}
