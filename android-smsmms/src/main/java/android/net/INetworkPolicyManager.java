/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/net/INetworkPolicyManager.aidl
 */
package android.net;

/**
 * Interface that creates and modifies network policy rules.
 * <p/>
 * {@hide}
 */
public interface INetworkPolicyManager extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.net.INetworkPolicyManager {
        private static final java.lang.String DESCRIPTOR = "android.net.INetworkPolicyManager";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.net.INetworkPolicyManager interface,
         * generating a proxy if needed.
         */
        public static android.net.INetworkPolicyManager asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof android.net.INetworkPolicyManager))) {
                return ((android.net.INetworkPolicyManager) iin);
            }
            return new android.net.INetworkPolicyManager.Stub.Proxy(obj);
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
                case TRANSACTION_setUidPolicy: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.setUidPolicy(_arg0, _arg1);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getUidPolicy: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _result = this.getUidPolicy(_arg0);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                }
                case TRANSACTION_getUidsWithPolicy: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int[] _result = this.getUidsWithPolicy(_arg0);
                    reply.writeNoException();
                    reply.writeIntArray(_result);
                    return true;
                }
                case TRANSACTION_isUidForeground: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    boolean _result = this.isUidForeground(_arg0);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_registerListener: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.INetworkPolicyListener _arg0;
                    _arg0 = android.net.INetworkPolicyListener.Stub.asInterface(data.readStrongBinder());
                    this.registerListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_unregisterListener: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.INetworkPolicyListener _arg0;
                    _arg0 = android.net.INetworkPolicyListener.Stub.asInterface(data.readStrongBinder());
                    this.unregisterListener(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setNetworkPolicies: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkPolicy[] _arg0;
                    _arg0 = data.createTypedArray(android.net.NetworkPolicy.CREATOR);
                    this.setNetworkPolicies(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getNetworkPolicies: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkPolicy[] _result = this.getNetworkPolicies();
                    reply.writeNoException();
                    reply.writeTypedArray(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    return true;
                }
                case TRANSACTION_snoozeLimit: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkTemplate _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.net.NetworkTemplate.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    this.snoozeLimit(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_setRestrictBackground: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.setRestrictBackground(_arg0);
                    reply.writeNoException();
                    return true;
                }
                case TRANSACTION_getRestrictBackground: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = this.getRestrictBackground();
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
                case TRANSACTION_getNetworkQuotaInfo: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkState _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.net.NetworkState.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    android.net.NetworkQuotaInfo _result = this.getNetworkQuotaInfo(_arg0);
                    reply.writeNoException();
                    if ((_result != null)) {
                        reply.writeInt(1);
                        _result.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                }
                case TRANSACTION_isNetworkMetered: {
                    data.enforceInterface(DESCRIPTOR);
                    android.net.NetworkState _arg0;
                    if ((0 != data.readInt())) {
                        _arg0 = android.net.NetworkState.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    boolean _result = this.isNetworkMetered(_arg0);
                    reply.writeNoException();
                    reply.writeInt(((_result) ? (1) : (0)));
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements android.net.INetworkPolicyManager {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            /**
             * Control UID policies.
             */
            @Override
            public void setUidPolicy(int uid, int policy) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(policy);
                    mRemote.transact(Stub.TRANSACTION_setUidPolicy, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public int getUidPolicy(int uid) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(uid);
                    mRemote.transact(Stub.TRANSACTION_getUidPolicy, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public int[] getUidsWithPolicy(int policy) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                int[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(policy);
                    mRemote.transact(Stub.TRANSACTION_getUidsWithPolicy, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createIntArray();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public boolean isUidForeground(int uid) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(uid);
                    mRemote.transact(Stub.TRANSACTION_isUidForeground, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public void registerListener(android.net.INetworkPolicyListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_registerListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public void unregisterListener(android.net.INetworkPolicyListener listener) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStrongBinder((((listener != null)) ? (listener.asBinder()) : (null)));
                    mRemote.transact(Stub.TRANSACTION_unregisterListener, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * Control network policies atomically.
             */
            @Override
            public void setNetworkPolicies(android.net.NetworkPolicy[] policies) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeTypedArray(policies, 0);
                    mRemote.transact(Stub.TRANSACTION_setNetworkPolicies, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public android.net.NetworkPolicy[] getNetworkPolicies() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkPolicy[] _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getNetworkPolicies, _data, _reply, 0);
                    _reply.readException();
                    _result = _reply.createTypedArray(android.net.NetworkPolicy.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            /**
             * Snooze limit on policy matching given template.
             */
            @Override
            public void snoozeLimit(android.net.NetworkTemplate template) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((template != null)) {
                        _data.writeInt(1);
                        template.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_snoozeLimit, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            /**
             * Control if background data is restricted system-wide.
             */
            @Override
            public void setRestrictBackground(boolean restrictBackground) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((restrictBackground) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_setRestrictBackground, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override
            public boolean getRestrictBackground() throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    mRemote.transact(Stub.TRANSACTION_getRestrictBackground, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }

            @Override
            public android.net.NetworkQuotaInfo getNetworkQuotaInfo(android.net.NetworkState state) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                android.net.NetworkQuotaInfo _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((state != null)) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_getNetworkQuotaInfo, _data, _reply, 0);
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
            public boolean isNetworkMetered(android.net.NetworkState state) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                android.os.Parcel _reply = android.os.Parcel.obtain();
                boolean _result;
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    if ((state != null)) {
                        _data.writeInt(1);
                        state.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    mRemote.transact(Stub.TRANSACTION_isNetworkMetered, _data, _reply, 0);
                    _reply.readException();
                    _result = (0 != _reply.readInt());
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
                return _result;
            }
        }

        static final int TRANSACTION_setUidPolicy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_getUidPolicy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_getUidsWithPolicy = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
        static final int TRANSACTION_isUidForeground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
        static final int TRANSACTION_registerListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
        static final int TRANSACTION_unregisterListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
        static final int TRANSACTION_setNetworkPolicies = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
        static final int TRANSACTION_getNetworkPolicies = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
        static final int TRANSACTION_snoozeLimit = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
        static final int TRANSACTION_setRestrictBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
        static final int TRANSACTION_getRestrictBackground = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
        static final int TRANSACTION_getNetworkQuotaInfo = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
        static final int TRANSACTION_isNetworkMetered = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    }

    /**
     * Control UID policies.
     */
    public void setUidPolicy(int uid, int policy) throws android.os.RemoteException;

    public int getUidPolicy(int uid) throws android.os.RemoteException;

    public int[] getUidsWithPolicy(int policy) throws android.os.RemoteException;

    public boolean isUidForeground(int uid) throws android.os.RemoteException;

    public void registerListener(android.net.INetworkPolicyListener listener) throws android.os.RemoteException;

    public void unregisterListener(android.net.INetworkPolicyListener listener) throws android.os.RemoteException;

    /**
     * Control network policies atomically.
     */
    public void setNetworkPolicies(android.net.NetworkPolicy[] policies) throws android.os.RemoteException;

    public android.net.NetworkPolicy[] getNetworkPolicies() throws android.os.RemoteException;

    /**
     * Snooze limit on policy matching given template.
     */
    public void snoozeLimit(android.net.NetworkTemplate template) throws android.os.RemoteException;

    /**
     * Control if background data is restricted system-wide.
     */
    public void setRestrictBackground(boolean restrictBackground) throws android.os.RemoteException;

    public boolean getRestrictBackground() throws android.os.RemoteException;

    public android.net.NetworkQuotaInfo getNetworkQuotaInfo(android.net.NetworkState state) throws android.os.RemoteException;

    public boolean isNetworkMetered(android.net.NetworkState state) throws android.os.RemoteException;
}
