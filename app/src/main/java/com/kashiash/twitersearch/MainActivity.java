package com.kashiash.twitersearch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    // nazwa pliku XML SharedPreferences, w którym zapisywane są zapytania kierowane do wyszukiwarki
    private static final String SEARCHES = "searches";

    private EditText queryEditText; // miejsce wprowadzenia zapytania
    private EditText tagEditText; // miejsce wprowadzenia znacznika zapytania
    private FloatingActionButton saveFloatingActionButton; // miejsce wprowadzenia znacznika zapytania
    private SharedPreferences savedSearches; // ulubione zapytania użytkownika
    private List<String> tags; // lista etykiet zapisanych zapytań
    private SearchesAdapter adapter; // umożliwia wiązanie danych z obiektem RecyclerView

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                handleUncaughtException(thread, e);
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // uzyskaj odwołania do pól EditText i przypisz im obiekty TextWatcher
        queryEditText = ((TextInputLayout) findViewById(
                R.id.queryTextInputLayout)).getEditText();
        queryEditText.addTextChangedListener(textWatcher);
        tagEditText = ((TextInputLayout) findViewById(
                R.id.tagTextInputLayout)).getEditText();
        tagEditText.addTextChangedListener(textWatcher);

        // uzyskaj dostęp do pliku SharedPreferences zawierającego zapytania zapisane przez użytkownika
        savedSearches = getSharedPreferences(SEARCHES, MODE_PRIVATE);

        // umieść zapisane etykiety w tablicy ArrayList, a następnie je posortuj
        tags = new ArrayList<>(savedSearches.getAll().keySet());
        Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);

        // uzyskaj odwołanie do obiektu RecyclerView w celu skonfigurowania go
        RecyclerView recyclerView =
                (RecyclerView) findViewById(R.id.recyclerView);

        // skorzystaj z menedżera LinearLayoutManager w celu wyświetlenia elementów tworzących pionową listę
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // utwórz obiekt RecyclerView.Adapter w celu powiązania etykiet z obiektem RecyclerView
        adapter = new SearchesAdapter(
                tags, itemClickListener, itemLongClickListener);
        recyclerView.setAdapter(adapter);

        // określ zmodyfikowany obiekt ItemDecorator w celu wyświetlenia linii pomiędzy elementami listy
        recyclerView.addItemDecoration(new ItemDivider(this));

        // zarejestruj obiekt nasłuchujący zapisania nowego zapytania lub edycji zapisanego wcześniej zapytania
        saveFloatingActionButton =
                (FloatingActionButton) findViewById(R.id.fab);
        saveFloatingActionButton.setOnClickListener(saveButtonListener);
        updateSaveFAB(); // ukrywa przycisk, ponieważ pola EditText są na początku puste


    }

    // ukrywa i pokazuje przycisk saveFloatingActionButton zależnie od zawartości pól EditTexts
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) { }

        // ukrywa i pokazuje przycisk saveFloatingActionButton po zmianie wprowadzonych danych
        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            updateSaveFAB();
        }

        @Override
        public void afterTextChanged(Editable s) { }
    };

    // ukrywa lub pokazuje przycisk saveFloatingActionButton
    private void updateSaveFAB() {
        // sprawdź, czy dane wprowadzono w pola EditText
        if (queryEditText.getText().toString().isEmpty() ||
                tagEditText.getText().toString().isEmpty())
            saveFloatingActionButton.hide();
        else
            saveFloatingActionButton.show();
    }

    // saveButtonListener zapisuje parę etykieta-zapytanie do pliku SharedPreferences
    private final View.OnClickListener saveButtonListener =
            new View.OnClickListener() {
                // dodaj-zaktualizuj zapytanie, jeżeli żadne z pól EditText nie jest puste
                @Override
                public void onClick(View view) {
                    String query = queryEditText.getText().toString();
                    String tag = tagEditText.getText().toString();

                    if (!query.isEmpty() && !tag.isEmpty()) {
                        // ukryj klawiaturę ekranową
                        ((InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                                view.getWindowToken(), 0);

                        addTaggedSearch(tag, query); // dodaj i (lub) zaktualizuj zapytanie
                        queryEditText.setText(""); // wyczyść pole queryEditText
                        tagEditText.setText(""); // wyczyść pole tagEditText
                        queryEditText.requestFocus(); // ustaw fokus klawiszy na queryEditText
                    }
                }
            };
    // dodaj nowe wyszukanie do pliku, a następnie odśwież wszystkie przyciski
    private void addTaggedSearch(String tag, String query) {
        // uzyskaj dostęp do SharedPreferences.Editor w celu zachowania nowej pary etykieta-zapytanie
        SharedPreferences.Editor preferencesEditor = savedSearches.edit();
        preferencesEditor.putString(tag, query); // zapisz bieżące wyszukanie
        preferencesEditor.apply(); // zapisz zaktualizowane preferencje

        // jeżeli etykieta jest nowa, to dodaj ją do listy etykiet, posortuj ją, a następnie ponownie wyświetl
        if (!tags.contains(tag)) {
            tags.add(tag); // dodaj nową etykietę
            Collections.sort(tags, String.CASE_INSENSITIVE_ORDER);
            adapter.notifyDataSetChanged(); // dodaj nową etykietę
        }
    }

    // itemClickListener uruchamia przeglądarkę internetową wyświetlającą wyniki wyszukiwania
    private final View.OnClickListener itemClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // odczytaj łańcuch zapytania i utwórz adres URL wyszukiwarki
                    String tag = ((TextView) view).getText().toString();
                    String urlString = getString(R.string.search_URL) +
                            Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

                    // utwórz intencję Intent uruchamiającą przeglądarkę internetową
                    Intent webIntent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(urlString));

                    startActivity(webIntent); // pokaż wyniki wyszukiwania w przeglądarce internetowej
                }
            };

    // itemLongClickListener wyświetla okno umożliwiające
    // udostępnienie, skasowanie i edycję zapisanego zapytania
    private final View.OnLongClickListener itemLongClickListener =
            new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // odczytaj etykietę dotkniętą przez użytkownika
                    final String tag = ((TextView) view).getText().toString();

                    // utwórz nowe okno AlertDialog
                    AlertDialog.Builder builder =
                            new AlertDialog.Builder(MainActivity.this);

                    // określ nazwę okna AlertDialog
                    builder.setTitle(
                            getString(R.string.share_edit_delete_title, tag));

                    // określ listę elementów do wyświetlenia i utwórz procedurę obsługi zdarzenia
                    builder.setItems(R.array.dialog_items,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case 0: // udostępnij
                                            shareSearch(tag);
                                            break;
                                        case 1: // edytuj
                                            // wypełnij pola EditText wyszukania i etykiety odpowiednią zawartością
                                            tagEditText.setText(tag);
                                            queryEditText.setText(
                                                    savedSearches.getString(tag, ""));
                                            break;
                                        case 2: // kasuj
                                            deleteSearch(tag);
                                            break;
                                    }
                                }
                            }
                    );

                    // określ przycisk negacji okna AlertDialog
                    builder.setNegativeButton(getString(R.string.cancel), null);

                    builder.create().show(); // wyświetl okno AlertDialog
                    return true;
                }
            };

    // pozwól użytkownikowi wybrać aplikację do udostępnienia adresu URL zapisanego zapytania
    private void shareSearch(String tag) {
        // utwórz adres URL zapytania
        String urlString = getString(R.string.search_URL) +
                Uri.encode(savedSearches.getString(tag, ""), "UTF-8");

        // utwórz obiekt Intent pozwalający na udostępnienie obiektu urlString
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_SUBJECT,
                getString(R.string.share_subject));
        shareIntent.putExtra(Intent.EXTRA_TEXT,
                getString(R.string.share_message, urlString));
        shareIntent.setType("text/plain");

        // wyświetla aplikacje mogące udostępniać zwykły tekst
        startActivity(Intent.createChooser(shareIntent,
                getString(R.string.share_search)));
    }


    // kasuje wyszukanie po dotknięciu przez użytkownika przycisku potwierdzenia
    private void deleteSearch(final String tag) {
        // utwórz nowe okno AlertDialog i określ wyświetlaną w nim treść
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
        confirmBuilder.setMessage(getString(R.string.confirm_message, tag));

        // skonfiguruj przycisk negacji (ANULUJ)
        confirmBuilder.setNegativeButton(getString(R.string.cancel), null);

        // skonfiguruj przycisk potwierdzenia (KASUJ)
        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        tags.remove(tag); // usuń obiekt tag z listy tags

                        // skorzystaj z edytora SharedPreferences.Editor w celu usunięcia zapisanego zapytania
                        SharedPreferences.Editor preferencesEditor =
                                savedSearches.edit();
                        preferencesEditor.remove(tag); // usuń zapytanie
                        preferencesEditor.apply(); // zapisz zmiany

                        // ponownie powiąż listę tags z elementami widoku RecyclerView w celu wyświetlenia zaktualizowanej listy
                        adapter.notifyDataSetChanged();
                    }
                }
        );

        confirmBuilder.create().show(); // wyświetl okno AlertDialog
    }

    public void handleUncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace(); // not all Android versions will print the stack trace automatically
    }
}