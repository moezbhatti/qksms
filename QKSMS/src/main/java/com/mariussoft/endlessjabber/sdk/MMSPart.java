package com.mariussoft.endlessjabber.sdk;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * An MMS attachment
 *
 */
public class MMSPart implements Parcelable {
	/**
	 * The name of the MMS attachment
	 */
	public String Name = "";
	/**
	 * The MimeType of the MMS attachment
	 */
	public String MimeType = "";
	/**
	 * The byte[] of the MMS attachment
	 */
	public byte[] Data;

	public MMSPart() {
	}

	public MMSPart(Parcel parcel) {
		Name = parcel.readString();
		MimeType = parcel.readString();
		Data = new byte[parcel.readInt()];
		parcel.readByteArray(Data);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(Name);
		dest.writeString(MimeType);
		dest.writeInt(Data.length);
		dest.writeByteArray(Data);
	}

	public static final Parcelable.Creator<MMSPart> CREATOR = new Parcelable.Creator<MMSPart>() {
		public MMSPart createFromParcel(Parcel in) {
			return new MMSPart(in);
		}

		public MMSPart[] newArray(int size) {
			return new MMSPart[size];
		}
	};
}