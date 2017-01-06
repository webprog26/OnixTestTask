package com.example.webpr.twittry_a.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.webpr.twittry_a.R;
import com.example.webpr.twittry_a.interfaces.OnTweetImageClickListener;
import com.example.webpr.twittry_a.models.Tweet;

import java.util.List;

/**
 * Created by webpr on 05.01.2017.
 */

public class TweetsListAdapterFull extends RecyclerView.Adapter<TweetsListAdapterFull.TweetsListViewHolder>{

    private List<Tweet> mTweets;
    private OnTweetImageClickListener mOnTweetImageClickListener;

    public TweetsListAdapterFull(List<Tweet> mTweets, OnTweetImageClickListener onTweetImageClickListener) {
        this.mTweets = mTweets;
        this.mOnTweetImageClickListener = onTweetImageClickListener;
    }

    @Override
    public TweetsListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.tweet_item, parent, false);
        view.getLayoutParams().height = parent.getHeight();
        view.requestLayout();
        return new TweetsListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TweetsListViewHolder holder, int position) {
        holder.bind(mTweets.get(position), mOnTweetImageClickListener);
    }

    @Override
    public int getItemCount() {
        return mTweets.size();
    }

    public class TweetsListViewHolder extends RecyclerView.ViewHolder{

        private TextView mTvUserName;
        private TextView mTvTweetText;
        private ImageView mIvTweetImage;

        public TweetsListViewHolder(View itemView) {
            super(itemView);
            mTvUserName = (TextView) itemView.findViewById(R.id.tvTweetUserName);
            mTvTweetText = (TextView) itemView.findViewById(R.id.tvTweetText);
            mIvTweetImage = (ImageView) itemView.findViewById(R.id.ivTweetImage);
        }

        public void bind(final Tweet tweet, final OnTweetImageClickListener onTweetImageClickListener){

            mTvUserName.setText(tweet.getUserName());
            mTvTweetText.setText(tweet.getText());
            mIvTweetImage.setImageBitmap(tweet.getImage());

            mIvTweetImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onTweetImageClickListener.onTweetImageClick(tweet.getImageUrl());
                }
            });
        }
    }
}
