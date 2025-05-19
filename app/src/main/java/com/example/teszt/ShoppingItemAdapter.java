package com.example.teszt;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ShoppingItemAdapter extends RecyclerView.Adapter<ShoppingItemAdapter.ViewHolder> implements Filterable {

    private ArrayList<ShoppingItem> mShoppinItemData;
    private ArrayList<ShoppingItem> mShoppinItemDataAll;

    private Context nContext;
    private int lastPosition = -1;


    ShoppingItemAdapter(Context context, ArrayList<ShoppingItem> itemsData){
        this.mShoppinItemData = itemsData;
        this.mShoppinItemDataAll = itemsData;
        this.nContext= context;
    }
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(nContext).inflate(R.layout.activity_list_item, parent,false));
    }

    @Override
    public void onBindViewHolder(ShoppingItemAdapter.ViewHolder holder, int position) {
        ShoppingItem currentItem = mShoppinItemData.get(position);

        holder.bindTo(currentItem);

        if(holder.getAdapterPosition() > lastPosition){
            Animation  animation = AnimationUtils.loadAnimation(nContext,R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }

    }

    @Override
    public int getItemCount() {
        return mShoppinItemData.size();
    }

    @Override
    public Filter getFilter() {
        return shoppingFilter;
    }
    private Filter shoppingFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<ShoppingItem> filteredList = new ArrayList<>();
            FilterResults results = new FilterResults();

            if(constraint == null || constraint.length() == 0){
                results.count = mShoppinItemDataAll.size();
                results.values = mShoppinItemDataAll;
            }else{
                String filterPattern = constraint.toString().toLowerCase().trim();

                for(ShoppingItem item : mShoppinItemDataAll){
                    if(item.getName().toLowerCase().contains(filterPattern)){
                        filteredList.add(item);
                    }
                }


                results.count = filteredList.size();
                results.values = filteredList;
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mShoppinItemData = (ArrayList) results.values;
            notifyDataSetChanged();
        }
    };

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView mTitleText;
        private TextView minfoText;
        private TextView mPriceText;
        private ImageView mItemImage;

        private RatingBar mRatingBar;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
             mTitleText = itemView.findViewById(R.id.itemTitle);
             minfoText = itemView.findViewById(R.id.subTitle);
             mPriceText = itemView.findViewById(R.id.price);
             mItemImage = itemView.findViewById(R.id.itemImage);

             mRatingBar = itemView.findViewById(R.id.ratingBar);


        }

        public void bindTo(ShoppingItem currentItem) {
            mTitleText.setText(currentItem.getName());
            minfoText.setText(currentItem.getInfo());
            mPriceText.setText(currentItem.getPrice());
            mRatingBar.setRating(currentItem.getRated());


            Glide.with(nContext).load(currentItem.getImageResource()).into(mItemImage);
            itemView.findViewById(R.id.add_to_cart).setOnClickListener(v -> {
                Log.d("Activigy", "Add cart button clicked!");
                ((Shop) nContext).updateAlertIcon(currentItem);
            });
            itemView.findViewById(R.id.delete).setOnClickListener(v -> {
                Log.d("Activigy", "delete button clicked!");
                ((Shop) nContext).deleteItem(currentItem);
            });        }
    };

}


