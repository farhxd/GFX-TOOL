package com.iam1xd.gfx;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

public class settings extends AppCompatActivity {

    private static final String SHARED_PREF_SP = "sp";
    private DBVM viewModel;
    private String tglink;
    private String ytLink;
    private String tutorLink;
    private String appStore;
    private String xdTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.black));
        Button back = findViewById(R.id.back);
        LinearLayout tutor = findViewById(R.id.tutor);
        LinearLayout tgram = findViewById(R.id.tgram);
        LinearLayout ytube = findViewById(R.id.ytube);
        LinearLayout moreap = findViewById(R.id.moreapp);
        LinearLayout xdtoo = findViewById(R.id.xdtool);
        Intent intent = new Intent();
        TextView textView = findViewById(R.id.textView);
        Switch autoSwitch = findViewById(R.id.autoSwitch);
        Switch switch1 = findViewById(R.id.switch1);
        Boolean switchFix;

        viewModel = new ViewModelProvider(this).get(DBVM.class);
        viewModel.loadLinks();
        viewModel.getLinks().observe(this, links -> {
            Toast.makeText(this, "hi", Toast.LENGTH_SHORT).show();
            Log.d("GG", "onCreate: " + links.get(0).getName());

            for (items item : links) {
                if (item.getName().equalsIgnoreCase("Telegram")) {
                    tglink = item.getUrl();
                } else if (item.getName().equalsIgnoreCase("YouTube")) {
                    ytLink = item.getUrl();
                } else if (item.getName().equalsIgnoreCase("Tutor")) {
                    tutorLink = item.getUrl();
                } else if (item.getName().equalsIgnoreCase("appstore")) {
                    appStore = item.getUrl();
                }
            }


        });

        SharedPreferences sp = getSharedPreferences(SHARED_PREF_SP, Context.MODE_PRIVATE);
        SharedPreferences.Editor editSP = sp.edit();

        autoSwitch.setChecked(sp.getBoolean("autoSwitch", false));
        switch1.setChecked(sp.getBoolean("switch1", false));





        autoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    editSP.putBoolean("autoSwitch", isChecked);
                    editSP.commit();
            }
        });

        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                editSP.putBoolean("switch1", isChecked);
                editSP.commit();
            }
        });

        try {
            textView.setText("Version: " + getPackageManager().getPackageInfo(getPackageName(),0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            textView.setText("Version: 0");
            throw new RuntimeException(e);
        }
        xdtoo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + "com.iam1xd.configtool"));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException anfe) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=" + "com.iam1xd.configtool"));
                    startActivity(intent);
                }

            }
        });
        moreap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String devId = "5366277629134371348";
                Intent intent;
                try {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=" + devId));
                    intent.setPackage("com.android.vending");
                    startActivity(intent);
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=" + devId));
                    startActivity(intent);
                }

            }
        });
        tutor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(tutorLink));
                startActivity(intent);
            }
        });

        tgram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(tglink));
                startActivity(intent);
            }
        });
        ytube.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(ytLink));
                startActivity(intent);
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               finish();
            }
        });

    }
}