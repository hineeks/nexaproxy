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

package com.hineeks.nexaproxy.tasker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hineeks.nexaproxy.NexaProxy
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.database.ProfileManager

class TaskerReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val settings = TaskerBundle.fromIntent(intent)
        when (settings.action) {
            TaskerBundle.ACTION_START -> {
                var reload = false
                if (settings.profileId > 0 && DataStore.selectedProxy != settings.profileId) {
                    if (ProfileManager.getProfile(settings.profileId) != null) {
                        DataStore.selectedProxy = settings.profileId
                        reload = DataStore.startedProfile != 0L
                    }
                }
                if (reload) NexaProxy.reloadService() else NexaProxy.startService()
            }
            TaskerBundle.ACTION_STOP -> {
                NexaProxy.stopService()
            }
        }
    }
}