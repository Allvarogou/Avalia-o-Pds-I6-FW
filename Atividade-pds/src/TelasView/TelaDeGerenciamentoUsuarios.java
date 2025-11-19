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
    private JButton btnEditar; // NOVO BOTÃO
    private JButton btnExcluir;
    private JButton btnVoltar;

    private PessoaDAO pessoaDAO;
    private JFrame telaAnterior;

    // --- Constantes de Estilo ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_TABELA = new Color(40, 40, 40);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239);
    private static final Color COR_SUCESSO = new Color(0, 200, 83);
    private static final Color COR_ERRO = new Color(213, 0, 0);
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

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

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                voltarParaTelaAnterior();
            }
        });

        carregarTabela();
    }

    private void configurarJanela() {
        setUndecorated(true);
        setSize(900, 500); // Aumentei um pouco a largura
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private JPanel inicializarComponentes() {
        JPanel painelConteudo = new JPanel(new BorderLayout(10, 10));
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setBorder(new EmptyBorder(20, 30, 20, 30));

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

        // --- Botões ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelBotoes.setOpaque(false);

        btnCadastrar = new JButton("Novo Usuário");
        btnEditar = new JButton("Editar Selecionado"); // INSTANCIAÇÃO
        btnExcluir = new JButton("Excluir Selecionado");
        btnVoltar = new JButton("Voltar");

        styleButton(btnCadastrar);
        styleButton(btnEditar);
        styleButton(btnExcluir);
        styleButton(btnVoltar);

        applyButtonHoverEffect(btnCadastrar, COR_SUCESSO, COR_DESTAQUE_IDLE);
        applyButtonHoverEffect(btnEditar, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE); // Azul
        applyButtonHoverEffect(btnExcluir, COR_ERRO, COR_DESTAQUE_IDLE);
        applyButtonHoverEffect(btnVoltar, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE);

        btnCadastrar.addActionListener(e -> abrirCadastro(null)); // Passa null para cadastro novo
        btnEditar.addActionListener(e -> editarUsuario()); // Ação de editar
        btnExcluir.addActionListener(e -> excluirUsuario());
        btnVoltar.addActionListener(e -> dispose());

        painelBotoes.add(btnCadastrar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnExcluir);
        painelBotoes.add(btnVoltar);
        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);

        return painelConteudo;
    }

    public void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            ArrayList<Pessoa> pessoas = pessoaDAO.listarTodos();
            for (Pessoa p : pessoas) {
                tableModel.addRow(new Object[] {
                        p.getNome(),
                        p.getCpf(),
                        p.isAdm() ? "Sim" : "Não"
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método unificado para abrir a tela de cadastro
    private void abrirCadastro(Pessoa pessoaParaEditar) {
        this.setVisible(false);
        if (pessoaParaEditar == null) {
            // Novo Cadastro
            new TelaDeCadastroUsuario(this).setVisible(true);
        } else {
            // Edição (Chama o construtor novo)
            new TelaDeCadastroUsuario(this, pessoaParaEditar).setVisible(true);
        }
    }

    private void editarUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para editar.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Pega os dados da tabela
        String nome = (String) tableModel.getValueAt(linha, 0);
        String cpf = (String) tableModel.getValueAt(linha, 1);
        String isAdmStr = (String) tableModel.getValueAt(linha, 2);
        boolean isAdm = isAdmStr.equalsIgnoreCase("Sim");

        // Cria o objeto e manda para a tela de cadastro
        Pessoa p = new Pessoa(nome, cpf, isAdm);
        abrirCadastro(p);
    }

    private void excluirUsuario() {
        int linha = tabelaUsuarios.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um usuário para excluir.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String cpf = (String) tableModel.getValueAt(linha, 1);
        String nome = (String) tableModel.getValueAt(linha, 0);

        int confirm = JOptionPane.showConfirmDialog(this, "Excluir usuário " + nome + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                pessoaDAO.excluir(cpf);
                carregarTabela();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao excluir: " + e.getMessage(), "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void voltarParaTelaAnterior() {
        telaAnterior.setVisible(true);
    }

    // --- Estilização (Mantida igual) ---
    private void estilizarTabela(JTable tabela) {
        tabela.setBackground(COR_FUNDO_TABELA);
        tabela.setForeground(COR_LETRA_PRINCIPAL);
        tabela.setGridColor(COR_DESTAQUE_IDLE);
        tabela.setFont(FONTE_TABELA);
        tabela.setRowHeight(25);
        tabela.setSelectionBackground(COR_DESTAQUE_PROCESSANDO);
        tabela.setSelectionForeground(Color.WHITE);
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

    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));
        JLabel tituloLabel = new JLabel("Gerenciamento de Usuários");
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        barraDeTitulo.add(tituloLabel, BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);
        JButton btnMin = new JButton("_");
        estilizarBotaoTitulo(btnMin);
        btnMin.addActionListener(e -> setState(JFrame.ICONIFIED));
        btnMaximizar = new JButton("[]");
        estilizarBotaoTitulo(btnMaximizar);
        btnMaximizar.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH)
                setExtendedState(NORMAL);
            else
                setExtendedState(MAXIMIZED_BOTH);
        });
        JButton btnClose = new JButton("X");
        estilizarBotaoTitulo(btnClose);
        btnClose.addActionListener(e -> dispose());
        applyButtonHoverEffect(btnMin, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnMaximizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnClose, COR_ERRO, Color.WHITE);
        painelBotoes.add(btnMin);
        painelBotoes.add(btnMaximizar);
        painelBotoes.add(btnClose);
        barraDeTitulo.add(painelBotoes, BorderLayout.EAST);

        MouseAdapter drag = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }

            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != MAXIMIZED_BOTH) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    setLocation(thisX + (e.getX() - initialClick.x), thisY + (e.getY() - initialClick.y));
                }
            }
        };
        barraDeTitulo.addMouseListener(drag);
        barraDeTitulo.addMouseMotionListener(drag);
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
            public void mouseEntered(MouseEvent e) {
                button.setForeground(hoverColor);
            }

            public void mouseExited(MouseEvent e) {
                button.setForeground(defaultColor);
            }
        });
    }
}