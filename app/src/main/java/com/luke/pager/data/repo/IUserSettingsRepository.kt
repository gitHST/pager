package com.luke.pager.data.repo

import Privacy
import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface IUserSettingsRepository {
    val themeModeFlow: Flow<ThemeMode>
    val defaultPrivacyFlow: Flow<Privacy>

    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setDefaultPrivacy(privacy: Privacy)
}
