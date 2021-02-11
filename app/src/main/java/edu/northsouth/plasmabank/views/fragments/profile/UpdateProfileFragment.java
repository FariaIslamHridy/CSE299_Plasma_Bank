package edu.northsouth.plasmabank.views.fragments.profile;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import edu.northsouth.plasmabank.R;
import edu.northsouth.plasmabank.models.Location;
import edu.northsouth.plasmabank.models.User;
import edu.northsouth.plasmabank.utils.Constants;
import edu.northsouth.plasmabank.utils.CustomProgressBar;
import edu.northsouth.plasmabank.utils.GpsTracker;
import edu.northsouth.plasmabank.utils.LocalStorage;

public class UpdateProfileFragment extends Fragment {
    CustomProgressBar progressBar;

    private TextView mPhone;
    private EditText mName;
    EditText mEmail;
    EditText mLocation;
    Spinner mBlood;
    ToggleButton mUserType;

    private ImageView mAvatar;

    private int avatar = 0;
    String userType;

    private DatabaseReference mDatabase;
    LocalStorage localStorage;
    GpsTracker gpsTracker;
    Location myLocation;
    User currentUser;

    Button mSave;

    public static UpdateProfileFragment newInstance() {
        return new UpdateProfileFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        progressBar = new CustomProgressBar(getContext());

        mAvatar = view.findViewById(R.id.iv_profile);
        mName = view.findViewById(R.id.et_name);
        mEmail = view.findViewById(R.id.et_email);
        mPhone = view.findViewById(R.id.tv_phone);
        mBlood = view.findViewById(R.id.spinner_blood);
        mLocation = view.findViewById(R.id.et_location);
        mUserType = view.findViewById(R.id.tb_user_type);
        mSave = view.findViewById(R.id.btn_save);

        localStorage = new LocalStorage(getContext());
        mDatabase = FirebaseDatabase.getInstance().getReference();

        currentUser = localStorage.getUserData();
        userType = localStorage.getUserType();
        mBlood.setAdapter(new ArrayAdapter<>(getContext(), R.layout.simple_spinner_dropdown_item, Constants.BLOOD_GROUPS));

        updateInitialUi();

        mUserType.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked) {
                userType = Constants.DONOR;
            } else {
                userType = Constants.RECEIVER;
            }
        });

        mSave.setOnClickListener(v -> updateInformation());

        mAvatar.setOnClickListener(v -> {
            mAvatar.setEnabled(false);
            toggleAvatar();
            mAvatar.setEnabled(true);
        });
    }

    private  void updateInitialUi() {
        if(currentUser != null) {
            avatar = currentUser.getSex();
            toggleAvatar();

            mName.setText(currentUser.getName());
            mEmail.setText(currentUser.getEmail());
            mPhone.setText(currentUser.getPhone());
            mBlood.setSelection(Constants.getPosition(currentUser.getBlood()));
            mLocation.setText(currentUser.getLocation().getCity());
            mUserType.setChecked(Constants.getUserType(currentUser.getType()));
        }

        mLocation.setEnabled(false);

        requestPermission();

        try {
            if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            } else {
                getLocation();
                mLocation.setText(myLocation.getCity());
            }
        } catch (Exception e) {
            e.printStackTrace();
            requestPermission();
        }
    }

    private void toggleAvatar() {
        if (avatar == 0) {
            mAvatar.animate().rotationBy(360f).setDuration(360).start();
            mAvatar.setImageResource(R.drawable.ic_male);
            avatar = 1;
        } else {
            mAvatar.animate().rotationBy(-360f).setDuration(360).start();
            mAvatar.setImageResource(R.drawable.ic_female);
            avatar = 0;
        }
    }

    private void updateInformation() {
        String name = mName.getText().toString().trim();
        String email = mEmail.getText().toString().trim();
        String blood = (String) mBlood.getSelectedItem();
        String location = mLocation.getText().toString().trim();
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();

        if (name.equals("") || email.isEmpty() || location.isEmpty()) {
            return;
        }

        if (blood.equals(Constants.BLOOD_GROUPS[0])) {
            progressBar.show("missing blood group");

            new Handler(Looper.myLooper()).postDelayed(() -> progressBar.hide(), 1000);
        } else {
            User user = new User(name, avatar, userType, blood, phoneNumber, email, myLocation);
            syncWithFirebase(user);
        }
    }

    private void syncWithFirebase(User user) {
        String uid = FirebaseAuth.getInstance().getUid();

        assert uid != null;
        mDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                Toast.makeText(getContext(), "Profile Updated!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Error Occurs!", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void changeFragment(Fragment fragment) {
//        FragmentManager fragmentManager = getChildFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//        fragmentTransaction.addToBackStack(null);
//        fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
//                R.anim.left_enter, R.anim.right_out);
//        fragmentTransaction.replace(R.id.frame_container, fragment);
//        fragmentTransaction.commit();
//    }

    private void getLocation() {
        gpsTracker = new GpsTracker(getContext());


        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();

            myLocation = new Location(latitude, longitude);
            getGeoCoding(latitude, longitude);
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    private void getGeoCoding(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());
        String city = "default";

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
            city = addresses.get(0).getLocality();

        } catch (IOException e) {
            e.printStackTrace();
        }

        myLocation.setCity(city);
    }

    private void requestPermission() {
        Dexter.withContext(getContext())
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
                        if (!multiplePermissionsReport.areAllPermissionsGranted()) {
                            showSettingsDialog();
                        }

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> list, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .withErrorListener(dexterError -> Toast.makeText(getContext(), "Error occurred" + dexterError.toString(), Toast.LENGTH_SHORT).show())
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomProgressBarTheme);
        builder.setTitle("Storage Permission");

        builder.setMessage("Storage Permission is needed to select your profile picture");
        builder.setPositiveButton("OPEN SETTINGS", (dialog, which) -> {
            dialog.cancel();
            openSettings();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.setCancelable(false);
        builder.show();

    }


    // navigating user to app settings
    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gpsTracker.stopUsingGPS();
    }
}