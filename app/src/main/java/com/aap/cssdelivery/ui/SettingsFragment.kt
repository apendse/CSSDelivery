package com.aap.cssdelivery.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.aap.cssdelivery.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}