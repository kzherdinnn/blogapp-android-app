package com.kzherdin.blogapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kzherdin.blogapp.Constant;
import com.kzherdin.blogapp.Models.Post;
import com.kzherdin.blogapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class AccountPostAdapter extends RecyclerView.Adapter<AccountPostAdapter.AccountPostHolder>{

    private Context context;
    private ArrayList<Post> arrayList;

    public AccountPostAdapter(Context context, ArrayList<Post> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public AccountPostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_account_post,parent,false);
        return new AccountPostHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountPostHolder holder, int position) {
        String postPhotoUrl = buildImageUrl("posts", arrayList.get(position).getPhoto());
        Picasso.get()
                .load(postPhotoUrl)
                .placeholder(R.color.colorLightGrey)
                .error(R.color.colorLightGrey)
                .noFade()
                .into(holder.imageView);
    }

    private String buildImageUrl(String folder, String filename) {
        if (filename == null || filename.isEmpty() || filename.equals("null")) {
            return null;
        }
        if (filename.startsWith("http")) {
            return filename;
        }
        String baseUrl = Constant.URL;
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/storage/" + folder + "/" + filename;
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    class AccountPostHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;

        public AccountPostHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imgAccountPost);
        }
    }
}
