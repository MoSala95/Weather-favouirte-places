package com.example.weather_demo;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.ExampleViewHolder> {
    private Context context;
    private ArrayList<City> mExampleItems;
    private OnItemClickListener listener;
    public CityAdapter(Context context, ArrayList<City> mExampleItems){
        this.context=context;
        this.mExampleItems=mExampleItems;
    }

    @NonNull
    @Override
    public ExampleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v= LayoutInflater.from(context).inflate(R.layout.example_item,viewGroup,false);
        return new ExampleViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ExampleViewHolder exampleViewHolder,final int position) {
        City currentcity=mExampleItems.get(position);


        String cityName=currentcity.getName();
        String temp=currentcity.getTemp();
        String disc=currentcity.getDisc();
        String imageUrl=currentcity.getImageUrl();

        exampleViewHolder.mTextCity.setText(cityName);
        exampleViewHolder.mTextTemp.setText("Temperature is : "+ temp+"°c");
        exampleViewHolder.mTextdisc.setText(disc);
        String icon = imageUrl;
        String iconUrl = "http://openweathermap.org/img/w/" + icon + ".png";
        Picasso.get().load(iconUrl).into( exampleViewHolder.imageCondition);

        exampleViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                intent.putExtra("CityName", mExampleItems.get(position).getName());
                intent.putExtra("Lat",mExampleItems.get(position).getLatitude());
                intent.putExtra("Long",mExampleItems.get(position).getLongitude());
                context.startActivity(intent);
            }
        });
     }

    @Override
    public int getItemCount() {
        return mExampleItems.size();
    }

    public class ExampleViewHolder extends RecyclerView.ViewHolder{

        public TextView mTextCity;
        public TextView mTextTemp;
        public TextView mTextdisc;
        public ImageView imageCondition;

        public ExampleViewHolder(@NonNull View itemView) {
            super(itemView);

            mTextCity=itemView.findViewById(R.id.textview_city);
            mTextTemp=itemView.findViewById(R.id.texview_temperature);
            mTextdisc=itemView.findViewById(R.id.texview_description);
            imageCondition=itemView.findViewById(R.id.image_condition);

        }

    }
    public interface OnItemClickListener {
        void onItemClick(City city);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


}
