package edu.northsouth.plasmabank.views.fragments;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.models.Post;
import edu.northsouth.plasmabank.utils.CustomProgressBar;
import edu.northsouth.plasmabank.utils.LocalStorage;
import edu.northsouth.plasmabank.utils.adapters.PostAdapter;
import edu.northsouth.plasmabank.views.CreatePostActivity;

public class HomeFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    FloatingActionButton mCreatePost;

    final static int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 999;
    LocalStorage localStorage;

    CustomProgressBar progressBar;

    private List<Post> postList;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.refresh);
        mCreatePost = view.findViewById(R.id.fab_create_post);

        adapter = new PostAdapter(getContext());
        localStorage = new LocalStorage(getContext());
        progressBar = new CustomProgressBar(getContext());

        if(localStorage.getUserType().equals("Donor")) {
            mCreatePost.setVisibility(View.GONE);
        } else {
            mCreatePost.setVisibility(View.VISIBLE);
        }

        getPostList();

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(true);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // implement Handler to wait for 3 seconds and then update UI means update value of TextView
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);

                        if (checkNetworkConnection()) {
                            getPostList();
                        }

                    }
                }, 2000);
            }
        });

        mCreatePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreatePostActivity activity = CreatePostActivity.newInstance(getContext());
                activity.show(getActivity().getSupportFragmentManager(), "createPost");
            }
        });
    }

    private void getPostList() {
        postList = new ArrayList<>();

        progressBar.show("Loading...");

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        
        reference.child("posts").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()){

                    Post post = dataSnapshot.getValue(Post.class);
                    post.setId(dataSnapshot.getKey());

                    if(post != null && !post.isManaged() && post.getBlood().equals(localStorage.getBloodGroup()) && post.getLocation().equals(localStorage.getUserData().getLocation().getCity())) {
//                        Log.d("POST_RQ", post.toString());

                        if(!isResponded(post)) {
                            postList.add(post);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        adapter.setPostList(postList);
        recyclerView.setAdapter(adapter);

        progressBar.hide();
    }

    private boolean isResponded(Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getUid();

        final boolean[] flag = {false};

        reference.child("responses").child(post.getId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {

                    if(dataSnapshot.getKey().equals(uid)) {
//                        Toast.makeText(getContext(), "key :" +dataSnapshot.getKey()+"\nUID : " + uid, Toast.LENGTH_SHORT).show();

                        flag[0] = true;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return flag[0];
    }

    public boolean checkNetworkConnection() {

        ConnectivityManager connectivityManager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;

        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        return (networkInfo != null && networkInfo.isConnected());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == MY_PERMISSIONS_REQUEST_READ_PHONE_STATE) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                // permission was granted, yay! Do the
                // read-phone-state-related task you need to do.


            } else {

                // permission denied, boo! Disable the
                // functionality that depends on this permission.
                Toast.makeText(getContext(), "permission denied", Toast.LENGTH_LONG).show();
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
}