package TelasView;

import SMModel.Mercado;
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
 * Tela para o administrador gerenciar os produtos do mercado.
 * Faz parte da camada VIEW.
 */
public class TelaDeAdmin extends JFrame {

    private static final long serialVersionUID = 1L;

    // OBS: O objeto mercado é mantido apenas para ser passado para outras telas
    private Mercado mercado; 
    private ProdutoDAO produtoDAO = new ProdutoDAO(); 
    private JFrame telaAnterior; 

    // Componentes de Interface
    private JTable tabelaProdutos;
    private DefaultTableModel tableModel;
    private JTextField campoNome, campoPreco, campoPrecoCompra, campoQuantidade;
    private JButton btnAdicionar, btnEditar, btnRemover, btnVoltar;
    private JButton btnCadastrarUsuario; // Botão para abrir a tela de cadastro de usuários

    // Construtor: recebe o Modelo principal e a tela que a chamou
    public TelaDeAdmin(Mercado mercado, JFrame telaAnterior) {
        this.mercado = mercado;
        this.telaAnterior = telaAnterior;

        setTitle("Área Administrativa - Gerenciamento de Produtos");
        setSize(850, 600); // Aumentei a largura para acomodar o botão de cadastro de usuário
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ação ao fechar a janela: reabre a tela anterior (Identificação)
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
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

        
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnAdicionar = new JButton("Adicionar");
        btnEditar = new JButton("Editar");
        btnRemover = new JButton("Remover");
        btnVoltar = new JButton("Voltar ao Login");
        btnCadastrarUsuario = new JButton("Cadastrar Usuário"); // Novo Botão

        // Adiciona ações aos botões
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnEditar.addActionListener(e -> editarProduto());
        btnRemover.addActionListener(e -> removerProduto());
        btnVoltar.addActionListener(e -> voltar());
        btnCadastrarUsuario.addActionListener(e -> abrirCadastroUsuario());

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnCadastrarUsuario); // Adicionado o botão de cadastro de usuário
        painelBotoes.add(btnVoltar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    // --- Métodos de Lógica (Interagindo com DAO) ---

    private void carregarTabelaProdutos() {
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
            produtoDAO.salvar(novoProduto); // Chama o DAO
            
            JOptionPane.showMessageDialog(this, "Produto adicionado com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarTabelaProdutos();
            limparCampos();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar no BD: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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

            produtoDAO.editar(nomeAntigo, produtoNovo); // Edita no BD

            JOptionPane.showMessageDialog(this, "Produto editado com sucesso!", "Sucesso",
                    JOptionPane.INFORMATION_MESSAGE);
            carregarTabelaProdutos();
            limparCampos();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erro de Validação", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao editar no BD: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
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
                produtoDAO.remover(nomeProduto); // Remove no BD

                JOptionPane.showMessageDialog(this, "Produto removido com sucesso!", "Sucesso",
                        JOptionPane.INFORMATION_MESSAGE);
                carregarTabelaProdutos();
                limparCampos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao remover no BD: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // --- Métodos de Navegação e Auxiliares ---
    
    private void abrirCadastroUsuario() {
        // Abre a tela de cadastro de usuário
        this.dispose();
        new TelaDeCadastroUsuario(this).setVisible(true);
    }

    private void voltar() {
        this.dispose();
        this.telaAnterior.setVisible(true);
    }

    private Produto criarProdutoDosCampos() throws IllegalArgumentException {
        
        String nome = campoNome.getText().trim();
        if (nome.isEmpty()) {
            throw new IllegalArgumentException("O nome do produto é obrigatório.");
        }

        float precoVenda, precoCompra;
        int quantidade;

        try {
            precoVenda = Float.parseFloat(campoPreco.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Preço de venda inválido.");
        }

        try {
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