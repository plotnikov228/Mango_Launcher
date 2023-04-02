package com.mango.mango_tv.Services.Downloader;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mango.mango_tv.BuildConfig;
import com.mango.mango_tv.R;

import java.io.File;

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
        StorageReference ref = FirebaseStorage.getInstance().getReferenceFromUrl("gs://mangotv-app-1ff36.appspot.com/APKs/Mango TV.apk");
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/";
        destination += fileName;
        final Uri uri = Uri.parse("file://" + destination);

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            //file.delete() - test this, I think sometimes it doesnt work
            file.delete();
        ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                installAPK(file);
            }
        });
    }

    private void installAPK(File file) {

        if (file.exists()) {
            try {
            Intent intent;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri apkUri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file);
                intent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                intent.setData(apkUri);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                Uri apkUri = Uri.fromFile(file);
                intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            activity.startActivity(intent);


            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(context, "installing", Toast.LENGTH_LONG).show();
        }
    }
}
