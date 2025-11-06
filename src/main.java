import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== SISTEMA DE CONFIGURACIÓN DEL JUEGO ===\n");
        
        System.out.print("¿Ya tienes cuenta? (s/n): ");
        String respuesta = scanner.nextLine();
        
        String userId = null;
        
        if (respuesta.equalsIgnoreCase("n")) {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Contraseña: ");
            String password = scanner.nextLine();
            
            userId = SupabaseClient.register(email, password);
        } else {
            System.out.print("Email: ");
            String email = scanner.nextLine();
            System.out.print("Contraseña: ");
            String password = scanner.nextLine();
            
            userId = SupabaseClient.login(email, password);
        }
        
        if (userId != null) {
            PlayerSettings settings = SupabaseClient.loadSettings();
            System.out.println("\n--- Configuración actual ---");
            System.out.println(settings);
            
            System.out.print("\n¿Quieres cambiar la configuración? (s/n): ");
            if (scanner.nextLine().equalsIgnoreCase("s")) {
                System.out.print("Nueva visión (actual: " + settings.getVisionRange() + "): ");
                float nuevaVision = Float.parseFloat(scanner.nextLine());
                
                System.out.print("Nueva velocidad (actual: " + settings.getSpeed() + "): ");
                float nuevaVelocidad = Float.parseFloat(scanner.nextLine());
                
                settings.setVisionRange(nuevaVision);
                settings.setSpeed(nuevaVelocidad);
                
                if (SupabaseClient.saveSettings(settings)) {
                    System.out.println("\n✓ ¡Configuración actualizada!");
                    System.out.println(settings);
                }
            }
            
            SupabaseClient.logout();
        } else {
            System.err.println("\n✗ No se pudo iniciar sesión");
        }
        
        scanner.close();
    }
}