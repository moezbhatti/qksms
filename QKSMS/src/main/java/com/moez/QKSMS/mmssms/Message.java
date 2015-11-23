/*
 * Copyright (C) 2015 QK Labs
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

package com.moez.QKSMS.mmssms;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;

/**
 * Class to hold all relevant message information to send
 */
public class Message {
    private static final String TAG = "Message";
    private static final boolean LOCAL_LOGV = false;

    private String text=null;
    private String subject= null;
    private String[] addresses = null;
    private Bitmap[] images = new Bitmap[0];
    private String[] imageNames = null;
    private byte[] media= new byte[0];
    private String mediaMimeType = null;
    private boolean save= true;
    private int type = 0;
    private int delay = 0;

    /**
     * Default send type, to be sent through SMS or MMS depending on contents
     */
    public static final int TYPE_SMSMMS = 0;

    /**
     * Google Voice send type
     */
    public static final int TYPE_VOICE = 1;

    /**
     * Default constructor
     */
    public Message() {
        new Message.Builder().build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     */
    public Message(String text, String address) {
        new Message.Builder().text(text).stringAddress(address).build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     * @param subject is the subject of the mms message
     */
    public Message(String text, String address, String subject) {
        new Message.Builder().text(text).stringAddress(address).subject(subject).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     */
    public Message(String text, String[] addresses) {
        new Message.Builder().text(text).addresses(addresses).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     * @param subject   is the subject of the mms message
     */
    public Message(String text, String[] addresses, String subject) {
        new Message.Builder().text(text).addresses(addresses).subject(subject).build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     * @param image   is the image that you want to send
     */
    public Message(String text, String address, Bitmap image) {
        new Message.Builder().text(text).stringAddress(address).images(new Bitmap[]{image}).build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     * @param image   is the image that you want to send
     * @param subject is the subject of the mms message
     */
    public Message(String text, String address, Bitmap image, String subject) {
        new Message.Builder().text(text).stringAddress(address).images(new Bitmap[]{image}).subject(subject).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     * @param image     is the image that you want to send
     */
    public Message(String text, String[] addresses, Bitmap image) {
       new Message.Builder().text(text).addresses(addresses).images(new Bitmap[]{image}).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     * @param image     is the image that you want to send
     * @param subject   is the subject of the mms message
     */
    public Message(String text, String[] addresses, Bitmap image, String subject) {
        new Message.Builder().text(text).addresses(addresses).images(new Bitmap[]{image}).subject(subject).build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     * @param images  is an array of images that you want to send
     */
    public Message(String text, String address, Bitmap[] images) {
       new Message.Builder().text(text).stringAddress(address).images(images).build();
    }

    /**
     * Constructor
     *
     * @param text    is the message to send
     * @param address is the phone number to send to
     * @param images  is an array of images that you want to send
     * @param subject is the subject of the mms message
     */
    public Message(String text, String address, Bitmap[] images, String subject) {
       new Message.Builder().text(text).stringAddress(address).images(images).subject(subject).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     * @param images    is an array of images that you want to send
     */
    public Message(String text, String[] addresses, Bitmap[] images) {
       new Message.Builder().text(text).addresses(addresses).images(images).build();
    }

    /**
     * Constructor
     *
     * @param text      is the message to send
     * @param addresses is an array of phone numbers to send to
     * @param images    is an array of images that you want to send
     * @param subject   is the subject of the mms message
     */
    public Message(String text, String[] addresses, Bitmap[] images, String subject) {
        new Message.Builder().text(text).addresses(addresses).images(images).subject(subject).build();
    }

    /**
     * Sets the message
     *
     * @param text is the string to set message to
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets recipients
     *
     * @param addresses is the array of recipients to send to
     */
    public void setAddresses(String[] addresses) {
        this.addresses = addresses;
    }

    /**
     * Sets single recipient
     *
     * @param address is the phone number of the recipient
     */
    public void setAddress(String address) {
        this.addresses = new String[1];
        this.addresses[0] = address;
    }

    /**
     * Sets images
     *
     * @param images is the array of images to send to recipient
     */
    public void setImages(Bitmap[] images) {
        this.images = images;
    }

    /**
     * Sets image names
     *
     * @param names
     */
    public void setImageNames(String[] names) {
        this.imageNames = names;
    }

    /**
     * Sets image
     *
     * @param image is the single image to send to recipient
     */
    public void setImage(Bitmap image) {
        this.images = new Bitmap[1];
        this.images[0] = image;
    }
    
    /**
     * Sets audio file
     *
     * @param audio is the single audio sample to send to recipient
     */
    public void setAudio(byte[] audio) {
        this.media = audio;
        this.mediaMimeType = "audio/wav";
    }

    /**
     * Sets video file
     *
     * @param video is the single video sample to send to recipient
     */
    public void setVideo(byte[] video) {
        this.media = video;
        this.mediaMimeType = "video/3gpp";
    }

    /**
     * Sets other media
     *
     * @param media is the media you want to send
     * @param mimeType is the mimeType of the media
     */
    public void setMedia(byte[] media, String mimeType) {
        this.media = media;
        this.mediaMimeType = mimeType;
    }

    /**
     * Sets the subject
     *
     * @param subject is the subject of the mms message
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Sets whether or not to save a message to the database
     *
     * @param save is whether or not to save the message
     */
    public void setSave(boolean save) {
        this.save = save;
    }

    /**
     * Sets the time delay before sending a message
     * NOTE: this is only applicable for SMS messages
     *
     * @param delay the time delay in milliseconds
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Method to add another recipient to the object
     *
     * @param address is the string of the recipients phone number to add to end of recipients array
     */
    public void addAddress(String address) {
        String[] temp = this.addresses;

        if (temp == null) {
            temp = new String[0];
        }

        this.addresses = new String[temp.length + 1];

        for (int i = 0; i < temp.length; i++) {
            this.addresses[i] = temp[i];
        }

        this.addresses[temp.length] = address;
    }

    /**
     * Add another image to the object
     *
     * @param image is the image that you want to add to the end of the bitmaps array
     */
    public void addImage(Bitmap image) {
        Bitmap[] temp = this.images;

        if (temp == null) {
            temp = new Bitmap[0];
        }

        this.images = new Bitmap[temp.length + 1];

        for (int i = 0; i < temp.length; i++) {
            this.images[i] = temp[i];
        }

        this.images[temp.length] = image;
    }

    /**
     * Sets the type of the message, could be any type definied in Message, for example
     * Message.TYPE_SMSMMS, Message.TYPE_VOICE, or Message.TYPE_FACEBOOK
     *
     * @param type the type of message to send
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the text of the message to send
     *
     * @return the string of the message to send
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the addresses of the message
     *
     * @return an array of strings with all of the addresses
     */
    public String[] getAddresses() {
        return this.addresses;
    }

    /**
     * Gets the images in the message
     *
     * @return an array of bitmaps with all of the images
     */
    public Bitmap[] getImages() {
        return this.images;
    }

    /**
     * Gets image names for the message
     *
     * @return
     */
    public String[] getImageNames() {
        return this.imageNames;
    }
    
    /**
     * Gets the audio sample in the message
     *
     * @return an array of bytes with audio information for the message
     */
    public byte[] getMedia() {
        return this.media;
    }

    /**
     * Gets the mimetype of the extra media (eg, audio or video)
     *
     * @return a string of the mimetype
     */
    public String getMediaMimeType() {
        return this.mediaMimeType;
    }

    /**
     * Gets the subject of the mms message
     *
     * @return a string with the subject of the message
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Gets whether or not to save the message to the database
     *
     * @return a boolean of whether or not to save
     */
    public boolean getSave() {
        return this.save;
    }

    /**
     * Gets the time to delay before sending the message
     *
     * @return the delay time in milliseconds
     */
    public int getDelay() {
        return this.delay;
    }

    /**
     * Gets the type of message to be sent, see Message.TYPE_SMSMMS, Message.TYPE_FACEBOOK, or Message.TYPE_VOICE
     *
     * @return the type of the message
     */
    public int getType() { return this.type; }

    /**
     * Static method to convert a bitmap into a byte array to easily send it over http
     *
     * @param image is the image to convert
     * @return a byte array of the image data
     */
    public static byte[] bitmapToByteArray(Bitmap image) {
        if (image == null) {
            if (LOCAL_LOGV) Log.v(TAG, "image is null, returning byte array of size 0");
            return new byte[0];
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }

    public static Builder builder(){
        return new Message.Builder();
    }
    public static class Builder{
        private Message message;

        public Builder()
        {
            message = new Message();
        }
        public Builder text(String text)
        {
            message.text = text;
            return this;
        }
        public Builder addresses(String[] addresses)
        {
            message.addresses = addresses;
            return this;
        }
        public Builder subject(String subject)
        {
            message.subject = subject;
            return this;
        }
        public Builder stringAddress(String address)
        {
            message.addresses= address.trim().split(" ");
            return this;
        }
        public Builder images(Bitmap[] bitmap)
        {
            message.images= bitmap;
            return this;
        }
        public Builder media(byte[] media)
        {
            message.media=media;
            return this;
        }
        public Builder mediaMimeType(String mediaMimeType)
        {
            message.mediaMimeType=mediaMimeType;
            return this;
        }
        public Builder save(boolean save)
        {
            message.save=save;
            return this;
        }
        public Builder type(int type)
        {
            message.type=type;
            return this;
        }
        public Builder delay(int delay)
        {
            message.delay=delay;
            return this;
        }
        public Message build()
        {
            return message;
        }


    }
}
