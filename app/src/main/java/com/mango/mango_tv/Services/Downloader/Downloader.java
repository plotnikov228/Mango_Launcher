package com.mango.mango_tv.Services.Downloader;

import static androidx.core.content.FileProvider.getUriForFile;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mango.mango_tv.BuildConfig;
import com.mango.mango_tv.R;

import java.io.File;
import java.util.Objects;

public class Downloader {
    private final String APP_VERSION;
    private final Context context;
    private final Activity activity;
    public Downloader(String APP_VERSION, Context context, Activity activity) {
        this.APP_VERSION = APP_VERSION;
        this.context = context;
        this.activity = activity;
    }


    public void createMessage(Intent intent) {



        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.a_new_update_is_available).setMessage(context.getString(R.string.update_your_app) + ": " + APP_VERSION).setPositiveButton(R.string.dialog_button_install,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                        downloadFile();
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }).setNegativeButton(R.string.dialog_button_cancel,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                        activity.startActivity(intent);
                        activity.finish();
                    }
                }).setCancelable(false).show();

    }

    private void downloadFile() {
        String fileName = "Mango TV.apk";
        StorageReference storage = FirebaseStorage.getInstance().getReference();
        StorageReference ref = storage.child("APKs").child(fileName);

        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File rootPath = new File(downloads + File.separator + "APKs");

        if (!rootPath.exists()) {
            rootPath.mkdirs();
        }

        final File localFile = new File(rootPath, fileName);

        try {
            ref.getFile(localFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task) {
                    installAPK(localFile);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e);
                }
            });


        } catch (Exception e) {
            System.out.println(e);
        }


    }

    private void installAPK(File file) {

        if (file.exists()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(context.getPackageManager().canRequestPackageInstalls()){
                    activity.startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES));
                }
            }

            Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
            intent.setDataAndType(uriFromFile(file), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                activity.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }

        } else {
            Toast.makeText(context, "installing", Toast.LENGTH_LONG).show();
        }
    }

    private Uri uriFromFile(File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getUriForFile(Objects.requireNonNull(context),
                    BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }


}
