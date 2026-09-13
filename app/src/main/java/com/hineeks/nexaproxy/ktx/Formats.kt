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

package com.hineeks.nexaproxy.ktx

import com.hineeks.nexaproxy.fmt.AbstractBean
import com.hineeks.nexaproxy.fmt.Serializable
import com.hineeks.nexaproxy.fmt.anytls.parseAnyTLS
import com.hineeks.nexaproxy.fmt.http.parseHttp
import com.hineeks.nexaproxy.fmt.http3.parseHttp3
import com.hineeks.nexaproxy.fmt.hysteria2.parseHysteria2
import com.hineeks.nexaproxy.fmt.juicity.parseJuicity
import com.hineeks.nexaproxy.fmt.mieru.parseMieru
import com.hineeks.nexaproxy.fmt.naive.parseNaive
import com.hineeks.nexaproxy.fmt.parseBackup
import com.hineeks.nexaproxy.fmt.shadowquic.parseShadowQUIC
import com.hineeks.nexaproxy.fmt.shadowsocks.parseShadowsocks
import com.hineeks.nexaproxy.fmt.shadowsocksr.parseShadowsocksR
import com.hineeks.nexaproxy.fmt.socks.parseSOCKS
import com.hineeks.nexaproxy.fmt.trusttunnel.parseTrustTunnel
import com.hineeks.nexaproxy.fmt.tuic5.parseTuic
import com.hineeks.nexaproxy.fmt.v2ray.parseV2Ray
import com.hineeks.nexaproxy.fmt.wireguard.parseWireGuard
import kotlin.io.encoding.Base64

fun String.decodeBase64(): String {
    if (this.lines().size > 1) {
        return String(Base64.Mime.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL).decode(this))
    }
    if (this.contains("-") || this.contains("_")) {
        return String(Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT_OPTIONAL).decode(this))
    }
    if (this.contains("+") || this.contains("/")) {
        return String(Base64.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL).decode(this))
    }
    return String(Base64.withPadding(Base64.PaddingOption.PRESENT_OPTIONAL).decode(this))
}

fun parseShareLinks(text: String): List<AbstractBean> {
    val links = text.split('\n').flatMap { it.trim().split(' ') }
    val linksByLine = text.split('\n').map { it.trim() }

    val entities = ArrayList<AbstractBean>()
    val entitiesByLine = ArrayList<AbstractBean>()

    fun String.parseLink(entities: ArrayList<AbstractBean>) {
        if (startsWith("socks://", ignoreCase = true)
            || startsWith("socks4://", ignoreCase = true)
            || startsWith("socks4a://", ignoreCase = true)
            || startsWith("socks5://", ignoreCase = true)
            || startsWith("socks5h://", ignoreCase = true)
            || startsWith("socks+tls://", ignoreCase = true)) {
            runCatching {
                entities.add(parseSOCKS(this))
            }
        } else if (startsWith("http://", ignoreCase = true)
            || startsWith("https://", ignoreCase = true)) {
            runCatching {
                entities.add(parseHttp(this))
            }
        } else if (startsWith("vmess://", ignoreCase = true)
            || startsWith("vless://", ignoreCase = true)
            || startsWith("trojan://", ignoreCase = true)) {
            runCatching {
                entities.add(parseV2Ray(this))
            }
        } else if (startsWith("ss://", ignoreCase = true)) {
            runCatching {
                entities.add(parseShadowsocks(this))
            }
        } else if (startsWith("ssr://", ignoreCase = true)) {
            runCatching {
                entities.add(parseShadowsocksR(this))
            }
        } else if (startsWith("naive+https", ignoreCase = true)
            || startsWith("naive+quic", ignoreCase = true)) {
            runCatching {
                entities.add(parseNaive(this))
            }
        } else if (startsWith("hysteria2://", ignoreCase = true)
            || startsWith("hy2://", ignoreCase = true)) {
            runCatching {
                entities.add(parseHysteria2(this))
            }
        } else if (startsWith("juicity://", ignoreCase = true)) {
            runCatching {
                entities.add(parseJuicity(this))
            }
        } else if (startsWith("tuic://", ignoreCase = true)) {
            runCatching {
                entities.add(parseTuic(this))
            }
        } else if (startsWith("wireguard://", ignoreCase = true) || startsWith("wg://", ignoreCase = true)) {
            runCatching {
                entities.add(parseWireGuard(this))
            }
        } else if (startsWith("mierus://", ignoreCase = true)) {
            runCatching {
                entities.addAll(parseMieru(this))
            }
        } else if (startsWith("quic://", ignoreCase = true)) {
            runCatching {
                entities.add(parseHttp3(this))
            }
        } else if (startsWith("anytls://", ignoreCase = true)) {
            runCatching {
                entities.add(parseAnyTLS(this))
            }
        } else if (startsWith("tt://", ignoreCase = true)) {
            runCatching {
                entities.addAll(parseTrustTunnel(this))
            }
        } else if (startsWith("sq://", ignoreCase = true) || startsWith("shadowquic://", ignoreCase = true)) {
            runCatching {
                entities.add(parseShadowQUIC(this))
            }
        }
    }

    for (link in links) {
        link.parseLink(entities)
    }
    for (link in linksByLine) {
        link.parseLink(entitiesByLine)
    }

    return if (entities.size > entitiesByLine.size) entities else entitiesByLine
}

fun parseBackupLines(text: String): List<AbstractBean> {
    val lines = text.split('\n')
    val entities = ArrayList<AbstractBean>()
    for (line in lines) {
        try {
            entities.add(parseBackup(line))
        } catch (_: Exception) {
            return listOf()
        }
    }
    return entities
}

fun <T : Serializable> T.applyDefaultValues(): T {
    initializeDefaultValues()
    return this
}
