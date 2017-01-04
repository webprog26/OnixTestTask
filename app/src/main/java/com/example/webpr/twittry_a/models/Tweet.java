package com.example.webpr.twittry_a.models;


import android.graphics.Bitmap;

/**
 * Created by webpr on 04.01.2017.
 */

public class Tweet {

    private long mId;
    private String mUserName;
    private String mText;
    private Bitmap mImage;

    public long getId() {
        return mId;
    }

    public String getUserName() {
        return mUserName;
    }

    public String getText() {
        return mText;
    }

    public Bitmap getImage() {
        return mImage;
    }

    public static Builder newBuilder(){
        return new Tweet(). new Builder();
    }

    public class Builder{

        public Builder setId(long id){
            Tweet.this.mId = id;
            return this;
        }

        public Builder setUserName(String userName){
            Tweet.this.mUserName = userName;
            return this;
        }

        public Builder setText(String text){
            Tweet.this.mText = text;
            return this;
        }

        public Builder setImage(Bitmap image){
            Tweet.this.mImage = image;
            return this;
        }

        public Tweet build(){
            return Tweet.this;
        }
    }
}
