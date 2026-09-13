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

package com.hineeks.nexaproxy.bg.test

import android.net.Network
import com.hineeks.nexaproxy.bg.GuardedProcessPool
import com.hineeks.nexaproxy.bg.LocalResolver
import com.hineeks.nexaproxy.bg.proto.V2RayInstance
import com.hineeks.nexaproxy.database.ProxyEntity
import com.hineeks.nexaproxy.fmt.buildV2RayConfig
import com.hineeks.nexaproxy.ktx.Logs
import com.hineeks.nexaproxy.ktx.runOnDefaultDispatcher
import com.hineeks.nexaproxy.ktx.tryResume
import com.hineeks.nexaproxy.ktx.tryResumeWithException
import com.hineeks.nexaproxy.utils.DefaultNetworkListener
import kotlinx.coroutines.delay
import libexclavecore.Libexclavecore
import kotlin.coroutines.Continuation
import kotlin.coroutines.suspendCoroutine

class V2RayTestInstance(profile: ProxyEntity, val link: String, val timeout: Int, val protectPath: String = "") : V2RayInstance(
    profile,
), LocalResolver {
    lateinit var continuation: Continuation<Int>
    suspend fun doTest(): Int {
        return suspendCoroutine { c ->
            continuation = c
            processes = GuardedProcessPool {
                Logs.w(it)
                c.tryResumeWithException(it)
            }
            runOnDefaultDispatcher {
                try {
                    init()
                    launch()
                    if (pluginConfigs.isNotEmpty()) {
                        delay(500L)
                    }
                    c.tryResume(Libexclavecore.urlTest(v2rayPoint, "", link, timeout))
                } catch (e: Exception) {
                    c.tryResumeWithException(e)
                }
            }
        }
    }

    @Volatile
    override var underlyingNetwork: Network? = null

    override suspend fun init() {
        super.init()
        v2rayPoint.withLocalResolver(this)
        v2rayPoint.withAlternativeSystemDialer(protectPath)
        DefaultNetworkListener.start(this) {
            underlyingNetwork = it
        }
    }

    override fun close() {
        runOnDefaultDispatcher {
            DefaultNetworkListener.stop(this)
        }
        super.close()
    }

    override fun buildConfig() {
        config = buildV2RayConfig(profile, forTest = true)
    }
}