/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.moez.QKSMS.ui.dialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Handler;


/**
 * This AsyncDialog class is used to execute a runnable in a background thread and once that
 * finishes, execute a runnable on the UI thread. If the background runnable task takes longer
 * than half a second, a progress modal dialog is displayed.
 *
 */
public class AsyncDialog {
    private ProgressDialog mProgressDialog;
    private final Activity mActivity;
    private final Handler mHandler;

    public AsyncDialog(Activity activity) {
        mActivity = activity;
        mHandler = new Handler();
    }

    /**
     * Asynchronously executes a task while blocking the UI with a progress spinner.
     *
     * Must be invoked by the UI thread.  No exceptions!
     *
     * @param backgroundTask the work to be done in the background wrapped in a Runnable
     * @param postExecuteTask an optional runnable to run on the UI thread when the background
     * runnable is finished
     * @param dialogStringId the id of the string to be shown in the dialog
     */
    public void runAsync(final Runnable backgroundTask,
            final Runnable postExecuteTask, final int dialogStringId) {
        new ModalDialogAsyncTask(dialogStringId, postExecuteTask)
            .execute(new Runnable[] {backgroundTask});
    }

    // Shows the activity's progress spinner. Should be canceled if exiting the activity.
    private Runnable mShowProgressDialogRunnable = new Runnable() {
        @Override
        public void run() {
            if (mProgressDialog != null) {
                mProgressDialog.show();
            }
        }
    };

    public void clearPendingProgressDialog() {
        // remove any callback to display a progress spinner
        mHandler.removeCallbacks(mShowProgressDialogRunnable);
        // clear the dialog so any pending dialog.dismiss() call can be avoided
        mProgressDialog = null;
    }

    /**
     * Asynchronously performs tasks specified by Runnables.
     * Displays a progress spinner while the tasks are running.  The progress spinner
     * will only show if tasks have not finished after a certain amount of time.
     *
     * This AsyncTask must be instantiated and invoked on the UI thread.
     *
     * TODO: Need to implement a way for the background thread to pass a result to
     * the onPostExecute thread. AsyncTask already provides this functionality.
     */
    private class ModalDialogAsyncTask extends AsyncTask<Runnable, Void, Void> {
        final Runnable mPostExecuteTask;

        /**
         * Creates the Task with the specified string id to be shown in the dialog
         */
        public ModalDialogAsyncTask(int dialogStringId,
                final Runnable postExecuteTask) {
            mPostExecuteTask = postExecuteTask;
            // lazy initialization of progress dialog for loading attachments
            if (mProgressDialog == null) {
                mProgressDialog = createProgressDialog();
            }
            mProgressDialog.setMessage(mActivity.getText(dialogStringId));
        }

        /**
         * Initializes the progress dialog with its intended settings.
         */
        private ProgressDialog createProgressDialog() {
            ProgressDialog dialog = new ProgressDialog(mActivity);
            dialog.setIndeterminate(true);
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            return dialog;
        }

        /**
         * Activates a progress spinner on the UI.  This assumes the UI has invoked this Task.
         */
        @Override
        protected void onPreExecute() {
            // activate spinner after half a second
            mHandler.postDelayed(mShowProgressDialogRunnable, 500);
        }

        /**
         * Perform the specified Runnable tasks on a background thread
         */
        @Override
        protected Void doInBackground(Runnable... params) {
            if (params != null) {
                try {
                    for (int i = 0; i < params.length; i++) {
                        params[i].run();
                    }

                    // Test code. Uncomment this block to test the progress dialog popping up.
//                    try {
//                        Thread.sleep(2000);
//                    } catch (Exception e) {
//                    }
                } finally {
                    // Cancel pending display of the progress bar if the background task has
                    // finished before the progress bar has popped up.
                    mHandler.removeCallbacks(mShowProgressDialogRunnable);
                }
            }
            return null;
        }

        /**
         * Deactivates the progress spinner on the UI. This assumes the UI has invoked this Task.
         */
        @Override
        protected void onPostExecute(Void result) {
            if (mActivity.isFinishing()) {
                return;
            }
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            if (mPostExecuteTask != null) {
                mPostExecuteTask.run();
            }
        }
    }
}
