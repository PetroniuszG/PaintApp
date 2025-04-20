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

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import android.widget.ImageButton;
import android.widget.SeekBar;

import android.widget.Toast;
import android.widget.LinearLayout;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;




/**
 * Klasa MainActivity reprezentuje główne okno aplikacji.
 * @author Jakub Prabucki
 * @version 1.0
 */

public class MainActivity extends AppCompatActivity {
    private DrawingView drawingView;
    private Paint paint;
    private int brushSize = 5;
    private static final int PICK_IMAGE = 1;
    private String currentShape = "Line";

    /**
     * Metoda onCreate jest wywoływana po tworzeniu aktywności.
     * Inicjalizuje pędzel, widok oraz przyciski.
     * @param savedInstanceState odniesienie do obiektu Bundle
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Inicjalizacja pędzla
        paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(brushSize);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

        //Inicjalizacja widoku rysowania
        drawingView = findViewById(R.id.drawing_view);
        drawingView.setPaint(paint);
        drawingView.setShape(currentShape);

        //Przycisk zapisu rysunku
        ImageButton saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> drawingView.saveDrawing(MainActivity.this));  //Zapisz rysunek

        //Przycisk odczytu rysunku
        ImageButton loadButton = findViewById(R.id.readFF_button);
        loadButton.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Wybierz obraz"), PICK_IMAGE);
        });

        //Przycisk gumki
        ImageButton eraserButton = findViewById(R.id.eraser_button);
        eraserButton.setOnClickListener(v -> drawingView.setShape("Eraser"));

        //Przycisk wyboru koloru
        ImageButton colorPickerButton = findViewById(R.id.color_picker_button);
        colorPickerButton.setOnClickListener(v -> {
            //Okno wyboru koloru
            ColorPickerDialog colorPickerDialog = new ColorPickerDialog();
            colorPickerDialog.setOnColorSelectedListener(color -> {
                drawingView.setShape(currentShape); //Zapobieganie zamiany kształtu na gumkę
                paint.setColor(color);
                String colorName = getColorName(color);
                Toast.makeText(MainActivity.this, "Wybrany kolor: " + colorName, Toast.LENGTH_SHORT).show();
            });
            colorPickerDialog.show(getFragmentManager(), "color_picker");
        });

        //Przycisk czyszczenia rysunku
        ImageButton clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(v -> {
            drawingView.clear();
            Toast.makeText(MainActivity.this, "Rysunek wyczyszczony", Toast.LENGTH_SHORT).show();
        });

        //Przycisk zmiany grubości pędzla
        ImageButton brushSizeButton = findViewById(R.id.brush_size_button);
        brushSizeButton.setOnClickListener(v -> showBrushSizeDialog());

        //Przycisk zmiany kształtu
        ImageButton shapePickerButton = findViewById(R.id.shape_picker_button);
        shapePickerButton.setOnClickListener(v -> showShapePickerDialog());

        //Przycisk Undo
        ImageButton undoButton = findViewById(R.id.undo_button);
        undoButton.setOnClickListener(v -> drawingView.undo());

        //Przycisk Redo
        ImageButton redoButton = findViewById(R.id.redo_button);
        redoButton.setOnClickListener(v -> drawingView.redo());

        //Przycisk zmiany stylu pędzla
        ImageButton brushStyleButton = findViewById(R.id.brush_style_button);
        brushStyleButton.setOnClickListener(v -> showBrushStyleDialog());
    }

    /**
     * Metoda onActivityResult jest wywoływana po zakończeniu wyboru obrazu przez użytkownika.
     * @param requestCode - kod żądania
     * @param resultCode - kod wyniku
     * @param data - dane zwrotne
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            drawingView.loadImage(imageUri, this);
        }
    }

    /**
     * Metoda do wyświetlenia okna wyboru kształtu.
     */

    private void showShapePickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz kształt")
                .setItems(new String[]{"Linia", "Prostokąt", "Elipsa", "Trójkąt"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            currentShape = "Line";
                            break;
                        case 1:
                            currentShape = "Rectangle";
                            break;
                        case 2:
                            currentShape = "Ellipse";
                            break;
                        case 3:
                            currentShape = "Triangle";
                            break;
                    }
                    drawingView.setShape(currentShape);
                })
                .show();
    }

    /**
     * Metoda do wyświetlenia okna wyboru stylu pędzla.
     */
    private void showBrushStyleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz styl pędzla")
                .setItems(new String[]{"Normalny", "Kreskowany", "Kropkowany", "Rozmyty"}, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            drawingView.setBrushStyle(DrawingView.BrushStyle.NORMAL);
                            Toast.makeText(MainActivity.this, "Styl: Normalny", Toast.LENGTH_SHORT).show();
                            break;
                        case 1:
                            drawingView.setBrushStyle(DrawingView.BrushStyle.DASHED);
                            Toast.makeText(MainActivity.this, "Styl: Kreskowany", Toast.LENGTH_SHORT).show();
                            break;
                        case 2:
                            drawingView.setBrushStyle(DrawingView.BrushStyle.DOTTED);
                            Toast.makeText(MainActivity.this, "Styl: Kropkowany", Toast.LENGTH_SHORT).show();
                            break;
                        case 3:
                            drawingView.setBrushStyle(DrawingView.BrushStyle.BLUR);
                            Toast.makeText(MainActivity.this, "Styl: Rozmyty", Toast.LENGTH_SHORT).show();
                            break;
                    }
                })
                .show();
    }
    /**
     * Metoda do wyświetlenia okna wyboru grubości pędzla.
     */
    private void showBrushSizeDialog() {
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.brush_size_dialog, null);
        SeekBar seekBar = layout.findViewById(R.id.seekBarBrushSize);
        seekBar.setProgress(brushSize);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Wybierz grubość pędzla")
                .setView(layout)
                .setPositiveButton("OK", (dialog, which) -> {
                    brushSize = seekBar.getProgress();
                    paint.setStrokeWidth(brushSize);
                    drawingView.setPaint(paint);
                    Toast.makeText(MainActivity.this, "Grubość pędzla: " + brushSize, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    /**
     * Metoda do pobrania nazwy koloru na podstawie jego kodu RGB.
     * @param color - kod RGB koloru
     * @return nazwa koloru
     */
    public String getColorName(int color) {
        switch (color) {
            case Color.RED:
                return "Czerwony";
            case Color.GREEN:
                return "Zielony";
            case Color.BLUE:
                return "Niebieski";
            case Color.YELLOW:
                return "Żółty";
            case Color.CYAN:
                return "Cyjan";
            case Color.MAGENTA:
                return "Magenta";
            case Color.BLACK:
                return "Czarny";
            case Color.WHITE:
                return "Biały";
            case Color.DKGRAY:
                return "Ciemnoszary";
            case Color.GRAY:
                return "Szary";
            default:
                String hexColor = String.format("#%06X", (0xFFFFFF & color));
                return "Kolor: " + hexColor;
        }
    }
}
