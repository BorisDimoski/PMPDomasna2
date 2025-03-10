package com.example.practice;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private Button add;
    private ListView listView;
    private ArrayList<String> wordList;
    private ArrayList<String> filteredList;
    private CustomArrayAdapter adapter;
    private EditText searchEditText;
    private boolean isDeleteMode = false;

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

        wordList = new ArrayList<>();
        filteredList = new ArrayList<>();

        adapter = new CustomArrayAdapter(this, android.R.layout.simple_list_item_1, filteredList);
        listView = findViewById(R.id.listView);
        listView.setAdapter(adapter);

        loadWordsFromFile();

        Button clearFileButton = findViewById(R.id.clearFileButton);
        Button deleteItemButton = findViewById(R.id.deleteItemButton);
        clearFileButton.setOnClickListener(v -> clearFile());
        deleteItemButton.setOnClickListener(v -> {
            isDeleteMode = true;
            Toast.makeText(this, "Изберете збор за бришење.", Toast.LENGTH_SHORT).show();
        });

        add = findViewById(R.id.add);
        searchEditText = findViewById(R.id.searchEditText);
        add.setOnClickListener(v -> showPopup());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                filterWords(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        listView.setOnItemClickListener((parent, view, position, id) -> {
            if (isDeleteMode) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                deleteSelectedItem(selectedItem);
                isDeleteMode = false;
            }
        });
    }

    private void showPopup() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dodajpopup);
        dialog.show();
        Button ins = dialog.findViewById(R.id.ins);
        if (ins != null) {
            ins.setOnClickListener(v -> insertWord(dialog));
        } else {
            Log.e("MainActivity", "Button is null, check your layout.");
        }
    }

    private void insertWord(Dialog dialog) {
        EditText mkd = dialog.findViewById(R.id.mkd);
        EditText ang = dialog.findViewById(R.id.ang);

        String mkdzbor = mkd.getText().toString().trim();
        String angzbor = ang.getText().toString().trim();


        if (!isValidEnglishWord(angzbor)) {
            Toast.makeText(this, "Невалиден внес", Toast.LENGTH_SHORT).show();
            return; // Stop if validation fails
        }

        if (!isValidMacedonianWord(mkdzbor)) {
            Toast.makeText(this, "Невалиден внес", Toast.LENGTH_SHORT).show();
            return;
        }

        String comb = mkdzbor + "," + angzbor;

        try (PrintWriter writer = new PrintWriter(openFileOutput("en_mk_recnik.txt", MODE_APPEND))) {
            writer.println(comb);
            wordList.add(comb);
            filteredList.add(comb);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Внесено: " + mkdzbor + " " + angzbor, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File", "Error while writing to the file", e);
        }
    }

    private boolean isValidEnglishWord(String word) {
        return word.matches("[a-zA-Z]+");
    }

    private boolean isValidMacedonianWord(String word) {
        return word.matches("[а-яА-ЯёЁ]+");
    }

    private void loadWordsFromFile() {
        File file = new File(getFilesDir(), "en_mk_recnik.txt");
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput("en_mk_recnik.txt")))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    wordList.add(line);  // Store raw data
                }
                filteredList.addAll(wordList);  // Initial display list
                adapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Грешка при читање на датотеката.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void filterWords(String query) {
        filteredList.clear();

        if (query.isEmpty()) {
            filteredList.addAll(wordList);
        } else {
            for (String word : wordList) {
                if (word.toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(word);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void clearFile() {
        wordList.clear();
        filteredList.clear();
        adapter.notifyDataSetChanged();

        try (PrintWriter writer = new PrintWriter(openFileOutput("en_mk_recnik.txt", MODE_PRIVATE))) {
            Toast.makeText(this, "Речникот е исчистен.", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Грешка при чистење на речникот.", Toast.LENGTH_SHORT).show();
        }
    }
    private void deleteSelectedItem(String selectedItem) {
        filteredList.remove(selectedItem);
        wordList.remove(selectedItem);

        try (PrintWriter writer = new PrintWriter(openFileOutput("en_mk_recnik.txt", MODE_PRIVATE))) {
            for (String word : wordList) {
                writer.println(word);
            }
            Toast.makeText(this, "Избришано", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Грешка при бришење на зборот.", Toast.LENGTH_SHORT).show();
        }
        adapter.notifyDataSetChanged();
    }

    public class CustomArrayAdapter extends ArrayAdapter<String> {

        public CustomArrayAdapter(Context context, int resource, List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String word = getItem(position).replace(".", "");  // Remove period
            View view = super.getView(position, convertView, parent);
            TextView textView = (TextView) view.findViewById(android.R.id.text1);
            textView.setText(word);
            return view;
        }
    }
}
