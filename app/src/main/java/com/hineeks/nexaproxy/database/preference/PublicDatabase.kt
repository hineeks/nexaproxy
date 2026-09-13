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

package com.hineeks.nexaproxy.database.preference

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hineeks.nexaproxy.Key
import com.hineeks.nexaproxy.NexaProxy
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

@Database(
    entities = [KeyValuePair::class], version = 1
)
abstract class PublicDatabase : RoomDatabase() {
    companion object {
        @Suppress("EXPERIMENTAL_API_USAGE")
        private val instance by lazy {
            NexaProxy.deviceStorage.getDatabasePath(Key.DB_PROFILE).parentFile?.mkdirs()
            Room.databaseBuilder(NexaProxy.deviceStorage, PublicDatabase::class.java, Key.DB_PUBLIC)
                .allowMainThreadQueries()
                .enableMultiInstanceInvalidation()
                .fallbackToDestructiveMigration()
                .setQueryExecutor { GlobalScope.launch { it.run() } }
                .build()
        }

        val kvPairDao get() = instance.keyValuePairDao()
    }

    abstract fun keyValuePairDao(): KeyValuePair.Dao

}
