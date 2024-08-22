package com.loja.view;

import com.loja.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.sql.*;

public class RegisterView extends JFrame {
    private JTextField usuarioField;
    private JTextField emailField;
    private JPasswordField senhaField;
    private JPasswordField confirmeSenhaField;
    private JCheckBox exibirSenhasCheckBox;
    private JButton registerButton;
    private JButton voltarButton;

    public RegisterView() {
        setTitle("Cadastro de Usuário");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        usuarioField = new JTextField(20);
        emailField = new JTextField(20);
        senhaField = new JPasswordField(20);
        confirmeSenhaField = new JPasswordField(20);
        exibirSenhasCheckBox = new JCheckBox("Exibir senhas");
        registerButton = new JButton("Cadastrar");
        voltarButton = new JButton("Voltar");

        JPanel panel = new JPanel(new GridLayout(7, 2));
        panel.add(new JLabel("Usuário:"));
        panel.add(usuarioField);
        panel.add(new JLabel("E-mail:"));
        panel.add(emailField);
        panel.add(new JLabel("Senha:"));
        panel.add(senhaField);
        panel.add(new JLabel("Confirme a senha:"));
        panel.add(confirmeSenhaField);
        panel.add(new JLabel());
        panel.add(exibirSenhasCheckBox);
        panel.add(registerButton);
        panel.add(voltarButton);

        add(panel);

        exibirSenhasCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (exibirSenhasCheckBox.isSelected()) {
                    senhaField.setEchoChar((char) 0);
                    confirmeSenhaField.setEchoChar((char) 0);
                } else {
                    senhaField.setEchoChar('•');
                    confirmeSenhaField.setEchoChar('•');
                }
            }
        });

        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String usuario = usuarioField.getText();
                String email = emailField.getText();
                String senha = new String(senhaField.getPassword());
                String confirmeSenha = new String(senhaField.getPassword());

                if (usuario.isEmpty() || email.isEmpty() || senha.isEmpty() || confirmeSenha.isEmpty()) {
                    JOptionPane.showMessageDialog(RegisterView.this, "Todos os campos são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (usuario.length() < 4 || usuario.length() > 20) {
                    JOptionPane.showMessageDialog(RegisterView.this, "O nome de usuário deve ter entre 4 e 20 caracteres.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!senha.equals(confirmeSenha)) {
                    JOptionPane.showMessageDialog(RegisterView.this, "As senhas não coincidem.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!validarEmail(email)) {
                    JOptionPane.showMessageDialog(RegisterView.this, "E-mail inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (senha.length() < 8) {
                    JOptionPane.showMessageDialog(RegisterView.this, "A senha deve conter no mínimo 8 caracteres.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Verificação de usuário ou email já existente
                String errorMessage = verificarUsuarioOuEmail(usuario, email);
                if (errorMessage != null) {
                    JOptionPane.showMessageDialog(RegisterView.this, errorMessage, "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                cadastrarUsuario(usuario, email, senha, confirmeSenha);
            }
        });

        voltarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginView();
                dispose();
            }
        });

        setVisible(true);
    }

    private void cadastrarUsuario(String usuario, String email, String senha, String confirmeSenha) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/loja_db", "root", "221117")) {
                String query = "INSERT INTO usuarios (usuario, email, senha) VALUES (?, ?, ?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, usuario);
                    preparedStatement.setString(2, email);
                    preparedStatement.setString(3, senha);
                    preparedStatement.executeUpdate();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        JOptionPane.showMessageDialog(this, "Usuário cadastrado com sucesso!");
        new LoginView();
        dispose();
    }

    private boolean validarEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    // Método para verificar se o usuário ou email já existe no banco de dados
    private String verificarUsuarioOuEmail(String usuario, String email) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection connection = DriverManager.getConnection("jdbc:mysql://localhost/loja_db", "root", "221117")) {
                String query = "SELECT usuario, email FROM usuarios WHERE usuario = ? OR email = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, usuario);
                    preparedStatement.setString(2, email);
                    ResultSet rs = preparedStatement.executeQuery();
                    if (rs.next()) {
                        if (rs.getString("usuario").equals(usuario)) {
                            return "O nome de usuário já está em uso.";
                        }
                        if (rs.getString("email").equals(email)) {
                            return "O email já está em uso.";
                        }
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return "Erro ao verificar usuário ou email.";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Erro ao verificar usuário ou email.";
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RegisterView::new);
    }
}