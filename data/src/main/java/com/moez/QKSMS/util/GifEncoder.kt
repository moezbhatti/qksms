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
package com.moez.QKSMS.util

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.VisibleForTesting
import com.bumptech.glide.gifdecoder.GifDecoder
import com.bumptech.glide.gifdecoder.GifHeaderParser
import com.bumptech.glide.gifdecoder.StandardGifDecoder
import com.bumptech.glide.gifencoder.AnimatedGifEncoder
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.Resource
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapResource
import com.bumptech.glide.load.resource.gif.GifBitmapProvider
import com.bumptech.glide.load.resource.gif.GifDrawable
import java.io.OutputStream
import java.nio.ByteBuffer

/**
 * Allows converting an animated [GifDrawable] to an [OutputStream]
 * Based on Glide's ReEncodingGifResourceEncoder
 */
class GifEncoder internal constructor(
        private val context: Context,
        private val bitmapPool: BitmapPool,
        private val factory: Factory = Factory()
) {

    private val provider = GifBitmapProvider(bitmapPool)

    fun encodeTransformedToStream(drawable: GifDrawable, os: OutputStream): Boolean {
        val transformation = drawable.frameTransformation
        val decoder = decodeHeaders(drawable.buffer)
        val encoder = factory.buildEncoder()
        if (!encoder.start(os)) {
            return false
        }

        for (i in 0 until decoder.frameCount) {
            val currentFrame = decoder.nextFrame
            val transformedResource = getTransformedFrame(currentFrame, transformation, drawable)
            try {
                if (!encoder.addFrame(transformedResource.get())) {
                    return false
                }
                val currentFrameIndex = decoder.currentFrameIndex
                val delay = decoder.getDelay(currentFrameIndex)
                encoder.setDelay(delay)

                decoder.advance()
            } finally {
                transformedResource.recycle()
            }
        }

        return encoder.finish()
    }

    private fun decodeHeaders(data: ByteBuffer): GifDecoder {
        val parser = factory.buildParser()
        parser.setData(data)
        val header = parser.parseHeader()

        val decoder = factory.buildDecoder(provider)
        decoder.setData(header, data)
        decoder.advance()

        return decoder
    }

    private fun getTransformedFrame(currentFrame: Bitmap?, transformation: Transformation<Bitmap>, drawable: GifDrawable): Resource<Bitmap> {
        // TODO: what if current frame is null?
        val bitmapResource = factory.buildFrameResource(currentFrame!!, bitmapPool)
        val transformedResource = transformation.transform(context, bitmapResource, drawable.intrinsicWidth, drawable.intrinsicHeight)
        if (bitmapResource != transformedResource) {
            bitmapResource.recycle()
        }
        return transformedResource
    }

    @VisibleForTesting
    internal class Factory {
        fun buildDecoder(bitmapProvider: GifDecoder.BitmapProvider) = StandardGifDecoder(bitmapProvider)

        fun buildParser() = GifHeaderParser()

        fun buildEncoder() = AnimatedGifEncoder()

        fun buildFrameResource(bitmap: Bitmap, bitmapPool: BitmapPool) = BitmapResource(bitmap, bitmapPool)
    }
}