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

package com.hineeks.nexaproxy

import android.annotation.SuppressLint
import android.app.*
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.LinkAddress
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import android.os.StrictMode
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import go.Seq
import com.hineeks.nexaproxy.bg.NexaProxyConnection
import com.hineeks.nexaproxy.bg.SubscriptionUpdater
import com.hineeks.nexaproxy.bg.test.DebugInstance
import com.hineeks.nexaproxy.database.DataStore
import com.hineeks.nexaproxy.database.NexaProxyDatabase
import com.hineeks.nexaproxy.ktx.Logs
import com.hineeks.nexaproxy.ktx.readableMessage
import com.hineeks.nexaproxy.ktx.runOnDefaultDispatcher
import com.hineeks.nexaproxy.ui.MainActivity
import com.hineeks.nexaproxy.utils.CrashHandler
import com.hineeks.nexaproxy.utils.DefaultNetworkListener
import com.hineeks.nexaproxy.utils.DeviceStorageApp
import com.hineeks.nexaproxy.utils.PackageCache
import com.hineeks.nexaproxy.utils.Theme
import kotlinx.coroutines.DEBUG_PROPERTY_NAME
import kotlinx.coroutines.DEBUG_PROPERTY_VALUE_ON
import libexclavecore.AndroidCAStore
import libexclavecore.Libexclavecore
import libexclavecore.UidDumper
import java.io.ByteArrayOutputStream
import java.net.Inet6Address
import java.net.InetSocketAddress
import java.security.KeyStore
import androidx.work.Configuration as WorkConfiguration

