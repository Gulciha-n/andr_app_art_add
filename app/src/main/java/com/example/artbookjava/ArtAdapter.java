package com.example.artbookjava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.artbookjava.databinding.RecyclerRawBinding;

import java.util.ArrayList;

public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder>{

    ArrayList<Art> artArrayList;

    public ArtAdapter(ArrayList<Art> artArrayList){

        this.artArrayList = artArrayList;
    }

    @NonNull
    @Override
    public ArtHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerRawBinding recyclerRawBinding = RecyclerRawBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new ArtHolder(recyclerRawBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtHolder holder, int position) {
        holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);

        holder.itemView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(), ArtActivity.class);
                intent.putExtra("artId",artArrayList.get(position).id);
                intent.putExtra("info","old");
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {

        return artArrayList.size();
    }

    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRawBinding binding;
        public ArtHolder(RecyclerRawBinding binding) { //RecyclerView XML'ini baglayacagız
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
