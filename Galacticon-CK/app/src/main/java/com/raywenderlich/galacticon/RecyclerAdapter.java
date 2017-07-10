package com.raywenderlich.galacticon;

/**
 * Created by colinknecht on 7/6/17.
 */
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.PhotoHolder> {

    private ArrayList<Photo> mPhotoList;

    public static class PhotoHolder extends RecyclerView.ViewHolder implements View.OnClickListener{ //inner Class
        //Made the class extend RecyclerView.ViewHolder, allowing it to be used as a ViewHolder for the adapter.

        //Added a list of references to the lifecycle of the object to allow the ViewHolder to hang on to your
        // ImageView and TextView, so it doesn’t have to repeatedly query the same information.
        private static final String TAG = "PhotoHolder";
        private ImageView mItemImage;
        private TextView mItemDate;
        private TextView mItemDescription;
        private Photo mPhoto;
        private static final String PHOTO_KEY = "PHOTO";//Added a key for easier reference to the particular item being used to launch your RecyclerView.

        public PhotoHolder(View v) {//Set up a constructor to handle grabbing references to various subviews of the photo layout.
            super(v);

            mItemImage = (ImageView) v.findViewById(R.id.item_image);
            mItemDate = (TextView) v.findViewById(R.id.item_date);
            mItemDescription = (TextView) v.findViewById(R.id.item_description);
            v.setOnClickListener(this);
        }

        public void bindPhoto(Photo photo) {
            //This binds the photo to the PhotoHolder, giving your item the data it needs to work out what it should show.
            //It also adds the suggested Picasso import, which is a library that makes it significantly simpler to get images from a given URL.
            mPhoto = photo;
            Picasso.with(mItemImage.getContext()).load(photo.getUrl()).into(mItemImage);
            mItemDate.setText(photo.getHumanDate());
            mItemDescription.setText(photo.getExplanation());
        }

        @Override
        public void onClick(View v) {//Implemented the required method for View.OnClickListener since ViewHolders are responsible for their own event handling.
            Log.d(TAG, "onClick: CLICK!");
            //Start a new activity by replacing the log in ViewHolder’s onClick with this code:
            Context context = itemView.getContext();
            Intent showPhotoIntent = new Intent(context, PhotoActivity.class);
            showPhotoIntent.putExtra(PHOTO_KEY, mPhoto);
            context.startActivity(showPhotoIntent);
            //This grabs the current context of your item view and creates an intent to show a new activity on the screen, passing the photo object you want to show.
            // Passing the context object into the intent allows the app to know what activity it is leaving.
        }
    }//end inner Class

    public RecyclerAdapter(ArrayList<Photo> photoList) { //Constructor
        mPhotoList = photoList;
    }
    @Override
    public RecyclerAdapter.PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Sometimes there are no ViewHolders available. In this scenario, RecylerView will ask onCreateViewHolder()
        // from RecyclerAdapter to make a new one.

        //Here you add the suggested LayoutInflater import. Then you inflate the view from its layout and pass it in to a PhotoHolder.
        View inflatedView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item_row, parent, false);

        return new PhotoHolder(inflatedView);
    }

    @Override
    public void onBindViewHolder(RecyclerAdapter.PhotoHolder holder, int position) {
        //Here you’re passing in a copy of your ViewHolder and the position where the item will show in your RecyclerView.
        Photo itemPhoto = mPhotoList.get(position);
        holder.bindPhoto(itemPhoto);
    }

    @Override
    public int getItemCount() {
        return mPhotoList.size();
    }
}
