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

package com.moez.QKSMS.model;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.PduBody;
import com.google.android.mms.pdu_alt.PduPart;
import com.moez.QKSMS.LogTag;
import com.moez.QKSMS.MmsConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRegionMediaElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;

public class MediaModelFactory {
    private static final String TAG = "Mms:media";
    private static final boolean LOCAL_LOGV = false;

    /**
     * Returns the media model for the given SMILMediaElement in the PduBody.
     *
     * @param context Context
     * @param sme     The SMILMediaElement to find
     * @param srcs    String array of sources
     * @param layouts LayoutModel
     * @param pb      PduBuddy
     * @return MediaModel
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws MmsException
     */
    public static MediaModel getMediaModel(Context context, SMILMediaElement sme,
                                           ArrayList<String> srcs, LayoutModel layouts,
                                           PduBody pb)

        throws IOException, IllegalArgumentException, MmsException {

        String tag = sme.getTagName();
        String src = sme.getSrc();

        PduPart part = findPart(context, pb, src, srcs);

        if (sme instanceof SMILRegionMediaElement) {
            return getRegionMediaModel(
                context, tag, src, (SMILRegionMediaElement) sme, layouts, part);
        } else {
            return getGenericMediaModel(
                context, tag, src, sme, part, null);
        }
    }

