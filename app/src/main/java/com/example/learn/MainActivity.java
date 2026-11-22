package com.example.learn;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;


public class MainActivity extends AppCompatActivity {

    private ArrayList<Item> items;
    private RecyclerView recyclerView;
    private MyAdapter adapter;
    private DatabaseReference userItemsRef;
    private ValueEventListener itemsListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        FirebaseUser currentUser = FirebaseRef.refAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
            return;
        }
        String userId = currentUser.getUid();
        userItemsRef = FirebaseRef.refUserItems.child(userId);

        items = new ArrayList<Item>();

        recyclerView = findViewById(R.id.recycler);
        adapter = new MyAdapter(items);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.RIGHT |ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();

                Collections.swap(items, fromPos, toPos);
                adapter.notifyItemMoved(fromPos, toPos);

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                if(direction == ItemTouchHelper.RIGHT){
                    Item item = items.get(position);
                    userItemsRef.child(item.getKeyID()).removeValue();
                    items.remove(position);
                    adapter.notifyDataSetChanged();
                }
                else {
                    adapter.notifyItemChanged(position);
                }
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
                EditText etName = dialogView.findViewById(R.id.etName);
                EditText etDesc = dialogView.findViewById(R.id.etDesc);

                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Add item");
                builder.setView(dialogView);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Item item = new Item(etName.getText().toString(), etDesc.getText().toString(), R.drawable.ic_launcher_background);
                        String id = userItemsRef.push().getKey();
                        item.setKeyID(id);
                        userItemsRef.child(id).setValue(item)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(MainActivity.this, "Item saved", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(MainActivity.this, "Error saving item", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
            }
        });


        itemsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                items.clear();
                for (DataSnapshot data : dS.getChildren()) {
                    Item item = data.getValue(Item.class);
                    items.add(item);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Error reading items", Toast.LENGTH_SHORT).show();
            }
        };

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (userItemsRef != null) {
            userItemsRef.addValueEventListener(itemsListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (itemsListener != null) {
            userItemsRef.removeEventListener(itemsListener);
        }
    }
}