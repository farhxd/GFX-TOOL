package com.iam1xd.gfx;

import static android.os.Build.VERSION.SDK_INT;
import static com.iam1xd.gfx.FileUtil.getPackageDataDir;
import static java.lang.Float.parseFloat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import rikka.shizuku.Shizuku;

public class MainActivity extends AppCompatActivity {

    private static final int SHIZUKU_CODE = 333;
    private String pkg = "";
    private final String DataDir = "/storage/emulated/0/Android/Data/";
    private final String ObbDir = "/storage/emulated/0/Android/Obb/";
    private final String Saved = "/files/UE4Game/ShadowTrackerExtra/ShadowTrackerExtra/Saved/";
    private final String Usercustom = Saved + "Config/Android";
    private final String Activesav = Saved + "SaveGames";
    private String DataUC = "";
    private String DataSav = "";

    private boolean resoPicked;
    private static final String SHARED_PREF_SP = "sp";
    boolean userClicked = true;

    private IUserService userService;
    private boolean bound = false;
    private String savPath;
    private String resolutionPath;
    private final String UCXD = DataDir + "com.iam1xd.gfx" + "/files/UCXD.ini";
    public final Boolean patchBlock = false;
    public boolean stuckOnError;
    public float versionApp;


    private final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            if (iBinder != null && iBinder.pingBinder()) {
            userService = IUserService.Stub.asInterface(iBinder);
            bound = true;
            } else {
                Toast.makeText(MainActivity.this, "invalid binder for " + componentName + " received", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
          //  Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            userService = null;
            bound = false;
        }
    };
    private IamOneXDFileUtil utils;
    private String initializee = "";
    private String zz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        SharedPreferences sp = getSharedPreferences(SHARED_PREF_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editSP = sp.edit();
        utils = new IamOneXDFileUtil(this);

        try {
            versionApp = parseFloat(getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            versionApp = 0.0f;
            throw new RuntimeException(e);
        }

        FirebaseApp.initializeApp(this);

        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance()
        );



        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("update");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, String> updateMap =(Map<String, String>) snapshot.getValue();
                if(updateMap != null) {
                    float newVer = parseFloat(updateMap.get("version"));
                    String upLink = updateMap.get("link");

                    if (newVer > versionApp) {
                        AlertDialog.Builder updateDialiog = new AlertDialog.Builder(MainActivity.this);
                        updateDialiog.setTitle("New Update is Available");
                        updateDialiog.setMessage("New Version " + newVer + " Brings new features and fixes some bugs and issues. \nYou must download the updated version else this app will not work in your device.");
                        updateDialiog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent down = new Intent(Intent.ACTION_VIEW);
                                down.setData(Uri.parse(upLink));
                                startActivity(down);
                            }
                        });
                        updateDialiog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                updateDialiog.create().show();
                            }
                        });
                        updateDialiog.setIcon(R.drawable.baseline_new_releases_24);
                        updateDialiog.setCancelable(false);
                        updateDialiog.create().show();
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });



        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch shizukuSwitch = findViewById(R.id.shizukuSwitch);
        if (SDK_INT <= 29) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }
        }
        if (Shizuku.pingBinder()) {
            boolean isGranted;
            isGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            if (isGranted) {
                bindUserService();
                userClicked = false;
                shizukuSwitch.setChecked(true);
                if (!sp.contains("autoSwitch")) {
                    sp.edit().putBoolean("autoSwitch", true).commit();
                }
                userClicked = true;
            }
        }
        int filesVerCode = 0;
       /* try {
            filesVerCode = getPackageManager().getPackageInfo("com.google.android.documentsui", 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }

        if (filesVerCode > 330543000) {
            AlertDialog.Builder dialogFC = new AlertDialog.Builder(this);
            dialogFC.setTitle("Your files app has some issue")
                    .setMessage("Uninstall updates of the files app from settings")
                    .setPositiveButton("UNINSTALL UPDATES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", "com.google.android.documentsui", null);
                            intent.setData(uri);
                            startActivity(intent);

                        }
                    })
                    .setNegativeButton("Close & use Shizuku API", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .setCancelable(true)
                    .create().show();
        } */

        if (!FileUtil.isDirectory(getPackageDataDir(getApplicationContext()) + "/pkg")) {
            try {
                InputStream zis = getAssets().open("pkg.zip");
                utils.unZip(zis, getPackageDataDir(getApplicationContext()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Spinner spinner = findViewById(R.id.spinVersion);
        List<String> items = new ArrayList<>();
        items.add(0, "Choose Version");
        items.add(1, "India");
        items.add(2, "Global");
        items.add(3, "Korea");
        items.add(4, "Vietnam");
        items.add(5, "Taiwan");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    pkg = "null";

                } else if (position == 1) {
                    pkg = "com.pubg.imobile";
                } else if (position == 2) {
                    pkg = "com.tencent.ig";
                } else if (position == 3) {
                    pkg = "com.pubg.krmobile";
                } else if (position == 4) {
                    pkg = "com.vng.pubgmobile";
                } else if (position == 5) {
                    pkg = "com.rekoo.pubgm";
                }


                DataUC = DataDir + pkg + Usercustom;
                DataSav = DataDir + pkg + Activesav;
                editSP.putInt("version", position).commit();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spinReso = findViewById(R.id.spinReso);
        List<String> resolutions = new ArrayList<>();
        resolutions.add(0, "144P");
        resolutions.add(1, "360P");
        resolutions.add(2, "480P");
        resolutions.add(3, "540P");
        resolutions.add(4, "720P");
        resolutions.add(5, "1080P");
        resolutions.add(6, "1440P");
        resolutions.add(7, "2160P");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, resolutions);
        spinReso.setAdapter(adapter1);
        spinReso.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                resoPicked = true;
                editSP.putInt("reso", position).commit();
                resolutionPath = FileUtil.getPackageDataDir(getApplicationContext()) + "/pkg/Resolution/" + resolutions.get(position) + ".ini";

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Spinner spinFPS = findViewById(R.id.spinFPS);
        ArrayList<String> savs = new ArrayList<>();
        savs.add(0, "20FPS");
        savs.add(1, "25FPS");
        savs.add(2, "30FPS");
        savs.add(3, "40FPS");
        savs.add(4, "60FPS");
        savs.add(5, "90FPS");

        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, savs);
        spinFPS.setAdapter(adapter2);
        spinFPS.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editSP.putInt("fps", position).commit();
                savPath = FileUtil.getPackageDataDir(getApplicationContext()) + "/pkg/Activesav/" + savs.get(position);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


            spinner.setSelection(sp.getInt("version", 0));
            spinFPS.setSelection(sp.getInt("fps", 4));
            spinReso.setSelection(sp.getInt("reso", 4));


        shizukuSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (userClicked) {
                        if (Shizuku.pingBinder()) {
                            boolean isGranted;
                            isGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
                            if (!isGranted) {
                                Shizuku.requestPermission(SHIZUKU_CODE);
                                userClicked = false;
                                shizukuSwitch.setChecked(false);
                                userClicked = true;
                            } else {
                                bindUserService();
                            }
                            if (isGranted) {
                                Toast.makeText(MainActivity.this, "Shizuku Mode Activated", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            userClicked = false;
                            shizukuSwitch.setChecked(false);
                            userClicked = true;
                            AlertDialog.Builder installShizuku = new AlertDialog.Builder(MainActivity.this);
                            installShizuku.setCancelable(false)
                                    .setTitle("Shizuku is not Active")
                                    .setMessage("To use this GFX on Android 14 Devices you must install Shizuku (It is an Open Source, Safe Application)")
                                    .setPositiveButton("TUTORIAL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent shizukuTutor = new Intent();
                                            shizukuTutor.setAction(Intent.ACTION_VIEW);
                                            shizukuTutor.setData(Uri.parse("https://telegram.me/iAM1XD_YT"));
                                            startActivity(shizukuTutor);
                                        }
                                    })
                                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                        }
                                    });
                            installShizuku.create().show();
                        }
                    }

                } else {
                    if (Shizuku.pingBinder()) {
                        unbindUserService();
                    }

                }
            }
        });
        Switch switch60 = findViewById(R.id.unlock60);
        Switch ipadSwitch = findViewById(R.id.ipadSwitch);
        Button rungame = findViewById(R.id.rungame);
        rungame.setOnClickListener(new View.OnClickListener() {
            private String uc;

            @Override
            public void onClick(View v) {
                if (ipadSwitch.isChecked()) {
                    if (!savPath.endsWith("i")) {
                        savPath = savPath + "i";
                    }
                }
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage(pkg);
                if (launchIntent != null) {
                    if (switch60.isChecked() || (spinReso.getSelectedItemPosition() < 4)) {
                        AlertDialog.Builder rungD = new AlertDialog.Builder(MainActivity.this);
                        rungD.setTitle("If you want to change Resolution or Unlock 60FPS");
                        rungD.setMessage("You will have to do extra steps. \n1. Click Backup & GO \n2.Your Data will be Backed-Up offline instantly \n3. Now clear Data of your Game\n4. Come Back to GFX now & Click RUN GAME again to Restore and Start \n\nNote! \nYour Data & Resrouces will be safe you will not re-download it.");
                        rungD.setCancelable(false);

                        if (sp.getBoolean("step2", false)) {
                            if (shizukuSwitch.isChecked()) {
                                try {
                                    userService.executeAdbCommands("mv " + DataDir + pkg + "XD " + DataDir + pkg);
                                    clearRAM(MainActivity.this);
                                    Thread.sleep(300);
                                    editSP.remove("step2").commit();
                                    startActivity(launchIntent);
                                } catch (RemoteException e) {
                                    throw new RuntimeException(e);
                                } catch (InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            } else {
                                if (SDK_INT <= 29) {
                                    if (!new File(ObbDir + pkg + "XD").renameTo(new File(ObbDir + pkg))) {
                                        Toast.makeText(MainActivity.this, "Error, Cant Restore 2", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                if (SDK_INT >= 33) {
                                    initializee = "/";
                                }
                                if (utils.rename(DataDir + pkg + "XD" + initializee, pkg)) {
                                    editSP.remove("step2").commit();
                                    clearRAM(MainActivity.this);
                                    startActivity(launchIntent);
                                } else {
                                    //     Toast.makeText(MainActivity.this, "Error, Cant Restore", Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            rungD.setPositiveButton("Backup & Go", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (shizukuSwitch.isChecked()) {

                                        try {
                                            if (!FileUtil.isExistFile(UCXD)) {
                                                uc = userService.executeAdbCommand2("cat " + DataDir + pkg + Usercustom + "/UserCustom.ini");
                                                if (uc != null) {
                                                    if (uc.contains("UserCustom DeviceProfile") && uc.contains("BackUp DeviceProfile")) {
                                                        if (uc.indexOf("[UserCustom DeviceProfile]") == 0) {
                                                            // start with uc
                                                            Toast.makeText(MainActivity.this, "Reading", Toast.LENGTH_SHORT).show();

                                                            FileUtil.writeFile(UCXD, uc.substring(uc.indexOf("[BackUp DeviceProfile]")));
                                                            executor2("echo \"" + FileUtil.readFile(resolutionPath) + "\n\n" + uc.substring(uc.indexOf("[BackUp DeviceProfile]")) + "\" > " + DataDir + pkg + Usercustom + "/UserCustom.ini");

                                                        } else {
                                                            // start backup
                                                            //  Toast.makeText(MainActivity.this, "Reading", Toast.LENGTH_SHORT).show();

                                                            FileUtil.writeFile(UCXD, uc.substring(uc.indexOf("[BackUp DeviceProfile]"), uc.indexOf("\n\n")));
                                                            executor2("echo \"" + FileUtil.readFile(resolutionPath) + "\n\n" + uc.substring(uc.indexOf("[BackUp DeviceProfile]"), uc.indexOf("\n\n")) + "\" > " + DataDir + pkg + Usercustom + "/UserCustom.ini");
                                                        }
                                                    } else {

                                                        android.app.AlertDialog.Builder errorD = new android.app.AlertDialog.Builder(MainActivity.this);
                                                        errorD.setCancelable(true);
                                                        errorD.setTitle("There is error in your GAME Files but don't worry")
                                                                .setPositiveButton("FIX & RUN", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        executor2("rm -rf " + DataDir + pkg + Usercustom + "/UserCustom.ini");
                                                                        executor2("touch " + DataDir + pkg + Usercustom + "/UserCustom.ini");
                                                                        startActivity(launchIntent);
                                                                        stuckOnError = true;
                                                                    }
                                                                })
                                                                .setMessage("You can fix the game files in just 1 minute \nWith the help of our Troubleshooter\nYou just need to click FIX & RUN. Wait 10-15 second in Loading Screen then reapply GFX without any errors.")
                                                                .create().show();
                                                    }
                                                }
                                            } else {
                                                executor2("echo \"" + FileUtil.readFile(resolutionPath) + "\n\n" + FileUtil.readFile(UCXD) + "\" > " + DataDir + pkg + Usercustom + "/UserCustom.ini");
                                            }
                                            userService.executeAdbCommands("cp " + savPath + " " + DataDir + pkg + Activesav + "/Active.sav");
                                            if (patchBlock) {
                                                userService.executeAdbCommands("mkdir -p " + DataDir + pkg + Saved + "Paks/new.filelist");
                                                executor2("rm -rf " + DataDir + pkg + Saved + "Paks/game*");
                                                userService.executeAdbCommands("cp " + FileUtil.getPackageDataDir(getApplicationContext()) + "/pkg/new.filelist/filelist " + DataDir + pkg + Saved + "Paks/new.filelist/filelist");

                                            }
                                            userService.executeAdbCommands("mv " + DataDir + pkg + " " + DataDir + pkg + "XD");

                                            if (!stuckOnError) {
                                                if (sp.getBoolean("autoSwitch", false)) {
                                                    userService.executeAdbCommands("pm clear " + pkg);
                                                    Thread.sleep(300);
                                                    userService.executeAdbCommands("mv " + DataDir + pkg + "XD " + DataDir + pkg);
                                                    clearRAM(MainActivity.this);
                                                    Thread.sleep(300);
                                                    startActivity(launchIntent);
                                                } else {
                                                    Intent clrData = new Intent();
                                                    clrData.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                    Uri uri = Uri.fromParts("package", pkg, null);
                                                    clrData.setData(uri);
                                                    editSP.putBoolean("step2", true).commit();
                                                    startActivity(clrData);
                                                }
                                            }
                                        } catch (RemoteException e) {
                                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                            throw new RuntimeException(e);
                                        } catch (InterruptedException e) {
                                            throw new RuntimeException(e);
                                        }

                                    } else {

                                        if (SDK_INT <= 29) {
                                            if (!new File(ObbDir + pkg).renameTo(new File(ObbDir + pkg + "XD"))) {
                                                //    Toast.makeText(MainActivity.this, "Error, Cant Backup", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        if (SDK_INT >= 33) {
                                            initializee = "/";
                                        }
                                        if (!FileUtil.isExistFile(UCXD)) {
                                            uc = utils.readFile(DataDir + pkg + Usercustom + "/UserCustom.ini");
                                            if (uc != null) {
                                                if (uc.contains("UserCustom DeviceProfile") && uc.contains("BackUp DeviceProfile")) {
                                                    if (uc.indexOf("[UserCustom DeviceProfile]") == 0) {
                                                        // start with uc
                                                        utils.writeFile(DataDir + pkg + Usercustom + "/UserCustom.ini", FileUtil.readFile(resolutionPath) + "\n\n\n" + uc);

                                                        FileUtil.writeFile(UCXD, uc.substring(uc.indexOf("[BackUp DeviceProfile]")));
                                                    } else {
                                                        // start backup
                                                        FileUtil.writeFile(UCXD, uc.substring(uc.indexOf("[BackUp DeviceProfile]"), uc.indexOf("\n\n\n")));
                                                        utils.writeFile(DataDir + pkg + Usercustom + "/UserCustom.ini", FileUtil.readFile(resolutionPath) + "\n\n\n" + uc);

                                                    }
                                                } else {

                                                    android.app.AlertDialog.Builder errorD = new android.app.AlertDialog.Builder(MainActivity.this);
                                                    errorD.setCancelable(true);
                                                    errorD.setTitle("There is error in your GAME Files but don't worry")
                                                            .setPositiveButton("FIX & RUN", new DialogInterface.OnClickListener() {
                                                                @Override
                                                                public void onClick(DialogInterface dialog, int which) {

                                                                    utils.deleteFile(DataDir + pkg + Usercustom + "/UserCustom.ini");
                                                                    utils.writeFile(DataDir + pkg + Usercustom + "/UserCustom.ini", "");
                                                                    startActivity(launchIntent);
                                                                    stuckOnError = true;
                                                                }
                                                            })
                                                            .setMessage("You can fix the game files in just 1 minute \nWith the help of our Troubleshooter\nYou just need to click FIX & RUN. Wait 10-15 second in Loading Screen then reapply GFX without any errors.")
                                                            .create().show();
                                                }
                                            }
                                        } else {
                                            utils.writeFile(DataDir + pkg + Usercustom + "/UserCustom.ini", FileUtil.readFile(resolutionPath) + "\n\n\n" + FileUtil.readFile(UCXD));
                                        }
                                        utils.copyFileUtil(savPath, DataDir + pkg + Activesav, "Active.sav");
                                        if (patchBlock) {
                                            if (utils.canReadAndWrite(utils.uriFromPathXD(DataDir + pkg + Saved + "Paks"))) {
                                                utils.copySingleDir(FileUtil.getPackageDataDir(getApplicationContext()) + "/pkg/new.filelist", DataDir + pkg + Saved + "Paks");
                                                utils.batchDelete(DataDir + pkg + Saved + "Paks", "game_patch");
                                            } else {
                                                utils.askPermission(100, utils.uriFromPathFull(DataDir + pkg + Saved + "Paks"));
                                            }
                                        }
                                        if(!stuckOnError) {
                                            if (utils.rename(DataDir + pkg + initializee, pkg + "XD")) {
                                                editSP.putBoolean("step2", true).commit();
                                                Intent clrData = new Intent();
                                                clrData.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                                Uri uri = Uri.fromParts("package", pkg, null);
                                                clrData.setData(uri);
                                                startActivity(clrData);
                                            }
                                        }
                                    }
                                }
                            });
                            rungD.create().show();
                        }

                    } else {
                        AlertDialog.Builder runSav = new AlertDialog.Builder(MainActivity.this);
                        runSav.setTitle("Do you want to apply these settings in your game?");
                        runSav.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                utils.copyFileUtil(savPath, DataDir + pkg + Activesav, "Active.sav");
                                if (patchBlock) {
                                    if (utils.canReadAndWrite(utils.uriFromPathXD(DataDir + pkg + Saved + "Paks"))) {
                                        utils.copySingleDir(FileUtil.getPackageDataDir(getApplicationContext()) + "/pkg/new.filelist", DataDir + pkg + Saved + "Paks");
                                        utils.batchDelete(DataDir + pkg + Saved + "Paks", "game_patch");
                                    }
                                    clearRAM(MainActivity.this);
                                    startActivity(launchIntent);
                                } else {
                                    utils.askPermission(100, utils.uriFromPathFull(DataDir + pkg + Saved + "Paks"));
                                }
                            }
                        });
                        runSav.create().show();
                    }
                } else {
                    if (pkg.equals("null")) {
                        Toast.makeText(MainActivity.this, "Please Select Game Version", Toast.LENGTH_SHORT).show();
                    } else {
                    Toast.makeText(MainActivity.this, pkg + " not installed", Toast.LENGTH_SHORT).show();
                }
            }
            }
        });

        ToggleButton btnGPU = findViewById(R.id.btnGPU);
        ToggleButton btnRAM = findViewById(R.id.btnRAM);
        ToggleButton btnCPU = findViewById(R.id.btnCPU);

        btnGPU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("btnGPU", btnGPU.isChecked()).commit();
                if (btnGPU.isChecked()) {
                    btnGPU.setTextColor(Color.parseColor("#FF007A05"));
                } else {
                    btnGPU.setTextColor(Color.RED);
                }
            }
        });
        btnRAM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("btnRAM", btnRAM.isChecked()).commit();
                if (btnRAM.isChecked()) {
                    btnRAM.setTextColor(Color.parseColor("#FF007A05"));
                    if (userClicked) {
                        clearRAM(MainActivity.this);
                    }
                } else {
                    btnRAM.setTextColor(Color.RED);
                }
            }
        });
        btnCPU.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("btnCPU", btnCPU.isChecked()).commit();
                if (btnCPU.isChecked()) {
                    btnCPU.setTextColor(Color.parseColor("#FF007A05"));
                } else {
                    btnCPU.setTextColor(Color.RED);
                }
            }
        });
        userClicked = false;
        btnRAM.setChecked(sp.getBoolean("btnRAM", false));
        userClicked = true;
        btnCPU.setChecked(sp.getBoolean("btnCPU", false));
        btnGPU.setChecked(sp.getBoolean("btnGPU", false));

        ipadSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("ipadSwitch", ipadSwitch.isChecked()).commit();
            }
        });
        switch60.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("switch60", switch60.isChecked()).commit();
            }
        });

        ipadSwitch.setChecked(sp.getBoolean("ipadSwitch", false));
        switch60.setChecked(sp.getBoolean("switch60", false));

        ImageButton helpBtn = findViewById(R.id.helpbtn);

        helpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent().setClass(getApplicationContext(), settings.class);
                startActivity(in);
            }
        });
        ImageButton icon = findViewById(R.id.iconbtn);

    }
    /*
    private final ServiceConnection userServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            StringBuilder res = new StringBuilder();
            res.append("onServiceConnected: ").append(componentName.getClassName()).append('\n');
            if (binder != null && binder.pingBinder()) {
                IUserService service = IUserService.Stub.asInterface(binder);
                try {
                    service.executeAdbCommands("echo hi");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    res.append(Log.getStackTraceString(e));
                }
            } else {
                res.append("invalid binder for ").append(componentName).append(" received");
            }
            Toast.makeText(MainActivity.this, res.toString().trim(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(MainActivity.this, "onServiceDisconnected: " + '\n' + componentName.getClassName(), Toast.LENGTH_SHORT).show();
        }
    };

     */
    private final Shizuku.UserServiceArgs userServiceArgs =
            new Shizuku.UserServiceArgs(new ComponentName("com.iam1xd.gfx", UserService.class.getName()))
                    .daemon(true)
                    .processNameSuffix("service");

    public static void clearRAM(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        long freeMemory = memoryInfo.availMem / (1024 * 1024); // Convert bytes to megabytes

        String message = "Free RAM: " + freeMemory + " MB";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void bindUserService() {
        StringBuilder res = new StringBuilder();
        try {
            if (Shizuku.getVersion() < 10) {
                res.append("requires Shizuku API 10");
            } else {
                Shizuku.bindUserService(userServiceArgs, userServiceConnection);
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
            res.append(tr);
        }
        res.append("Binding Done");
      //  Toast.makeText(this, res.toString().trim(), Toast.LENGTH_SHORT).show();
    }

    private void unbindUserService() {
        StringBuilder res = new StringBuilder();
        try {
            if (Shizuku.getVersion() < 10) {
                res.append("requires Shizuku API 10");
            } else {
                Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true);
              //  Toast.makeText(this, "Unbinding Done", Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
            res.append(tr);
        }
        Toast.makeText(this, res.toString().trim(), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        unbindUserService();
        super.onDestroy();
    }

    private void executor2(String command) {
        try {
            File temp = new File(getPackageDataDir(getApplicationContext()) + "/zee.sh");
            temp.createNewFile();
            Files.write(temp.toPath(), command.getBytes());
            temp.setExecutable(true);
            userService.executeAdbCommand2("sh " + temp.getAbsolutePath());
            temp.delete();
        } catch (IOException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        } catch (RemoteException e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 100) {
                utils.persistFolder(data);
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
