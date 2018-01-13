/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/net/IConnectivityManager.aidl
 */
package android.net;
/**
 * Interface that answers queries about, and allows changing, the
 * state of network connectivity.
 */

/**
 * {@hide}
 */
public interface IConnectivityManager extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.net.IConnectivityManager {
        private static final java.lang.String DESCRIPTOR = "android.net.IConnectivityManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.net.IConnectivityManager interface,
         * generating a proxy if needed.
         */
        public static android.net.IConnectivityManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof android.net.IConnectivityManager))) {
                return ((android.net.IConnectivityManager) iin);
            }
            return new android.net.IConnectivityManager.Stub.Proxy(obj);
        }

        @Override
        public android.os.IBinder asBinder() {
            return this;
        }

        @Override
        public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException {
            switch (code) {
                case INTERFACE_TRANSACTION: {
                    reply.writeString(DESCRIPTOR);
                    return true;
                }
                case TRANSACTION_setNetworkPreference: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    this.setNetworkPreference(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getNetworkPreference: {
                    data.enforceInterface(DESCRIPTOR);
                    int _result = this.getNetworkPreference();
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_getActiveNetworkInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkInfo _result = this.getActiveNetworkInfo();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getActiveNetworkInfoForUid: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    android.net.NetworkInfo _result = this.getActiveNetworkInfoForUid(_arg0);
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getNetworkInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    android.net.NetworkInfo _result = this.getNetworkInfo(_arg0);
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getAllNetworkInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkInfo[] _result = this.getAllNetworkInfo();
                    reply.writeNoException();
                    reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    return true;
                }
                case TRANSACTION_isNetworkSupported: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _result = this.isNetworkSupported(_arg0);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_getActiveLinkProperties: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.LinkProperties _result = this.getActiveLinkProperties();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getLinkProperties: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    android.net.LinkProperties _result = this.getLinkProperties(_arg0);
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_getAllNetworkState: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkState[] _result = this.getAllNetworkState();
                    reply.writeNoException();
                    reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    return true;
                }
                case TRANSACTION_getActiveNetworkQuotaInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkQuotaInfo _result = this.getActiveNetworkQuotaInfo();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_isActiveNetworkMetered: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.isActiveNetworkMetered();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_setRadios: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    boolean _result = this.setRadios(_arg0);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_setRadio: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    boolean _result = this.setRadio(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_startUsingNetworkFeature: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    android.os.IBinder _arg2;
                    _arg2 = data.readStrongBinder();
                    int _result = this.startUsingNetworkFeature(_arg0, _arg1, _arg2);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_stopUsingNetworkFeature: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    int _result = this.stopUsingNetworkFeature(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_requestRouteToHost: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    boolean _result = this.requestRouteToHost(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_requestRouteToHostAddress: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    byte[] _arg1;
                    _arg1 = data.createByteArray();
                    boolean _result = this.requestRouteToHostAddress(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_getMobileDataEnabled: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.getMobileDataEnabled();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_setMobileDataEnabled: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.setMobileDataEnabled(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setPolicyDataEnable: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.setPolicyDataEnable(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_tether: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    int _result = this.tether(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_untether: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    int _result = this.untether(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_getLastTetherError: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    int _result = this.getLastTetherError(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_isTetheringSupported: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.isTetheringSupported();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_getTetherableIfaces: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetherableIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetheredIfaces: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetheredIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetheredIfacePairs: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetheredIfacePairs();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetheringErroredIfaces: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetheringErroredIfaces();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetherableUsbRegexs: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetherableUsbRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetherableWifiRegexs: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetherableWifiRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_getTetherableBluetoothRegexs: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _result = this.getTetherableBluetoothRegexs();
                    reply.writeNoException();
                    reply.writeStringArray(_result);
                    return true;
                }
                case TRANSACTION_setUsbTethering: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    int _result = this.setUsbTethering(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_requestNetworkTransitionWakelock: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    this.requestNetworkTransitionWakelock(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_reportInetCondition: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.reportInetCondition(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getGlobalProxy: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.ProxyProperties _result = this.getGlobalProxy();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_setGlobalProxy: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.ProxyProperties _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.net.ProxyProperties.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.setGlobalProxy(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getProxy: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.ProxyProperties _result = this.getProxy();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_setDataDependency: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _arg1;
                    _arg1 = (0 != data.readInt());
                    this.setDataDependency(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_protectVpn: {
                    data.enforceInterface(DESCRIPTOR);
                    android.os.ParcelFileDescriptor _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.os.ParcelFileDescriptor.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _result = this.protectVpn(_arg0);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_prepareVpn: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String _arg0;
                    _arg0 = data.readString();
                    java.lang.String _arg1;
                    _arg1 = data.readString();
                    boolean _result = this.prepareVpn(_arg0, _arg1);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_establishVpn: {
                    data.enforceInterface(DESCRIPTOR);
                    com.android.internal.net.VpnConfig _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.android.internal.net.VpnConfig.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    android.os.ParcelFileDescriptor _result = this.establishVpn(_arg0);
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_startLegacyVpn: {
                    data.enforceInterface(DESCRIPTOR);
                    com.android.internal.net.VpnProfile _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = com.android.internal.net.VpnProfile.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.startLegacyVpn(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getLegacyVpnInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    com.android.internal.net.LegacyVpnInfo _result = this.getLegacyVpnInfo();
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_updateLockdownVpn: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.updateLockdownVpn();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_captivePortalCheckComplete: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkInfo _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = null;
                    } else {
                        _arg0 = null;
                    }
                    this.captivePortalCheckComplete(_arg0);
                    reply.writeNoException();
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements android.net.IConnectivityManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void setNetworkPreference(int pref) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(pref);
                    mRemote.transact(Stub.TRANSACTION_setNetworkPreference, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getNetworkPreference() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getNetworkPreference, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkInfo getActiveNetworkInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getActiveNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = null;
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkInfo getActiveNetworkInfoForUid(int uid) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(uid);
                    mRemote.transact(Stub.TRANSACTION_getActiveNetworkInfoForUid, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = null;
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    mRemote.transact(Stub.TRANSACTION_getNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = null;
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkInfo[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getAllNetworkInfo, _data, _reply, 0);
                    _reply.readException();
                    _result = null;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isNetworkSupported(int networkType) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    mRemote.transact(Stub.TRANSACTION_isNetworkSupported, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.LinkProperties _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getActiveLinkProperties, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.net.LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.LinkProperties getLinkProperties(int networkType) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.LinkProperties _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    mRemote.transact(Stub.TRANSACTION_getLinkProperties, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.net.LinkProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkState[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getAllNetworkState, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArray(android.net.NetworkState.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkQuotaInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getActiveNetworkQuotaInfo, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.net.NetworkQuotaInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isActiveNetworkMetered() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isActiveNetworkMetered, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean setRadios(boolean onOff) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((onOff) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setRadios, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean setRadio(int networkType, boolean turnOn) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(((turnOn) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setRadio, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int startUsingNetworkFeature(int networkType, java.lang.String feature, android.os.IBinder binder) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeString(feature);
                    _data.writeStrongBinder(binder);
                    mRemote.transact(Stub.TRANSACTION_startUsingNetworkFeature, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int stopUsingNetworkFeature(int networkType, java.lang.String feature) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeString(feature);
                    mRemote.transact(Stub.TRANSACTION_stopUsingNetworkFeature, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean requestRouteToHost(int networkType, int hostAddress) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(hostAddress);
                    mRemote.transact(Stub.TRANSACTION_requestRouteToHost, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeByteArray(hostAddress);
                    mRemote.transact(Stub.TRANSACTION_requestRouteToHostAddress, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean getMobileDataEnabled() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getMobileDataEnabled, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void setMobileDataEnabled(boolean enabled) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((enabled) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setMobileDataEnabled, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * Policy control over specific {@link NetworkStateTracker}.
             */
            @Override
            public void setPolicyDataEnable(int networkType, boolean enabled) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(((enabled) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setPolicyDataEnable, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int tether(java.lang.String iface) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(iface);
                    mRemote.transact(Stub.TRANSACTION_tether, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int untether(java.lang.String iface) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(iface);
                    mRemote.transact(Stub.TRANSACTION_untether, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int getLastTetherError(java.lang.String iface) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(iface);
                    mRemote.transact(Stub.TRANSACTION_getLastTetherError, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isTetheringSupported() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_isTetheringSupported, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetherableIfaces() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetherableIfaces, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetheredIfaces() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetheredIfaces, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            /**
             * Return list of interface pairs that are actively tethered.  Even indexes are
             * remote interface, and odd indexes are corresponding local interfaces.
             */
            @Override
            public java.lang.String[] getTetheredIfacePairs() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetheredIfacePairs, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetheringErroredIfaces() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetheringErroredIfaces, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetherableUsbRegexs() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetherableUsbRegexs, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetherableWifiRegexs() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetherableWifiRegexs, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public java.lang.String[] getTetherableBluetoothRegexs() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                java.lang.String[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getTetherableBluetoothRegexs, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createStringArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int setUsbTethering(boolean enable) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((enable) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setUsbTethering, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void requestNetworkTransitionWakelock(java.lang.String forWhom) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(forWhom);
                    mRemote.transact(Stub.TRANSACTION_requestNetworkTransitionWakelock, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void reportInetCondition(int networkType, int percentage) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(percentage);
                    mRemote.transact(Stub.TRANSACTION_reportInetCondition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public android.net.ProxyProperties getGlobalProxy() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.ProxyProperties _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getGlobalProxy, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.net.ProxyProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void setGlobalProxy(android.net.ProxyProperties p) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((p != null)) {
                        _data.writeInt(1);
                        p.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_setGlobalProxy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public android.net.ProxyProperties getProxy() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.ProxyProperties _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getProxy, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.net.ProxyProperties.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void setDataDependency(int networkType, boolean met) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(networkType);
                    _data.writeInt(((met) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setDataDependency, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean protectVpn(android.os.ParcelFileDescriptor socket) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((socket != null)) {
                        _data.writeInt(1);
                        socket.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_protectVpn, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean prepareVpn(java.lang.String oldPackage, java.lang.String newPackage) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeString(oldPackage);
                    _data.writeString(newPackage);
                    mRemote.transact(Stub.TRANSACTION_prepareVpn, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.os.ParcelFileDescriptor _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((config != null)) {
                        _data.writeInt(1);
                        config.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_establishVpn, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = android.os.ParcelFileDescriptor.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((profile != null)) {
                        _data.writeInt(1);
                        profile.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_startLegacyVpn, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                com.android.internal.net.LegacyVpnInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getLegacyVpnInfo, _data, _reply, 0);
                    _reply.readException();
                    if ((0 != _reply.readInt())) {
                        _result = com.android.internal.net.LegacyVpnInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean updateLockdownVpn() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_updateLockdownVpn, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void captivePortalCheckComplete(android.net.NetworkInfo info) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((info != null)) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_captivePortalCheckComplete, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_setNetworkPreference = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getNetworkPreference = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_getActiveNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_getActiveNetworkInfoForUid = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_getNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_getAllNetworkInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_isNetworkSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_getActiveLinkProperties = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_getLinkProperties = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_getAllNetworkState = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_getActiveNetworkQuotaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_isActiveNetworkMetered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_setRadios = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
        static final int TRANSACTION_setRadio = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
        static final int TRANSACTION_startUsingNetworkFeature = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
        static final int TRANSACTION_stopUsingNetworkFeature = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
        static final int TRANSACTION_requestRouteToHost = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
        static final int TRANSACTION_requestRouteToHostAddress = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
        static final int TRANSACTION_getMobileDataEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
        static final int TRANSACTION_setMobileDataEnabled = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
        static final int TRANSACTION_setPolicyDataEnable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
        static final int TRANSACTION_tether = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
        static final int TRANSACTION_untether = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
        static final int TRANSACTION_getLastTetherError = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
        static final int TRANSACTION_isTetheringSupported = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
        static final int TRANSACTION_getTetherableIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
        static final int TRANSACTION_getTetheredIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
        static final int TRANSACTION_getTetheredIfacePairs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
        static final int TRANSACTION_getTetheringErroredIfaces = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
        static final int TRANSACTION_getTetherableUsbRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
        static final int TRANSACTION_getTetherableWifiRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 30);
        static final int TRANSACTION_getTetherableBluetoothRegexs = (android.os.IBinder.FIRST_CALL_TRANSACTION + 31);
        static final int TRANSACTION_setUsbTethering = (android.os.IBinder.FIRST_CALL_TRANSACTION + 32);
        static final int TRANSACTION_requestNetworkTransitionWakelock = (android.os.IBinder.FIRST_CALL_TRANSACTION + 33);
        static final int TRANSACTION_reportInetCondition = (android.os.IBinder.FIRST_CALL_TRANSACTION + 34);
        static final int TRANSACTION_getGlobalProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 35);
        static final int TRANSACTION_setGlobalProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 36);
        static final int TRANSACTION_getProxy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 37);
        static final int TRANSACTION_setDataDependency = (android.os.IBinder.FIRST_CALL_TRANSACTION + 38);
        static final int TRANSACTION_protectVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 39);
        static final int TRANSACTION_prepareVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 40);
        static final int TRANSACTION_establishVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 41);
        static final int TRANSACTION_startLegacyVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 42);
        static final int TRANSACTION_getLegacyVpnInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 43);
        static final int TRANSACTION_updateLockdownVpn = (android.os.IBinder.FIRST_CALL_TRANSACTION + 44);
        static final int TRANSACTION_captivePortalCheckComplete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 45);
    }

    public void setNetworkPreference(int pref) throws android.os.RemoteException;

    public int getNetworkPreference() throws android.os.RemoteException;

    public android.net.NetworkInfo getActiveNetworkInfo() throws android.os.RemoteException;

    public android.net.NetworkInfo getActiveNetworkInfoForUid(int uid) throws android.os.RemoteException;

    public android.net.NetworkInfo getNetworkInfo(int networkType) throws android.os.RemoteException;

    public android.net.NetworkInfo[] getAllNetworkInfo() throws android.os.RemoteException;

    public boolean isNetworkSupported(int networkType) throws android.os.RemoteException;

    public android.net.LinkProperties getActiveLinkProperties() throws android.os.RemoteException;

    public android.net.LinkProperties getLinkProperties(int networkType) throws android.os.RemoteException;

    public android.net.NetworkState[] getAllNetworkState() throws android.os.RemoteException;

    public android.net.NetworkQuotaInfo getActiveNetworkQuotaInfo() throws android.os.RemoteException;

    public boolean isActiveNetworkMetered() throws android.os.RemoteException;

    public boolean setRadios(boolean onOff) throws android.os.RemoteException;

    public boolean setRadio(int networkType, boolean turnOn) throws android.os.RemoteException;

    public int startUsingNetworkFeature(int networkType, java.lang.String feature, android.os.IBinder binder) throws android.os.RemoteException;

    public int stopUsingNetworkFeature(int networkType, java.lang.String feature) throws android.os.RemoteException;

    public boolean requestRouteToHost(int networkType, int hostAddress) throws android.os.RemoteException;

    public boolean requestRouteToHostAddress(int networkType, byte[] hostAddress) throws android.os.RemoteException;

    public boolean getMobileDataEnabled() throws android.os.RemoteException;

    public void setMobileDataEnabled(boolean enabled) throws android.os.RemoteException;

    /**
     * Policy control over specific {@link NetworkStateTracker}.
     */
    public void setPolicyDataEnable(int networkType, boolean enabled) throws android.os.RemoteException;

    public int tether(java.lang.String iface) throws android.os.RemoteException;

    public int untether(java.lang.String iface) throws android.os.RemoteException;

    public int getLastTetherError(java.lang.String iface) throws android.os.RemoteException;

    public boolean isTetheringSupported() throws android.os.RemoteException;

    public java.lang.String[] getTetherableIfaces() throws android.os.RemoteException;

    public java.lang.String[] getTetheredIfaces() throws android.os.RemoteException;

    /**
     * Return list of interface pairs that are actively tethered.  Even indexes are
     * remote interface, and odd indexes are corresponding local interfaces.
     */
    public java.lang.String[] getTetheredIfacePairs() throws android.os.RemoteException;

    public java.lang.String[] getTetheringErroredIfaces() throws android.os.RemoteException;

    public java.lang.String[] getTetherableUsbRegexs() throws android.os.RemoteException;

    public java.lang.String[] getTetherableWifiRegexs() throws android.os.RemoteException;

    public java.lang.String[] getTetherableBluetoothRegexs() throws android.os.RemoteException;

    public int setUsbTethering(boolean enable) throws android.os.RemoteException;

    public void requestNetworkTransitionWakelock(java.lang.String forWhom) throws android.os.RemoteException;

    public void reportInetCondition(int networkType, int percentage) throws android.os.RemoteException;

    public android.net.ProxyProperties getGlobalProxy() throws android.os.RemoteException;

    public void setGlobalProxy(android.net.ProxyProperties p) throws android.os.RemoteException;

    public android.net.ProxyProperties getProxy() throws android.os.RemoteException;

    public void setDataDependency(int networkType, boolean met) throws android.os.RemoteException;

    public boolean protectVpn(android.os.ParcelFileDescriptor socket) throws android.os.RemoteException;

    public boolean prepareVpn(java.lang.String oldPackage, java.lang.String newPackage) throws android.os.RemoteException;

    public android.os.ParcelFileDescriptor establishVpn(com.android.internal.net.VpnConfig config) throws android.os.RemoteException;

    public void startLegacyVpn(com.android.internal.net.VpnProfile profile) throws android.os.RemoteException;

    public com.android.internal.net.LegacyVpnInfo getLegacyVpnInfo() throws android.os.RemoteException;

    public boolean updateLockdownVpn() throws android.os.RemoteException;

    public void captivePortalCheckComplete(android.net.NetworkInfo info) throws android.os.RemoteException;
}
