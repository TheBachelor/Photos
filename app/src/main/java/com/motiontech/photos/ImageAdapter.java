package com.motiontech.photos;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ceylonlabs.imageviewpopup.ImagePopup;
import com.github.ybq.android.spinkit.SpinKitView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by manengamungandi on 2017/08/21.
 */

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {

    private ArrayList<Image> mData = new ArrayList<Image>();
    private LayoutInflater mInflater;
    private Context ctx;
    private ImagePopup imagePopup;

    // data is passed into the constructor
    public ImageAdapter(Context context, ArrayList<Image> data) {
        this.mInflater = LayoutInflater.from(context);
        imagePopup = new ImagePopup(context);
        this.mData = data;
        this.ctx = context;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.image_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;
    }

    // binds the data to each cell
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final Image image = mData.get(position);

        if (image.getUrl().isEmpty()) {
            loadImage(holder.spinner, holder.image, "https://500px.com/graphics/developer/app-icon.png");
        } else {
            loadImage(holder.spinner, holder.image, image.getUrl());

            //image pop up listener
            holder.image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    imagePopup.setBackgroundColor(Color.TRANSPARENT);
                    imagePopup.setHideCloseIcon(true);
                    imagePopup.setImageOnClickClose(true);
                    imagePopup.initiatePopupWithPicasso(image.getUrl());
                    imagePopup.viewPopup();
                }
            });
        }

        holder.score.setText(image.getScore());
        holder.name.setText(image.getName());
    }

    private void loadImage(final SpinKitView spin, ImageView image, final String url){
        spin.setVisibility(View.VISIBLE);
        Picasso.with(ctx)
                .load(url)
                .into(image, new Callback() {
                    @Override
                    public void onSuccess() {
                        spin.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        System.out.println("Failed to load: " + url);
                    }
                });
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        public CardView card;
        public SpinKitView spinner;
        public ImageView image;
        public TextView score;
        public TextView name;

        public ViewHolder(View itemView) {
            super(itemView);

            score = itemView.findViewById(R.id.imageScore);
            name = itemView.findViewById(R.id.imageName);
            card = itemView.findViewById(R.id.cardView);
            image = itemView.findViewById(R.id.image);
            spinner = itemView.findViewById(R.id.imageSpinner);
        }
    }
}
