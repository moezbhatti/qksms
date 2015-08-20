package com.mariussoft.endlessjabber.sdk;

import android.content.Context;
import android.content.Intent;

public class EndlessJabberReceiver extends WakefulBroadcastReceiver {

	public static final String EndlessJabber_INTENT = "com.mariussoft.endlessjabber.action.extend";

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(EndlessJabber_INTENT)) {

			// This is the Intent to deliver to our service.
			Intent service = new Intent(context, EndlessJabberWakefulService.class);
			service.putExtras(intent.getExtras());

			startWakefulService(context, service);
		}
	}
}
