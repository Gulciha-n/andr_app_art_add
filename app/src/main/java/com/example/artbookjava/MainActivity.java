package com.example.artbookjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.widget.Toolbar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.artbookjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        setSupportActionBar(binding.toolbar.getRoot());

        getData();

        artArrayList = new ArrayList<Art>();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //recyclerView => LinearLayout
        artAdapter = new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);
    }

    private  void getData(){
        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);

            Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM arts ", null);

            int nameIx = cursor.getColumnIndex("artname");
            int idIx = cursor.getColumnIndex("id");

            while (cursor.moveToNext()){
                String name = cursor.getString(nameIx);
                int id = cursor.getInt(idIx);
                Art art = new Art(name,id); //aldıgımız degerleri bir array icerisine alalım ki recycleView da kullanalım
                artArrayList.add(art);
            }

            artAdapter.notifyDataSetChanged();
            cursor.close();
        }
        catch (Exception exception){
            exception.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_art){
            Intent intent = new Intent(this,ArtActivity.class);
            startActivity(intent);
        }
        return true;
    }
}


