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

import androidx.room.*
import com.hineeks.nexaproxy.Key
import com.hineeks.nexaproxy.NexaProxy
import com.hineeks.nexaproxy.fmt.KryoConverters
import com.hineeks.nexaproxy.fmt.gson.GsonConverters
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [ProxyGroup::class, ProxyEntity::class, RuleEntity::class, StatsEntity::class, AssetEntity::class],
    version = 37,
    autoMigrations = [AutoMigration(
        from = 12,
        to = 14,
    ), AutoMigration(
        from = 14, to = 15, spec = NexaProxyDatabase_Migration_14_15::class
    ), AutoMigration(
        from = 15,
        to = 16,
    ), AutoMigration(
        from = 16,
        to = 17,
    ), AutoMigration(
        from = 15,
        to = 17,
    ), AutoMigration(
        from = 17,
        to = 18,
    ), AutoMigration(
        from = 18,
        to = 19,
    ), AutoMigration(
        from = 19,
        to = 20,
    ), AutoMigration(
        from = 20,
        to = 21,
    ), AutoMigration(
        from = 21,
        to = 22,
    ), AutoMigration(
        from = 22,
        to = 23,
        spec = NexaProxyDatabase_Migration_22_23::class
    ), AutoMigration(
        from = 23,
        to = 24,
    ), AutoMigration(
        from = 24,
        to = 25,
    ), AutoMigration(
        from = 25,
        to = 26,
    ), AutoMigration(
        from = 26,
        to = 27,
    ), AutoMigration(
        from = 27,
        to = 28,
    ), AutoMigration(
        from = 28,
        to = 29,
    ), AutoMigration(
        from = 29,
        to = 30,
    ), AutoMigration(
        from = 30,
        to = 31,
    ), AutoMigration(
        from = 31,
        to = 32,
        spec = NexaProxyDatabase_Migration_31_32::class
    ), AutoMigration(
        from = 32,
        to = 33,
    ), AutoMigration(
        from = 33,
        to = 34,
        spec = NexaProxyDatabase_Migration_33_34::class
    ), AutoMigration(
        from = 34,
        to = 35,
    ), AutoMigration(
        from = 35,
        to = 36,
        spec = NexaProxyDatabase_Migration_35_36::class
    ), AutoMigration(
        from = 36,
        to = 37,
    )]
)
@TypeConverters(value = [KryoConverters::class, GsonConverters::class])
abstract class NexaProxyDatabase : RoomDatabase() {

    companion object {
        @Suppress("EXPERIMENTAL_API_USAGE")
        private val instance by lazy {
            NexaProxy.application.getDatabasePath(Key.DB_PROFILE).parentFile?.mkdirs()
            Room.databaseBuilder(NexaProxy.application, NexaProxyDatabase::class.java, Key.DB_PROFILE)
                .addMigrations(
                    NexaProxyDatabase_Migration_1_2,
                    NexaProxyDatabase_Migration_2_3,
                    NexaProxyDatabase_Migration_3_4,
                    NexaProxyDatabase_Migration_4_5,
                    NexaProxyDatabase_Migration_5_6,
                    NexaProxyDatabase_Migration_6_7,
                    NexaProxyDatabase_Migration_7_8,
                    NexaProxyDatabase_Migration_8_9,
                    NexaProxyDatabase_Migration_9_10,
                    NexaProxyDatabase_Migration_10_11,
                    NexaProxyDatabase_Migration_11_12
                )
                .fallbackToDestructiveMigrationOnDowngrade()
                .allowMainThreadQueries()
                .enableMultiInstanceInvalidation()
                .setQueryExecutor { GlobalScope.launch { it.run() } }
                .build()
        }

        val groupDao get() = instance.groupDao()
        val proxyDao get() = instance.proxyDao()
        val rulesDao get() = instance.rulesDao()
        val statsDao get() = instance.statsDao()
        val assetDao get() = instance.assetDao()

    }

    abstract fun groupDao(): ProxyGroup.Dao
    abstract fun proxyDao(): ProxyEntity.Dao
    abstract fun rulesDao(): RuleEntity.Dao
    abstract fun statsDao(): StatsEntity.Dao
    abstract fun assetDao(): AssetEntity.Dao

}
