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
 * @author Jakub Prabucki
 * @version 1.0
 */
package com.example.paintapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Klasa DrawingView reprezentuje widok do rysowania.
 */
public class DrawingView extends View {
    //Aktualny obiekt Paint używany do rysowania
    private Paint currentPaint;
    //Aktualnie rysowana ścieżka
    private Path currentPath;
    //Lista wszystkich narysowanych ścieżek
    private List<PathWithPaint> paths;
    //Aktualna figura do rysowania - domyślnie linia
    private String currentShape = "Line";
    //Zmienne przechowujące punkty figury
    private float startX, startY, endX, endY;
    //Stała szerokość gumki
    private static final float ERASER_STROKE_WIDTH = 20f;
    //Pędzel do rysowania w trybie gumki
    private Paint eraserPaint;
    //Historia wszystkich stanów rysunku
    private List<List<PathWithPaint>> history;
    //Aktualny indeks w historii
    private int historyIndex;
    //Bitmapa tła rysunku
    private Bitmap backgroundBitmap;
    //Flaga określająca ustawienie tła
    private boolean hasBackground = false;
    //Flaga określająca, czy aktualnie rysowana figura jest gumką
    private boolean isEraserActive = false;

    //Wyliczenie styli pędzla
    public enum BrushStyle {
        NORMAL,
        DASHED,
        DOTTED,
        BLUR
    }

    /**
     * Konstruktor widoku rysowania.
     * @param context - kontekst aplikacji
     * @param attrs - atrybuty widoku
     */
    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * Inicjalizacja początkowych parametrów widoku rysowania.
     * Ustawia domyślne parametry pędzla, ścieżek i historii.
     */
    private void init() {
        currentPaint = new Paint();
        currentPaint.setColor(Color.BLACK);
        currentPaint.setStrokeWidth(5);
        currentPaint.setStyle(Paint.Style.STROKE);
        currentPaint.setAntiAlias(true);

        //Inicjalizacja gumki jako pędzla o kolorze tła
        eraserPaint = new Paint();
        eraserPaint.setColor(Color.WHITE); //Kolor tła
        eraserPaint.setStrokeWidth(ERASER_STROKE_WIDTH);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setAntiAlias(true);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND); // Zaokrąglone końce dla płynniejszego wyglądu

        paths = new ArrayList<>();
        currentPath = new Path();

