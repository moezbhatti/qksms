package com.mariussoft.endlessjabber.sdk;

import android.content.Context;

/**
 *
 * Interface to implement EndlessJabber integration
 *
 */
public interface IEndlessJabberImplementation {

	/**
	 * Update all messages in the SMS/MMS repositories as read where the time <= the provided time as well as where the threadID matches the provided ID
	 * @param context	The context to utilize
	 * @param time		The 'time' in the SMS database to use when marking messages as read
	 * 					(NOTE* for MMS divide by 1000 as MMS repository stores times as 1/1000th of the SMS)
	 * @param threadID	The ID of the thread in the SMS/MMS repositories to update
	 */
	void UpdateReadMessages(Context context, long time, int threadID);

	/**
	 * Delete any messages/conversations in the SMS/MMS repositories where the threadID matches the provided ID
	 * @param context	The context to utilize
	 * @param threadID	The ID of the thread in the SMS/MMS repositories to delete
	 */
	void DeleteThread(Context context,int threadID);

	/**
	 * Method gets called when EndlessJabber is requested to send an MMS message via the web app
	 * @param context		The context to utilize
	 * @param recipients	List of recipients to send message to
	 * @param parts			The message parts to send along with message
	 * @param subject		The MMS subject of the message
	 * @param save			Whether or not to save the MMS to the MMS repository
	 * @param send			If true, send the message along with saving it to the MMS repository
	 * 						NOTE: On KitKat save will be true only when enabled in the SDK & your app is the default messaging app on the system
	 *
	 * If both save & send are false, this call is only for informational purposes (e.g. modify notifications, update UI, etc...)
	 */
	void SendMMS(Context context, String[] recipients, MMSPart[] parts, String subject, boolean save, boolean send);

	/**
	 * Method gets called when EndlessJabber is requested to send an SMS message via the web app
	 * @param context		The context to utilize
	 * @param recipients	List of recipients to send message to
	 * @param message		The message to send
	 * @param send			If true, send the message along with saving it to the SMS repository, otherwise this is only for informational purposes (e.g. modify notifications, update UI, etc...)
	 */
	void SendSMS(Context context, String[] recipients, String message, boolean send);
}
