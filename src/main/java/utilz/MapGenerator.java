package utilz;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

import static utilz.LoadSave.GetSpriteAtlas;

public class MapGenerator {
    private static final int FRAGMENT_ROWS = 8;
    private static final int FRAGMENT_COLS = 8;
    private static final int FINAL_GRID_SIZE = 25; // 25x25 fragmentos -> ~200x200 px
    private static final int AVOID_EDGE = 1; // distancia mínima a esquinas (en celdas)
    private static final Random rand = new Random();

    public static BufferedImage generarMapaFinal(String levelPath) {
        BufferedImage base = GetSpriteAtlas(levelPath);
        BufferedImage[][] fragmentos = dividirMapa(base, FRAGMENT_ROWS, FRAGMENT_COLS);

        BufferedImage frag253 = null, frag254 = null, frag255 = null;
        List<BufferedImage> lista = new ArrayList<>();

        for (int y = 0; y < FRAGMENT_ROWS; y++) {
            for (int x = 0; x < FRAGMENT_COLS; x++) {
                BufferedImage frag = fragmentos[y][x];
                if (contieneObjetoAzul(frag, 253)) frag253 = frag;
                else if (contieneObjetoAzul(frag, 254)) frag254 = frag;
                else if (contieneObjetoAzul(frag, 255)) frag255 = frag;
                lista.add(frag);
            }
        }

        if (frag253 == null || frag254 == null || frag255 == null)
            throw new RuntimeException("No se encontraron los tres fragmentos con azul 253, 254 y 255");

        Collections.shuffle(lista);

        List<Point> posiciones = calcularPosicionesAlejadas(FINAL_GRID_SIZE);
        Point p1 = posiciones.get(0), p2 = posiciones.get(1), p3 = posiciones.get(2);

        int fragSize = fragmentos[0][0].getWidth();
        BufferedImage nuevo = new BufferedImage(
                FINAL_GRID_SIZE * fragSize,
                FINAL_GRID_SIZE * fragSize,
                base.getType()
        );
        Graphics2D g = nuevo.createGraphics();

        for (int y = 0; y < FINAL_GRID_SIZE; y++) {
            for (int x = 0; x < FINAL_GRID_SIZE; x++) {
                BufferedImage f;
                if (x == p1.x && y == p1.y) f = frag253;
                else if (x == p2.x && y == p2.y) f = frag254;
                else if (x == p3.x && y == p3.y) f = frag255;
                else f = lista.get(rand.nextInt(lista.size()));

                f = rotarAleatorio(f);
                g.drawImage(f, x * fragSize, y * fragSize, null);
            }
        }

        g.dispose();
        return nuevo;
    }

    public static BufferedImage[][] dividirMapa(BufferedImage img, int filas, int columnas) {
        int fragW = img.getWidth() / columnas;
        int fragH = img.getHeight() / filas;
        BufferedImage[][] fragmentos = new BufferedImage[filas][columnas];
        for (int y = 0; y < filas; y++) {
            for (int x = 0; x < columnas; x++) {
                fragmentos[y][x] = img.getSubimage(x * fragW, y * fragH, fragW, fragH);
            }
        }
        return fragmentos;
    }

    public static boolean contieneObjetoAzul(BufferedImage frag, int azulBuscado) {
        for (int y = 0; y < frag.getHeight(); y++) {
            for (int x = 0; x < frag.getWidth(); x++) {
                Color c = new Color(frag.getRGB(x, y));
                if (c.getBlue() == azulBuscado)
                    return true;
            }
        }
        return false;
    }

    public static BufferedImage rotarAleatorio(BufferedImage img) {
        int rot = rand.nextInt(4); // 0, 90, 180, 270
        if (rot == 0) return img;
        int w = img.getWidth(), h = img.getHeight();
        BufferedImage nueva = new BufferedImage(w, h, img.getType());
        Graphics2D g2 = nueva.createGraphics();
        g2.rotate(Math.toRadians(rot * 90), w / 2.0, h / 2.0);
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        return nueva;
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
}
