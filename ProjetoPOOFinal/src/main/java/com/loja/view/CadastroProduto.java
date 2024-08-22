package com.loja.view;

import com.loja.model.Produto;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.loja.database.DatabaseConnection;

public class CadastroProduto extends JFrame {
    private JTextField nomeField;
    private JTextField descricaoField;
    private JTextField codigoField;
    private JComboBox<String> categoriaComboBox;
    private JComboBox<String> unidadeComboBox;
    private JTextField precoField;
    private JTextField estoqueField;
    private JTextField dataField;
    private JButton cancelarButton;
    private JButton salvarButton;

    private MainPage mainPage;

    public CadastroProduto(Produto produto, MainPage mainPage) {
        this.mainPage = mainPage;

        setTitle(produto == null ? "Cadastro de Produto" : "Edição de Produto");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        nomeField = new JTextField(20);
        descricaoField = new JTextField(20);
        codigoField = new JTextField(20);
        String[] categorias = {"Sem Categoria", "Hardware", "Periféricos", "Outros"};
        categoriaComboBox = new JComboBox<>(categorias);
        String[] unidades = {"Un", "Pç", "Kg"};
        unidadeComboBox = new JComboBox<>(unidades);
        precoField = new JTextField(20);
        estoqueField = new JTextField(20);
        dataField = new JTextField(20);
        cancelarButton = new JButton("Cancelar");
        salvarButton = new JButton(produto == null ? "Cadastrar" : "Editar");

        if (produto != null) {
            nomeField.setText(produto.getNome());
            descricaoField.setText(produto.getDescricao());
            codigoField.setText(produto.getCodigo());
            categoriaComboBox.setSelectedItem(produto.getCategoria());
            unidadeComboBox.setSelectedItem(produto.getUnidade());
            precoField.setText(String.valueOf(produto.getPreco()).replace('.', ','));
            estoqueField.setText(String.valueOf(produto.getEstoque()));
            dataField.setText(produto.getData());
        }

        Dimension buttonSize = new Dimension(120, 30);
        cancelarButton.setPreferredSize(buttonSize);
        salvarButton.setPreferredSize(buttonSize);

        JPanel panel = new JPanel(new GridLayout(9, 2));
        panel.add(new JLabel("Nome:"));
        panel.add(nomeField);
        panel.add(new JLabel("Descrição:"));
        panel.add(descricaoField);
        panel.add(new JLabel("Código:"));
        panel.add(codigoField);
        panel.add(new JLabel("Categoria:"));
        panel.add(categoriaComboBox);
        panel.add(new JLabel("Unidade:"));
        panel.add(unidadeComboBox);
        panel.add(new JLabel("Preço:"));
        panel.add(precoField);
        panel.add(new JLabel("Estoque:"));
        panel.add(estoqueField);
        panel.add(new JLabel("Data:"));
        panel.add(dataField);
        panel.add(cancelarButton);
        panel.add(salvarButton);

        add(panel);

        cancelarButton.addActionListener(e -> {
            new MainPage("Nome do Usuário");
            dispose();
        });

        salvarButton.addActionListener(e -> {
            if (produto == null) {
                cadastrarProduto();
            } else {
                editarProduto(produto);
            }
        });

        setVisible(true);
    }

    private void cadastrarProduto() {
        String nome = nomeField.getText();
        String descricao = descricaoField.getText();
        String codigo = codigoField.getText();
        String categoria = (String) categoriaComboBox.getSelectedItem();
        String unidade = (String) unidadeComboBox.getSelectedItem();
        double preco;
        int estoque;
        String data = dataField.getText();

        try {
            preco = Double.parseDouble(precoField.getText().replace(",", "."));
            estoque = Integer.parseInt(estoqueField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Preço e Estoque devem ser valores numéricos.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (nome.isEmpty() || descricao.isEmpty() || codigo.isEmpty() || data.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos os campos são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidDate(data)) {
            JOptionPane.showMessageDialog(this, "Data inválida. Use o formato dd/MM/yyyy.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO produtos (nome, descricao, codigo, categoria, unidade, preco, estoque, data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, nome);
                stmt.setString(2, descricao);
                stmt.setString(3, codigo);
                stmt.setString(4, categoria);
                stmt.setString(5, unidade);
                stmt.setDouble(6, preco);
                stmt.setInt(7, estoque);
                stmt.setDate(8, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(data))));
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Produto cadastrado com sucesso!");
            if (mainPage != null) {
                mainPage.loadProdutos();
            }
            dispose();
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao cadastrar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarProduto(Produto produto) {
        produto.setNome(nomeField.getText());
        produto.setDescricao(descricaoField.getText());
        produto.setCodigo(codigoField.getText());
        produto.setCategoria((String) categoriaComboBox.getSelectedItem());
        produto.setUnidade((String) unidadeComboBox.getSelectedItem());
        produto.setPreco(Double.parseDouble(precoField.getText().replace(",", ".")));
        produto.setEstoque(Integer.parseInt(estoqueField.getText()));
        produto.setData(dataField.getText());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "UPDATE produtos SET nome = ?, descricao = ?, codigo = ?, categoria = ?, unidade = ?, preco = ?, estoque = ?, data = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, produto.getNome());
                stmt.setString(2, produto.getDescricao());
                stmt.setString(3, produto.getCodigo());
                stmt.setString(4, produto.getCategoria());
                stmt.setString(5, produto.getUnidade());
                stmt.setDouble(6, produto.getPreco());
                stmt.setInt(7, produto.getEstoque());
                stmt.setDate(8, java.sql.Date.valueOf(new SimpleDateFormat("yyyy-MM-dd").format(new SimpleDateFormat("dd/MM/yyyy").parse(produto.getData()))));
                stmt.setInt(9, produto.getId());
                stmt.executeUpdate();
            }
            JOptionPane.showMessageDialog(this, "Produto editado com sucesso!");
            if (mainPage != null) {
                mainPage.loadProdutos();
            }
            dispose();
        } catch (SQLException | ParseException e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
        SwingUtilities.invokeLater(() -> new CadastroProduto(null, null));
    }
}