package com.sakethh.linkora.ui.components.sorting

import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.domain.Sorting
import com.sakethh.linkora.ui.domain.model.SortingBtmSheet
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel


class SortingBtmSheetVM(preferencesRepository: PreferencesRepository) :
    SettingsScreenViewModel(preferencesRepository) {

    fun sortingBtmSheetData(): List<SortingBtmSheet> {
        return listOf(
            SortingBtmSheet(
                sortingName = Localization.Key.NewestToOldest.getLocalizedString(), onClick = {
                    AppPreferences.selectedSortingType.value = Sorting.NEW_TO_OLD.name
                    changeSettingPreferenceValue(
                        preferenceKey = stringPreferencesKey(
                            AppPreferenceType.SORTING_PREFERENCE.name
                        ), newValue = Sorting.NEW_TO_OLD.name
                    )
                }, sortingType = Sorting.NEW_TO_OLD
            ),
            SortingBtmSheet(
                sortingName = Localization.Key.OldestToNewest.getLocalizedString(), onClick = {
                    AppPreferences.selectedSortingType.value = Sorting.OLD_TO_NEW.name
                    changeSettingPreferenceValue(
                        preferenceKey = stringPreferencesKey(
                            AppPreferenceType.SORTING_PREFERENCE.name
                        ), newValue = Sorting.OLD_TO_NEW.name
                    )
                }, sortingType = Sorting.OLD_TO_NEW
            ),
            SortingBtmSheet(
                sortingName = Localization.Key.AToZSequence.getLocalizedString(), onClick = {
                    AppPreferences.selectedSortingType.value = Sorting.A_TO_Z.name
                    changeSettingPreferenceValue(
                        preferenceKey = stringPreferencesKey(
                            AppPreferenceType.SORTING_PREFERENCE.name
                        ), newValue = Sorting.A_TO_Z.name
                    )
                }, sortingType = Sorting.A_TO_Z
            ),
            SortingBtmSheet(
                sortingType = Sorting.Z_TO_A,
                sortingName = Localization.Key.ZToASequence.getLocalizedString(),
                onClick = {
                    AppPreferences.selectedSortingType.value = Sorting.Z_TO_A.name
                    changeSettingPreferenceValue(
                        preferenceKey = stringPreferencesKey(
                            AppPreferenceType.SORTING_PREFERENCE.name
                        ), newValue = Sorting.Z_TO_A.name
                    )
                }),
        )
    }
}