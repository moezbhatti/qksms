/*
 * Copyright (C) 2020 Moez Bhatti <moez.bhatti@gmail.com>
 *
 * This file is part of QKSMS.
 *
 * QKSMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * QKSMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with QKSMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.moez.QKSMS.feature.compose.injection

import com.moez.QKSMS.feature.compose.ComposeController
import com.moez.QKSMS.injection.scope.ControllerScope
import com.moez.QKSMS.model.Attachment
import com.moez.QKSMS.model.Attachments
import dagger.Module
import dagger.Provides
import javax.inject.Named

@Module
class ComposeModule(private val controller: ComposeController) {

    @Provides
    @ControllerScope
    @Named("query")
    fun provideQuery(): String = controller.query

    @Provides
    @ControllerScope
    @Named("threadId")
    fun provideThreadId(): Long = controller.threadId

    @Provides
    @ControllerScope
    @Named("addresses")
    fun provideAddresses(): List<String> = controller.addresses

    @Provides
    @ControllerScope
    @Named("text")
    fun provideSharedText(): String = controller.sharedText

    @Provides
    @ControllerScope
    @Named("attachments")
    fun provideSharedAttachments(): Attachments = Attachments(controller.sharedAttachments)

}
