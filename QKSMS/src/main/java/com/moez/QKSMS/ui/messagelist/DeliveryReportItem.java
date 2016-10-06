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

package com.moez.QKSMS.ui.messagelist;

/**
 * This object holds information used by the DeliveryReportAdapter to create
 * DeliveryReportListItems.
 */
public class DeliveryReportItem {
    public String recipient;
    public String status;
    public String deliveryDate;

    public DeliveryReportItem(String recipient, String status, String deliveryDate) {
        this.recipient = recipient;
        this.status = status;
        this.deliveryDate = deliveryDate;
    }
}
