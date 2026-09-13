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

package com.hineeks.nexaproxy.database

import android.content.res.Resources
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.icu.util.ULocale
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import com.hineeks.nexaproxy.R
import com.hineeks.nexaproxy.NexaProxy
import com.hineeks.nexaproxy.aidl.TrafficStats
import com.hineeks.nexaproxy.fmt.AbstractBean
import com.hineeks.nexaproxy.ktx.Logs
import com.hineeks.nexaproxy.ktx.app
import com.hineeks.nexaproxy.ktx.applyDefaultValues
import java.io.IOException
import java.sql.SQLException
import java.util.*

object ProfileManager {

    interface Listener {
        suspend fun onAdd(profile: ProxyEntity)
        suspend fun onUpdated(profileId: Long, trafficStats: TrafficStats)
        suspend fun onUpdated(profile: ProxyEntity)
        suspend fun onRemoved(groupId: Long, profileId: Long)
    }

    interface RuleListener {
        suspend fun onAdd(rule: RuleEntity)
        suspend fun onUpdated(rule: RuleEntity)
        suspend fun onRemoved(ruleId: Long)
        suspend fun onCleared()
    }

    private val listeners = ArrayList<Listener>()
    private val ruleListeners = ArrayList<RuleListener>()

    suspend fun iterator(what: suspend Listener.() -> Unit) {
        synchronized(listeners) {
            listeners.toList()
        }.forEach { listener ->
            what(listener)
        }
    }

    suspend fun ruleIterator(what: suspend RuleListener.() -> Unit) {
        val ruleListeners = synchronized(ruleListeners) {
            ruleListeners.toList()
        }
        for (listener in ruleListeners) {
            what(listener)
        }
    }

    fun addListener(listener: Listener) {
        synchronized(listeners) {
            listeners.add(listener)
        }
    }

    fun removeListener(listener: Listener) {
        synchronized(listeners) {
            listeners.remove(listener)
        }
    }

    fun addListener(listener: RuleListener) {
        synchronized(ruleListeners) {
            ruleListeners.add(listener)
        }
    }

    fun removeListener(listener: RuleListener) {
        synchronized(ruleListeners) {
            ruleListeners.remove(listener)
        }
    }

    suspend fun createProfile(groupId: Long, bean: AbstractBean): ProxyEntity {
        bean.applyDefaultValues()

        val profile = ProxyEntity(groupId = groupId).apply {
            id = 0
            putBean(bean)
            userOrder = NexaProxyDatabase.proxyDao.nextOrder(groupId) ?: 1
        }
        profile.id = NexaProxyDatabase.proxyDao.addProxy(profile)
        iterator { onAdd(profile) }
        return profile
    }

    suspend fun updateProfile(profile: ProxyEntity) {
        NexaProxyDatabase.proxyDao.updateProxy(profile)
        iterator { onUpdated(profile) }
    }

    suspend fun updateProfile(profiles: List<ProxyEntity>) {
        NexaProxyDatabase.proxyDao.updateProxy(profiles)
        profiles.forEach {
            iterator { onUpdated(it) }
        }
    }

    suspend fun deleteProfile(groupId: Long, profileId: Long) {
        if (NexaProxyDatabase.proxyDao.deleteById(profileId) == 0) return
        if (DataStore.selectedProxy == profileId) {
            DataStore.selectedProxy = 0L
        }
        iterator { onRemoved(groupId, profileId) }
        if (NexaProxyDatabase.proxyDao.countByGroup(groupId) > 1) {
            GroupManager.rearrange(groupId)
        }
    }

    suspend fun deleteProfile2(groupId: Long, profileId: Long) {
        if (NexaProxyDatabase.proxyDao.deleteById(profileId) == 0) return
        if (DataStore.selectedProxy == profileId) {
            DataStore.selectedProxy = 0L
        }
    }

    fun getProfile(profileId: Long): ProxyEntity? {
        if (profileId == 0L) return null
        return try {
            NexaProxyDatabase.proxyDao.getById(profileId)
        } catch (ex: SQLiteCantOpenDatabaseException) {
            throw IOException(ex)
        } catch (ex: SQLException) {
            Logs.w(ex)
            null
        }
    }

