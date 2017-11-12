package com.notcmput301.habitbook;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

public class HabitTypeListActivity extends AppCompatActivity {
    private User loggedInUser;
    private ArrayList<HabitType> habitTypes;
    private ListView habitTypeList;
    private Button addNewHabit;
    ArrayAdapter<HabitType> Adapter;
    private String target;
    private Gson gson;
    private User user;
    //private LocalFileControler localFileControler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_type_list);

        Bundle bundle = getIntent().getExtras();
        this.target = bundle.getString("user");
        this.gson = new Gson();
        this.user = gson.fromJson(target, User.class);
        this.habitTypes = user.getHabitTypes();
        //this.target = bundle.getString("localfilecontroller");
        //this.localFileControler = gson.fromJson(target, LocalFileControler.class);

        this.Adapter = new ArrayAdapter<HabitType>(HabitTypeListActivity.this,
                android.R.layout.simple_list_item_1, this.habitTypes);
        this.habitTypeList = (ListView) findViewById(R.id.HabitList);
        this.habitTypeList.setAdapter(Adapter);
        this.habitTypeList.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l){
                Intent intent = new Intent(HabitTypeListActivity.this, HabitTypeDetailsActivity.class);
                intent.putExtra("index", i);
                target = gson.toJson(user);
                intent.putExtra("user", target);
                //target = gson.toJson(localFileControler);
                //intent.putExtra("localfilecontroller", target);
                startActivityForResult(intent, 1);
                //startActivity(intent);

            }
        });



        Button NewHabit = (Button) findViewById(R.id.NewHabit);
        NewHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HabitTypeListActivity.this, CreateHabitActivity.class);
                target = gson.toJson(user);
                intent.putExtra("user", target);
                //target = gson.toJson(localFileControler);
                //intent.putExtra("localfilecontroller", target);
                //startActivityForResult(intent,2);
                startActivityForResult(intent, 2);
            }
        }   );
    }

    public void refresh(View v){
        ElasticSearch.verifyLogin vl = new ElasticSearch.verifyLogin();
        vl.execute(this.user.getUsername(), this.user.getPassword());
        try{
            User u = vl.get();
            if (u==null){
                Toast.makeText(this, "Check password or internet connection", Toast.LENGTH_LONG).show();
            }else{
                this.user = u;
                Toast.makeText(this, u.getHabitTypes().get(0).getTitle(), Toast.LENGTH_LONG).show();

            }
        }catch(Exception e){
            Log.e("get failure", e.toString());
            e.printStackTrace();
        }


        this.habitTypes = this.user.getHabitTypes();


        this.Adapter = new ArrayAdapter<HabitType>(HabitTypeListActivity.this,
                android.R.layout.simple_list_item_1, this.habitTypes);
        this.habitTypeList = (ListView) findViewById(R.id.HabitList);
        this.habitTypeList.setAdapter(Adapter);
        this.Adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){
        // back from detail page
        if (requestCode == 1){
            // if user makes changes
            //if (resultCode == Activity.RESULT_OK){
                // reload user from saved file
                //this.user = this.localFileControler.Login(this.user.getUsername(), this.user.getPassword());

            ElasticSearch.verifyLogin vl = new ElasticSearch.verifyLogin();
            vl.execute(this.user.getUsername(), this.user.getPassword());
            try{
                User u = vl.get();
                if (u==null){
                    Toast.makeText(this, "Check password or internet connection", Toast.LENGTH_LONG).show();
                }else{
                    this.user = u;
                    Toast.makeText(this, u.getHabitTypes().get(0).getTitle(), Toast.LENGTH_LONG).show();

                }
            }catch(Exception e){
                Log.e("get failure", e.toString());
                e.printStackTrace();
            }


            this.habitTypes = this.user.getHabitTypes();
            this.Adapter.notifyDataSetChanged();
            //}
        }
        // back from creating habit type page
        else if (requestCode == 2){
            // if user makes changes
            if (resultCode == Activity.RESULT_OK){
                // reload user from saved file
                //this.user = this.localFileControler.Login(this.user.getUsername(), this.user.getPassword());
                Bundle bundle = intent.getExtras();
                this.target = bundle.getString("HabitType");
                HabitType ht = gson.fromJson(target, HabitType.class);
                this.user.addHabitType(ht);
                this.Adapter.notifyDataSetChanged();
                ElasticSearch.UpdateUserTask updateUserTask = new ElasticSearch.UpdateUserTask();
                updateUserTask.execute(this.user);
            }
        }
    }
}
