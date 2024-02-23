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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.mango.mango_tv.BuildConfig;
import com.mango.mango_tv.R;

import java.io.File;
import java.util.concurrent.Callable;

public class Downloader {
    private Context context;

    private Activity activity;



    public void createMessage(Intent intent, Callable<Void> onCatch, String APP_VERSION, Context cont, Activity activity1) {
        context = cont;
        activity = activity1;
        final AlertDialog.Builder builder = new AlertDialog.Builder(cont);
        builder.setTitle(R.string.a_new_update_is_available).setMessage(cont.getString(R.string.update_your_app) + ": " + APP_VERSION).setPositiveButton(R.string.dialog_button_install,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                        downloadFile("Mango TV.apk", context, activity);
                        try {
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (ActivityNotFoundException e) {
                            System.out.println(e);
                            // IPTV core app is not installed, let's ask the user to install it.
                            try {
                                onCatch.call();
                            } catch (Exception exception) {
                                Toast.makeText(activity, exception.toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).setNegativeButton(cont.getString(R.string.dialog_button_cancel),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int id) {
                        dialogInterface.dismiss();
                        try {
                            activity.startActivity(intent);
                            activity.finish();
                        } catch (ActivityNotFoundException e) {
                            // IPTV core app is not installed, let's ask the user to install it.
                            try {
                                System.out.println(e);
                                onCatch.call();
                            } catch (Exception exception) {
                                Toast.makeText(activity, exception.toString(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }).setCancelable(false).show();

    }

    public String getFilePath(String name, String folder) {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+name;

    }

    public void downloadFile(String fileName, Context cont, Activity activity1) {
        context = cont;
        activity = activity1;
        Toast.makeText(activity,"downloading a file - " + fileName,
                Toast.LENGTH_LONG).show();
                StorageReference reference = FirebaseStorage.getInstance().getReferenceFromUrl("gs://mangotv-app-1ff36.appspot.com/");
                StorageReference ref = reference.child("APKs/").child(fileName);
        String destination = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + fileName;

        //Delete update file if exists
        File file = new File(destination);
        if (file.exists())
            file.delete();
        ref.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                installAPK(file);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(activity, e.toString(),
                        Toast.LENGTH_LONG).show();
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
                Toast.makeText(context, context.getString(R.string.installing), Toast.LENGTH_LONG).show();
                activity.startActivityForResult(intent, 1);


            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();

            }

        } else {
            Toast.makeText(context, "installing", Toast.LENGTH_LONG).show();
        }
    }
}
