package com.moez.QKSMS.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Comparator;

public class ChangeModel implements Comparator<Long> {
    @SerializedName("version_name") private String mVersion;
    @SerializedName("release_date") private String mDate;
    @SerializedName("changes") private ArrayList<String> mChanges;

    long mDateLong = 0;

    public String getVersion() {
        if (mVersion == null) {
            return "";
        }

        return mVersion;
    }

    public void setVersion(String version) {
        mVersion = version;
    }

    public String getDate() {
        if (mDate == null) {
            return "";
        }

        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public ArrayList<String> getChanges() {
        if (mChanges == null) {
            return new ArrayList<>();
        }

        return mChanges;
    }

    public void setChanges(ArrayList<String> changes) {
        mChanges = changes;
    }

    public long getDateLong() {
        return mDateLong;
    }

    public void setDateLong(long date) {
        mDateLong = date;
    }

    @Override
    public int compare(Long lhs, Long rhs) {
        return lhs.compareTo(rhs);
    }
}
