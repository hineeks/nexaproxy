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

package com.hineeks.nexaproxy.fmt;

import androidx.room.TypeConverter;

import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.ByteBufferOutput;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.hineeks.nexaproxy.database.SubscriptionBean;
import com.hineeks.nexaproxy.fmt.anytls.AnyTLSBean;
import com.hineeks.nexaproxy.fmt.http.HttpBean;
import com.hineeks.nexaproxy.fmt.http3.Http3Bean;
import com.hineeks.nexaproxy.fmt.hysteria2.Hysteria2Bean;
import com.hineeks.nexaproxy.fmt.internal.BalancerBean;
import com.hineeks.nexaproxy.fmt.internal.ChainBean;
import com.hineeks.nexaproxy.fmt.internal.ConfigBean;
import com.hineeks.nexaproxy.fmt.juicity.JuicityBean;
import com.hineeks.nexaproxy.fmt.mieru.MieruBean;
import com.hineeks.nexaproxy.fmt.naive.NaiveBean;
import com.hineeks.nexaproxy.fmt.shadowquic.ShadowQUICBean;
import com.hineeks.nexaproxy.fmt.shadowsocks.ShadowsocksBean;
import com.hineeks.nexaproxy.fmt.shadowsocksr.ShadowsocksRBean;
import com.hineeks.nexaproxy.fmt.socks.SOCKSBean;
import com.hineeks.nexaproxy.fmt.ssh.SSHBean;
import com.hineeks.nexaproxy.fmt.trojan.TrojanBean;
import com.hineeks.nexaproxy.fmt.trusttunnel.TrustTunnelBean;
import com.hineeks.nexaproxy.fmt.snell.SnellBean;
import com.hineeks.nexaproxy.fmt.tuic5.Tuic5Bean;
import com.hineeks.nexaproxy.fmt.v2ray.VLESSBean;
import com.hineeks.nexaproxy.fmt.v2ray.VMessBean;
import com.hineeks.nexaproxy.fmt.wireguard.WireGuardBean;
import com.hineeks.nexaproxy.ktx.KryosKt;
import com.hineeks.nexaproxy.ktx.Logs;

public class KryoConverters {

    private static final byte[] NULL = new byte[0];

    @TypeConverter
    public static byte[] serialize(Serializable bean) {
        if (bean == null) return NULL;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteBufferOutput buffer = KryosKt.byteBuffer(out);
        bean.serializeToBuffer(buffer);
        buffer.flush();
        buffer.close();
        return out.toByteArray();
    }

    public static <T extends Serializable> T deserialize(T bean, byte[] bytes) {
        if (bytes == null) return bean;
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        ByteBufferInput buffer = KryosKt.byteBuffer(input);
        try {
            bean.deserializeFromBuffer(buffer);
        } catch (KryoException e) {
            Logs.INSTANCE.w(e);
        }
        bean.initializeDefaultValues();
        return bean;
    }

    @TypeConverter
    public static SOCKSBean socksDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new SOCKSBean(), bytes);
    }

    @TypeConverter
    public static HttpBean httpDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new HttpBean(), bytes);
    }

    @TypeConverter
    public static ShadowsocksBean shadowsocksDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new ShadowsocksBean(), bytes);
    }

    @TypeConverter
    public static ShadowsocksRBean shadowsocksRDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new ShadowsocksRBean(), bytes);
    }

    @TypeConverter
    public static VMessBean vmessDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new VMessBean(), bytes);
    }

    @TypeConverter
    public static VLESSBean vlessDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new VLESSBean(), bytes);
    }

    @TypeConverter
    public static TrojanBean trojanDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new TrojanBean(), bytes);
    }

    @TypeConverter
    public static NaiveBean naiveDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new NaiveBean(), bytes);
    }

    @TypeConverter
    public static Hysteria2Bean hysteria2Deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new Hysteria2Bean(), bytes);
    }

    @TypeConverter
    public static SSHBean sshDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new SSHBean(), bytes);
    }

    @TypeConverter
    public static WireGuardBean wireguardDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new WireGuardBean(), bytes);
    }

    @TypeConverter
    public static MieruBean mieruDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new MieruBean(), bytes);
    }

    @TypeConverter
    public static Tuic5Bean tuic5Deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new Tuic5Bean(), bytes);
    }

    @TypeConverter
    public static JuicityBean juicityDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new JuicityBean(), bytes);
    }

    @TypeConverter
    public static Http3Bean http3Deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new Http3Bean(), bytes);
    }

    @TypeConverter
    public static AnyTLSBean anytlsDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new AnyTLSBean(), bytes);
    }

    @TypeConverter
    public static ShadowQUICBean shadowquicDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new ShadowQUICBean(), bytes);
    }

    @TypeConverter
    public static TrustTunnelBean trusttunnelDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new TrustTunnelBean(), bytes);
    }

    @TypeConverter
    public static SnellBean snellDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new SnellBean(), bytes);
    }

    @TypeConverter
    public static ConfigBean configDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new ConfigBean(), bytes);
    }

    @TypeConverter
    public static ChainBean chainDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new ChainBean(), bytes);
    }

    @TypeConverter
    public static BalancerBean balancerBeanDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new BalancerBean(), bytes);
    }

    @TypeConverter
    public static SubscriptionBean subscriptionDeserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return null;
        return deserialize(new SubscriptionBean(), bytes);
    }

}
