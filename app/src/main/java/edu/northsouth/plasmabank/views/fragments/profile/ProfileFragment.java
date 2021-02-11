package edu.northsouth.plasmabank.views.fragments.profile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.models.User;
import edu.northsouth.plasmabank.utils.CustomProgressBar;
import edu.northsouth.plasmabank.utils.LocalStorage;
import edu.northsouth.plasmabank.views.EnterActivity;

public class ProfileFragment extends Fragment {
    ImageButton mSettings;

    TextView mName;
    TextView mEmail;
    TextView mPhone;
    TextView mBlood;
    TextView mLocation;
    TextView mStatus;

    CircleImageView mAvatar;
    CustomProgressBar progressBar;

    ImageButton mLogout;

    FirebaseDatabase database;
    DatabaseReference reference;
    LocalStorage localStorage;

    User user;

    public static ProfileFragment newInstance() {
        return new ProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = new CustomProgressBar(getContext());

        mAvatar = view.findViewById(R.id.iv_profile);

        mName = view.findViewById(R.id.tv_name);
        mPhone = view.findViewById(R.id.tv_phone);
        mEmail = view.findViewById(R.id.tv_email);
        mBlood = view.findViewById(R.id.tv_blood);
        mLocation = view.findViewById(R.id.tv_location);
        mStatus = view.findViewById(R.id.tv_status);

        mLogout = view.findViewById(R.id.ib_logout);
        mSettings = view.findViewById(R.id.ib_settings);

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        localStorage = new LocalStorage(getContext());

        loadDataFromFirebase();

        mSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(UpdateProfileFragment.newInstance());
            }
        });

        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();

                progressBar.show("Logging out...");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.hide();

                        Intent intent = new Intent(getContext(), EnterActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                }, 650);

            }
        });

    }

    private void updateUI(User user) {
        if(user != null) {
            mStatus.setText(user.getType());
            mName.setText(user.getName());
            mPhone.setText(user.getPhone());
            mEmail.setText(user.getEmail());
            mBlood.setText(user.getBlood());
            mLocation.setText(user.getLocation().getCity());

            if(user.getSex() == 0) {
                mAvatar.setImageResource(R.drawable.ic_female);
            } else {
                mAvatar.setImageResource(R.drawable.ic_male);

            }
        }
    }

    private void loadDataFromFirebase() {
        String uid = FirebaseAuth.getInstance().getUid();

        Log.e("ERROR", uid);

        reference.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    user = snapshot.getValue(User.class);

                    localStorage.saveUserData(user);

                    Log.d("FIREBASE_DATA", user.getBlood());
                    updateUI(user);
                }
                else {
                    Toast.makeText(getContext(), "NOT GETTING DATA", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                user = localStorage.getUserData();
            }
        });
    }

    private void changeFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                R.anim.left_enter, R.anim.right_out);
        fragmentTransaction.replace(R.id.frame_container, fragment);
        fragmentTransaction.commit();
    }

}