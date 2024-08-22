package com.loja.view;

import com.loja.model.Cliente;
import com.loja.model.Produto;
import com.loja.model.Servico;
import com.loja.database.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.sql.PreparedStatement;

public class MainPage extends JFrame {
    private JPanel mainPanel;
    private JButton sairButton;
    private JLabel userNameLabel;
    private JButton incluirCadastroButton;
    private JComboBox<String> cadastroComboBox;
    private JTable cadastroTable;
    private JCheckBox selecionarTodosCheckBox;
    private JTextField searchField;
    private JButton searchButton;
    private JButton editarButton;
    private JButton excluirButton;

    private static ArrayList<Cliente> clientes = new ArrayList<>();
    private static ArrayList<Produto> produtos = new ArrayList<>();
    private static ArrayList<Servico> servicos = new ArrayList<>();

    public MainPage(String userName) {
        setTitle("Página Inicial");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout());

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        String[] cadastroOptions = {"Clientes", "Produtos", "Serviços"};
        cadastroComboBox = new JComboBox<>(cadastroOptions);
        cadastroComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateCadastroTable();
            }
        });

        selecionarTodosCheckBox = new JCheckBox("Selecionar todos");
        selecionarTodosCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean isSelected = selecionarTodosCheckBox.isSelected();
                for (int i = 0; i < cadastroTable.getRowCount(); i++) {
                    cadastroTable.setValueAt(isSelected, i, 0);
                }
                updateButtonsState();
            }
        });

        searchField = new JTextField(20);
        searchButton = new JButton("Pesquisar");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchItems();
            }
        });

        sairButton = new JButton("Sair");
        sairButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new LoginView();
                dispose();
            }
        });

        incluirCadastroButton = new JButton("+ Incluir cadastro");
        incluirCadastroButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch ((String) cadastroComboBox.getSelectedItem()) {
                    case "Clientes":
                        new CadastroCliente(null, MainPage.this);
                        break;
                    case "Produtos":
                        new CadastroProduto(null, MainPage.this);
                        break;
                    case "Serviços":
                        new CadastroServico(null, MainPage.this);
                        break;
                }
            }
        });

        editarButton = new JButton("Editar");
        editarButton.setEnabled(false);
        editarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = getSelectedRow();
                if (selectedRow == -1) {
                    JOptionPane.showMessageDialog(MainPage.this, "Selecione um item para editar.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                switch ((String) cadastroComboBox.getSelectedItem()) {
                    case "Clientes":
                        Cliente cliente = clientes.get(selectedRow);
                        new CadastroCliente(cliente, MainPage.this);
                        break;
                    case "Produtos":
                        Produto produto = produtos.get(selectedRow);
                        new CadastroProduto(produto, MainPage.this);
                        break;
                    case "Serviços":
                        Servico servico = servicos.get(selectedRow);
                        new CadastroServico(servico, MainPage.this);
                        break;
                }
            }
        });

        excluirButton = new JButton("Excluir");
        excluirButton.setEnabled(false);
        excluirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] selectedRows = getSelectedRows();
                if (selectedRows.length == 0) {
                    JOptionPane.showMessageDialog(MainPage.this, "Selecione um ou mais itens para excluir.", "Erro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                switch ((String) cadastroComboBox.getSelectedItem()) {
                    case "Clientes":
                        for (int rowIndex : selectedRows) {
                            int clienteId = clientes.get(rowIndex).getId();
                            excluirClienteDoBanco(clienteId);
                        }
                        removeSelectedItems(clientes, selectedRows);
                        break;
                    case "Produtos":
                        for (int rowIndex : selectedRows) {
                            int produtoId = produtos.get(rowIndex).getId();
                            excluirProdutoDoBanco(produtoId);
                        }
                        removeSelectedItems(produtos, selectedRows);
                        break;
                    case "Serviços":
                        for (int rowIndex : selectedRows) {
                            int servicoId = servicos.get(rowIndex).getId();
                            excluirServicoDoBanco(servicoId);
                        }
                        removeSelectedItems(servicos, selectedRows);
                        break;
                }

                updateCadastroTable();
            }
        });

        userNameLabel = new JLabel("Usuário: " + userName);

        topPanel.add(cadastroComboBox);
        topPanel.add(selecionarTodosCheckBox);
        topPanel.add(searchField);
        topPanel.add(searchButton);
        actionPanel.add(userNameLabel);
        actionPanel.add(incluirCadastroButton);
        actionPanel.add(editarButton);
        actionPanel.add(excluirButton);
        actionPanel.add(sairButton);

        cadastroTable = new JTable();
        updateCadastroTable();

        mainPanel.add(topPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(cadastroTable), BorderLayout.CENTER);
        mainPanel.add(actionPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loadProdutos();
        loadClientes();
        loadServicos();

        setVisible(true);
    }

    private void updateCadastroTable() {
        String selectedType = (String) cadastroComboBox.getSelectedItem();
        DefaultTableModel tableModel = new DefaultTableModel() {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Boolean.class;
                }
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };

        switch (selectedType) {
            case "Clientes":
                tableModel.setColumnIdentifiers(new String[]{"", "Nome", "E-mail", "Celular", "Cliente desde", "Endereço"});
                for (Cliente cliente : clientes) {
                    tableModel.addRow(new Object[]{false, cliente.getNome(), cliente.getEmail(), cliente.getCelular(), cliente.getClienteDesde(), cliente.getEndereco()});
                }
                break;
            case "Produtos":
                tableModel.setColumnIdentifiers(new String[]{"", "Produto", "Descrição", "Código (SKU)", "Categoria", "Unidade", "Preço", "Estoque", "Data"});
                for (Produto produto : produtos) {
                    tableModel.addRow(new Object[]{false, produto.getNome(), produto.getDescricao(), produto.getCodigo(), produto.getCategoria(), produto.getUnidade(), produto.getPreco(), produto.getEstoque(), produto.getData()});
                }
                break;
            case "Serviços":
                tableModel.setColumnIdentifiers(new String[]{"", "Serviço", "Descrição", "Cliente", "Preço", "Data inicial", "Data final"});
                for (Servico servico : servicos) {
                    tableModel.addRow(new Object[]{false, servico.getNome(), servico.getDescricao(), servico.getCliente(), servico.getPreco(), servico.getDataInicial(), servico.getDataFinal()});
                }
                break;
        }

        cadastroTable.setModel(tableModel);
        cadastroTable.getModel().addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                updateButtonsState();
            }
        });

        updateButtonsState();
    }

    public void loadProdutos() {
        produtos.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM produtos");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Produto produto = new Produto();
                produto.setId(rs.getInt("id"));
                produto.setNome(rs.getString("nome"));
                produto.setDescricao(rs.getString("descricao"));
                produto.setCodigo(rs.getString("codigo"));
                produto.setCategoria(rs.getString("categoria"));
                produto.setUnidade(rs.getString("unidade"));
                produto.setPreco(rs.getDouble("preco"));
                produto.setEstoque(rs.getInt("estoque"));
                produto.setData(rs.getString("data"));
                produtos.add(produto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCadastroTable();
    }

    public void loadClientes() {
        clientes.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM clientes");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setNome(rs.getString("nome"));
                cliente.setEmail(rs.getString("email"));
                cliente.setCelular(rs.getString("celular"));
                cliente.setClienteDesde(rs.getString("cliente_desde"));
                cliente.setEndereco(rs.getString("endereco"));
                clientes.add(cliente);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCadastroTable();
    }

    public void loadServicos() {
        servicos.clear();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM servicos");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Servico servico = new Servico();
                servico.setId(rs.getInt("id"));
                servico.setNome(rs.getString("nome"));
                servico.setDescricao(rs.getString("descricao"));
                servico.setCliente(rs.getString("cliente"));
                servico.setPreco(rs.getDouble("preco"));
                servico.setDataInicial(rs.getString("data_inicial"));
                servico.setDataFinal(rs.getString("data_final"));
                servicos.add(servico);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        updateCadastroTable();
    }

    private void excluirClienteDoBanco(int clienteId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM clientes WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, clienteId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir cliente: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirProdutoDoBanco(int produtoId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM produtos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, produtoId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir produto: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void excluirServicoDoBanco(int servicoId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM servicos WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, servicoId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Erro ao excluir serviço: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSelectedRow() {
        for (int i = 0; i < cadastroTable.getRowCount(); i++) {
            if ((Boolean) cadastroTable.getValueAt(i, 0)) {
                return i;
            }
        }
        return -1;
    }

    private int[] getSelectedRows() {
        ArrayList<Integer> selectedRows = new ArrayList<>();
        for (int i = 0; i < cadastroTable.getRowCount(); i++) {
            if ((Boolean) cadastroTable.getValueAt(i, 0)) {
                selectedRows.add(i);
            }
        }
        return selectedRows.stream().mapToInt(i -> i).toArray();
    }

    private <T> void removeSelectedItems(ArrayList<T> items, int[] selectedRows) {
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            items.remove(selectedRows[i]);
        }
    }

    private void updateButtonsState() {
        int selectedCount = 0;
        for (int i = 0; i < cadastroTable.getRowCount(); i++) {
            if ((Boolean) cadastroTable.getValueAt(i, 0)) {
                selectedCount++;
            }
        }

        if (selectedCount == 0) {
            editarButton.setEnabled(false);
            excluirButton.setEnabled(false);
        } else if (selectedCount == 1) {
            editarButton.setEnabled(true);
            excluirButton.setEnabled(true);
        } else {
            editarButton.setEnabled(false);
            excluirButton.setEnabled(true);
        }
    }

    private void searchItems() {
        String searchText = searchField.getText().toLowerCase();
        String selectedType = (String) cadastroComboBox.getSelectedItem();
        DefaultTableModel tableModel = (DefaultTableModel) cadastroTable.getModel();
        tableModel.setRowCount(0);

        switch (selectedType) {
            case "Clientes":
                for (Cliente cliente : clientes) {
                    if (cliente.getNome().toLowerCase().contains(searchText) ||
                            cliente.getEmail().toLowerCase().contains(searchText) ||
                            cliente.getCelular().toLowerCase().contains(searchText) ||
                            cliente.getEndereco().toLowerCase().contains(searchText)) {
                        tableModel.addRow(new Object[]{false, cliente.getNome(), cliente.getEmail(), cliente.getCelular(), cliente.getClienteDesde(), cliente.getEndereco()});
                    }
                }
                break;
            case "Produtos":
                for (Produto produto : produtos) {
                    if (produto.getNome().toLowerCase().contains(searchText) ||
                            produto.getDescricao().toLowerCase().contains(searchText) ||
                            produto.getCodigo().toLowerCase().contains(searchText) ||
                            produto.getCategoria().toLowerCase().contains(searchText)) {
                        tableModel.addRow(new Object[]{false, produto.getNome(), produto.getDescricao(), produto.getCodigo(), produto.getCategoria(), produto.getUnidade(), produto.getPreco(), produto.getEstoque(), produto.getData()});
                    }
                }
                break;
            case "Serviços":
                for (Servico servico : servicos) {
                    if (servico.getNome().toLowerCase().contains(searchText) ||
                            servico.getDescricao().toLowerCase().contains(searchText)) {
                        tableModel.addRow(new Object[]{false, servico.getNome(), servico.getDescricao(), servico.getDataInicial(), servico.getDataFinal(), servico.getPreco()});
                    }
                }
                break;
        }

        updateButtonsState();
    }

    public static void addCliente(Cliente cliente) {
        clientes.add(cliente);
    }

    public static void addProduto(Produto produto) {
        produtos.add(produto);
    }

    public static void addServico(Servico servico) {
        servicos.add(servico);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainPage("Nome do Usuário"));
    }
}