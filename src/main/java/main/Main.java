package main;

import database.Settings;
import database.SupaClient;

public class Main {
    public static void main(String[] args) {
        // 🔹 Nombre del jugador (puedes cambiarlo o pedirlo desde un menú)
        String playerName = "Anbit";

        // 🔹 Obtener configuración del jugador desde la base de datos
        String json = SupaClient.getPlayerSettings(playerName);
        System.out.println("Configuración obtenida: " + json);

        // 🔹 Si no hay datos, usamos valores por defecto
        Settings settings = new Settings(playerName, 0.8f, 1.0f);

        // 🔹 (Opcional) parsear el JSON real si ya tienes conexión funcionando
        // Por ahora dejamos valores por defecto hasta confirmar que la API responde

        // 🔹 Iniciar el juego con la configuración cargada
        new Juego(settings);
    }
}
