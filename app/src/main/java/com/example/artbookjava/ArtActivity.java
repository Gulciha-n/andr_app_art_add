package com.example.artbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.Manifest.permission;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.example.artbookjava.databinding.ActivityArtBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImageGallery;
    SQLiteDatabase database;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        registerLauncher();

        database = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);



        Intent intent = getIntent();
        String info = intent.getStringExtra("info");

        if (info.matches("new")) {
            binding.nameText.setText("");
            binding.artistText.setText("");
            binding.yearText.setText("");
            binding.save.setVisibility(View.VISIBLE);

            Bitmap selectImage = BitmapFactory.decodeResource(getApplicationContext().getResources(),R.drawable.selectimage);
            binding.selectImage.setImageBitmap(selectImage);


        } else {
            int artId = intent.getIntExtra("artId",1);
            binding.save.setVisibility(View.INVISIBLE);

            try {

                Cursor cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?",new String[] {String.valueOf(artId)});

                int artNameIx = cursor.getColumnIndex("artname");
                int painterNameIx = cursor.getColumnIndex("paintername");
                int yearIx = cursor.getColumnIndex("year");
                int imageIx = cursor.getColumnIndex("image");

                while (cursor.moveToNext()) {

                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes = cursor.getBlob(imageIx);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.selectImage.setImageBitmap(bitmap);


                }

                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
    public void selectImage(View view){

        if(Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU ){
            //Android 33+ ise READ_EXTERNAL_STORAGE ise yaramaz READ_MEDIA_IMAGES kullanacagız.
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != getPackageManager().PERMISSION_GRANTED){

                //kullanıcıya açıklama göstermek zorunda mıyız onu kontrol ediyoruz.Android kendi kontrol ediyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_MEDIA_IMAGES)){

                    //snackbar olusturduk ve buton ekledik
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                        }
                    }).show();
                }
                else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);

                }
            }
            else{
                //permission granted
                //izin varsa galerye gidip görsel alıp gelecegız (PICK edecek)
                //Galerye gittikten sonra ne yapılacagını kontrol etmek icin ActivityResultLauncher kullanıyoruz
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);
            }
        }
        else{
            //Android 32- READ_EXTERNAL_STORAGE kullanıyoruz
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != getPackageManager().PERMISSION_GRANTED){

                //kullanıcıya açıklama göstermek zorunda mıyız onu kontrol ediyoruz.Android kendi kontrol ediyor
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, permission.READ_EXTERNAL_STORAGE)){

                    //snackbar olusturduk ve buton ekledik
                    Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //request permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                        }
                    }).show();
                }
                else{
                    //request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                }
            }
            else{
                //permission granted
                //izin varsa galerye gidip görsel alıp gelecegız (PICK edecek)
                //Galerye gittikten sonra ne yapılacagını kontrol etmek icin ActivityResultLauncher kullanıyoruz
                Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                activityResultLauncher.launch(intentToGallery);

            }
        }
    }

    //registerLauncher metodunda activityResultLauncher  ve permissionLauncher'ı registerForActivityResult kullanarak register islemini yapalım
    private void registerLauncher(){

        //go to the gallery
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {

                if (result.getResultCode() == Activity.RESULT_OK){
                    Intent intentFromResult =  result.getData();
                    if(intentFromResult != null){
                        Uri imageData =  intentFromResult.getData(); //uri of data
                        //binding.selectImage.setImageURI(imageData); //we need the data , not where it's stored. we will add to the database

                        //convert data(uri) to bitmap
                        //galeryden alacagımız gorseli bitmap'e ceviriyoruz
                        try {
                            if(Build.VERSION.SDK_INT >=28){
                                ImageDecoder.Source source = ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                                selectedImageGallery = ImageDecoder.decodeBitmap(source);
                                binding.selectImage.setImageBitmap(selectedImageGallery);
                            }
                            else {
                                selectedImageGallery = MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                
                            }

                        }
                        catch (Exception exception){
                            exception.printStackTrace(); //show all error message in logcat

                        }
                    }
                }
            }
        });

        //ask permission
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean result) {

                if(result){
                    //permission granted , go to gallery
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);

                }
                else{
                    //permission denied
                    Toast.makeText(ArtActivity.this, "Permission needed", Toast.LENGTH_LONG).show();
                }

            }
        });
    }
    public void save(View view){

        String name = binding.nameText.getText().toString();
        String artistName = binding.artistText.getText().toString();
        String year = binding.yearText.getText().toString();

        Bitmap smallImage = makeSmallerImage(selectedImageGallery,300);

        //gorseli veriye cevirelim - 0 ve 1 lere
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(CompressFormat.PNG,50,outputStream);
        byte[] byteArray =  outputStream.toByteArray();

        //SQLite initialize try-catch

        try {
            database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY , artname VARCHAR , artistName VARCHAR, year VARCHAR,image BLOB)");

            String sqlString = "INSERT INTO arts (artname , artistName , year , image) VALUES (? , ? , ? , ?)";
            //alacagımız VALUE'ları binding etmemizi saglayan SQLiteStatement kullanıyoruz bu sekilde yaparak farklı degerleri baglayabiliyoruz
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);

            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);  //image'ı byte a cevirmistik
            sqLiteStatement.execute();

        }
        catch (Exception e){
            e.printStackTrace();
        }
        //sqlite'a kayıt olduktan sonra mainActivity'e geri donelim
        Intent intent = new Intent(ArtActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);  //diger sayfaya gecmeden once birikmis aktiviteleri kapatalım
        startActivity(intent);

    }
    //selectedImage'ı kucultelim bunun için bitmap türünde bir metod yazalım
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize) {

        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if (bitmapRatio > 1) {
            width = maximumSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maximumSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(image,width,height,true);
    }
}



