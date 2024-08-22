package com.loja.view;

import com.loja.model.Cliente;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.loja.database.DatabaseConnection;

public class CadastroCliente extends JFrame {
    private JTextField nomeField;
    private JTextField emailField;
    private JTextField celularField;
    private JTextField clienteDesdeField;
    private JTextField enderecoField;
    private JButton cancelarButton;
    private JButton salvarButton;


    private MainPage mainPage;


    public CadastroCliente(Cliente cliente, MainPage mainPage) {
        this.mainPage = mainPage;

        setTitle(cliente == null ? "Cadastro de Cliente" : "Edição de Cliente");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        nomeField = new JTextField(20);
        emailField = new JTextField(20);
        celularField = new JTextField(20);
        clienteDesdeField = new JTextField(20);
        enderecoField = new JTextField(20);
        cancelarButton = new JButton("Cancelar");
        salvarButton = new JButton(cliente == null ? "Cadastrar" : "Editar");

        if (cliente != null) {
            nomeField.setText(cliente.getNome());
            emailField.setText(cliente.getEmail());
            celularField.setText(cliente.getCelular());
            clienteDesdeField.setText(cliente.getClienteDesde());
            enderecoField.setText(cliente.getEndereco());
        }

        Dimension buttonSize = new Dimension(120, 30);
        cancelarButton.setPreferredSize(buttonSize);
        salvarButton.setPreferredSize(buttonSize);

        JPanel panel = new JPanel(new GridLayout(6, 2));
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("E-mail:"));
        panel.add(emailField);
        panel.add(new JLabel("Celular:"));
        panel.add(celularField);
        panel.add(new JLabel("Cliente desde:"));
        panel.add(clienteDesdeField);
        panel.add(new JLabel("Endereço:"));
        panel.add(enderecoField);
        panel.add(cancelarButton);
        panel.add(salvarButton);

        add(panel);

        cancelarButton.addActionListener(e -> {
            new MainPage("Nome do Usuário");
            dispose();
        });

        salvarButton.addActionListener(e -> {
            if (cliente == null) {
                cadastrarCliente();
            } else {
                editarCliente(cliente);
            }
        });

        setVisible(true);
    }

    private void cadastrarCliente() {
        String nome = nomeField.getText();
        String email = emailField.getText();
        String celular = formatarCelular(celularField.getText());
        String clienteDesde = clienteDesdeField.getText();
        String endereco = enderecoField.getText();

        if (nome.isEmpty() || email.isEmpty() || celular.isEmpty() || clienteDesde.isEmpty() || endereco.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha os campos corretamente.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            JOptionPane.showMessageDialog(this, "E-mail inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!celular.matches("^\\(\\d{2}\\) \\d{4,5}-\\d{4}$")) {
            JOptionPane.showMessageDialog(this, "Celular inválido.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidDate(clienteDesde)) {
            JOptionPane.showMessageDialog(this, "Data 'Cliente desde' inválida. Use o formato dd/MM/yyyy.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO clientes (nome, email, celular, cliente_desde, endereco) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, email);
                stmt.setString(3, celular);
                stmt.setDate(4, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(clienteDesde))));
                stmt.setString(5, endereco);
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Cliente cadastrado com sucesso!");
            if (mainPage != null) {
                mainPage.loadClientes();
            }
            dispose();
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarCliente(Cliente cliente) {
        cliente.setNome(nomeField.getText());
        cliente.setEmail(emailField.getText());
        cliente.setCelular(formatarCelular(celularField.getText()));
        cliente.setClienteDesde(clienteDesdeField.getText());
        cliente.setEndereco(enderecoField.getText());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE clientes SET nome = ?, email = ?, celular = ?, cliente_desde = ?, endereco = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, cliente.getNome());
                stmt.setString(2, cliente.getEmail());
                stmt.setString(3, cliente.getCelular());
                stmt.setDate(4, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(cliente.getClienteDesde()))));
                stmt.setString(5, cliente.getEndereco());
                stmt.setInt(6, cliente.getId());
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Cliente editado com sucesso!");
            if (mainPage != null) {
                mainPage.loadClientes();
            }
            dispose();
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String formatarCelular(String celular) {
        String celularNumerico = celular.replaceAll("\\D", "");
        if (celularNumerico.length() == 10) {
            return String.format("(%s) %s-%s", celularNumerico.substring(0, 2), celularNumerico.substring(2, 6), celularNumerico.substring(6));
        } else if (celularNumerico.length() == 11) {
            return String.format("(%s) %s-%s", celularNumerico.substring(0, 2), celularNumerico.substring(2, 7), celularNumerico.substring(7));
        } else {
            return celular;
        }
    }

    private boolean isValidDate(String date) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        sdf.setLenient(false);
        try {
            Date parsedDate = sdf.parse(date);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CadastroCliente(null, null));
    }
}