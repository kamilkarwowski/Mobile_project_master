package com.example.farm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.farm.database.entity.Animal;
import com.example.farm.database.view.AnimalViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final int NEW_BOOK_ACTIVITY_REQUEST_CODE = 1;
    public static final int DETAILS_OF_BOOK_ACTIVITY_REQUEST_CODE = 2;


    private AnimalViewModel animalViewModel;
    private Animal editedAnimal;
    private LiveData<List<Animal>> animals;
    ArrayAdapter adapterArray;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final AnimalAdapter adapter = new AnimalAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        animalViewModel = ViewModelProviders.of(this).get(AnimalViewModel.class);
        animalViewModel.findAll().observe(this, adapter::setAnimals);

        FloatingActionButton addAnimalButton = findViewById(R.id.add_button);
        addAnimalButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, EditAnimalActivity.class);
            startActivityForResult(intent, NEW_BOOK_ACTIVITY_REQUEST_CODE);
        });

        animals = animalViewModel.findAll();
        ArrayList<String> filmList = new ArrayList<>();
        adapterArray = new ArrayAdapter(this, android.R.layout.activity_list_item, filmList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_item_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                searchDatabase(s);
                return true;
            }
        });


        return true;
    }

    private void searchDatabase(String query){
        String search = "%"+query+"%";
        ArrayList<Animal> animalList = new ArrayList<>();
        for(Animal animal : animals.getValue()){
            if (animal.getName().toLowerCase().contains(query.toLowerCase()) || animal.getIndex_id().toLowerCase().contains(query.toLowerCase())){
                animalList.add(animal);
            }
        }
        //RecyclerView recyclerView = findViewById(R.id.recyclerview);

        RecyclerView view = findViewById(R.id.recyclerview);
        AnimalAdapter animalAdapter = new AnimalAdapter();
        animalAdapter.setAnimals(animalList);
        view.setAdapter(animalAdapter);
        view.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_item_search) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NEW_BOOK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            Animal animal = new Animal(data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_NAME),
                    data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_GENDER),
                    data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_INDEXID));
            animalViewModel.insert(animal);
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.animal_added),
                    Snackbar.LENGTH_LONG).show();
        } else if (requestCode == DETAILS_OF_BOOK_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            editedAnimal.setName(data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_NAME));
            editedAnimal.setGender(data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_GENDER));
            editedAnimal.setIndex_id(data.getStringExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_INDEXID));
            animalViewModel.update(editedAnimal);
            editedAnimal = null;
            Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.animal_edited),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(findViewById(R.id.coordinator_layout),
                            getString(R.string.empty_not_saved),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private class AnimalHolder extends RecyclerView.ViewHolder {
        private final TextView animalNameTextView;
        private final TextView animalGenderTextView;
        private final TextView animalIndexidTextView;
        private Animal animal;
        //private BreakIterator AnimalTextView;

        public AnimalHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.animal_item_list, parent, false));

            animalNameTextView = itemView.findViewById(R.id.animal_name);
            animalGenderTextView =itemView.findViewById(R.id.animal_gender);
            animalIndexidTextView = itemView.findViewById(R.id.animal_index);
            View animalItem = itemView.findViewById(R.id.book_item);
            //Button edit = findViewById(R.id.btnEdit);
            animalItem.setOnLongClickListener(v -> {
                animalViewModel.delete(animal);
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                getString(R.string.animal_deleted),
                                Snackbar.LENGTH_LONG)
                        .show();
                return true;
            });
            animalItem.setOnClickListener(v -> {
                editedAnimal = animal;
                Intent intent = new Intent(MainActivity.this, DetailAnimalActivity.class);
                intent.putExtra(DetailAnimalActivity.EXTRA_EDIT_ANIMAL_NAME,  animalNameTextView.getText());
                intent.putExtra(DetailAnimalActivity.EXTRA_EDIT_ANIMAL_GENDER, animalGenderTextView.getText());
                intent.putExtra(DetailAnimalActivity.EXTRA_EDIT_ANIMAL_INDEX_ID, animalIndexidTextView.getText());
                startActivity(intent);
            });

            animalItem.findViewById(R.id.btnEdit).setOnClickListener(v -> {
                editedAnimal = animal;
                Intent intent = new Intent(MainActivity.this, EditAnimalActivity.class);
                intent.putExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_NAME, animalNameTextView.getText());
                intent.putExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_GENDER, animalGenderTextView.getText());
                intent.putExtra(EditAnimalActivity.EXTRA_EDIT_ANIMAL_INDEXID, animalIndexidTextView.getText());
                startActivityForResult(intent, DETAILS_OF_BOOK_ACTIVITY_REQUEST_CODE);
            });

        }

        public void bind(Animal animal) {
            animalNameTextView.setText(animal.getName());
            animalIndexidTextView.setText(animal.getIndex_id());
            animalGenderTextView.setText(animal.getGender());
            this.animal = animal;
        }
    }

    private class AnimalAdapter extends RecyclerView.Adapter<AnimalHolder> {
        
        private List<Animal> animals;

        @NonNull
        @Override
        public AnimalHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AnimalHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(AnimalHolder holder, int position) {
            if (animals != null) {
                Animal animal = animals.get(position);
                holder.bind(animal);
            } else {
                Log.d("MainActivity", "No animals");
            }
        }

        public int getItemCount() {
            if (animals != null) {
                return animals.size();
            } else {
                return 0;
            }
        }

        void setAnimals(List<Animal> animals) {
            this.animals = animals;
            notifyDataSetChanged();
        }
    }
}