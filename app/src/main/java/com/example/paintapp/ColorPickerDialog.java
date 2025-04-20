/**
 * Program PaintApp - aplikacja umożliwiająca rysowanie na ekranie.
 * Funkcjonalność: W domyślnym trybie rysowanie kolorem czarnym w wyznaczonym polu.
 * Paleta - umożliwia wybranie innego koloru pędzla.
 * Dwie strzałki - zmieniają grubość pędzla.
 * Kwadrat - pozwala wybrać figurę do rysowania.
 * Strzałka w lewo - cofa ostatnią operację.
 * Strzałka w prawo - przywraca ostatnią operację
 * Zapis - zapis drawingView do pliku.
 * Wczytanie - wczytanie zdjęcia do drawingView.
 * Krzyżyk - czyszczenie rysunku.
 * Gumka - rysowanie w kolorze tła rysunku, co daje efekt "gumki".
 * @author PetroniuszG
 * @version 1.0
 */
package com.example.paintapp;
import android.app.DialogFragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;



/**
 * Klasa ColorPickerDialog reprezentuje okno dialogowe do wyboru koloru.
 * @author Jakub Prabucki
 * @version 1.0
 */
public class ColorPickerDialog extends DialogFragment {

    private OnColorSelectedListener listener;

    /**
     * Interfejs OnColorSelectedListener jest używany do komunikacji między aktywnością a fragmentem dialogowym.
     */
    public interface OnColorSelectedListener {
        /**
         * Metoda onColorSelected jest wywoływana po wybraniu koloru.
         * @param color - wybrany kolor
         */
        void onColorSelected(int color);
    }

    /**
     * Metoda setOnColorSelectedListener ustawia obiekt OnColorSelectedListener.
     * @param listener - obiekt OnColorSelectedListener
     */
    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.listener = listener;
    }

    /**
     * Metoda onCreateView jest wywoływana po tworzeniu widoku dialogowego.
     * @param inflater - inflator do tworzenia widoku
     * @param container - kontener widoku
     * @param savedInstanceState - odniesienie do obiektu Bundle
     * @return widok dialogowy
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //Infla i ustawianie widoku dialogu
        View view = inflater.inflate(R.layout.color_picker_dialog, container, false);

        //Siatka kolorów
        GridLayout gridLayout = view.findViewById(R.id.color_grid);

        //Lista kolorów
        int[] colors = new int[] {
                Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.CYAN,
                Color.MAGENTA, Color.BLACK, Color.GRAY, Color.WHITE, Color.DKGRAY
        };

        //Dodanie kolorów do siatki
        for (int color : colors) {
            Button colorButton = new Button(getActivity());
            colorButton.setBackgroundColor(color);
            colorButton.setLayoutParams(new GridLayout.LayoutParams());
            colorButton.setOnClickListener(v -> {
                //Zwracanie wybranego koloru do aktywności
                if (listener != null) {
                    listener.onColorSelected(color);
                }
                dismiss(); //Zamkniecie dialogu po wybraniu koloru
            });
            gridLayout.addView(colorButton);
        }

        //Przycisk "Zamknij" w oknie dialogowym
        Button closeButton = view.findViewById(R.id.close_button);
        closeButton.setOnClickListener(v -> dismiss());  //Zamkniecie dialogu po kliknieciu przycisku
        //Zwracanie widoku
        return view;
    }
}
