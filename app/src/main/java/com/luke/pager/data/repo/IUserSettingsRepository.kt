package com.luke.pager.data.repo

import Privacy
import com.luke.pager.ui.theme.DiaryLayout
import com.luke.pager.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface IUserSettingsRepository {
    val themeModeFlow: Flow<Result<ThemeMode>>
    val defaultPrivacyFlow: Flow<Result<Privacy>>
    val syncOverCellularFlow: Flow<Result<Boolean>>

    suspend fun setThemeMode(mode: ThemeMode): Result<Unit>

    suspend fun setDefaultPrivacy(privacy: Privacy): Result<Unit>

    suspend fun setSyncOverCellular(enabled: Boolean): Result<Unit>

    suspend fun getDiaryLayout(): Result<DiaryLayout>
    suspend fun setDiaryLayout(layout: DiaryLayout): Result<Unit>
}
