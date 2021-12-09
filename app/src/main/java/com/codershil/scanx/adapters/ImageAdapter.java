package com.codershil.scanx.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codershil.scanx.R;

import java.util.ArrayList;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    ArrayList<Uri> selectedImagesList;
    Context context;
    OnDeleteListener listener;
    public ImageAdapter(ArrayList<Uri> selectedImagesList, Context context,OnDeleteListener listener){
        this.selectedImagesList = selectedImagesList;
        this.context = context;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.image_item,null,false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder imageViewHolder, int i) {
        imageViewHolder.imageView.setImageURI(selectedImagesList.get(imageViewHolder.getAdapterPosition()));
        imageViewHolder.imgDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDeleteClicked(imageViewHolder.getAdapterPosition());
            }
        });
        imageViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onImageClicked(imageViewHolder.getAdapterPosition());
            }
        });
    }

    // method to update recycler view
    public void updateData(ArrayList<Uri> selectedImagesList,int position){
        this.selectedImagesList = selectedImagesList;
        notifyItemRemoved(position);
    }

    public void updateList(ArrayList<Uri> selectedImagesList,int startIndex,int endIndex){
        this.selectedImagesList = selectedImagesList;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return selectedImagesList.size();
    }

    // view holder class for holding image item
    public class ImageViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView,imgDelete;
        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.selected_image);
            imgDelete = itemView.findViewById(R.id.imgDelete);
        }
    }

    // onclick handling interface
    public interface OnDeleteListener{
        void onDeleteClicked(int position);
        void onImageClicked(int position);
    }
}
