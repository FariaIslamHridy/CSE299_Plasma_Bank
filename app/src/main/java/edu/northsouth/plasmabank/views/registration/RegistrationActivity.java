package edu.northsouth.plasmabank.views.registration;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class RegistrationActivity extends AppCompatActivity {

    private TextView mPhone;
    private EditText mName;
    private EditText mEmail;
    private EditText mLocation;
    private Spinner mBlood;
    private ToggleButton mUserType;

    private ImageView mAvatar;
    private int avatar = 0;    // 0 = female, 1 = male

    CustomProgressBar progressBar;
    GpsTracker gpsTracker;
    Location myLocation;

    DatabaseReference mDatabase;
    LocalStorage localStorage;

    Button mSave;
    String userType = "Receiver";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_registration);

        progressBar = new CustomProgressBar(this);

        mAvatar = findViewById(R.id.iv_profile);
        mName = findViewById(R.id.et_name);
        mEmail = findViewById(R.id.et_email);
        mPhone = findViewById(R.id.tv_phone);
        mBlood = findViewById(R.id.spinner_blood);
        mLocation = findViewById(R.id.et_location);
        mUserType = findViewById(R.id.tb_user_type);
        mLocation.setEnabled(false);

        mSave = findViewById(R.id.btn_save);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        localStorage = new LocalStorage(this);

        setUI();

        mBlood.setAdapter(new ArrayAdapter<>(this, R.layout.simple_spinner_dropdown_item, Constants.BLOOD_GROUPS));

        requestPermission();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            } else {
                getLocation();
                mLocation.setText(myLocation.getCity());
            }
        } catch (Exception e) {
            e.printStackTrace();
            requestPermission();
        }

        mUserType.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    userType = "Donor";
                } else {
                    userType = "Receiver";
                }
            }
        });

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registration();
            }
        });

        mAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleAvatar();
            }
        });

    }

    private void registration() {
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

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.hide();
                }
            }, 1000);
        } else {
            User user = new User(name, avatar, userType, blood, phoneNumber, email, myLocation);
            syncWithFirebase(user);
        }
    }

    private void syncWithFirebase(User user) {
        String uid = FirebaseAuth.getInstance().getUid();

        assert uid != null;
        mDatabase.child("users").child(uid).setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    localStorage.saveUserData(user);
                    Intent intent = new Intent(RegistrationActivity.this, CompleteRegistrationActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                } else {
                    Toast.makeText(RegistrationActivity.this, "Error Occurs!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setUI() {
        String phoneNumber = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getPhoneNumber();
        mPhone.setText(phoneNumber);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    private void getLocation() {
        gpsTracker = new GpsTracker(this);


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
        geocoder = new Geocoder(this.getApplicationContext(), Locale.getDefault());
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
        Dexter.withContext(this)
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
                .withErrorListener(dexterError -> Toast.makeText(RegistrationActivity.this, "Error occurred" + dexterError.toString(), Toast.LENGTH_SHORT).show())
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.CustomProgressBarTheme);
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
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsTracker.stopUsingGPS();
    }
}