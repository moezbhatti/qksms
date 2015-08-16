/*
 * Copyright (C) 2008 Esmertec AG.
 * Copyright (C) 2008 The Android Open Source Project
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

package com.moez.QKSMS.ui.mms;

import android.content.Context;

import com.moez.QKSMS.interfaces.ViewInterface;
import com.moez.QKSMS.model.IModelChangedObserver;
import com.moez.QKSMS.model.Model;
import com.moez.QKSMS.common.google.ItemLoadedCallback;

/**
 * An abstract message presenter.
 */
public abstract class Presenter implements IModelChangedObserver {
    protected final Context mContext;
    protected ViewInterface mView;
    protected Model mModel;

    public Presenter(Context context, ViewInterface view, Model model) {
        mContext = context;
        mView = view;

        mModel = model;
        mModel.registerModelChangedObserver(this);
    }

    public ViewInterface getView() {
        return mView;
    }

    public void setView(ViewInterface view) {
        mView = view;
    }

    public Model getModel() {
        return mModel;
    }

    public void setModel(Model model) {
        mModel = model;
    }

    public abstract void present(ItemLoadedCallback callback);

    public abstract void cancelBackgroundLoading();
}
