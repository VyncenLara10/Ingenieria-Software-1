package database;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SupaClient {
    private static final String SUPABASE_URL = "https://ujxsyngfqtjmzuzxhhai.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqeHN5bmdmcXRqbXp1enhoaGFpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIzOTIyMTIsImV4cCI6MjA3Nzk2ODIxMn0.BFHGdyei4X8ExaEjOIMNx7j5sP8wDoHqpq2fAoF4s30";
    private static final String TABLE = "player_settings";

    public static String getPlayerSettings(String playerName) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + TABLE + "?player_name=eq." + playerName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            return response.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void updatePlayerSettings(String playerName, float brightness, float speed) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/" + TABLE + "?player_name=eq." + playerName);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("apikey", SUPABASE_KEY);
            conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_KEY);
            conn.setRequestProperty("Content-Type", "application/json");

            String jsonInputString = String.format("{\"brightness\": %.2f, \"speed\": %.2f}", brightness, speed);
            conn.setDoOutput(true);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("✅ Configuración actualizada: " + response);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
