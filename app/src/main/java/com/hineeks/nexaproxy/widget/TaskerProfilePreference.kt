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

package com.hineeks.nexaproxy.widget

import android.content.Context
import android.util.AttributeSet
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.database.ProfileManager

class TaskerProfilePreference : ReselectableSimpleMenuPreference {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    override fun getSummary(): CharSequence? {
        if (value == "1") {
            val taskerProfileId = DataStore.taskerProfileId
            if (taskerProfileId > 0) {
                ProfileManager.getProfile(taskerProfileId)?.displayName()?.let {
                    return it
                }
            }
        }
        return super.getSummary()
    }

}