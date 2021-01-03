package com.kashiash.twitersearch;

// Podklasa klasy RecyclerView.Adapter przeznaczona do wiązania danych z elementami RecyclerView



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchesAdapter
        extends RecyclerView.Adapter<SearchesAdapter.ViewHolder> {

    // obiekty  nasłuchujące klasy MainActivity zarejestrowane dla każdego elementu listy
    private final View.OnClickListener clickListener;
    private final View.OnLongClickListener longClickListener;

    // obiekt List<String> używany jest do odczytywania danych elementów widoku RecyclerView
    private final List<String> tags; // etykiety zapytań

    // konstruktor
    public SearchesAdapter(List<String> tags,
                           View.OnClickListener clickListener,
                           View.OnLongClickListener longClickListener) {
        this.tags = tags;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    // zagnieżdżona podklasa klasy RecyclerView.ViewHolder używana do implementacji
    // wzorca ViewHolder w kontekście widoku RecyclerView — nie musisz samodzielnie
    // tworzyć logiki ponownego korzystania z widoków usuniętych z ekranu w wyniku przewijania listy
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textView;

        // konfiguruje obiekt ViewHolder elementu widoku RecyclerView
        public ViewHolder(View itemView,
                          View.OnClickListener clickListener,
                          View.OnLongClickListener longClickListener) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.textView);

            // dołącz obiekty nasłuchujące zdarzeń do itemView
            itemView.setOnClickListener(clickListener);
            itemView.setOnLongClickListener(longClickListener);
        }
    }

    // konfiguruje nowy element listy i odpowiadający mu obiekt ViewHolder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // przygotuj do wyświetlenia rozkład elementu list_item
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.list_item, parent, false);

        // utwórz obiekt ViewHolder dla bieżącego elementu
        return (new ViewHolder(view, clickListener, longClickListener));
    }

    // określ tekst elementu listy w celu wyświetlenia etykiety zapytania
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(tags.get(position));
    }

    // zwraca liczbę elementów powiązanych za pomocą adaptera
    @Override
    public int getItemCount() {
        return tags.size();
    }
}
