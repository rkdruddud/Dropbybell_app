package com.example.alamproject1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class mainAdaper extends RecyclerView.Adapter<mainAdaper.holder> {

    String data[];

    public mainAdaper(String[] data) {
        this.data = data;
    }

    @NonNull
    @Override
    public holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.nav_item,parent,false);
        return new holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull holder holder, int position) {
        holder.textView.setText(data[position]);
    }

    @Override
    public int getItemCount() {
        return data.length;
    }


    class holder extends RecyclerView.ViewHolder{
        TextView textView;
        public holder(@NonNull View itemView){
            super(itemView);
            textView=(TextView) itemView.findViewById(R.id.itemText);
        }
    }
}
