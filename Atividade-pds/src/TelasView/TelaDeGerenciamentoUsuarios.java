package TelasView;

import SMModel.Pessoa;
import DAO.PessoaDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.border.EmptyBorder;

public class TelaDeGerenciamentoUsuarios extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Componentes ---
    private JTable tabelaUsuarios;
    private DefaultTableModel tableModel;
    private JButton btnCadastrar;
    private JButton btnExcluir;
    private JButton btnVoltar;

    private PessoaDAO pessoaDAO;
    private JFrame telaAnterior;

    // --- Constantes de Estilo ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_TABELA = new Color(40, 40, 40);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239); // Azul
    private static final Color COR_SUCESSO = new Color(0, 200, 83); // Verde
    private static final Color COR_ERRO = new Color(213, 0, 0); // Vermelho
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // --- Variáveis de Janela Customizada ---
    private Point initialClick;
    private JButton btnMaximizar;

    public TelaDeGerenciamentoUsuarios(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;
        this.pessoaDAO = new PessoaDAO();

        configurarJanela();
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        JPanel painelConteudo = inicializarComponentes();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);

        // Ação ao fechar a janela (pelo "X" ou "Voltar")
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                voltarParaTelaAnterior();
            }
        });
        
        // Carrega os dados na tabela ao abrir
        carregarTabela();
    }

    private void configurarJanela() {
        setUndecorated(true);
        setSize(800, 500);
        setMinimumSize(new Dimension(500, 300));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private JPanel inicializarComponentes() {
        JPanel painelConteudo = new JPanel(new BorderLayout(10, 10));
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Tabela de Usuários (CENTRO) ---
        String[] colunas = { "Nome", "CPF", "Administrador" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaUsuarios = new JTable(tableModel);
        estilizarTabela(tabelaUsuarios);

        JScrollPane scrollPane = new JScrollPane(tabelaUsuarios);
        estilizarScrollPane(scrollPane);

        JPanel painelCentral = new JPanel(new BorderLayout(0, 10));
        painelCentral.setOpaque(false);
        JLabel labelTituloTabela = new JLabel("Usuários Cadastrados:");
        labelTituloTabela.setFont(FONTE_LABEL);
        labelTituloTabela.setForeground(COR_LETRA_PRINCIPAL);
        
        painelCentral.add(labelTituloTabela, BorderLayout.NORTH);
        painelCentral.add(scrollPane, BorderLayout.CENTER);
        painelConteudo.add(painelCentral, BorderLayout.CENTER);

        // --- Painel de Botões (SUL) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelBotoes.setOpaque(false);

        btnCadastrar = new JButton("Cadastrar Novo Usuário");
        btnExcluir = new JButton("Excluir Selecionado");
        btnVoltar = new JButton("Voltar");

        styleButton(btnCadastrar);
        styleButton(btnExcluir);
        styleButton(btnVoltar);
        
        applyButtonHoverEffect(btnCadastrar, COR_SUCESSO, COR_DESTAQUE_IDLE); // Verde
        applyButtonHoverEffect(btnExcluir, COR_ERRO, COR_DESTAQUE_IDLE); // Vermelho
        applyButtonHoverEffect(btnVoltar, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE); // Azul

        btnCadastrar.addActionListener(e -> abrirCadastro());
        btnExcluir.addActionListener(e -> excluirUsuario());
        btnVoltar.addActionListener(e -> dispose()); // Dispara o windowClosing

        painelBotoes.add(btnCadastrar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnVoltar);
        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);

        return painelConteudo;
    }

    /**
     * Carrega/Recarrega os dados do DAO para a JTable.
     */
    public void carregarTabela() {
        tableModel.setRowCount(0); // Limpa a tabela
        try {
            ArrayList<Pessoa> pessoas = pessoaDAO.listarTodos();
            for (Pessoa p : pessoas) {
                tableModel.addRow(new Object[] {
                        p.getNome(),
                        p.getCpf(), // idealmente formatado
                        p.isAdm() ? "Sim" : "Não"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage(), "Erro de BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void abrirCadastro() {
        // Passa 'this' (a tela de gerenciamento) para a tela de cadastro
        TelaDeCadastroUsuario telaCadastro = new TelaDeCadastroUsuario(this);
        telaCadastro.setVisible(true);
        this.setVisible(false); // Esconde a tela de gerenciamento
    }

    private void excluirUsuario() {
        int linhaSelecionada = tabelaUsuarios.getSelectedRow();
        
        if (linhaSelecionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário na tabela para excluir.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String cpf = (String) tableModel.getValueAt(linhaSelecionada, 1);
        String nome = (String) tableModel.getValueAt(linhaSelecionada, 0);

        int confirmacao = JOptionPane.showConfirmDialog(this, 
            "Tem certeza que deseja excluir o usuário:\n" + nome + "\nCPF: " + cpf,
            "Confirmar Exclusão", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirmacao == JOptionPane.YES_OPTION) {
            try {
                // Remove do banco de dados
                pessoaDAO.excluir(cpf.replaceAll("[^0-9]", "")); // Garante CPF limpo
                // Recarrega a tabela
                carregarTabela(); 
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir usuário: " + e.getMessage(), "Erro de BD",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void voltarParaTelaAnterior() {
        telaAnterior.setVisible(true);
    }
    
    // --- Métodos de Estilização ---

    private void estilizarTabela(JTable tabela) {
        tabela.setBackground(COR_FUNDO_TABELA);
        tabela.setForeground(COR_LETRA_PRINCIPAL);
        tabela.setGridColor(COR_DESTAQUE_IDLE);
        tabela.setFont(FONTE_TABELA);
        tabela.setRowHeight(25);
        tabela.setSelectionBackground(COR_DESTAQUE_PROCESSANDO);
        tabela.setSelectionForeground(Color.WHITE);
        tabela.setBorder(null);
        tabela.getTableHeader().setBackground(COR_FUNDO);
        tabela.getTableHeader().setForeground(COR_LETRA_PRINCIPAL);
        tabela.getTableHeader().setFont(FONTE_TABELA_HEADER);
        tabela.getTableHeader().setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));
    }

    private void estilizarScrollPane(JScrollPane scrollPane) {
        scrollPane.setBackground(COR_FUNDO);
        scrollPane.getViewport().setBackground(COR_FUNDO_TABELA);
        scrollPane.setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));
    }

    // --- Métodos de Suporte da Janela (Copiados) ---

    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JPanel painelTituloIcone = new JPanel(new BorderLayout(10, 0));
        painelTituloIcone.setOpaque(false);
        // (O ícone da digital é opcional, pode remover se não for relevante aqui)
        // FingerprintIconPanel iconPanel = new FingerprintIconPanel();
        // iconPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        // painelTituloIcone.add(iconPanel, BorderLayout.WEST);
         
        JLabel tituloLabel = new JLabel("Gerenciamento de Usuários"); 
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTituloIcone.add(tituloLabel, BorderLayout.CENTER);
        barraDeTitulo.add(painelTituloIcone, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);

        JButton btnMinimizar = new JButton("\u2014");
        btnMinimizar.addActionListener(e -> setState(JFrame.ICONIFIED));
        estilizarBotaoTitulo(btnMinimizar);
         
        btnMaximizar = new JButton("\u25A1");
        btnMaximizar.addActionListener(e -> toggleMaximize());
        estilizarBotaoTitulo(btnMaximizar);
         
        JButton btnFechar = new JButton("\u00D7");
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 21));
        btnFechar.addActionListener(e -> dispose()); // Chama o windowClosing
        estilizarBotaoTitulo(btnFechar);

        applyButtonHoverEffect(btnMinimizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnMaximizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnFechar, COR_ERRO, Color.WHITE);

        painelBotoes.add(btnMinimizar);
        painelBotoes.add(btnMaximizar);
        painelBotoes.add(btnFechar);
        barraDeTitulo.add(painelBotoes, BorderLayout.EAST);

        // --- Listeners para arrastar ---
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
        painelTituloIcone.addMouseListener(titleBarAdapter);
        painelTituloIcone.addMouseMotionListener(titleBarAdapter);
        tituloLabel.addMouseListener(titleBarAdapter);
        tituloLabel.addMouseMotionListener(titleBarAdapter);

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