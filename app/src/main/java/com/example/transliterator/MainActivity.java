package com.example.transliterator;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;


import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.mannan.translateapi.Language;
import com.mannan.translateapi.TranslateAPI;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {

    EditText inputTxt;
    Button btnTranslate,saveButton;
    TextView translatedTxt, uploadTxt;

    private static final int READ_REQUEST_CODE = 42;
    private static final int PERMISSION_REQUEST_STORAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputTxt = findViewById(R.id.inputTxt);
        btnTranslate = findViewById(R.id.btnTranslate);
        translatedTxt = findViewById(R.id.translatedTxt);
        saveButton = findViewById(R.id.saveButton);
        uploadTxt = findViewById(R.id.inputTxt);


//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
//        }

        btnTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TranslateAPI translateAPI = new TranslateAPI(

                        Language.AUTO_DETECT, //target
                        Language.ENGLISH,   // o/p
                        inputTxt.getText().toString()

                );

                translateAPI.setTranslateListener(new TranslateAPI.TranslateListener() {
                    @Override
                    public void onSuccess(String translatedText) {
                        translatedTxt.setText(translatedText);
                        translatedTxt.setMovementMethod(new ScrollingMovementMethod());
                    }

                    @Override
                    public void onFailure(String ErrorText) {
                        Log.d("Error",ErrorText);
                    }
                });

            }
        });



        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(!Utils.isPermissionGranted(this)) {
            new AlertDialog.Builder(this)
                    .setTitle("All files Permission")
                    .setMessage("Due to android 11 restrictions, this app requires all files permission")
                    .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            takePermission();
                        }
                    })
                    .setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            //Toast.makeText(this, "Permission Already Granted", Toast.LENGTH_SHORT).show();
        }

    }

    private void takePermission() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                Uri uri = Uri.fromParts("package",getPackageName(),null);
                intent.setData(uri);
                startActivityForResult(intent,101);
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, 101);
            }
        } else {
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE
            },101);
        }

    }

//    public void myPDF(View view){
//
//        PdfDocument myPdfDocument = new PdfDocument();
//        PdfDocument.PageInfo myPageInfo = new PdfDocument.PageInfo.Builder(300,600,1).create();
//        PdfDocument.Page myPage = myPdfDocument.startPage(myPageInfo);
//
//        Paint myPaint = new Paint();
//        String myString = translatedTxt.getText().toString();
//        int x = 10, y=25;
//
//        for (String line:myString.split("\n")){
//            myPage.getCanvas().drawText(line, x, y, myPaint);
//            y+=myPaint.descent()-myPaint.ascent();
//        }
//
//        myPdfDocument.finishPage(myPage);
//
//        String myFilePath = Environment.getExternalStorageDirectory().getPath() + "/myPDFfile.pdf";
//        File myFile = new File(myFilePath);
//        try {
//            myPdfDocument.writeTo(new FileOutputStream(myFile));
//        }
//        catch (Exception e){
//            e.printStackTrace();
//            Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
//        }
//
//        myPdfDocument.close();
//    }

    public void downloadTxt(View view) {
//        Toast.makeText(this, "Save Button Clicked", Toast.LENGTH_SHORT).show();
        if(translatedTxt.getText() == null) {
            Toast.makeText(this, "Text Is Empty!", Toast.LENGTH_SHORT).show();
        } else {
            String content = translatedTxt.getText().toString();
            Log.e("path",Environment.getExternalStorageDirectory().toString());
            try {
                FileOutputStream writer = new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/Download/Translated.txt");
                writer.write(content.getBytes());
                writer.close();
                Toast.makeText(this, "Translated Text File Downloaded", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error Occured! Check Logs", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    public void uploadOnClick(View view) {

        performFileSearch();

    }

    private String readText(String input) {
        File file = new File(input);
        StringBuilder text = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while((line = br.readLine()) != null) {
                text.append(line);
                text.append("\n");
            }
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return text.toString();
    }

    private void performFileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }

    private boolean isEmulator() {
        return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("google_sdk")
                || Build.PRODUCT.contains("sdk")
                || Build.PRODUCT.contains("sdk_x86")
                || Build.PRODUCT.contains("sdk_gphone64_arm64")
                || Build.PRODUCT.contains("vbox86p")
                || Build.PRODUCT.contains("emulator")
                || Build.PRODUCT.contains("simulator");
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String path = uri.getPath();
                Log.e("path",path);
                path = path.substring(path.indexOf(":") + 1);
                Toast.makeText(this, "" + path, Toast.LENGTH_SHORT).show();
                if(isEmulator()) inputTxt.setText(readText(path));
                else inputTxt.setText(readText(Environment.getExternalStorageDirectory().getPath() + "/" + path));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(grantResults.length > 0) {
            if(requestCode == 101) {
                boolean readExt = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if(!readExt) {
                    takePermission();
                }
            }
        }

    }
}