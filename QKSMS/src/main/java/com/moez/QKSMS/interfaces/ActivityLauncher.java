package com.moez.QKSMS.interfaces;

import android.content.Intent;

/**
 * An interface for launching activities for results.
 */
public interface ActivityLauncher {
    public void startActivityForResult(Intent request, int requestCode);
    public void onActivityResult(int requestCode, int resultCode, Intent data);
}
