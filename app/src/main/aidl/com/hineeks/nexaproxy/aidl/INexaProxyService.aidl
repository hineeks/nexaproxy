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

package com.hineeks.nexaproxy.aidl;

import com.hineeks.nexaproxy.aidl.INexaProxyServiceCallback;

interface INexaProxyService {
  int getState();
  String getProfileName();

  void registerCallback(in INexaProxyServiceCallback cb);
  void startListeningForBandwidth(in INexaProxyServiceCallback cb, long timeout);
  oneway void stopListeningForBandwidth(in INexaProxyServiceCallback cb);
  void startListeningForStats(in INexaProxyServiceCallback cb, long timeout);
  oneway void stopListeningForStats(in INexaProxyServiceCallback cb);
  oneway void unregisterCallback(in INexaProxyServiceCallback cb);
  oneway void protect(int fd);
  int urlTest();
  oneway void resetTrafficStats();
  boolean getTrafficStatsEnabled();
}
