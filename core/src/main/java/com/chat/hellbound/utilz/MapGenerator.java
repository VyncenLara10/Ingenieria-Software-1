package com.chat.hellbound.utilz;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Gdx;

import java.util.*;

public class MapGenerator {

    private static final int FRAGMENT_ROWS = 8;
    private static final int FRAGMENT_COLS = 8;
    private static final int FINAL_GRID_SIZE = 8;
    private static final int AVOID_EDGE = 1;
    private static final Random rand = new Random();

    public static Pixmap generarMapaFinal(String levelPath) {
        FileHandle fh = Gdx.files.internal(levelPath);
        Pixmap base = new Pixmap(fh);

        Pixmap[][] fragmentos = dividirMapa(base, FRAGMENT_ROWS, FRAGMENT_COLS);

        Pixmap frag253 = null, frag254 = null, frag255 = null;
        List<Pixmap> lista = new ArrayList<>();

        for (int y = 0; y < FRAGMENT_ROWS; y++) {
            for (int x = 0; x < FRAGMENT_COLS; x++) {
                Pixmap frag = fragmentos[y][x];
                if (contieneObjetoAzul(frag, 253)) frag253 = frag;
                else if (contieneObjetoAzul(frag, 254)) frag254 = frag;
                else if (contieneObjetoAzul(frag, 255)) frag255 = frag;
                lista.add(frag);
            }
        }

        if (frag253 == null || frag254 == null || frag255 == null)
            throw new RuntimeException("No se encontraron los fragmentos con azul 253, 254 y 255.");

        Collections.shuffle(lista);

        List<Point> posiciones = calcularPosicionesAlejadas(FINAL_GRID_SIZE);
        Point p1 = posiciones.get(0), p2 = posiciones.get(1), p3 = posiciones.get(2);

        int fragW = fragmentos[0][0].getWidth();
        int fragH = fragmentos[0][0].getHeight();

        Pixmap nuevo = new Pixmap(
            FINAL_GRID_SIZE * fragW,
            FINAL_GRID_SIZE * fragH,
            base.getFormat()
        );

        for (int y = 0; y < FINAL_GRID_SIZE; y++) {
            for (int x = 0; x < FINAL_GRID_SIZE; x++) {
                Pixmap f;
                if (x == p1.x && y == p1.y) f = frag253;
                else if (x == p2.x && y == p2.y) f = frag254;
                else if (x == p3.x && y == p3.y) f = frag255;
                else f = lista.get(rand.nextInt(lista.size()));

                Pixmap rotado = rotarAleatorio(f);
                nuevo.drawPixmap(rotado, x * fragW, y * fragH);
                rotado.dispose();
            }
        }

        base.dispose();
        return nuevo;
    }

    public static Pixmap[][] dividirMapa(Pixmap img, int filas, int columnas) {
        int fragW = img.getWidth() / columnas;
        int fragH = img.getHeight() / filas;
        Pixmap[][] fragmentos = new Pixmap[filas][columnas];

        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                Pixmap frag = new Pixmap(fragW, fragH, img.getFormat());
                frag.drawPixmap(img, 0, 0, x * fragW, y * fragH, fragW, fragH);
                fragmentos[y][x] = frag;
            }
        }
        return fragmentos;
    }

    public static boolean contieneObjetoAzul(Pixmap frag, int azulBuscado) {
        int w = frag.getWidth();
        int h = frag.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int pixel = frag.getPixel(x, y);
                int b = (pixel >>> 8) & 0xFF;
                if (b == azulBuscado)
                    return true;
            }
        }
        return false;
    }

    public static Pixmap rotarAleatorio(Pixmap src) {
        int rot = rand.nextInt(4);
        if (rot == 0) return copiarPixmap(src);

        int w = src.getWidth();
        int h = src.getHeight();
        Pixmap rotated = new Pixmap(w, h, src.getFormat());

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int color = src.getPixel(x, y);
                int rx = x, ry = y;

                switch (rot) {
                    case 1: // 90°
                        rx = h - 1 - y;
                        ry = x;
                        break;
                    case 2: // 180°
                        rx = w - 1 - x;
                        ry = h - 1 - y;
                        break;
                    case 3: // 270°
                        rx = y;
                        ry = w - 1 - x;
                        break;
                }
                rotated.drawPixel(rx, ry, color);
            }
        }

        return rotated;
    }

    private static Pixmap copiarPixmap(Pixmap src) {
        Pixmap copy = new Pixmap(src.getWidth(), src.getHeight(), src.getFormat());
        copy.drawPixmap(src, 0, 0);
        return copy;
    }

    public static List<Point> calcularPosicionesAlejadas(int gridSize) {
        List<Point> result = new ArrayList<>();

        Point p1;
        do {
            p1 = new Point(rand.nextInt(gridSize), rand.nextInt(gridSize));
        } while (estaCercaDeBorde(p1, gridSize));

        result.add(p1);

        Point p2 = null;
        double maxDist = -1;
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                Point p = new Point(x, y);
                if (estaCercaDeBorde(p, gridSize)) continue;
                double d = p.distance(p1);
                if (d > maxDist) {
                    maxDist = d;
                    p2 = p;
                }
            }
        }
        result.add(p2);

        Point p3 = null;
        maxDist = -1;
        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                Point p = new Point(x, y);
                if (estaCercaDeBorde(p, gridSize)) continue;
                double d1 = p.distance(p1);
                double d2 = p.distance(p2);
                double minD = Math.min(d1, d2);
                if (minD > maxDist) {
                    maxDist = minD;
                    p3 = p;
                }
            }
        }
        result.add(p3);

        return result;
    }

    private static boolean estaCercaDeBorde(Point p, int gridSize) {
        return (p.x < AVOID_EDGE || p.y < AVOID_EDGE ||
            p.x >= gridSize - AVOID_EDGE || p.y >= gridSize - AVOID_EDGE);
    }

    public static class Point {
        public int x, y;
        public Point(int x, int y) { this.x = x; this.y = y; }
        public double distance(Point other) {
            int dx = this.x - other.x;
            int dy = this.y - other.y;
            return Math.sqrt(dx * dx + dy * dy);
        }
    }
}
