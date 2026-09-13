/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by hineeks             *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package com.hineeks.nexaproxy.ui.profile

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.hineeks.nexaproxy.Key
import com.hineeks.nexaproxy.R
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.fmt.internal.ConfigBean
import com.hineeks.nexaproxy.ktx.onMainDispatcher
import com.hineeks.nexaproxy.ktx.runOnDefaultDispatcher
import com.hineeks.nexaproxy.widget.EditConfigPreference

class ConfigSettingsActivity : ProfileSettingsActivity<ConfigBean>() {

    override fun createEntity() = ConfigBean()

    var config = ""

    override fun ConfigBean.init() {
        DataStore.profileName = name
        DataStore.serverProtocol = type
        DataStore.serverConfig = content
        DataStore.serverAddress = serverAddresses
        config = content
    }

    override fun ConfigBean.serialize() {
        name = DataStore.profileName
        type = DataStore.serverProtocol
        content = config
        serverAddresses = DataStore.serverAddress
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.setTitle(R.string.config_settings)
    }

    lateinit var editConfigPreference: EditConfigPreference
    override fun PreferenceFragmentCompat.createPreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        addPreferencesFromResource(R.xml.config_preferences)
        editConfigPreference = findPreference(Key.SERVER_CONFIG)!!
        val serverAddresses = findPreference<EditTextPreference>(Key.SERVER_ADDRESS)!!
        val serverProtocol = findPreference<ListPreference>(Key.SERVER_PROTOCOL)!!
        fun updateProtocol(protocol: String) {
            serverAddresses.isVisible = protocol == "v2ray_outbound"
        }
        updateProtocol(DataStore.serverProtocol)
        serverProtocol.setOnPreferenceChangeListener { _, newValue ->
            updateProtocol(newValue as String)
            true
        }
    }

    override fun onResume() {
        super.onResume()

        if (::editConfigPreference.isInitialized) {
            runOnDefaultDispatcher {
                val newConfig = DataStore.serverConfig

                if (newConfig != config) {
                    config = newConfig

                    onMainDispatcher {
                        editConfigPreference.notifyChanged()
                    }
                }
            }
        }
    }

}