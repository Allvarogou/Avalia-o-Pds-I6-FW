package TelasView;

import SMModel.Produto;
import SMModel.Venda;
import DAO.VendaDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.ArrayList;

public class TelaHistoricoVendas extends JFrame {

    private static final long serialVersionUID = 1L;
    private VendaDAO vendaDAO = new VendaDAO();
    private JFrame telaAnterior;

    // Componentes
    private JTable tabelaVendas;
    private DefaultTableModel tableModel;
    private JButton btnVerNota;
    private JButton btnVoltar;

    // Estilo (Dark Mode)
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_TABELA = new Color(40, 40, 40);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // Variáveis de Janela
    private Point initialClick;
    private JButton btnMaximizar;

    public TelaHistoricoVendas(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;

        configurarJanela();
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        // Atualiza título
        ((JLabel) ((JPanel) barraDeTitulo.getComponent(0)).getComponent(0)).setText("Histórico de Vendas");

        JPanel painelConteudo = inicializarComponentes();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);

        carregarHistorico();
    }

    private void configurarJanela() {
        setUndecorated(true);
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private JPanel inicializarComponentes() {
        JPanel painel = new JPanel(new BorderLayout(10, 10));
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(20, 30, 20, 30));

        // --- Tabela ---
        String[] colunas = { "ID Venda", "Data", "Cliente", "Total (R$)" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaVendas = new JTable(tableModel);
        estilizarTabela(tabelaVendas);

        JScrollPane scrollPane = new JScrollPane(tabelaVendas);
        estilizarScrollPane(scrollPane);

        painel.add(scrollPane, BorderLayout.CENTER);

        // --- Botões ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        painelBotoes.setOpaque(false);

        btnVerNota = new JButton("Abrir Nota Fiscal");
        styleButton(btnVerNota);
        applyButtonHoverEffect(btnVerNota, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE);

        btnVoltar = new JButton("Voltar");
        styleButton(btnVoltar);
        applyButtonHoverEffect(btnVoltar, new Color(213, 0, 0), COR_DESTAQUE_IDLE); // Vermelho

        painelBotoes.add(btnVerNota);
        painelBotoes.add(btnVoltar);

        painel.add(painelBotoes, BorderLayout.SOUTH);

        // Ações
        btnVerNota.addActionListener(e -> exibirNotaFiscalSelecionada());
        btnVoltar.addActionListener(e -> {
            this.dispose();
            if (telaAnterior != null)
                telaAnterior.setVisible(true);
        });

        return painel;
    }

    private void carregarHistorico() {
        tableModel.setRowCount(0);
        try {
            ArrayList<Venda> vendas = vendaDAO.listarTodasVendas();
            for (Venda v : vendas) {
                tableModel.addRow(new Object[] {
                        v.getId(),
                        v.getDataVenda(),
                        v.getNomeCliente(),
                        String.format("%.2f", v.getTotal())
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar histórico: " + e.getMessage());
        }
    }

    private void exibirNotaFiscalSelecionada() {
        int linha = tabelaVendas.getSelectedRow();
        if (linha == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma venda na lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idVenda = (int) tableModel.getValueAt(linha, 0);
        String data = (String) tableModel.getValueAt(linha, 1);
        String cliente = (String) tableModel.getValueAt(linha, 2);
        String total = (String) tableModel.getValueAt(linha, 3);

        try {
            ArrayList<Produto> itens = vendaDAO.listarItensDaVenda(idVenda);

            StringBuilder nota = new StringBuilder();
            nota.append("============= NOTA FISCAL (2ª VIA) =============\n");
            nota.append("ID Venda: ").append(idVenda).append("\n");
            nota.append("Data: ").append(data).append("\n");
            nota.append("Cliente: ").append(cliente).append("\n");
            nota.append("------------------------------------------------\n");
            nota.append("ITENS:\n");

            for (Produto p : itens) {
                double subtotal = p.getPreco() * p.getQuantidade();
                nota.append(String.format("- %s\n  %d x R$ %.2f = R$ %.2f\n",
                        p.getProduto(), p.getQuantidade(), p.getPreco(), subtotal));
            }

            nota.append("------------------------------------------------\n");
            nota.append("TOTAL PAGO: R$ ").append(total).append("\n");
            nota.append("================================================");

            JTextArea areaNota = new JTextArea(nota.toString());
            areaNota.setFont(new Font("Monospaced", Font.PLAIN, 12));
            areaNota.setEditable(false);

            JOptionPane.showMessageDialog(this, new JScrollPane(areaNota), "Visualizar Nota Fiscal",
                    JOptionPane.PLAIN_MESSAGE);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar itens: " + e.getMessage());
        }
    }

    // --- Métodos de Estilo (Reutilizados) ---
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

    private void estilizarScrollPane(JScrollPane scroll) {
        scroll.setBackground(COR_FUNDO);
        scroll.getViewport().setBackground(COR_FUNDO_TABELA);
        scroll.setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));
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

    // --- Janela Customizada ---
    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));
        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTitulo.setOpaque(false);
        JLabel tituloLabel = new JLabel("Titulo");
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTitulo.add(tituloLabel);
        barraDeTitulo.add(painelTitulo, BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);

        JButton btnMin = new JButton("_");
        styleButton(btnMin);
        btnMin.addActionListener(e -> setState(ICONIFIED));

        btnMaximizar = new JButton("[]");
        styleButton(btnMaximizar);
        btnMaximizar.addActionListener(e -> {
            if (getExtendedState() == MAXIMIZED_BOTH)
                setExtendedState(NORMAL);
            else
                setExtendedState(MAXIMIZED_BOTH);
        });

        JButton btnClose = new JButton("X");
        styleButton(btnClose);
        btnClose.addActionListener(e -> {
            this.dispose();
            if (telaAnterior != null)
                telaAnterior.setVisible(true);
        });

        applyButtonHoverEffect(btnClose, Color.RED, COR_DESTAQUE_IDLE);

        painelBotoes.add(btnMin);
        painelBotoes.add(btnMaximizar);
        painelBotoes.add(btnClose);
        barraDeTitulo.add(painelBotoes, BorderLayout.EAST);

        // Arrastar
        MouseAdapter drag = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
            }

            public void mouseDragged(MouseEvent e) {
                if (getExtendedState() != MAXIMIZED_BOTH) {
                    int thisX = getLocation().x;
                    int thisY = getLocation().y;
                    int xMoved = thisX + (e.getX() - initialClick.x);
                    int yMoved = thisY + (e.getY() - initialClick.y);
                    setLocation(xMoved, yMoved);
                }
            }
        };
        barraDeTitulo.addMouseListener(drag);
        barraDeTitulo.addMouseMotionListener(drag);

        return barraDeTitulo;
    }
}