class NexaProxy : Application(),
    UidDumper,
    AndroidCAStore,
    WorkConfiguration.Provider {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        application = this
    }

    val externalAssets by lazy { getExternalFilesDir(null) ?: filesDir }

    override fun onCreate() {
        super.onCreate()

        System.setProperty(DEBUG_PROPERTY_NAME, DEBUG_PROPERTY_VALUE_ON)
        Thread.setDefaultUncaughtExceptionHandler(CrashHandler)
        DataStore.init()
        updateNotificationChannels()
        Seq.setContext(this)

        if (DataStore.getInstalledPackagesInited) {
            runOnDefaultDispatcher {
                PackageCache.register()
            }
        }

        val processName = if (Build.VERSION.SDK_INT >= 28) {
            getProcessName()
        } else {
            @SuppressLint("PrivateApi")
            try {
                Class.forName("android.app.ActivityThread").getDeclaredMethod("currentProcessName").invoke(null) as String
            } catch (_: Exception) {
                BuildConfig.APPLICATION_ID
            }
        }

        val isMainProcess = processName == BuildConfig.APPLICATION_ID

        if (!isMainProcess) {
            Libexclavecore.setUidDumper(this, Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            if (DataStore.enableDebug && DataStore.pprofServer.isNotEmpty()) {
                DebugInstance().launch()
            }
        }

        if (!isMainProcess) runOnDefaultDispatcher {
            runCatching {
                SubscriptionUpdater.reconfigureUpdater()
            }
        }

        Libexclavecore.setenv("exclave.conf.geoloader", "memconservative")
        externalAssets.mkdirs()
        Libexclavecore.initializeV2Ray(
            filesDir.absolutePath + "/",
            externalAssets.absolutePath + "/",
            "exclave-core/",
        )

        try {
            if (DataStore.providerRootCA == RootCAProvider.SYSTEM_AND_USER) {
                Libexclavecore.registerAndroidCAStore(this)
            }
            Libexclavecore.updateSystemRoots(DataStore.providerRootCA)
        } catch (e: Exception) {
            Toast.makeText(this, e.readableMessage, Toast.LENGTH_LONG).show()
        }

        Theme.apply(this)
        Theme.applyNightTheme()

        if (BuildConfig.DEBUG) StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .detectLeakedRegistrationObjects()
                .penaltyLog()
                .build()
        )
    }

    override fun certificates(): ByteArray {
        try {
            val keyStore = KeyStore.getInstance("AndroidCAStore")
            val byteArrayStream = ByteArrayOutputStream()
            keyStore.load(null, null)
            val aliases = keyStore.aliases()
            while (aliases.hasMoreElements()) {
                byteArrayStream.write(keyStore.getCertificate(aliases.nextElement()).encoded)
            }
            return byteArrayStream.toByteArray()
        } catch (e: Exception) {
            throw e
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun dumpUid(
        ipProto: Int, srcIp: String, srcPort: Int, destIp: String, destPort: Int
    ): Int {
        return connectivity.getConnectionOwnerUid(
            ipProto, InetSocketAddress(srcIp, srcPort), InetSocketAddress(destIp, destPort)
        )
    }

    override fun getPackageName(uid: Int): String? {
        PackageCache.awaitLoadSync()
        if (uid == 1000) {
            return "android"
        }
        val packageNames = PackageCache.uidMap[uid]
        if (!packageNames.isNullOrEmpty()) for (packageName in packageNames) {
            return packageName
        }
        return null
    }

    fun getPackageInfo(packageName: String) = packageManager.getPackageInfo(
        packageName, if (Build.VERSION.SDK_INT >= 28) PackageManager.GET_SIGNING_CERTIFICATES
        else @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
    )!!

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateNotificationChannels()
    }

    override val workManagerConfiguration: androidx.work.Configuration
        get() {
            return WorkConfiguration.Builder()
                // .setDefaultProcessName("${BuildConfig.APPLICATION_ID}:bg")
                .setDefaultProcessName(BuildConfig.APPLICATION_ID)
                .build()
        }

    @SuppressLint("InlinedApi")
    companion object {

        @Volatile
        var started = false

        lateinit var application: NexaProxy

        val isTv by lazy {
            uiMode.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION
        }

        val deviceStorage by lazy {
            if (Build.VERSION.SDK_INT < 24) application else DeviceStorageApp(application)
        }

        val configureIntent: (Context) -> PendingIntent by lazy {
            {
                PendingIntent.getActivity(
                    it,
                    0,
                    Intent(
                        application, MainActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT),
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
                )
            }
        }
        val activity by lazy { application.getSystemService<ActivityManager>()!! }
        val clipboard by lazy { application.getSystemService<ClipboardManager>()!! }
        val connectivity by lazy { application.getSystemService<ConnectivityManager>()!! }

        val ime by lazy { application.getSystemService<InputMethodManager>()!! }
        val notification by lazy { application.getSystemService<NotificationManager>()!! }
        val uiMode by lazy { application.getSystemService<UiModeManager>()!! }
        val power by lazy { application.getSystemService<PowerManager>()!! }
        val wifi by lazy { application.getSystemService<WifiManager>()!! }
        val location by lazy { application.getSystemService<LocationManager>()!! }
        val locale by lazy { application.getSystemService<LocaleManager>()!! }

        val currentProfile get() = NexaProxyDatabase.proxyDao.getById(DataStore.selectedProxy)

        fun getClipboardText(): String {
            return clipboard.primaryClip?.takeIf { it.itemCount > 0 }
                ?.getItemAt(0)?.text?.toString() ?: ""
        }

        fun trySetPrimaryClip(clip: String) = try {
            clipboard.setPrimaryClip(ClipData.newPlainText(null, clip))
            true
        } catch (e: RuntimeException) {
            Logs.w(e)
            false
        }

        fun updateNotificationChannels() {
            if (Build.VERSION.SDK_INT >= 26) @RequiresApi(26) {
                notification.createNotificationChannels(
                    listOf(
                        NotificationChannel(
                            "service-vpn",
                            application.getText(R.string.service_vpn),
                            if (Build.VERSION.SDK_INT >= 28) NotificationManager.IMPORTANCE_MIN
                            else NotificationManager.IMPORTANCE_LOW
                        ),   // #1355
                        NotificationChannel(
                            "service-proxy",
                            application.getText(R.string.service_proxy),
                            NotificationManager.IMPORTANCE_LOW
                        ), NotificationChannel(
                            "service-subscription",
                            application.getText(R.string.service_subscription),
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                    )
                )
            }
        }

        var currentNetwork: Network? = null
        var currentLinkAddresses: Set<LinkAddress>? = null

        fun reloadNetwork(network: Network?) {
            val capabilities = connectivity.getNetworkCapabilities(network) ?: return
            val linkProperties = connectivity.getLinkProperties(network) ?: return

            val networkType = when {
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> "wifi"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "data"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> "bluetooth"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_USB) -> "usb"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_SATELLITE) -> "satellite"
                // capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE) -> "wifiaware"
                // capabilities.hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN) -> "lowpan"
                // capabilities.hasTransport(NetworkCapabilities.TRANSPORT_THREAD) -> "thread"
                else -> ""
            }
            Libexclavecore.setNetworkType(networkType)

            var ssid = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                DefaultNetworkListener.ssid ?: ""
            } else {
                @Suppress("DEPRECATION")
                wifi.connectionInfo?.ssid ?: ""
            }
            if (ssid == WifiManager.UNKNOWN_SSID) ssid = ""
            if (ssid.length >= 2 && ssid.first() == '\"' && ssid.last() == '\"') {
                ssid = ssid.substring(1, ssid.length - 1)
            }
            Libexclavecore.setSSID(ssid)

            val linkAddresses = linkProperties.linkAddresses.toSet()
            if (DataStore.logLevel == LogLevel.DEBUG && currentLinkAddresses != linkAddresses) {
                Log.d("NexaProxy", "updated link addresses: " + linkAddresses.joinToString(" ", "[", "]") { it.address.hostAddress!! + "/" + it.prefixLength })
            }
            Libexclavecore.setDiscardIPv6(!linkAddresses.any { it.address is Inet6Address && !it.address.isLinkLocalAddress })

            if (DataStore.interruptReusedConnections) {
                val networkChanged = currentNetwork != null && currentNetwork != network
                val linkAddressesChanged = currentLinkAddresses != null && !linkAddresses.containsAll(currentLinkAddresses!!)
                if (networkChanged || linkAddressesChanged) {
                    if (DataStore.logLevel == LogLevel.DEBUG) {
                        Log.d("NexaProxy", "network changed, interrupt reused connections")
                    }
                    Libexclavecore.interfaceUpdate()
                }
            }

            currentNetwork = network
            currentLinkAddresses = linkAddresses
        }

        fun startService() = ContextCompat.startForegroundService(
            application, Intent(application, NexaProxyConnection.serviceClass)
        )

        fun reloadService() =
            application.sendBroadcast(Intent(Action.RELOAD).setPackage(application.packageName))

        fun stopService() =
            application.sendBroadcast(Intent(Action.CLOSE).setPackage(application.packageName))

    }

}