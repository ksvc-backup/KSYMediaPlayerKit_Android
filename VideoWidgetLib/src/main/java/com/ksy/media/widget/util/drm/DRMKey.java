package com.ksy.media.widget.util.drm;

import android.os.Parcel;
import android.os.Parcelable;

public class DRMKey implements Parcelable {

	private final String mKey;
	private final String mVersion;

	public DRMKey(String key, String version) {

		this.mKey = key;
		this.mVersion = version;
	}

	public DRMKey(Parcel p) {

		this.mKey = p.readString();
		this.mVersion = p.readString();
	}

	public String getKey() {

		return mKey;
	}

	public String getVersion() {

		return mVersion;
	}

	@Override
	public int describeContents() {

		return 0;
	}

	@Override
	public void writeToParcel(Parcel p, int flags) {

		p.writeString(mKey);
		p.writeString(mVersion);
	}

	public static final Creator<DRMKey> CREATOR = new Creator<DRMKey>() {

		@Override
		public DRMKey createFromParcel(Parcel p) {

			return new DRMKey(p);
		}

		@Override
		public DRMKey[] newArray(int size) {

			return new DRMKey[size];
		}
	};
}
