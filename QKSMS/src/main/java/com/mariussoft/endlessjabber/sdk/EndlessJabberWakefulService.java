package com.mariussoft.endlessjabber.sdk;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class EndlessJabberWakefulService extends IntentService {

    public EndlessJabberWakefulService() {
        super("EndlessJabberWakefulService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = getApplicationContext();

        // Get the class to call
        String implementationClass = context.getSharedPreferences("EndlessJabberSDK", Context.MODE_PRIVATE).getString("InterfaceClass", null);

        if (implementationClass != null) {
            IEndlessJabberImplementation instanceOfMyClass;
            try {
                instanceOfMyClass = (IEndlessJabberImplementation) Class.forName(implementationClass).newInstance();

                Bundle extras = intent.getExtras();

                switch (extras.getString("Action")) {
                    case "UpdateRead": {
                        long time = extras.getLong("Time");
                        int conversationID = extras.getInt("ConversationID");
                        instanceOfMyClass.UpdateReadMessages(context, time, conversationID);
                        break;
                    }
                    case "DeleteThread": {

                        int conversationID = extras.getInt("ConversationID");
                        instanceOfMyClass.DeleteThread(context, conversationID);
                        break;
                    }
                    case "SendMMS": {

                        boolean save = extras.getBoolean("Save");
                        boolean send = extras.getBoolean("Send");
                        String[] Recipients = extras.getStringArray("Recipients");

                        MMSPart[] parts = new MMSPart[extras.getParcelableArray("Parts").length];

                        for (int i = 0; i < parts.length; i++) {
                            parts[i] = (MMSPart) extras.getParcelableArray("Parts")[i];
                        }

                        instanceOfMyClass.SendMMS(context, Recipients, parts, extras.getString("Subject"), save, send);
                        break;
                    }
                    case "SendSMS": {
                        boolean send = extras.getBoolean("Send");
                        String[] Recipients = extras.getStringArray("Recipients");
                        String message = extras.getString("Message");

                        instanceOfMyClass.SendSMS(context, Recipients, message, send);
                        break;
                    }
                    case "UpdateInfo": {
                        EndlessJabberInterface.SendInfoToEndlessJabber(context);
                        break;
                    }
                }
            } catch (InstantiationException e) {

            } catch (IllegalAccessException e) {

            } catch (ClassNotFoundException e) {

            }
        }
        EndlessJabberReceiver.completeWakefulIntent(intent);
    }
}
