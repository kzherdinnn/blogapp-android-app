package com.pam.blogapp.Adapters;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pam.blogapp.CommentActivity;
import com.pam.blogapp.Constant;
import com.pam.blogapp.Fragments.HomeFragment;
import com.pam.blogapp.Models.Comment;
import com.pam.blogapp.Models.Post;
import com.pam.blogapp.R;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsHolder>{

    private Context context;
    private ArrayList<Comment> list;
    private SharedPreferences preferences;
    private ProgressDialog dialog;


    public CommentsAdapter(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
        dialog = new ProgressDialog(context);
        dialog.setCancelable(false);
        preferences = context.getSharedPreferences("user",Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public CommentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_comment,parent,false);
        return new CommentsHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsHolder holder, int position) {
        Comment comment = list.get(position);
        Picasso.get().load(comment.getUser().getPhoto()).into(holder.imgProfile);
        holder.txtName.setText(comment.getUser().getUserName());
        holder.txtDate.setText(formatDate(comment.getDate()));
        holder.txtComment.setText(comment.getComment());

        if (preferences.getInt("id",0)!=comment.getUser().getId()){
            holder.btnDelete.setVisibility(View.GONE);
        }
        else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v->{
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure?");
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteComment(comment.getId(),position);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            });
        }

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

    private void deleteComment(int commentId, int position) {
        dialog.setMessage("Deleting comment");
        dialog.show();

        String url = Constant.DELETE_COMMENT + "?id=" + commentId;

        StringRequest request = new StringRequest(Request.Method.DELETE, url, res -> {
            try {
                JSONObject object = new JSONObject(res);
                if (object.getBoolean("success")) {
                    list.remove(position);

                    Post post = HomeFragment.arrayList.get(CommentActivity.postPosition);
                    post.setComments(post.getComments() - 1);
                    HomeFragment.arrayList.set(CommentActivity.postPosition, post);
                    HomeFragment.getRecyclerView().getAdapter().notifyDataSetChanged();

                    notifyDataSetChanged();
                    Toast.makeText(context, "Comment deleted successfully", Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        }, err -> {
            err.printStackTrace();
            dialog.dismiss();
            Toast.makeText(context, "Failed to delete comment", Toast.LENGTH_SHORT).show();
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


    @Override
    public int getItemCount() {
        return list.size();
    }

    class CommentsHolder extends RecyclerView.ViewHolder{

        private CircleImageView imgProfile;
        private TextView txtName,txtDate,txtComment;
        private ImageButton btnDelete;

        public CommentsHolder(@NonNull View itemView) {
            super(itemView);

            imgProfile = itemView.findViewById(R.id.imgCommentProfile);
            txtName = itemView.findViewById(R.id.txtCommentName);
            txtDate = itemView.findViewById(R.id.txtCommentDate);
            txtComment = itemView.findViewById(R.id.txtCommentText);
            btnDelete = itemView.findViewById(R.id.btnDeleteComment);
        }
    }
}
