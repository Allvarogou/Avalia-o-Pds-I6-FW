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
 * Tela para o administrador visualizar os produtos e navegar para telas de gerenciamento.
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
    
    private JButton btnCadastrarUsuario; 
    private JButton btnGerenciarProdutos; // Novo botão para a tela de CRUD de Produtos
    private JButton btnVoltar;

    // Construtor: recebe o Modelo principal e a tela que a chamou
    public TelaDeAdmin(Mercado mercado, JFrame telaAnterior) {
        this.mercado = mercado;
        this.telaAnterior = telaAnterior;

        setTitle("Área Administrativa - Menu Principal e Visualização");
        setSize(850, 500); 
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
        carregarTabelaProdutos(); // Carrega os produtos na tabela de visualização
    }

    private void inicializarComponentes() {
        setLayout(new BorderLayout(10, 10));

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
        
        // Adiciona um título descritivo para a tabela (Visualização)
        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.add(new JLabel("Estoque Atual:"), BorderLayout.NORTH);
        painelCentral.add(scrollPane, BorderLayout.CENTER);

        add(painelCentral, BorderLayout.CENTER);

        
        // --- Painel de Botões de Navegação (SUL) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        btnGerenciarProdutos = new JButton("Gerenciar Produtos (CRUD)"); // Novo botão
        btnCadastrarUsuario = new JButton("Cadastrar Usuário"); 
        btnVoltar = new JButton("Voltar ao Login");

        // Adiciona ações aos botões
        btnGerenciarProdutos.addActionListener(e -> abrirGerenciamentoProdutos()); // Nova Ação
        btnCadastrarUsuario.addActionListener(e -> abrirCadastroUsuario());
        btnVoltar.addActionListener(e -> voltar());

        painelBotoes.add(btnGerenciarProdutos); // Adicionado o botão de gerenciamento
        painelBotoes.add(btnCadastrarUsuario); 
        painelBotoes.add(btnVoltar);

        add(painelBotoes, BorderLayout.SOUTH);
    }

    // --- Métodos de Lógica (Interagindo com DAO) ---

    // Método público para que a TelaDeGerenciamentoProdutos possa forçar a atualização
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
            e.printStackTrace(); // Adiciona o stack trace
        }
    }
    
    // --- Métodos de Navegação ---
    
    private void abrirGerenciamentoProdutos() {
        this.setVisible(false); // Esconde a tela atual
        new TelaDeGerenciamentoProdutos(this).setVisible(true); // Abre a nova tela de CRUD
    }

    private void abrirCadastroUsuario() {
        this.dispose();
        new TelaDeCadastroUsuario(this).setVisible(true);
    }

    private void voltar() {
        this.dispose();
        this.telaAnterior.setVisible(true);
    }
}