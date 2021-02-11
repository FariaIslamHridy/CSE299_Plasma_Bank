package edu.northsouth.plasmabank.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.models.Post;
import edu.northsouth.plasmabank.models.User;
import edu.northsouth.plasmabank.utils.Constants;
import edu.northsouth.plasmabank.utils.LocalStorage;

public class CreatePostActivity extends BottomSheetDialogFragment {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @SuppressLint("StaticFieldLeak")
    private static CreatePostActivity activity = null;

    EditText mDescription;
    EditText mDate;
    Spinner mBlood;
    EditText mLocation;
    Button mPost;

    LocalStorage localStorage;
    User user;

    private CreatePostActivity() {

    }

    public static CreatePostActivity newInstance(Context context) {
        if(activity == null) {
            activity = new CreatePostActivity();
        }
        CreatePostActivity.context = context;
        return activity;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_create_post, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        localStorage = new LocalStorage(context);

        mBlood = view.findViewById(R.id.spinner_blood);
        mDescription = view.findViewById(R.id.et_post);
        mLocation = view.findViewById(R.id.et_location);
        mPost = view.findViewById(R.id.btn_post);
        mDate = view.findViewById(R.id.et_date);

        mLocation.setEnabled(false);
        mLocation.setText(localStorage.getUserData().getLocation().getCity());

        mBlood.setAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_spinner_dropdown_item, Constants.BLOOD_GROUPS));


        mPost.setOnClickListener(v -> createPost());

    }

    private void createPost() {
        String description = mDescription.getText().toString();
        String date = mDate.getText().toString().trim();
        String location = mLocation.getText().toString().trim();

        user = localStorage.getUserData();

        String blood = (String) mBlood.getSelectedItem();
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();

        if (location.equals("") || description.isEmpty() || date.isEmpty() || blood.isEmpty()) {
            return;
        }

        if (!blood.equals(Constants.BLOOD_GROUPS[0])) {
            Post post = new Post(user.getSex(), user.getName(), location, description, phoneNumber, date, blood);

            syncDatabase(post);

        }
    }

    private void syncDatabase(Post post) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
//        String uid = FirebaseAuth.getInstance().getUid();
        String id = reference.push().getKey();

        post.setId(id);
        post.setAuthor(user.getName());
        post.setManaged(false);

        reference.child("posts").child(id).setValue(post).addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                dismiss();
                Toast.makeText(getContext(), "Request post Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}