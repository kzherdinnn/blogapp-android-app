package com.pam.blogapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pam.blogapp.CommentActivity;
import com.pam.blogapp.Constant;
import com.pam.blogapp.EditPostActivity;
import com.pam.blogapp.HomeActivity;
import com.pam.blogapp.Models.Post;
import com.pam.blogapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsHolder> {


    private Context context;
    private ArrayList<Post> list;
    private ArrayList<Post> listAll;
    private SharedPreferences preferences;

    public PostsAdapter(Context context, ArrayList<Post> list) {
        this.context = context;
        this.list = list;
        this.listAll = new ArrayList<>(list);
        preferences = context.getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post,parent,false);
        return new PostsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostsHolder holder, int position) {
        Post post = list.get(position);
        Picasso.get().load(Constant.URL+"storage/profiles/"+post.getUser().getPhoto()).into(holder.imgProfile);
        Picasso.get().load(Constant.URL+"storage/posts/"+post.getPhoto()).into(holder.imgPost);
        holder.txtName.setText(post.getUser().getUserName());
        holder.txtComments.setText("View all "+post.getComments()+" comments");
        holder.txtLikes.setText(post.getLikes()+" Likes");
        holder.txtDate.setText(formatDate(post.getDate()));
        holder.txtDesc.setText(post.getDesc());

        holder.btnLike.setImageResource(
                post.isSelfLike()?R.drawable.ic_favorite_red:R.drawable.ic_favorite_outline
        );
        // like click
        holder.btnLike.setOnClickListener(v->{
            holder.btnLike.setImageResource(
                    post.isSelfLike()?R.drawable.ic_favorite_outline:R.drawable.ic_favorite_red
            );

            StringRequest request = new StringRequest(Request.Method.POST,Constant.LIKE_POST,response -> {

                Post mPost = list.get(position);

                try {
                    JSONObject object = new JSONObject(response);
                    if (object.getBoolean("success")){
                        mPost.setSelfLike(!post.isSelfLike());
                        mPost.setLikes(mPost.isSelfLike()?post.getLikes()+1:post.getLikes()-1);
                        list.set(position,mPost);
                        notifyItemChanged(position);
                        notifyDataSetChanged();
                    }
                    else {
                        holder.btnLike.setImageResource(
                                post.isSelfLike()?R.drawable.ic_favorite_red:R.drawable.ic_favorite_outline
                        );
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            },err->{
                err.printStackTrace();
            }){
                // add token

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    String token = preferences.getString("token","");
                    HashMap<String,String> map = new HashMap<>();
                    map.put("Authorization","Bearer "+token);
                    return map;
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<>();
                    map.put("id",post.getId()+"");
                    return map;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(context);
            queue.add(request);

        });

        if(post.getUser().getId()==preferences.getInt("id",0)){
            holder.btnPostOption.setVisibility(View.VISIBLE);
        } else {
            holder.btnPostOption.setVisibility(View.GONE);
        }

        holder.btnPostOption.setOnClickListener(v->{
            PopupMenu popupMenu = new PopupMenu(context,holder.btnPostOption);
            popupMenu.inflate(R.menu.menu_post_options);
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    if (item.getItemId() == R.id.item_edit) {
                        Intent i = new Intent(((HomeActivity) context), EditPostActivity.class);
                        i.putExtra("postId", post.getId());
                        i.putExtra("position", position);
                        i.putExtra("text", post.getDesc());
                        context.startActivity(i);
                        return true;
                    } else if (item.getItemId() == R.id.item_delete) {
                        deletePost(post.getId(), position);
                        return true;
                    }

                    return false;
                }
            });
            popupMenu.show();
        });

        holder.txtComments.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context), CommentActivity.class);
            i.putExtra("postId",post.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });

        holder.btnComment.setOnClickListener(v->{
            Intent i = new Intent(((HomeActivity)context),CommentActivity.class);
            i.putExtra("postId",post.getId());
            i.putExtra("postPosition",position);
            context.startActivity(i);
        });

    }

    private String formatDate(String date) {
        try {
            // Date from backend is like: 2026-02-05T17:38:02.000000Z
            // We need to parse it. The 'T' separates date and time, and 'Z' means UTC.
            String formattedDate = date.substring(0, 19).replace("T", " ");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC")); // The date from backend is UTC
            Date postDate = sdf.parse(formattedDate);
            long now = System.currentTimeMillis();
            long diff = now - postDate.getTime(); // diff in milliseconds

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1) {
                return "baru saja";
            } else if (minutes < 60) {
                return minutes + " menit yang lalu";
            } else if (hours < 24) {
                return hours + " jam yang lalu";
            } else {
                // For showing date, it will use the device's local timezone, which is what we want.
                return new SimpleDateFormat("dd MMM").format(postDate);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return date;
        }
    }

    private void deletePost(int postId, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Confirm");
        builder.setMessage("Delete post?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Buat URL dengan format yang benar
                String deleteUrl = String.format(Constant.DELETE_POST, postId);

                // Ubah ke DELETE method
                StringRequest request = new StringRequest(Request.Method.DELETE, deleteUrl,
                        response -> {
                            try {
                                JSONObject object = new JSONObject(response);
                                if (object.getBoolean("success")) {
                                    list.remove(position);
                                    notifyItemRemoved(position);
                                    notifyDataSetChanged();
                                    listAll.clear();
                                    listAll.addAll(list);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            error.printStackTrace();
                        }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        String token = preferences.getString("token", "");
                        HashMap<String, String> map = new HashMap<>();
                        map.put("Authorization", "Bearer " + token);
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            ArrayList<Post> filteredList = new ArrayList<>();
            if (constraint.toString().isEmpty()){
                filteredList.addAll(listAll);
            } else {
                for (Post post : listAll){
                    if(post.getDesc().toLowerCase().contains(constraint.toString().toLowerCase())
                    || post.getUser().getUserName().toLowerCase().contains(constraint.toString().toLowerCase())){
                        filteredList.add(post);
                    }
                }

            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return  results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            list.clear();
            list.addAll((Collection<? extends Post>) results.values);
            notifyDataSetChanged();
        }
    };

    public Filter getFilter() {
        return filter;
    }

    class PostsHolder extends RecyclerView.ViewHolder{

        private TextView txtName,txtDate,txtDesc,txtLikes,txtComments;
        private CircleImageView imgProfile;
        private ImageView imgPost;
        private ImageButton btnPostOption,btnLike,btnComment;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtPostName);
            txtDate = itemView.findViewById(R.id.txtPostDate);
            txtDesc = itemView.findViewById(R.id.txtPostDesc);
            txtLikes = itemView.findViewById(R.id.txtPostLikes);
            txtComments = itemView.findViewById(R.id.txtPostComments);
            imgProfile = itemView.findViewById(R.id.imgPostProfile);
            imgPost = itemView.findViewById(R.id.imgPostPhoto);
            btnPostOption = itemView.findViewById(R.id.btnPostOption);
            btnLike = itemView.findViewById(R.id.btnPostLike);
            btnComment = itemView.findViewById(R.id.btnPostComment);
            btnPostOption.setVisibility(View.GONE);
        }
    }
}
