package kr.devta.amessage;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SplashLogoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_logo);
        Manager.showActivityName(this);
        Manager.init(getApplicationContext());

        if (!Manager.checkNetworkConnect()) {
            Toast.makeText(getApplicationContext(), "Check Network Status..", Toast.LENGTH_SHORT).show();
            finish();
        }

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                next();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getApplicationContext(), "앱을 종료합니다..", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        TedPermission.Builder tedPermission = TedPermission.with(getApplicationContext());
        tedPermission.setPermissionListener(permissionListener);
        tedPermission.setPermissions(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS
                , Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS
                , Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
        tedPermission.setRationaleMessage("권한 허용");
        tedPermission.setDeniedMessage("권한을 거부하시면 앱을 사용할 수 없습니다. [ 설정 ] -> [ 권한 ] 에서 다시 허용할 수 있습니다. ");
        tedPermission.check();
    }

    private void next() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), Manager.REQUEST_CODE_FIREBASE_LOGIN);
        } else done();
    }

    private void done() {
        if (!Manager.isServiceRunning(MainService.class)) {
            Manager.print("MainService is not running");
            startService(new Intent(getApplicationContext(), MainService.class));
        }

//        Manager.checkNetworkMessage();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(), TutorialActivity.class));
                finish();
            }
        }, 1800);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Manager.REQUEST_CODE_FIREBASE_LOGIN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Toast.makeText(getApplicationContext(), "UID: " + user.getUid(), Toast.LENGTH_SHORT).show();
                done();
            } else {
                Toast.makeText(getApplicationContext(), "Login Error: Exit Application..", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