        history = new ArrayList<>();
        history.add(new ArrayList<>());
        historyIndex = 0;
    }

    /**
     * Metoda rysowania widoku.
     * Renderuje tło, wszystkie zapisane ścieżki oraz aktualnie rysowaną figurę.
     * @param canvas - płótno do rysowania
     */
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE); // Ustawienie białego tła

        // Najpierw rysuj tło jeśli istnieje
        if (hasBackground && backgroundBitmap != null) {
            canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        }

        // Rysuj wszystkie zapisane ścieżki
        for (PathWithPaint pathWithPaint : paths) {
            canvas.drawPath(pathWithPaint.path, pathWithPaint.paint);
        }

        // Rysuj aktualną ścieżkę
        if (currentPath != null) {
            if (isEraserActive) {
                canvas.drawPath(currentPath, eraserPaint);
            } else if (currentShape.equals("Line")) {
                canvas.drawPath(currentPath, currentPaint);
            } else if (currentShape.equals("Rectangle")) {
                float left = Math.min(startX, endX);
                float top = Math.min(startY, endY);
                float right = Math.max(startX, endX);
                float bottom = Math.max(startY, endY);
                canvas.drawRect(left, top, right, bottom, currentPaint);
            } else if (currentShape.equals("Ellipse")) {
                float left = Math.min(startX, endX);
                float top = Math.min(startY, endY);
                float right = Math.max(startX, endX);
                float bottom = Math.max(startY, endY);
                canvas.drawOval(left, top, right, bottom, currentPaint);
            } else if (currentShape.equals("Triangle")) {
                @SuppressLint("DrawAllocation") Path trianglePath = new Path();
                trianglePath.moveTo(startX, startY);
                trianglePath.lineTo(endX, startY);
                trianglePath.lineTo((startX + endX) / 2, endY);
                trianglePath.close();
                canvas.drawPath(trianglePath, currentPaint);
            }
        }
    }


    /**
     * Metoda ustawiająca styl pędzla.
     * Style: normalny, kreskowany, kropkowany, rozmyty.
     * @param style - wybrany styl
     */
    public void setBrushStyle(BrushStyle style) {
        //Aktualny styl pędzla
        switch (style) {
            case NORMAL:
                currentPaint.setPathEffect(null);
                currentPaint.setMaskFilter(null);
                break;
            case DASHED:
                currentPaint.setPathEffect(new DashPathEffect(new float[]{20, 10}, 0));
                currentPaint.setMaskFilter(null);
                break;
            case DOTTED:
                currentPaint.setPathEffect(new DashPathEffect(new float[]{5, 10}, 0));
                currentPaint.setMaskFilter(null);
                break;
            case BLUR:
                currentPaint.setPathEffect(null);
                currentPaint.setMaskFilter(new BlurMaskFilter(10, BlurMaskFilter.Blur.NORMAL));
                break;
        }
    }

    /**
     * Obsługa zdarzeń dotykowych dla rysowania.
     * Zarządza rozpoczęciem, przebiegiem i zakończeniem rysowania.
     * @param event - zdarzenie dotykowe
     * @return true, jeśli zdarzenie zostało obsłużone, false w przeciwnym razie
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                currentPath = new Path();
                currentPath.moveTo(startX, startY);
                return true;

            case MotionEvent.ACTION_MOVE:
                endX = x;
                endY = y;
                if (isEraserActive || currentShape.equals("Line")) {
                    currentPath.lineTo(endX, endY);
                    if (isEraserActive) {
                        // Przy gumce od razu dodajemy ścieżkę do listy
                        Path eraserPath = new Path(currentPath);
                        paths.add(new PathWithPaint(eraserPath, new Paint(eraserPaint)));
                        currentPath = new Path();
                        currentPath.moveTo(endX, endY);
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                endX = x;
                endY = y;

                if (!isEraserActive) {
                    Path path = new Path();
                    switch (currentShape) {
                        case "Line":
                            path.set(currentPath);
                            break;
                        case "Rectangle": {
                            float left = Math.min(startX, endX);
                            float top = Math.min(startY, endY);
                            float right = Math.max(startX, endX);
                            float bottom = Math.max(startY, endY);
                            path.addRect(left, top, right, bottom, Path.Direction.CW);
                            break;
                        }
                        case "Ellipse": {
                            float left = Math.min(startX, endX);
                            float top = Math.min(startY, endY);
                            float right = Math.max(startX, endX);
                            float bottom = Math.max(startY, endY);
                            path.addOval(left, top, right, bottom, Path.Direction.CW);
                            break;
                        }
                        case "Triangle":
                            path.moveTo(startX, startY);
                            path.lineTo(endX, startY);
                            path.lineTo((startX + endX) / 2, endY);
                            path.close();
                            break;
                    }
                    paths.add(new PathWithPaint(path, new Paint(currentPaint)));
                }

                // Zapisz aktualny stan w historii
                if (historyIndex < history.size() - 1) {
                    while (history.size() > historyIndex + 1) {
                        history.remove(history.size() - 1);
                    }
                }

                List<PathWithPaint> pathsCopy = new ArrayList<>();
                for (PathWithPaint pwp : paths) {
                    Path pathCopy = new Path(pwp.path);
                    Paint paintCopy = new Paint(pwp.paint);
                    pathsCopy.add(new PathWithPaint(pathCopy, paintCopy));
                }

                history.add(pathsCopy);
                historyIndex++;

                currentPath = new Path();
                invalidate();
                return true;
        }
        return false;
    }

    /**
     * Ustawia aktualny styl malowania.
     * @param paint - Obiekt Paint do ustawienia
     */
    public void setPaint(Paint paint) {
        this.currentPaint = paint;
    }

    /**
     * Metoda do czyszczenia rysunku, resetuje historię i tło.
     */
    public void clear() {
        paths.clear();
        currentPath = new Path();
        startX = startY = endX = endY = 0;

        //Reset historii z pustym stanem
        history.clear();
        history.add(new ArrayList<>());
        historyIndex = 0;

        //Czyszczenie tła
        hasBackground = false;
        backgroundBitmap = null;

        invalidate();
    }

    /**
     * Ustawia aktualny kształt lub aktywuje gumkę
     * @param shape - nazwa kształtu lub gumka
     */
    public void setShape(String shape) {
        if (shape.equals("Eraser")) {
            isEraserActive = true;
            // Aktualizacja szerokości gumki przy każdym jej wyborze
            eraserPaint.setStrokeWidth(ERASER_STROKE_WIDTH);
        } else {
            isEraserActive = false;
            currentShape = shape;
        }
        currentPath = new Path();
    }

    /**
     * Metoda do cofania ostatniej operacji rysowania.
     */
    public void undo() {
        if (historyIndex > 0) {
            historyIndex--;

            //Głębokie kopiowanie stanu z historii
            paths.clear();
            List<PathWithPaint> historicalPaths = history.get(historyIndex);
            for (PathWithPaint pwp : historicalPaths) {
                Path pathCopy = new Path(pwp.path);
                Paint paintCopy = new Paint(pwp.paint);
                paths.add(new PathWithPaint(pathCopy, paintCopy));
            }

            //Reset aktualnie rysowanej figury
            currentPath = new Path();
            startX = startY = endX = endY = 0;

            invalidate();
        }
    }

    /**
     * Metoda do przywracania ostatniej operacji rysowania.
     */
    public void redo() {
        if (historyIndex < history.size() - 1) {
            historyIndex++;

            //Głębokie kopiowanie stanu z historii
            paths.clear();
            List<PathWithPaint> historicalPaths = history.get(historyIndex);
            for (PathWithPaint pwp : historicalPaths) {
                Path pathCopy = new Path(pwp.path);
                Paint paintCopy = new Paint(pwp.paint);
                paths.add(new PathWithPaint(pathCopy, paintCopy));
            }

            //Reset aktualnie rysowanej figury
            currentPath = new Path();
            startX = startY = endX = endY = 0;

            invalidate();
        }
    }

    /**
     * Zapisuje aktualny rysunek jako plik PNG.
     * @param context - Kontekst aplikacji
     */
    public void saveDrawing(Context context) {
        //Tworzenie bitmapy z widoku
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);

        //Zapisywanie bitmapy do pliku
        String fileName = "rysunek_" + System.currentTimeMillis() + ".png";
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);

        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            Toast.makeText(context, "Rysunek zapisany: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, "Błąd podczas zapisywania rysunku", Toast.LENGTH_SHORT).show();
            e.getMessage();
        }
    }

    /**
     * Wczytuje obraz z lokalizacji i ustawia go jako tło rysunku.
     * @param imageUri - URI obrazu
     * @param context - Kontekst aplikacji
     */
    public void loadImage(Uri imageUri, Context context) {
        try {
            //Wczytaj bitmapę z URI
            InputStream imageStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap loadedBitmap = BitmapFactory.decodeStream(imageStream);

            //Przeskaluj bitmapę do rozmiaru widoku jeśli jest potrzeba
            if (getWidth() > 0 && getHeight() > 0) {
                backgroundBitmap = Bitmap.createScaledBitmap(loadedBitmap, getWidth(), getHeight(), true);
            } else {
                backgroundBitmap = loadedBitmap;
            }

            hasBackground = true;
            invalidate(); //Odśwież widok
            Toast.makeText(context, "Obraz wczytany pomyślnie", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.getMessage();
            Toast.makeText(context, "Błąd podczas wczytywania obrazu", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Klasa reprezentująca ścieżkę i styl rysowania.
     */
    public static class PathWithPaint {
        Path path;
        Paint paint;

        PathWithPaint(Path path, Paint paint) {
            this.path = path;
            this.paint = paint;
        }
    }
}