    fun getProfiles(profileIds: List<Long>): List<ProxyEntity> {
        if (profileIds.isEmpty()) return listOf()
        return try {
            NexaProxyDatabase.proxyDao.getEntities(profileIds)
        } catch (ex: SQLiteCantOpenDatabaseException) {
            throw IOException(ex)
        } catch (ex: SQLException) {
            Logs.w(ex)
            listOf()
        }
    }

    suspend fun postUpdate(profileId: Long) {
        postUpdate(getProfile(profileId) ?: return)
    }

    suspend fun postUpdate(profile: ProxyEntity) {
        iterator { onUpdated(profile) }
    }

    suspend fun postTrafficUpdated(profileId: Long, stats: TrafficStats) {
        iterator { onUpdated(profileId, stats) }
    }

    suspend fun createRule(rule: RuleEntity, post: Boolean = true): RuleEntity {
        rule.userOrder = NexaProxyDatabase.rulesDao.nextOrder() ?: 1
        rule.id = NexaProxyDatabase.rulesDao.createRule(rule)
        if (post) {
            ruleIterator { onAdd(rule) }
        }
        return rule
    }

    suspend fun updateRule(rule: RuleEntity) {
        NexaProxyDatabase.rulesDao.updateRule(rule)
        ruleIterator { onUpdated(rule) }
    }

    suspend fun deleteRule(ruleId: Long) {
        NexaProxyDatabase.rulesDao.deleteById(ruleId)
        ruleIterator { onRemoved(ruleId) }
    }

    suspend fun deleteRules(rules: List<RuleEntity>) {
        NexaProxyDatabase.rulesDao.deleteRules(rules)
        ruleIterator {
            rules.forEach {
                onRemoved(it.id)
            }
        }
    }

    suspend fun getRules(): List<RuleEntity> {
        var rules = NexaProxyDatabase.rulesDao.allRules()
        if (rules.isEmpty() && !DataStore.rulesFirstCreate) {
            DataStore.rulesFirstCreate = true
            val systemLocale = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> NexaProxy.locale.systemLocales[0]!!
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> LocaleList.getDefault()[0]
                else -> Locale.getDefault()
            }
            val appLocales = AppCompatDelegate.getApplicationLocales()
            val appLocale = when {
                appLocales.size() > 0 -> appLocales[0]!!
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> Resources.getSystem().configuration.locales[0]!!
                else -> @Suppress("DEPRECATION") Resources.getSystem().configuration.locale
            }
            val country = systemLocale.country
            val displayCountry = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ULocale.getDisplayCountry(systemLocale.toLanguageTag(), appLocale.toLanguageTag())
            } else {
                systemLocale.getDisplayCountry(appLocale)
            }
            when (country) {
                "CN" -> {
                    createRule(
                        RuleEntity(
                            name = app.getString(R.string.route_play_store, displayCountry),
                            domains = "domain:googleapis.cn",
                        ), false
                    )
                    createRule(
                        RuleEntity(
                            name = app.getString(R.string.route_bypass_domain, displayCountry),
                            domains = "geosite:cn",
                            outbound = -1
                        ), false
                    )
                }
                "IR" -> {
                    createRule(
                        RuleEntity(
                            name = app.getString(R.string.route_bypass_domain, displayCountry),
                            domains = "geosite:category-ir",
                            outbound = -1
                        ), false
                    )
                }
                "RU" -> {
                    createRule(
                        // https://habr.com/ru/articles/1020080/
                        // Added because of the request from users. Do not rely on it.
                        RuleEntity(
                            name = "UID -1", // TODO: l10n
                            customPackageNames = listOf("-1"),
                            outbound = -1
                        ), false
                    )
                    createRule(
                        RuleEntity(
                            name = app.getString(R.string.route_bypass_domain, displayCountry),
                            domains = "geosite:category-ru",
                            outbound = -1
                        ), false
                    )
                }
            }
            if (country.length == 2) {
                createRule(
                    RuleEntity(
                        name = app.getString(R.string.route_bypass_ip, displayCountry),
                        ip = "geoip:${country.lowercase()}",
                        outbound = -1
                    ), false
                )
            }
            createRule(
                RuleEntity(
                    enabled = true,
                    name = app.getString(R.string.route_opt_bypass_lan),
                    ip = "geoip:private",
                    outbound = -1
                ), false
            )
            rules = NexaProxyDatabase.rulesDao.allRules()
        }
        return rules
    }

}
















