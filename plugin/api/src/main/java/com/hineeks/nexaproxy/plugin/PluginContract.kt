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

package com.hineeks.nexaproxy.plugin

object PluginContract {

    const val ACTION_NATIVE_PLUGIN = "com.hineeks.nexaproxy.plugin.ACTION_NATIVE_PLUGIN"
    const val EXTRA_ENTRY = "com.hineeks.nexaproxy.plugin.EXTRA_ENTRY"
    const val METADATA_KEY_ID = "com.hineeks.nexaproxy.plugin.id"
    const val METADATA_KEY_EXECUTABLE_PATH = "com.hineeks.nexaproxy.plugin.executable_path"
    const val METHOD_GET_EXECUTABLE = "nexaproxy:getExecutable"

    const val COLUMN_PATH = "path"
    const val COLUMN_MODE = "mode"
    const val SCHEME = "plugin"
}
