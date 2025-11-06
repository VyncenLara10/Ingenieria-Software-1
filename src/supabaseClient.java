import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

public class SupabaseClient {
    private static final String SUPABASE_URL = "https://ujxsyngfqtjmzuzxhhai.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVqeHN5bmdmcXRqbXp1enhoaGFpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjIzOTIyMTIsImV4cCI6MjA3Nzk2ODIxMn0.BFHGdyei4X8ExaEjOIMNx7j5sP8wDoHqpq2fAoF4s30";
    
    private static final OkHttpClient httpClient = new OkHttpClient();
    private static final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    
    private static String accessToken = null;
    private static String currentUserId = null;
    
    public static String register(String email, String password) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("email", email);
            json.addProperty("password", password);
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            if (response.isSuccessful()) {
                JsonObject result = gson.fromJson(responseBody, JsonObject.class);
                
                // Guardar token y userId
                if (result.has("session") && !result.get("session").isJsonNull()) {
                    accessToken = result.getAsJsonObject("session")
                        .get("access_token").getAsString();
                }
                
                if (result.has("user") && !result.get("user").isJsonNull()) {
                    currentUserId = result.getAsJsonObject("user")
                        .get("id").getAsString();
                }
                
                System.out.println("✓ Registro exitoso: " + email);
                System.out.println("✓ User ID: " + currentUserId);
                return currentUserId;
            } else {
                System.err.println("✗ Error al registrar: " + responseBody);
                return null;
            }
            
        } catch (IOException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public static String login(String email, String password) {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("email", email);
            json.addProperty("password", password);
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
            
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            if (response.isSuccessful()) {
                JsonObject result = gson.fromJson(responseBody, JsonObject.class);
                accessToken = result.get("access_token").getAsString();
                currentUserId = result.getAsJsonObject("user")
                    .get("id").getAsString();
                
                System.out.println("✓ Login exitoso: " + email);
                System.out.println("✓ User ID: " + currentUserId);
                return currentUserId;
            } else {
                System.err.println("✗ Error al iniciar sesión: " + responseBody);
                return null;
            }
            
        } catch (IOException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Cargar configuración
    public static PlayerSettings loadSettings() {
        if (accessToken == null || currentUserId == null) {
            System.err.println("✗ Debes iniciar sesión primero");
            return new PlayerSettings();
        }
        
        try {
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/player_settings?player_id=eq." + currentUserId)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .get()
                .build();
            
            Response response = httpClient.newCall(request).execute();
            String responseBody = response.body().string();
            
            if (response.isSuccessful()) {
                JsonArray results = gson.fromJson(responseBody, JsonArray.class);
                
                if (results.size() > 0) {
                    JsonObject data = results.get(0).getAsJsonObject();
                    float visionRange = data.get("vision_range").getAsFloat();
                    float speed = data.get("speed").getAsFloat();
                    
                    System.out.println("✓ Configuración cargada");
                    return new PlayerSettings(visionRange, speed);
                } else {
                    System.out.println("⚠ No hay configuración guardada, usando valores por defecto");
                    return new PlayerSettings();
                }
            } else {
                System.err.println("✗ Error al cargar configuración: " + responseBody);
                return new PlayerSettings();
            }
            
        } catch (IOException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return new PlayerSettings();
        }
    }
    
    public static boolean saveSettings(PlayerSettings settings) {
        if (accessToken == null || currentUserId == null) {
            System.err.println("✗ Debes iniciar sesión primero");
            return false;
        }
        
        try {
            JsonObject json = new JsonObject();
            json.addProperty("vision_range", settings.getVisionRange());
            json.addProperty("speed", settings.getSpeed());
            
            RequestBody body = RequestBody.create(json.toString(), JSON);
            
            Request request = new Request.Builder()
                .url(SUPABASE_URL + "/rest/v1/player_settings?player_id=eq." + currentUserId)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=minimal")
                .patch(body)
                .build();
            
            Response response = httpClient.newCall(request).execute();
            
            if (response.isSuccessful()) {
                System.out.println("✓ Configuración guardada exitosamente");
                return true;
            } else {
                String responseBody = response.body().string();
                System.err.println("✗ Error al guardar configuración: " + responseBody);
                return false;
            }
            
        } catch (IOException e) {
            System.err.println("✗ Error de conexión: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public static void logout() {
        accessToken = null;
        currentUserId = null;
        System.out.println("✓ Sesión cerrada");
    }
}
    

