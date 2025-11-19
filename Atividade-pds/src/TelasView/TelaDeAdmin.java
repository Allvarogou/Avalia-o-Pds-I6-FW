package TelasView;

import SMModel.Mercado;
import SMModel.Produto;
import DAO.ProdutoDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Tela para o administrador visualizar os produtos e navegar para telas de
 * gerenciamento.
 * Faz parte da camada VIEW.
 * (Versão reestilizada com o tema escuro e corrigida)
 */
public class TelaDeAdmin extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Campos Funcionais Originais ---
    // (O mercado não está sendo usado diretamente aqui, mas mantido caso precise
    // repassar)
    @SuppressWarnings("unused")
    private Mercado mercado;
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private JFrame telaAnterior;

    // --- Componentes de Interface ---
    private JTable tabelaProdutos;
    private DefaultTableModel tableModel;

    // Botões
    private JButton btnCadastrarUsuario;
    private JButton btnGerenciarProdutos;
    private JButton btnHistorico; // Novo botão de histórico
    private JButton btnVoltar;

    // --- Constantes de Estilo (Copiadas) ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_TABELA = new Color(40, 40, 40);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239); // Azul
    private static final Color COR_ERRO = new Color(213, 0, 0); // Vermelho
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // --- Variáveis para Janela Customizada ---
    private Point initialClick;
    private JButton btnMaximizar;

    // Construtor: recebe o Modelo principal e a tela que a chamou
    public TelaDeAdmin(Mercado mercado, JFrame telaAnterior) {
        this.mercado = mercado;
        this.telaAnterior = telaAnterior;
        this.produtoDAO = new ProdutoDAO();

        // 1. Configuração da Janela
        configurarJanela();

        // 2. Barra de Título Customizada
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        // Ajusta o título para esta tela
        ((JLabel) ((JPanel) barraDeTitulo.getComponent(0)).getComponent(0)).setText("Painel Administrativo");

        // 3. Painel de Conteúdo
        JPanel painelConteudo = inicializarComponentes();

        // 4. Montagem final da Janela
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);

        // 5. Adição do MouseListener para arrastar
        MouseAdapter draggableAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    initialClick = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    int xMoved = thisX + (e.getX() - initialClick.x);
                    int yMoved = thisY + (e.getY() - initialClick.y);
                    setLocation(xMoved, yMoved);
                }
            }
        };

        painelConteudo.addMouseListener(draggableAdapter);
        painelConteudo.addMouseMotionListener(draggableAdapter);

        // 6. Ação ao fechar a janela
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                telaAnterior.setVisible(true);
            }
        });

        // 7. Carregamento dos dados
        carregarTabelaProdutos();
    }

    /**
     * Inicializa e estiliza todos os componentes da tela.
     */
    private JPanel inicializarComponentes() {
        // Painel principal de conteúdo
        JPanel painelConteudo = new JPanel(new BorderLayout(10, 10));
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Tabela de Produtos (CENTRO) ---
        String[] colunas = { "Nome", "Preço Venda", "Preço Compra", "Quantidade" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tabela não editável
            }
        };
        tabelaProdutos = new JTable(tableModel);

        // Aplicando estilo à Tabela
        tabelaProdutos.setBackground(COR_FUNDO_TABELA);
        tabelaProdutos.setForeground(COR_LETRA_PRINCIPAL);
        tabelaProdutos.setGridColor(COR_DESTAQUE_IDLE);
        tabelaProdutos.setFont(FONTE_TABELA);
        tabelaProdutos.setRowHeight(25);
        tabelaProdutos.setSelectionBackground(COR_DESTAQUE_PROCESSANDO);
        tabelaProdutos.setSelectionForeground(Color.WHITE);
        tabelaProdutos.setBorder(null);

        // Aplicando estilo ao Header da Tabela
        tabelaProdutos.getTableHeader().setBackground(COR_FUNDO);
        tabelaProdutos.getTableHeader().setForeground(COR_LETRA_PRINCIPAL);
        tabelaProdutos.getTableHeader().setFont(FONTE_TABELA_HEADER);
        tabelaProdutos.getTableHeader().setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));

        // Aplicando estilo ao ScrollPane
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        scrollPane.setBackground(COR_FUNDO);
        scrollPane.getViewport().setBackground(COR_FUNDO_TABELA);
        scrollPane.setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));
        scrollPane.getVerticalScrollBar().setBackground(COR_FUNDO);

        // Painel central para Título da Tabela + Tabela
        JPanel painelCentral = new JPanel(new BorderLayout(0, 10));
        painelCentral.setOpaque(false); // Fundo transparente

        JLabel labelTituloTabela = new JLabel("Estoque Atual:");
        labelTituloTabela.setFont(FONTE_LABEL);
        labelTituloTabela.setForeground(COR_LETRA_PRINCIPAL);

        painelCentral.add(labelTituloTabela, BorderLayout.NORTH);
        painelCentral.add(scrollPane, BorderLayout.CENTER);

        painelConteudo.add(painelCentral, BorderLayout.CENTER);

        // --- Painel de Botões de Navegação (SUL) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelBotoes.setOpaque(false); // Fundo transparente

        // 1. INSTANCIAÇÃO DOS BOTÕES (Correção do NullPointer)
        btnGerenciarProdutos = new JButton("Gerenciar Produtos");
        btnHistorico = new JButton("Histórico de Vendas");
        btnCadastrarUsuario = new JButton("Gerenciar Usuários"); // Nome ajustado para refletir a nova tela
        btnVoltar = new JButton("Voltar ao Login");

        // 2. ESTILIZAÇÃO
        styleButton(btnGerenciarProdutos);
        styleButton(btnHistorico);
        styleButton(btnCadastrarUsuario);
        styleButton(btnVoltar);

        // 3. HOVER EFFECTS
        applyButtonHoverEffect(btnGerenciarProdutos, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE);
        applyButtonHoverEffect(btnHistorico, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE);
        applyButtonHoverEffect(btnCadastrarUsuario, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE);
        applyButtonHoverEffect(btnVoltar, COR_ERRO, COR_DESTAQUE_IDLE); // Botão de sair/voltar em vermelho

        // 4. LISTENERS (Ações)
        btnGerenciarProdutos.addActionListener(e -> abrirGerenciamentoProdutos());
        btnHistorico.addActionListener(e -> abrirHistoricoVendas());
        btnCadastrarUsuario.addActionListener(e -> abrirCadastroUsuario());
        btnVoltar.addActionListener(e -> voltar());

        // 5. ADICIONAR AO PAINEL
        painelBotoes.add(btnGerenciarProdutos);
        painelBotoes.add(btnHistorico);
        painelBotoes.add(btnCadastrarUsuario);
        painelBotoes.add(btnVoltar);

        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);

        return painelConteudo;
    }

    // --- Métodos de Lógica (Interagindo com DAO) ---

    public void carregarTabelaProdutos() {
        tableModel.setRowCount(0);
        try {
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
            e.printStackTrace();
        }
    }

    // --- Métodos de Navegação ---

    private void abrirGerenciamentoProdutos() {
        this.setVisible(false);
        new TelaDeGerenciamentoProdutos(this).setVisible(true);
    }

    private void abrirHistoricoVendas() {
        this.setVisible(false);
        new TelaHistoricoVendas(this).setVisible(true);
    }

    private void abrirCadastroUsuario() {
        this.setVisible(false);
        // Agora chama a tela de gerenciamento de usuários, não o cadastro direto
        new TelaDeGerenciamentoUsuarios(this).setVisible(true);
    }

    private void voltar() {
        this.dispose();
        this.telaAnterior.setVisible(true);
    }

    // --- Métodos de Suporte da Janela ---

    private void configurarJanela() {
        setUndecorated(true);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        // IMPORTANTE: Mantém DISPOSE_ON_CLOSE para o WindowListener funcionar
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTitulo.setOpaque(false);

        JLabel tituloLabel = new JLabel("Painel Administrativo");
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTitulo.add(tituloLabel);

        barraDeTitulo.add(painelTitulo, BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);

        JButton btnMinimizar = new JButton("\u2014");
        estilizarBotaoTitulo(btnMinimizar);
        btnMinimizar.addActionListener(e -> setState(JFrame.ICONIFIED));

        JButton btnMaximizar = new JButton("\u25A1");
        estilizarBotaoTitulo(btnMaximizar);
        btnMaximizar.addActionListener(e -> toggleMaximize());

        JButton btnFechar = new JButton("\u00D7");
        estilizarBotaoTitulo(btnFechar);
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 21));
        // IMPORTANTE: Chama dispose() para ativar o WindowListener original
        btnFechar.addActionListener(e -> dispose());

        applyButtonHoverEffect(btnMinimizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnMaximizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnFechar, COR_ERRO, Color.WHITE); // Vermelho no hover

        painelBotoes.add(btnMinimizar);
        painelBotoes.add(btnMaximizar);
        painelBotoes.add(btnFechar);
        barraDeTitulo.add(painelBotoes, BorderLayout.EAST);

        // --- Listeners para arrastar e maximizar/restaurar ---
        MouseAdapter titleBarAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    initialClick = e.getPoint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    toggleMaximize();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    int xMoved = thisX + (e.getX() - initialClick.x);
                    int yMoved = thisY + (e.getY() - initialClick.y);
                    setLocation(xMoved, yMoved);
                }
            }
        };

        barraDeTitulo.addMouseListener(titleBarAdapter);
        barraDeTitulo.addMouseMotionListener(titleBarAdapter);
        painelTitulo.addMouseListener(titleBarAdapter);
        painelTitulo.addMouseMotionListener(titleBarAdapter);

        this.addWindowStateListener(e -> {
            if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                btnMaximizar.setText("\u29C9");
            } else {
                btnMaximizar.setText("\u25A1");
            }
        });

        return barraDeTitulo;
    }

    private void estilizarBotaoTitulo(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    }

    /**
     * Aplica o estilo padrão "flat" da aplicação a um JButton.
     */
    private void styleButton(JButton button) {
        button.setFont(FONTE_BOTAO);
        button.setForeground(COR_DESTAQUE_IDLE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    private void applyButtonHoverEffect(JButton button, Color hoverColor, Color defaultColor) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setForeground(hoverColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setForeground(defaultColor);
            }
        });
    }

    private void toggleMaximize() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
}