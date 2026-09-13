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

package com.hineeks.nexaproxy.bg

import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import com.hineeks.nexaproxy.ktx.Logs
import java.io.File
import java.io.IOException
import androidx.core.text.isDigitsOnly

object Executable {
    private val EXECUTABLES = setOf(
        "libnaive.so",
    )

    fun killAll() {
        for (process in File("/proc").listFiles { _, name -> name.isDigitsOnly() }
            ?: return) {
            val exe = File(try {
                File(process, "cmdline").inputStream().bufferedReader().use {
                    it.readText()
                }
            } catch (_: IOException) {
                continue
            }.split(Character.MIN_VALUE, limit = 2).first())
            if (EXECUTABLES.contains(exe.name)) try {
                Os.kill(process.name.toInt(), OsConstants.SIGKILL)
                Logs.w("SIGKILL ${exe.nameWithoutExtension} (${process.name}) succeed")
            } catch (e: ErrnoException) {
                if (e.errno != OsConstants.ESRCH) {
                    Logs.w("SIGKILL ${exe.absolutePath} (${process.name}) failed")
                    Logs.w(e)
                }
            }
        }
    }
}
