package main;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MenuPrincipal());
    }
}

class MenuPrincipal extends JFrame {

    public MenuPrincipal() {
        setTitle("Menú Principal");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // centrar ventana

        // Panel principal
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2, 1, 10, 10));

        // Botón Host
        JButton btnHost = new JButton("Host");
        btnHost.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // cierra el menú
                new Juego(true); // abre la clase Juego
            }
        });

        // Botón Unirse
        JButton btnUnirse = new JButton("Unirse");
        btnUnirse.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // cierra el menú
                new Juego(false); // abre la clase Juego
            }
        });

        panel.add(btnHost);
        panel.add(btnUnirse);

        add(panel);
        setVisible(true);
    }
}
