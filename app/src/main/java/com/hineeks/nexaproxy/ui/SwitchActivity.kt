/******************************************************************************
 * Copyright (C) 2026 by hineeks                  *
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

package com.hineeks.nexaproxy.ui

import android.os.Bundle
import com.hineeks.nexaproxy.R
import com.hineeks.nexaproxy.NexaProxy
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.database.ProfileManager
import com.hineeks.nexaproxy.ktx.onMainDispatcher
import com.hineeks.nexaproxy.ktx.runOnDefaultDispatcher

class SwitchActivity : ThemedActivity(R.layout.layout_empty),
    ConfigurationFragment.SelectCallback {

    override val type = Type.Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction().replace(
            R.id.fragment_holder, ConfigurationFragment(true, null, R.string.quick_toggle)
        ).commitAllowingStateLoss()
    }

    override fun returnProfile(profileId: Long) {
        runOnDefaultDispatcher {
            val lastProxy = DataStore.selectedProxy
            DataStore.selectedProxy = profileId
            ProfileManager.postUpdate(lastProxy)
            ProfileManager.postUpdate(profileId)
            onMainDispatcher {
                NexaProxy.reloadService()
            }
        }
        finish()
    }
}