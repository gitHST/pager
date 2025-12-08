package com.luke.pager.data.repo

import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface IUserSettingsRepository {
    val themeModeFlow: Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
