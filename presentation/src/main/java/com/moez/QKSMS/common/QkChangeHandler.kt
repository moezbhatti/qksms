/*
 * Copyright (C) 2017 Moez Bhatti <moez.bhatti@gmail.com>
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
package com.moez.QKSMS.common

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.bluelinelabs.conductor.ControllerChangeHandler
import com.bluelinelabs.conductor.changehandler.AnimatorChangeHandler
import com.moez.QKSMS.common.util.extensions.dpToPx

class QkChangeHandler(removesFromViewOnPush: Boolean = true) : AnimatorChangeHandler(250, removesFromViewOnPush) {

    @NonNull
    override fun getAnimator(@NonNull container: ViewGroup, @Nullable from: View?, @Nullable to: View?, isPush: Boolean, toAddedToContainer: Boolean): Animator {
        val animatorSet = AnimatorSet()
        animatorSet.interpolator = DecelerateInterpolator()

        if (isPush) {
            if (from != null) {
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, -from.width.toFloat() / 4))
            }
            if (to != null) {
                to.translationZ = 8.dpToPx(to.context).toFloat()
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, to.width.toFloat(), 0f))
            }
        } else {
            if (from != null) {
                from.translationZ = 8.dpToPx(from.context).toFloat()
                animatorSet.play(ObjectAnimator.ofFloat(from, View.TRANSLATION_X, from.width.toFloat()))
            }
            if (to != null) {
                // Allow this to have a nice transition when coming off an aborted push animation
                val fromLeft = from?.translationX ?: 0f
                animatorSet.play(ObjectAnimator.ofFloat(to, View.TRANSLATION_X, fromLeft - to.width / 4, 0f))
            }
        }

        return animatorSet
    }

    override fun resetFromView(@NonNull from: View) {
        from.translationX = 0f
        from.translationZ = 0f
    }

    @NonNull
    override fun copy(): ControllerChangeHandler {
        return QkChangeHandler(removesFromViewOnPush())
    }

}
