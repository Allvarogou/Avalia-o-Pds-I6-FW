package TelasView;

import SMModel.Carrinho;
import SMModel.Produto;
import SMModel.Mercado;
import SMModel.Pessoa;
import DAO.CarrinhoDAO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class TelaDeCompra extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Lógica e Dados ---
    private CarrinhoDAO carrinhoDAO = new CarrinhoDAO();
    private Mercado supermercado;
    private String nomeCliente;
    private String cpfCliente;
    private Carrinho carrinho;
    private JFrame telaAnterior;

    // --- Componentes ---
    private DefaultListModel<Produto> listModelProdutos;
    private JList<Produto> listaProdutos;
    private DefaultListModel<Produto> listModelCarrinho;
    private JList<Produto> listaCarrinho;
    private JLabel labelTotal;
    private JSpinner spinnerQuantidade;

    // --- Constantes de Estilo (Padrão do Projeto) ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_LISTA = new Color(45, 45, 45);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239); // Azul
    private static final Color COR_SUCESSO = new Color(0, 200, 83); // Verde
    private static final Color COR_ERRO = new Color(213, 0, 0); // Vermelho
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font FONTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_TOTAL = new Font("Segoe UI", Font.BOLD, 20);

    // --- Variáveis para Janela Customizada ---
    private Point initialClick;
    private JButton btnMaximizar;

    public TelaDeCompra(Mercado supermercado, String nomeCliente, String cpfCliente, JFrame telaAnterior) {
        this.supermercado = supermercado;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
        this.carrinho = new Carrinho();
        this.telaAnterior = telaAnterior;

        configurarJanela();

        // Barra de Título
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        // Atualiza o título na barra
        ((JLabel) ((JPanel) barraDeTitulo.getComponent(0)).getComponent(0)).setText("Caixa - Cliente: " + nomeCliente);

        // Painel de Conteúdo
        JPanel painelConteudo = inicializarComponentes();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);

        // Habilita arrastar a janela pelo painel de conteúdo também
        adicionarListenerArrastar(painelConteudo);

        // Carregar produtos no início
        carregarProdutos();
    }

    private void configurarJanela() {
        setUndecorated(true);
        setSize(900, 600);
        setMinimumSize(new Dimension(800, 500));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private JPanel inicializarComponentes() {
        JPanel painelPrincipal = new JPanel(new BorderLayout(10, 10));
        painelPrincipal.setBackground(COR_FUNDO);
        painelPrincipal.setBorder(new EmptyBorder(10, 20, 20, 20));

        // --- Listas (SplitPane no Centro) ---

        // Lista de Produtos (Esquerda)
        listModelProdutos = new DefaultListModel<>();
        listaProdutos = criarListaEstilizada(listModelProdutos);
        JScrollPane scrollProdutos = criarScrollPaneEstilizado(listaProdutos, "Produtos Disponíveis (Estoque)");

        // Lista do Carrinho (Direita)
        listModelCarrinho = new DefaultListModel<>();
        listaCarrinho = criarListaEstilizada(listModelCarrinho);
        JScrollPane scrollCarrinho = criarScrollPaneEstilizado(listaCarrinho, "Carrinho de Compras");

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollProdutos, scrollCarrinho);
        splitPane.setResizeWeight(0.5);
        splitPane.setOpaque(false); // Transparente para pegar a cor de fundo
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        // Hack básico para escurecer o divisor (nem sempre funciona em todos LAFs, mas
        // ajuda)
        splitPane.setBackground(COR_FUNDO);

        painelPrincipal.add(splitPane, BorderLayout.CENTER);

        // --- Painel de Controle (Sul) ---
        JPanel painelControle = new JPanel(new BorderLayout(0, 15));
        painelControle.setBackground(COR_FUNDO);
        painelControle.setBorder(new EmptyBorder(10, 0, 0, 0));

        // 1. Controles de Quantidade e Adição
        JPanel painelAcoesCima = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        painelAcoesCima.setOpaque(false);

        JLabel labelQtd = new JLabel("Quantidade:");
        labelQtd.setFont(FONTE_NORMAL);
        labelQtd.setForeground(COR_LETRA_PRINCIPAL);

        spinnerQuantidade = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        estilizarSpinner(spinnerQuantidade); // Método auxiliar para escurecer o spinner
        spinnerQuantidade.setPreferredSize(new Dimension(70, 30));

        JButton btnAdicionar = new JButton("Adicionar ao Carrinho >>");
        styleButton(btnAdicionar);
        applyButtonHoverEffect(btnAdicionar, COR_SUCESSO, COR_DESTAQUE_IDLE); // Verde

        JButton btnRemover = new JButton("<< Remover Item");
        styleButton(btnRemover);
        applyButtonHoverEffect(btnRemover, COR_ERRO, COR_DESTAQUE_IDLE); // Vermelho

        painelAcoesCima.add(labelQtd);
        painelAcoesCima.add(spinnerQuantidade);
        painelAcoesCima.add(btnAdicionar);
        painelAcoesCima.add(Box.createHorizontalStrut(20)); // Espaçamento
        painelAcoesCima.add(btnRemover);

        // 2. Total e Finalização
        JPanel painelAcoesBaixo = new JPanel(new BorderLayout());
        painelAcoesBaixo.setOpaque(false);

        labelTotal = new JLabel("Total: R$ 0.00", SwingConstants.CENTER);
        labelTotal.setFont(FONTE_TOTAL);
        labelTotal.setForeground(COR_SUCESSO); // Total em verde

        JPanel painelBotoesFinais = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        painelBotoesFinais.setOpaque(false);

        JButton btnNotaFiscal = new JButton("Finalizar Compra");
        styleButton(btnNotaFiscal);
        // Botão de finalizar maior/destacado
        btnNotaFiscal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnNotaFiscal.setBorder(new EmptyBorder(10, 25, 10, 25));
        applyButtonHoverEffect(btnNotaFiscal, Color.WHITE, COR_DESTAQUE_PROCESSANDO); // Azul fundo padrão? Não, vamos
                                                                                      // usar texto azul
        // Vamos inverter para dar destaque: Texto Azul, Hover Branco
        btnNotaFiscal.setForeground(COR_DESTAQUE_PROCESSANDO);

        JButton btnVoltar = new JButton("Sair / Cancelar");
        styleButton(btnVoltar);
        applyButtonHoverEffect(btnVoltar, COR_ERRO, COR_DESTAQUE_IDLE);

        painelBotoesFinais.add(btnNotaFiscal);
        painelBotoesFinais.add(btnVoltar);

        painelAcoesBaixo.add(labelTotal, BorderLayout.NORTH);
        painelAcoesBaixo.add(painelBotoesFinais, BorderLayout.CENTER);

        painelControle.add(painelAcoesCima, BorderLayout.NORTH);
        painelControle.add(painelAcoesBaixo, BorderLayout.SOUTH);

        painelPrincipal.add(painelControle, BorderLayout.SOUTH);

        // --- Ações ---
        btnAdicionar.addActionListener(e -> adicionarAoCarrinho());
        btnRemover.addActionListener(e -> removerDoCarrinho());
        btnNotaFiscal.addActionListener(e -> emitirNotaFiscal());
        btnVoltar.addActionListener(e -> sairDaTela());

        return painelPrincipal;
    }

    // --- Métodos de Lógica (Funcionalidade Original Preservada) ---

    public void carregarProdutos() {
        listModelProdutos.clear();
        try {
            for (Produto p : supermercado.listarProdutos()) {
                listModelProdutos.addElement(p);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar o estoque: " + e.getMessage(), "Erro no BD",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void adicionarAoCarrinho() {
        Produto produtoSelecionado = listaProdutos.getSelectedValue();
        if (produtoSelecionado == null) {
            JOptionPane.showMessageDialog(this, "Selecione um produto na lista da esquerda.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantidade;
        try {
            quantidade = (int) spinnerQuantidade.getValue();
            if (quantidade <= 0)
                throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (quantidade > produtoSelecionado.getQuantidade()) {
            JOptionPane.showMessageDialog(this,
                    String.format("Estoque insuficiente! Apenas %d disponíveis.", produtoSelecionado.getQuantidade()),
                    "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Produto produtoParaCarrinho = new Produto(
                    produtoSelecionado.getProduto(),
                    produtoSelecionado.getPreco(),
                    produtoSelecionado.getPrecoCompra(),
                    quantidade);

            carrinho.adicionarAoCarrinho(produtoParaCarrinho);
            atualizarCarrinho();
            spinnerQuantidade.setValue(1);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removerDoCarrinho() {
        Produto produtoSelecionado = listaCarrinho.getSelectedValue();
        if (produtoSelecionado != null) {
            String qtdStr = JOptionPane.showInputDialog(this, "Quantas unidades remover?", "Remover Item",
                    JOptionPane.QUESTION_MESSAGE);
            if (qtdStr == null)
                return;

            try {
                int qtd = Integer.parseInt(qtdStr);
                if (qtd <= 0 || qtd > produtoSelecionado.getQuantidade())
                    throw new NumberFormatException();

                Produto remover = new Produto(produtoSelecionado.getProduto(), produtoSelecionado.getPreco(),
                        produtoSelecionado.getPrecoCompra(), qtd);
                carrinho.removerDoCarrinho(remover);
                atualizarCarrinho();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantidade inválida.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Selecione um produto no carrinho (direita) para remover.", "Aviso",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void atualizarCarrinho() {
        listModelCarrinho.clear();
        for (Produto p : carrinho.getProdutosCarrinho()) {
            listModelCarrinho.addElement(p);
        }
        labelTotal.setText(String.format("Total a Pagar: R$ %.2f", carrinho.calcularTotal()));
    }

    private void emitirNotaFiscal() {
        if (carrinho.getProdutosCarrinho().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carrinho vazio.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Pessoa cliente = new Pessoa(nomeCliente, cpfCliente, false);

        StringBuilder nota = new StringBuilder();
        nota.append("============= NOTA FISCAL =============\n");
        nota.append("Cliente: ").append(nomeCliente).append("\n");
        nota.append("CPF: ").append(cpfCliente).append("\n");
        nota.append("---------------------------------------\n");
        for (Produto p : carrinho.getProdutosCarrinho()) {
            nota.append(String.format("- %s\n  %d x R$ %.2f = R$ %.2f\n", p.getProduto(), p.getQuantidade(),
                    p.getPreco(), (p.getPreco() * p.getQuantidade())));
        }
        nota.append("---------------------------------------\n");
        nota.append(String.format("TOTAL: R$ %.2f\n", carrinho.calcularTotal()));
        nota.append("=======================================\n");

        try {
            boolean sucesso = carrinhoDAO.finalizarCompra(cliente, carrinho);
            if (sucesso) {
                JTextArea areaNota = new JTextArea(nota.toString());
                areaNota.setFont(new Font("Monospaced", Font.PLAIN, 12));
                areaNota.setEditable(false);

                JOptionPane.showMessageDialog(this, new JScrollPane(areaNota), "Compra Finalizada",
                        JOptionPane.PLAIN_MESSAGE);

                carrinho.limparCarrinho();
                carregarProdutos();
                atualizarCarrinho();
            } else {
                JOptionPane.showMessageDialog(this, "Falha ao registrar compra.", "Erro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro de banco de dados: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sairDaTela() {
        this.dispose();
        if (telaAnterior != null)
            telaAnterior.setVisible(true);
    }

    // --- Helpers de Estilização ---

    private JList<Produto> criarListaEstilizada(DefaultListModel<Produto> model) {
        JList<Produto> lista = new JList<>(model);
        lista.setBackground(COR_FUNDO_LISTA);
        lista.setForeground(COR_LETRA_PRINCIPAL);
        lista.setSelectionBackground(COR_DESTAQUE_PROCESSANDO);
        lista.setSelectionForeground(Color.WHITE);
        lista.setFont(FONTE_NORMAL);
        lista.setCellRenderer(new ProdutoListCellRenderer());
        return lista;
    }

    private JScrollPane criarScrollPaneEstilizado(JList<Produto> lista, String titulo) {
        JScrollPane scroll = new JScrollPane(lista);
        scroll.setBackground(COR_FUNDO);
        scroll.getViewport().setBackground(COR_FUNDO_LISTA);
        scroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COR_DESTAQUE_IDLE),
                titulo,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FONTE_TITULO,
                COR_LETRA_PRINCIPAL));
        return scroll;
    }

    private void estilizarSpinner(JSpinner spinner) {
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(COR_FUNDO_LISTA);
            tf.setForeground(Color.WHITE);
            tf.setCaretColor(Color.WHITE);
            tf.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        spinner.setBorder(BorderFactory.createLineBorder(COR_DESTAQUE_IDLE));
    }

    private void styleButton(JButton button) {
        button.setFont(FONTE_BOTAO);
        button.setForeground(COR_DESTAQUE_IDLE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(8, 15, 8, 15));
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

    // --- Renderer Customizado (Para funcionar com fundo escuro) ---
    private class ProdutoListCellRenderer extends DefaultListCellRenderer {
        private static final long serialVersionUID = 1L;

        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (isSelected) {
                setBackground(COR_DESTAQUE_PROCESSANDO);
                setForeground(Color.WHITE);
            } else {
                setBackground(COR_FUNDO_LISTA);
                setForeground(COR_LETRA_PRINCIPAL);
            }

            // Adiciona padding ao texto
            setBorder(new EmptyBorder(5, 5, 5, 5));

            if (value instanceof Produto) {
                Produto produto = (Produto) value;
                String texto;
                if (list == listaProdutos) {
                    texto = String.format(
                            "<html><b>%s</b><br>R$ %.2f <font color='#AAAAAA'>(Estoque: %d)</font></html>",
                            produto.getProduto(), produto.getPreco(), produto.getQuantidade());
                } else {
                    texto = String.format("<html><b>%s</b><br>R$ %.2f <font color='#AAAAAA'>(Qtd: %d)</font></html>",
                            produto.getProduto(), produto.getPreco(), produto.getQuantidade());
                }
                setText(texto);
            }
            return this;
        }
    }

    // --- Janela Customizada (Copied Helpers) ---

    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JPanel painelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        painelTitulo.setOpaque(false);

        JLabel tituloLabel = new JLabel("Tela de Compra");
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTitulo.add(tituloLabel);
        barraDeTitulo.add(painelTitulo, BorderLayout.WEST);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);

        JButton btnMinimizar = new JButton("\u2014");
        estilizarBotaoTitulo(btnMinimizar);
        btnMinimizar.addActionListener(e -> setState(JFrame.ICONIFIED));

        btnMaximizar = new JButton("\u25A1");
        estilizarBotaoTitulo(btnMaximizar);
        btnMaximizar.addActionListener(e -> toggleMaximize());

        JButton btnFechar = new JButton("\u00D7");
        estilizarBotaoTitulo(btnFechar);
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 21));
        btnFechar.addActionListener(e -> sairDaTela());

        applyButtonHoverEffect(btnMinimizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnMaximizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnFechar, COR_ERRO, Color.WHITE);

        painelBotoes.add(btnMinimizar);
        painelBotoes.add(btnMaximizar);
        painelBotoes.add(btnFechar);
        barraDeTitulo.add(painelBotoes, BorderLayout.EAST);

        // Listener para arrastar (aplicado na barra)
        adicionarListenerArrastar(barraDeTitulo);
        adicionarListenerArrastar(painelTitulo);
        adicionarListenerArrastar(tituloLabel);

        return barraDeTitulo;
    }

    private void adicionarListenerArrastar(JComponent component) {
        MouseAdapter dragAdapter = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (getExtendedState() != JFrame.MAXIMIZED_BOTH) {
                    initialClick = e.getPoint();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && component.getParent() instanceof JPanel) { // Apenas na barra
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
        component.addMouseListener(dragAdapter);
        component.addMouseMotionListener(dragAdapter);
    }

    private void estilizarBotaoTitulo(JButton button) {
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
    }

    private void toggleMaximize() {
        if (getExtendedState() == JFrame.MAXIMIZED_BOTH) {
            setExtendedState(JFrame.NORMAL);
        } else {
            setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
    }
}