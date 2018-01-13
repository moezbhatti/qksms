/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: frameworks/base/core/java/android/net/INetworkPolicyListener.aidl
 */
package android.net;

/**
 * {@hide}
 */
public interface INetworkPolicyListener extends android.os.IInterface {
    /**
     * Local-side IPC implementation stub class.
     */
    public static abstract class Stub extends android.os.Binder implements android.net.INetworkPolicyListener {
        private static final java.lang.String DESCRIPTOR = "android.net.INetworkPolicyListener";

        /**
         * Construct the stub at attach it to the interface.
         */
        public Stub() {
            this.attachInterface(this, DESCRIPTOR);
        }

        /**
         * Cast an IBinder object into an android.net.INetworkPolicyListener interface,
         * generating a proxy if needed.
         */
        public static android.net.INetworkPolicyListener asInterface(android.os.IBinder obj) {
            if ((obj == null)) {
                return null;
            }
            android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (((iin != null) && (iin instanceof android.net.INetworkPolicyListener))) {
                return ((android.net.INetworkPolicyListener) iin);
            }
            return new android.net.INetworkPolicyListener.Stub.Proxy(obj);
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
                case TRANSACTION_onUidRulesChanged: {
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0;
                    _arg0 = data.readInt();
                    int _arg1;
                    _arg1 = data.readInt();
                    this.onUidRulesChanged(_arg0, _arg1);
                    return true;
                }
                case TRANSACTION_onMeteredIfacesChanged: {
                    data.enforceInterface(DESCRIPTOR);
                    java.lang.String[] _arg0;
                    _arg0 = data.createStringArray();
                    this.onMeteredIfacesChanged(_arg0);
                    return true;
                }
                case TRANSACTION_onRestrictBackgroundChanged: {
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg0;
                    _arg0 = (0 != data.readInt());
                    this.onRestrictBackgroundChanged(_arg0);
                    return true;
                }
            }
            return super.onTransact(code, data, reply, flags);
        }

        private static class Proxy implements android.net.INetworkPolicyListener {
            private android.os.IBinder mRemote;

            Proxy(android.os.IBinder remote) {
                mRemote = remote;
            }

            @Override
            public android.os.IBinder asBinder() {
                return mRemote;
            }

            @Override
            public void onUidRulesChanged(int uid, int uidRules) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(uidRules);
                    mRemote.transact(Stub.TRANSACTION_onUidRulesChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }

            @Override
            public void onMeteredIfacesChanged(java.lang.String[] meteredIfaces) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeStringArray(meteredIfaces);
                    mRemote.transact(Stub.TRANSACTION_onMeteredIfacesChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }

            @Override
            public void onRestrictBackgroundChanged(boolean restrictBackground) throws android.os.RemoteException {
                android.os.Parcel _data = android.os.Parcel.obtain();
                try {
                    _data.writeInterfaceToken(DESCRIPTOR);
                    _data.writeInt(((restrictBackground) ? (1) : (0)));
                    mRemote.transact(Stub.TRANSACTION_onRestrictBackgroundChanged, _data, null, android.os.IBinder.FLAG_ONEWAY);
                } finally {
                    _data.recycle();
                }
            }
        }

        static final int TRANSACTION_onUidRulesChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
        static final int TRANSACTION_onMeteredIfacesChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
        static final int TRANSACTION_onRestrictBackgroundChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    }

    public void onUidRulesChanged(int uid, int uidRules) throws android.os.RemoteException;

    public void onMeteredIfacesChanged(java.lang.String[] meteredIfaces) throws android.os.RemoteException;

    public void onRestrictBackgroundChanged(boolean restrictBackground) throws android.os.RemoteException;
}
