package com.example.daniel.memorygame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    Database db;
    Button AddImage;
    private  ImageView imageView;
    Button AddCategory;
    EditText NameCategory;
    TextView CountCategory;
    Button ShowButton;
    Button DeleteBase;
    Button Play;
    private Uri imageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_FILE = 2;
    String pathToSave;
    private int view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        db = new Database(this);
        //db.DeleteBase();
        AddImage =  findViewById(R.id.AddImage);
        imageView = findViewById(R.id.imageView);
        AddCategory = findViewById(R.id.addCategory);
        NameCategory =  findViewById(R.id.nameCatagory);
        CountCategory = findViewById(R.id.countCategory);
        SQLiteCursor cursor = db.TakeData();
        CountCategory.setText(Integer.toString(cursor.getCount())+"/3 kategori");
        ShowButton = findViewById(R.id.showCategory);
        DeleteBase =  findViewById(R.id.clearBase);
        Play = findViewById(R.id.play);

        final String[] items = new String[] {"Aparat", "Galeria"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item,items );
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zdjecie");
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if(which == 0){
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File file = new File(Environment.getExternalStorageDirectory(),"temp_avatar"+String.valueOf(System.currentTimeMillis())+".jpg");
                    imageCaptureUri = Uri.fromFile(file);

                    try {
                        startActivityForResult(intent, PICK_FROM_CAMERA);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
                        intent.putExtra("return data", true);


                    }catch (Exception ex){
                        ex.printStackTrace();
                    }
                    dialogInterface.cancel();
                }
                else {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_FROM_FILE);
                    Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    getIntent.setType("image/*");
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri);
                }

            }
        });
        final AlertDialog dialog = builder.create();

        AddImage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                dialog.show();
            }
        });
        AddCategory.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                db.AddCategoryToBase(NameCategory.getText().toString(),pathToSave);
                SQLiteCursor cursor = db.TakeData();
                CountCategory.setText(Integer.toString(cursor.getCount())+"/3 kategori");
            }
        });
        ShowButton.setOnClickListener(new View.OnClickListener(){
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
            public void onClick(View v){
                SQLiteCursor cursor = db.TakeData();
                if(cursor.getCount()>0)
                {
                    StringBuffer buff = new StringBuffer();
                    while (cursor.moveToNext()){
                        buff.append("Nazwa: "+ cursor.getString(1)+"\n");
                        buff.append("sciezka: "+ cursor.getString(2)+"\n");
                    }
                    ShowMessage("rekord", buff.toString());
                }else{
                    ShowMessage("Brak","brak kategorii");
                }
            }
        });
        DeleteBase.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                db.DeleteBase();
                CountCategory.setText("0/3 kategori");
            }
        });
        Play.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SQLiteCursor cursor = db.TakeData();
                if(cursor.getCount()>=3){
                    Intent intent = new Intent(MainActivity.this, Game.class);
                    startActivity(intent);
                }else {
                    ShowMessage("Malo kategorii","Musisz mieć min 3 kategorie");
                }
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode != RESULT_OK)
            return;
        Bitmap bitmap = null;
        String path = "";
        if(requestCode == PICK_FROM_FILE) {
            imageCaptureUri = data.getData();
            path = getRealPathFromUri(imageCaptureUri);
            if(path==null){

                path = imageCaptureUri.getPath();
            }
            if(path!=null){
                bitmap = BitmapFactory.decodeFile(path);
            }
        }
        else {
            path = imageCaptureUri.getPath();
            bitmap = BitmapFactory.decodeFile(path);
        }
        pathToSave = path;
        imageView.setImageBitmap(bitmap);
        //imageView.setImageURI(imageCaptureUri);
    }

    public String getRealPathFromUri(Uri contentURI) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor =  getContentResolver().query(contentURI, proj, null, null, null);;
        if(cursor ==null)return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


    public void ShowMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.show();
    }
}
