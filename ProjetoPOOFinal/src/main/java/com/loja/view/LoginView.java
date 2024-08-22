package com.loja.view;

import com.loja.database.DatabaseConnection;
import com.loja.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.*;


public class LoginView extends JFrame {
    private JTextField usuarioEmailField;
    private JPasswordField senhaField;
    private JCheckBox exibirSenhaCheckBox;
    private JButton loginButton;
    private JButton registerButton;

    public LoginView() {
        setTitle("Login");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        usuarioEmailField = new JTextField(20);
        senhaField = new JPasswordField(20);
        exibirSenhaCheckBox = new JCheckBox("Exibir senha");
        loginButton = new JButton("Login");
        registerButton = new JButton("Cadastre-se");

        JPanel panel = new JPanel(new GridLayout(5, 2));
        panel.add(new JLabel("Usuário ou e-mail:"));
        panel.add(usuarioEmailField);
        panel.add(new JLabel("Senha:"));
        panel.add(senhaField);
        panel.add(new JLabel());
        panel.add(exibirSenhaCheckBox);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        exibirSenhaCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (exibirSenhaCheckBox.isSelected()) {
                    senhaField.setEchoChar((char) 0);
                } else {
                    senhaField.setEchoChar('•');
                }
            }
        });

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                realizarLogin();
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new RegisterView();
                dispose();
            }
        });

        setVisible(true);
    }

    private void realizarLogin() {
        String usuarioEmail = usuarioEmailField.getText();
        String senha = new String(senhaField.getPassword());

        if (usuarioEmail.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Usuário/e-mail e senha devem ser preenchidos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/loja_db", "root", "221117")) {
                String query = "SELECT usuario, email, senha FROM usuarios WHERE (usuario = ? OR email = ?) AND senha = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, usuarioEmail);
                    preparedStatement.setString(2, usuarioEmail);
                    preparedStatement.setString(3, senha);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Login realizado com sucesso!");
                        new MainPage(rs.getString("usuario"));
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Usuário ou senha inválidos.", "Erro", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao conectar com o banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao consultar o banco de dados.", "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(LoginView::new);
    }
}