package com.akmo.collecterapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class MainActivity extends AppCompatActivity {

    TextView txtView;
    ImageView picView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtView = (TextView) findViewById(R.id.txt);
        picView = (ImageView) findViewById(R.id.picture);

        Intent receivedIntent = getIntent();
        String receivedAction = receivedIntent.getAction();
        String receivedType = receivedIntent.getType();

        if (receivedAction.equals("com.akmo.collector")) {
            if (receivedType.startsWith("text/")) {

                String receivedText = receivedIntent.getStringExtra(Intent.EXTRA_TEXT);

                if (receivedText != null) {

                    txtView.setText(receivedText);
                    String filename = "LeakFile.txt";
                    String data = receivedText;

                    FileOutputStream fos;
                    try {
                        File myFile = new File(Environment.getExternalStorageDirectory().getPath() + filename);
                        myFile.createNewFile();
                        FileOutputStream fOut = new

                        FileOutputStream(myFile);
                        OutputStreamWriter myOutWriter = new

                                OutputStreamWriter(fOut);
                        myOutWriter.append(data);
                        myOutWriter.close();
                        fOut.close();

                        Toast.makeText(getApplicationContext(), filename + "saved", Toast.LENGTH_LONG).show();


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } else if (receivedType.startsWith("image/")) {
                String name;
                txtView.setVisibility(View.GONE);

                Uri uri = (Uri) receivedIntent.getParcelableExtra(Intent.EXTRA_STREAM);
                if (uri != null)
                    picView.setImageURI(uri);
            } else if (receivedType.startsWith("file/")) {
                Uri uri = (Uri) receivedIntent.getExtras().get("file");
                File f = new File(uri.getPath());
                String s = f.getName();
                txtView.setText("File Name: " + s);
            }
        }
    }
}