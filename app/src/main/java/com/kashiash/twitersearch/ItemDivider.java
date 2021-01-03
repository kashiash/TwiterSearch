package com.kashiash.twitersearch;
// ItemDivider.java
// Klasa definiująca linie rozdzielające elementy widoku RecyclerView;
// kod oparto na przykładowej implementacji przygotowanej przez firmę Google: bit.ly/DividerItemDecoration.
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

class ItemDivider extends RecyclerView.ItemDecoration {
    private final Drawable divider;

    // konstruktor ładuje wbudowany w system Android element dzielący listy
    public ItemDivider(Context context) {
        int[] attrs = {android.R.attr.listDivider};
        divider = context.obtainStyledAttributes(attrs).getDrawable(0);
    }

    // rysuje linie rozdzielające elementy widoku RecyclerView
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent,
                           RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        // oblicz współrzędne x w płaszczyźnie lewo-prawo wszystkich linii rozdzielających
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        // linię narysuj pod każdym z elementów poza ostatnim elementem
        for (int i = 0; i < parent.getChildCount() - 1; ++i) {
            View item = parent.getChildAt(i); // odczytaj i-ty element listy

            // oblicz współrzędne y w płaszczyźnie góra-dół wszystkich linii rozdzielających
            int top = item.getBottom() + ((RecyclerView.LayoutParams)
                    item.getLayoutParams()).bottomMargin;
            int bottom = top + divider.getIntrinsicHeight();

            // rysuj linię rozdzielającą, korzystając z obliczonych granic
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
    }
}
