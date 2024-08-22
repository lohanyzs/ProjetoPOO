package com.loja.view;

import javax.swing.*;
import java.awt.*;

public abstract class CadastroBase extends JFrame {
    protected JPanel panel;
    protected JTextField nomeField, emailField, celularField, enderecoField, dataField;
    protected JButton cadastrarButton, editarButton, excluirButton, voltarButton;
    protected JList<String> itemList;
    protected DefaultListModel<String> listModel;

    public CadastroBase(String titulo, int largura, int altura) {
        super(titulo);
        setSize(largura, altura);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(new GridBagLayout());
        add(panel, BorderLayout.CENTER);

        cadastrarButton = new JButton("Cadastrar");
        editarButton = new JButton("Editar");
        excluirButton = new JButton("Excluir");
        voltarButton = new JButton("Voltar");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        JScrollPane listScrollPane = new JScrollPane(itemList);

        gbc.gridx = 0;
        gbc.gridy = 8;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        panel.add(listScrollPane, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 9;
        gbc.weighty = 0;
        panel.add(cadastrarButton, gbc);

        gbc.gridx = 1;
        panel.add(editarButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 10;
        panel.add(excluirButton, gbc);

        gbc.gridx = 1;
        panel.add(voltarButton, gbc);

        setVisible(true);
    }

    protected abstract void cadastrarItem();

    protected abstract void editarItem();

    protected abstract void excluirItem();

    protected boolean validarData(String data) {
        String dataRegex = "^(0[1-9]|[12][0-9]|3[01])[/\\-](0[1-9]|1[0-2])[/\\-](\\d{4})$";
        return data.matches(dataRegex);
    }
}