    /**
     * This method is meant to identify the part in the given PduBody that corresponds to the given
     * src string.
     *
     * Essentially, a SMIL MMS is formatted as follows:
     *
     * 1. A smil/application part, which contains XML-like formatting for images, text, audio,
     * slideshows, videos, etc.
     * 2. One or more parts that correspond to one of the elements that was mentioned in the
     * formatting above.
     *
     * In the smil/application part, elements are identified by a "src" attribute in an XML-like
     * element. The challenge of this method lies in the fact that sometimes, the src string isn't
     * located at all in the part that it is meant to identify.
     *
     * We employ several methods of pairing src strings up to parts, using certain patterns we've
     * seen in failed MMS messages. These are described in this method.
     * TODO TODO TODO: Create a testing suite for this!
     */
    private static PduPart findPart(final Context context, PduBody pb, String src,
                                    ArrayList<String> srcs) {

        PduPart result = null;

        if (src != null) {
            src = unescapeXML(src);

            // Sometimes, src takes the form of "cid:[NUMBER]".
            if (contentIdSrc(src)) {

                // Extract the content ID, and try finding the part using that.
                result = pb.getPartByContentId("<" + src.substring("cid:".length()) + ">");

                if (result == null) {
                    // Another thing that can happen is that there is a slideshow of images, each with
                    // "cid:[NUMBER]" src, but the parts aren't labelled with the content ID. If
                    // this is the case, then we just return the ith image part, where i is the position
                    // of src if all the srcs are sorted in ascending order as content IDs. srcs may
                    // include duplicates; we remove those and then identify i when the list is free from
                    // duplicates.
                    //
                    // i.e., for srcs = [ "cid:755", "cid:755", "cid:756", "cid:757", "cid:758" ],
                    // the i of "cid:755" is 0; for "cid:756" is 1, etc.

                    // First check that all the src strings are content IDs.
                    boolean allContentIDs = true;
                    for (String _src : srcs) {
                        if (!contentIdSrc(_src)) {
                            allContentIDs = false;
                            break;
                        }
                    }

                    if (allContentIDs) {
                        // Now, build a list of long IDs, sort them, and remove the duplicates.
                        ArrayList<Long> cids = new ArrayList<>();
                        for (String _src : srcs) {
                            cids.add(getContentId(_src));
                        }
                        Collections.sort(cids);

                        int removed = 0;
                        long previous = -1;
                        for (int i = 0; i < cids.size() - removed; i++) {
                            long cid = cids.get(i);

                            if (cid == previous) {
                                cids.remove(i);
                                removed++;
                            } else {
                                previous = cid;
                            }
                        }

                        // Find the i such that getContentId(src) == cids[i]
                        long cid = getContentId(src);
                        int i = cids.indexOf(cid);

                        // Finally, since the SMIL formatted part will come first, we expect to see
                        // 1 + cids.size() parts, and the right part for this particular cid will be
                        // 1 + i.
                        if (1 + i < pb.getPartsNum()) {
                            result = pb.getPart(i + 1);
                        }
                    }
                }

            } else if (textSrc(src)) {
                // This is just a text src, so look for the PduPart that is has the "text/plain"
                // content type.
                for (int i = 0; i < pb.getPartsNum(); i++) {
                    PduPart part = pb.getPart(i);
                    String contentType = byteArrayToString(part.getContentType());
                    if ("text/plain".equals(contentType)) {
                        result = part;
                        break;
                    }
                }
            }

            // Try a few more things in case the previous processing didn't work correctly:

            // Search by name
            if (result == null) {
                result = pb.getPartByName(src);
            }

            // Search by filename
            if (result == null) {
                result = pb.getPartByFileName(src);
            }

            // Search by content location
            if (result == null) {
                result = pb.getPartByContentLocation(src);
            }

            // Try treating the src string as a content ID, and searching by content ID.
            if (result == null) {
                result = pb.getPartByContentId("<" + src + ">");
            }

            // TODO:
            // four remaining cases currently in Firebase:
            // 1. src: "image:[NUMBER]" (broken formatting)
            // 2. src: "[NUMBER]" (broken formatting)
            // 3. src: "[name].vcf" (we don't support x-vcard)
            // 3. src: "Current Location.loc.vcf" (we don't support x-vcard)

        }

        if (result != null) {
            return result;
        }

        if (pb.getPartsNum() > 0) {

            final JSONArray array = new JSONArray();

            for (int i = 0; i < pb.getPartsNum(); i++) {

                JSONObject object = new JSONObject();

                try {
                    object.put("part_number", i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("location", i);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("charset", pb.getPart(i).getCharset());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("content_disposition", byteArrayToString(pb.getPart(i).getContentDisposition()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("content_id", byteArrayToString(pb.getPart(i).getContentId()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("content_location", byteArrayToString(pb.getPart(i).getContentLocation()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("content_transfer_encoding", byteArrayToString(pb.getPart(i).getContentTransferEncoding()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("content_type", byteArrayToString(pb.getPart(i).getContentType()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("data", byteArrayToString(pb.getPart(i).getData()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("data_uri", pb.getPart(i).getDataUri());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("file_name", byteArrayToString(pb.getPart(i).getFilename()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    object.put("name", byteArrayToString(pb.getPart(i).getName()));
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (pb.getPart(i).generateLocation() != null) {
                    Log.d(TAG, "Location: " + pb.getPart(i).generateLocation());
                    if (pb.getPart(i).generateLocation().contains(src)) {
                        return pb.getPart(i);
                    }
                }

                array.put(object);
            }
        }

        throw new IllegalArgumentException("No part found for the model.");
    }

    private static boolean textSrc(String src) {
        return src != null && src.startsWith("text") && src.endsWith(".txt");
    }

    private static boolean contentIdSrc(String src) {
        return src != null && src.startsWith("cid:");
    }

    private static long getContentId(String src) {
        if (src == null) {
            return -1;
        } else {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Initial contentId: " + src);
            }
            src = src.substring("cid:".length());
            src = unescapeXML(src);
            // Strip any leading < or trailing > ... they are present sometimes and causing error(s)
            src = src.replaceAll("(^\\<)|(\\>$)", "");
            if (LOCAL_LOGV) {
                Log.v(TAG, "Final contentId: " + src);
            }
            return Long.parseLong(src);
        }
    }

    private static String byteArrayToString(byte[] bytes) {
        if (bytes != null) {
            return new String(bytes);
        }

        return "null";
    }

    private static boolean hasBeenPosted(Context context, JSONArray array) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(array.toString().getBytes());
        String encryptedString = new String(messageDigest.digest());

        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(encryptedString, false);
    }

    private static void savePostStatus(Context context, JSONArray array, boolean posted) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
        messageDigest.update(array.toString().getBytes());
        String encryptedString = new String(messageDigest.digest());

        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(encryptedString, true);
    }

    private static String unescapeXML(String str) {
        return str.replaceAll("&lt;", "<")
            .replaceAll("&gt;", ">")
            .replaceAll("&quot;", "\"")
            .replaceAll("&apos;", "'")
            .replaceAll("&amp;", "&");
    }

    private static MediaModel getRegionMediaModel(Context context,
                                                  String tag, String src, SMILRegionMediaElement srme,
                                                  LayoutModel layouts, PduPart part) throws IOException, MmsException {
        SMILRegionElement sre = srme.getRegion();
        if (sre != null) {
            RegionModel region = layouts.findRegionById(sre.getId());
            if (region != null) {
                return getGenericMediaModel(context, tag, src, srme, part, region);
            }
        } else {
            String rId;

            if (tag.equals(SmilHelper.ELEMENT_TAG_TEXT)) {
                rId = LayoutModel.TEXT_REGION_ID;
            } else {
                rId = LayoutModel.IMAGE_REGION_ID;
            }

            RegionModel region = layouts.findRegionById(rId);
            if (region != null) {
                return getGenericMediaModel(context, tag, src, srme, part, region);
            }
        }

        throw new IllegalArgumentException("Region not found or bad region ID.");
    }

    // When we encounter a content type we can't handle, such as "application/vnd.smaf", instead
    // of throwing an exception and crashing, insert an empty TextModel in its place.
    private static MediaModel createEmptyTextModel(Context context, RegionModel regionModel)
        throws IOException {
        return new TextModel(context, ContentType.TEXT_PLAIN, null, regionModel);
    }

    private static MediaModel getGenericMediaModel(Context context,
                                                   String tag, String src, SMILMediaElement sme, PduPart part,
                                                   RegionModel regionModel) throws IOException, MmsException {
        byte[] bytes = part.getContentType();
        if (bytes == null) {
            throw new IllegalArgumentException(
                "Content-Type of the part may not be null.");
        }

        String contentType = new String(bytes);
        MediaModel media;

        switch (tag) {
            case SmilHelper.ELEMENT_TAG_TEXT:
                media = new TextModel(context, contentType, src,
                    part.getCharset(), part.getData(), regionModel);
                break;
            case SmilHelper.ELEMENT_TAG_IMAGE:
                media = new ImageModel(context, contentType, src,
                    part.getDataUri(), regionModel);
                break;
            case SmilHelper.ELEMENT_TAG_VIDEO:
                media = new VideoModel(context, contentType, src,
                    part.getDataUri(), regionModel);
                break;
            case SmilHelper.ELEMENT_TAG_AUDIO:
                media = new AudioModel(context, contentType, src,
                    part.getDataUri());
                break;
            case SmilHelper.ELEMENT_TAG_REF:
                if (ContentType.isTextType(contentType)) {
                    media = new TextModel(context, contentType, src,
                        part.getCharset(), part.getData(), regionModel);
                } else if (ContentType.isImageType(contentType)) {
                    media = new ImageModel(context, contentType, src,
                        part.getDataUri(), regionModel);
                } else if (ContentType.isVideoType(contentType)) {
                    media = new VideoModel(context, contentType, src,
                        part.getDataUri(), regionModel);
                } else if (ContentType.isAudioType(contentType)) {
                    media = new AudioModel(context, contentType, src,
                        part.getDataUri());
                } else {
                    Log.d(TAG, "[MediaModelFactory] getGenericMediaModel Unsupported Content-Type: "
                        + contentType);
                    media = createEmptyTextModel(context, regionModel);
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported TAG: " + tag);
        }

        // Set 'begin' property.
        int begin = 0;
        TimeList tl = sme.getBegin();
        if ((tl != null) && (tl.getLength() > 0)) {
            // We only support a single begin value.
            Time t = tl.item(0);
            begin = (int) (t.getResolvedOffset() * 1000);
        }
        media.setBegin(begin);

        // Set 'duration' property.
        int duration = (int) (sme.getDur() * 1000);
        if (duration <= 0) {
            tl = sme.getEnd();
            if ((tl != null) && (tl.getLength() > 0)) {
                // We only support a single end value.
                Time t = tl.item(0);
                if (t.getTimeType() != Time.SMIL_TIME_INDEFINITE) {
                    duration = (int) (t.getResolvedOffset() * 1000) - begin;

                    if (duration == 0 && (media instanceof AudioModel || media instanceof VideoModel)) {
                        duration = MmsConfig.getMinimumSlideElementDuration();
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "[MediaModelFactory] compute new duration for " + tag + ", duration=" + duration);
                        }
                    }
                }
            }
        }

        media.setDuration(duration);

        if (!MmsConfig.getSlideDurationEnabled()) {
            /**
             * Because The slide duration is not supported by mmsc,
             * the device has to set fill type as FILL_FREEZE.
             * If not, the media will disappear while rotating the screen
             * in the slide show play view.
             */
            media.setFill(SMILMediaElement.FILL_FREEZE);
        } else {
            // Set 'fill' property.
            media.setFill(sme.getFill());
        }
        return media;
    }
}
