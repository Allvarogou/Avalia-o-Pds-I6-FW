package TelasView;

import SMModel.Produto;
import DAO.ProdutoDAO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Tela dedicada para o administrador gerenciar (Adicionar, Editar, Remover)
 * os produtos do mercado.
 */
public class TelaDeGerenciamentoProdutos extends JFrame {

    private static final long serialVersionUID = 1L;

    private ProdutoDAO produtoDAO = new ProdutoDAO(); 
    private JFrame telaAnterior; 

    // Componentes de Interface
    private JTable tabelaProdutos;
    private DefaultTableModel tableModel;
    private JTextField campoNome, campoPreco, campoPrecoCompra, campoQuantidade;
    private JButton btnAdicionar, btnEditar, btnRemover, btnVoltar;

    // Construtor: recebe a tela que a chamou (TelaDeAdmin)
    public TelaDeGerenciamentoProdutos(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;

        setTitle("Gerenciamento de Produtos - CRUD");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ação ao fechar a janela: reabre a tela anterior (TelaDeAdmin)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                // Ao fechar, garante que a lista na TelaDeAdmin está atualizada
                if (telaAnterior instanceof TelaDeAdmin) {
                    ((TelaDeAdmin) telaAnterior).carregarTabelaProdutos();
                }
                telaAnterior.setVisible(true);
            }
        });

        inicializarComponentes();
        carregarTabelaProdutos();
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

        // --- Painel de Formulário (NORTE) ---
        JPanel painelFormulario = new JPanel(new GridLayout(2, 4, 10, 10));
        painelFormulario.setBorder(BorderFactory.createTitledBorder("Dados do Produto"));


        campoNome = new JTextField(10);
        campoPreco = new JTextField(10);
        campoPrecoCompra = new JTextField(10);
        campoQuantidade = new JTextField(10);

        painelFormulario.add(new JLabel("Nome do Produto:"));
        painelFormulario.add(new JLabel("Preço de Venda:"));
        painelFormulario.add(new JLabel("Preço de Compra:"));
        painelFormulario.add(new JLabel("Quantidade em Estoque:"));

        painelFormulario.add(campoNome);
        painelFormulario.add(campoPreco);
        painelFormulario.add(campoPrecoCompra);
        painelFormulario.add(campoQuantidade);

        add(painelFormulario, BorderLayout.NORTH);

        // --- Tabela de Produtos (CENTRO) ---
        String[] colunas = { "Nome", "Preço Venda", "Preço Compra", "Quantidade" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaProdutos = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        add(scrollPane, BorderLayout.CENTER);

        // Ação para carregar os campos ao selecionar uma linha
        tabelaProdutos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaProdutos.getSelectedRow() != -1) {
                int linhaSelecionada = tabelaProdutos.getSelectedRow();
                campoNome.setText(tableModel.getValueAt(linhaSelecionada, 0).toString());
                campoPreco.setText(tableModel.getValueAt(linhaSelecionada, 1).toString());
                campoPrecoCompra.setText(tableModel.getValueAt(linhaSelecionada, 2).toString());
                campoQuantidade.setText(tableModel.getValueAt(linhaSelecionada, 3).toString());
            }
        });

        
        // --- Painel de Botões (SUL) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnAdicionar = new JButton("Adicionar Novo");
        btnEditar = new JButton("Salvar Edição");
        btnRemover = new JButton("Remover Produto");
        btnVoltar = new JButton("Voltar");

        // Adiciona ações aos botões
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnEditar.addActionListener(e -> editarProduto());
        btnRemover.addActionListener(e -> removerProduto());
        btnVoltar.addActionListener(e -> voltar());

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnVoltar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    // --- Métodos de Lógica (Interagindo com DAO) ---

    public void carregarTabelaProdutos() {
        tableModel.setRowCount(0);
        try {
            // Busca os dados diretamente do DAO
            ArrayList<Produto> produtos = produtoDAO.listarTodos();
            for (Produto produto : produtos) {
                tableModel.addRow(new Object[] {
                        produto.getProduto(),
                        produto.getPreco(),
                        produto.getPrecoCompra(),
                        produto.getQuantidade()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage(), "Erro de BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarProduto() {
        try {
            Produto novoProduto = criarProdutoDosCampos();
            produtoDAO.salvar(novoProduto); 
            
            JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarTabelaProdutos();
            limparCampos();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no BD: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Adiciona o stack trace
        }
    }

    private void editarProduto() {
        int linhaSelecionada = tabelaProdutos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para editar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            String nomeAntigo = tableModel.getValueAt(linhaSelecionada, 0).toString();
            Produto produtoNovo = criarProdutoDosCampos();

            produtoDAO.editar(nomeAntigo, produtoNovo); 

            JOptionPane.showMessageDialog(this, "Produto editado com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarTabelaProdutos();
            limparCampos();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar no BD: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace(); // Adiciona o stack trace
        }
    }

    private void removerProduto() {
        int linhaSelecionada = tabelaProdutos.getSelectedRow();
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um produto para remover.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String nomeProduto = tableModel.getValueAt(linhaSelecionada, 0).toString();
        int confirmacao = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja remover o produto: " + nomeProduto + "?", "Confirmação de Remoção",
                JOptionPane.YES_NO_OPTION);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                produtoDAO.remover(nomeProduto); 

                JOptionPane.showMessageDialog(this, "Produto removido com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarTabelaProdutos();
                limparCampos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao remover no BD: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
                e.printStackTrace(); // Adiciona o stack trace
            }
        }
    }
    
    private void voltar() {
        this.dispose();
        // A lógica de reabrir a tela anterior já está no WindowListener, mas a chamada direta
        // garante que a ação do botão tenha prioridade caso o listener não seja chamado imediatamente
    }

    private Produto criarProdutoDosCampos() throws IllegalArgumentException {
        
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }

        float precoVenda, precoCompra;
        int quantidade;

        try {
            // Conversão com substituição de vírgula por ponto (caso o usuário use vírgula)
            precoVenda = Float.parseFloat(campoPreco.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço de venda inválido.");
        }

        try {
            // Conversão com substituição de vírgula por ponto
            precoCompra = Float.parseFloat(campoPrecoCompra.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço de compra inválido.");
        }

        try {
            quantidade = Integer.parseInt(campoQuantidade.getText().trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Quantidade em estoque inválida.");
        }

        
        return new Produto(nome, precoVenda, precoCompra, quantidade);
    }

    private void limparCampos() {
        campoNome.setText("");
        campoPreco.setText("");
        campoPrecoCompra.setText("");
        campoQuantidade.setText("");
        tabelaProdutos.clearSelection();
    }
}