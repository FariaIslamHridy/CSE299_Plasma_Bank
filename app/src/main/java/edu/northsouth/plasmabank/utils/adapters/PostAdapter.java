package edu.northsouth.plasmabank.utils.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.models.Post;
import edu.northsouth.plasmabank.utils.Constants;
import edu.northsouth.plasmabank.utils.LocalStorage;

/*
 *
 * this class extends RecyclerView.Adapter class
 * so that we can have more flexibility like
 * we can add custom item for the RecyclerView
 *
 */

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    Context context;
    LayoutInflater layoutInflater;

    final static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;

    private List<Post> postList;
    LocalStorage storage;



    public PostAdapter(Context context) {
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        postList = new ArrayList<>();

        storage = new LocalStorage(context);
    }

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.item_post, parent, false);

        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        if (postList != null) {
            Post post = postList.get(position);

            holder.mUsername.setText(post.getAuthor());
            holder.mDate.setText(post.getDate());

            if (post.getLocation() != null) {
                holder.mLocation.setText(post.getLocation());
            }

            holder.mPost.setText(post.getPostDetails());
            holder.mBlood.setText(post.getBlood());
            holder.mContact.setText((post.getContact()));

            if (post.getAvatar() == 0) {
                holder.mAvatar.setImageResource(R.drawable.ic_female);
            } else {
                holder.mAvatar.setImageResource(R.drawable.ic_male);
            }

            holder.mInterested.setOnClickListener(v -> {
                // if accept a user to donate
                // a notification will be delivered to post author
                // his user status will be changed to receiver
                // after that he can not click on interested

                if (storage.getUserType().equals(Constants.DONOR)) {
                    // write on responses table on firebase
                    syncWithFirebase(post, true);
                    //
                } else {
                    Toast.makeText(context, "Receiver can to donate blood. To donate, change your user status.", Toast.LENGTH_LONG).show();
                }
            });

            holder.mDeny.setOnClickListener(v -> {
                // removed from user home
                // will not show the same post again
                // author won't be notified
                if (storage.getUserType().equals(Constants.DONOR)) {
                    // write on responses table on firebase
                    syncWithFirebase(post, false);

                    // remove from list
                    postList.remove(position);
                    notifyDataSetChanged();
                } else {
                    Toast.makeText(context, "Receiver can to donate blood. To donate, change your user status.", Toast.LENGTH_LONG).show();
                }

            });

            holder.mContact.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + postList.get(position).getContact()));

                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions((Activity) context,
                            new String[]{Manifest.permission.READ_PHONE_STATE},
                            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);
                    return;
                }
                context.startActivity(intent);
            });
        }
    }

    private void syncWithFirebase(Post post, boolean response) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getUid();
        HashMap<String, Boolean> myResponse = new HashMap<>();

        myResponse.put(uid, response);

        reference.child("responses").child(post.getId()).setValue(myResponse).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                Toast.makeText(context, "response recorded!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        if (postList != null) {
            return postList.size();
        } else {
            return 0;
        }
    }

    public void setPostList(List<Post> postList) {
        this.postList = postList;
        notifyDataSetChanged();
    }

    /*
     *
     * this class contains the attributes of each item
     * we define the items inside the constructor
     *
     * and control data from ViewHolder method
     *
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView mUsername;
        TextView mLocation;
        TextView mPost;
        TextView mDate;
        TextView mContact;
        TextView mBlood;
        CircleImageView mAvatar;

        Button mInterested;
        Button mDeny;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAvatar = itemView.findViewById(R.id.cv_avatar);

            mUsername = itemView.findViewById(R.id.tv_username);
            mLocation = itemView.findViewById(R.id.tv_location);
            mPost = itemView.findViewById(R.id.tv_post);
            mBlood = itemView.findViewById(R.id.tv_blood);
            mDate = itemView.findViewById(R.id.tv_calendar);
            mContact = itemView.findViewById(R.id.tv_contact);

            mInterested = itemView.findViewById(R.id.btn_interested);
            mDeny = itemView.findViewById(R.id.btn_deny);

        }
    }


}
