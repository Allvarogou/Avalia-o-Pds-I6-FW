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

// Imports para a nova estética
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.border.EmptyBorder;

/**
 * Tela dedicada para o administrador gerenciar (Adicionar, Editar, Remover)
 * os produtos do mercado.
 * (Versão reestilizada com o tema escuro)
 */
public class TelaDeGerenciamentoProdutos extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Campos Funcionais Originais ---
    private ProdutoDAO produtoDAO = new ProdutoDAO();
    private JFrame telaAnterior;

    // --- Componentes de Interface Originais ---
    private JTable tabelaProdutos;
    private DefaultTableModel tableModel;
    private JTextField campoNome, campoPreco, campoPrecoCompra, campoQuantidade;
    private JButton btnAdicionar, btnEditar, btnRemover, btnVoltar;

    // --- Constantes de Estilo (Copiadas) ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_FUNDO_TABELA = new Color(40, 40, 40);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239); // Azul
    private static final Color COR_SUCESSO = new Color(0, 200, 83); // Verde
    private static final Color COR_ERRO = new Color(213, 0, 0); // Vermelho
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FONTE_TABELA_HEADER = new Font("Segoe UI", Font.BOLD, 14);

    // --- Variáveis para Janela Customizada (Copiadas) ---
    private Point initialClick;
    private JButton btnMaximizar;

    // Construtor: recebe a tela que a chamou (TelaDeAdmin)
    public TelaDeGerenciamentoProdutos(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;
        this.produtoDAO = new ProdutoDAO(); // Instancia o DAO

        // 1. Configuração da Janela (Estilo aplicado)
        configurarJanela();

        // 2. Barra de Título Customizada (Estilo aplicado)
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        // --- CORREÇÃO APLICADA AQUI ---
        // O painel do título (componente 0) agora só tem um filho (o label, componente 0)
        ((JLabel) ((JPanel) barraDeTitulo.getComponent(0)).getComponent(0)).setText("Gerenciamento de Produtos");

        // 3. Painel de Conteúdo (Funcionalidade mantida, Estilo aplicado)
        JPanel painelConteudo = inicializarComponentes();

        // 4. Montagem final da Janela
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);

        // 5. Adição do MouseListener para arrastar (Estilo aplicado)
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

        // 6. Ação ao fechar a janela (Funcionalidade 100% mantida)
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

        // 7. Carregamento dos dados (Funcionalidade 100% mantida)
        carregarTabelaProdutos();
    }

    /**
     * Modificado para retornar um JPanel e aplicar a estética.
     */
    private JPanel inicializarComponentes() {
        // Painel de conteúdo principal
        JPanel painelConteudo = new JPanel(new BorderLayout(10, 10));
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setBorder(new EmptyBorder(10, 20, 10, 20)); // Padding geral

        // --- Painel de Formulário (NORTE) ---
        JPanel painelFormulario = new JPanel(new GridLayout(2, 4, 10, 10));
        painelFormulario.setOpaque(false); // Fundo transparente
        painelFormulario.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding

        // Campos do formulário
        campoNome = new JTextField(10);
        styleTextField(campoNome);
        campoPreco = new JTextField(10);
        styleTextField(campoPreco);
        campoPrecoCompra = new JTextField(10);
        styleTextField(campoPrecoCompra);
        campoQuantidade = new JTextField(10);
        styleTextField(campoQuantidade);

        // Adiciona Labels (estilizados) e Campos
        painelFormulario.add(styleLabel(new JLabel("Nome do Produto:")));
        painelFormulario.add(styleLabel(new JLabel("Preço de Venda:")));
        painelFormulario.add(styleLabel(new JLabel("Preço de Compra:")));
        painelFormulario.add(styleLabel(new JLabel("Quantidade em Estoque:")));

        painelFormulario.add(campoNome);
        painelFormulario.add(campoPreco);
        painelFormulario.add(campoPrecoCompra);
        painelFormulario.add(campoQuantidade);

        painelConteudo.add(painelFormulario, BorderLayout.NORTH);

        // --- Tabela de Produtos (CENTRO) ---
        String[] colunas = { "Nome", "Preço Venda", "Preço Compra", "Quantidade" };
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaProdutos = new JTable(tableModel);
        estilizarTabela(tabelaProdutos); // Aplicando estética
        
        JScrollPane scrollPane = new JScrollPane(tabelaProdutos);
        estilizarScrollPane(scrollPane); // Aplicando estética

        // Ação para carregar os campos ao selecionar uma linha (Funcionalidade 100% mantida)
        tabelaProdutos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tabelaProdutos.getSelectedRow() != -1) {
                int linhaSelecionada = tabelaProdutos.getSelectedRow();
                campoNome.setText(tableModel.getValueAt(linhaSelecionada, 0).toString());
                campoPreco.setText(tableModel.getValueAt(linhaSelecionada, 1).toString());
                campoPrecoCompra.setText(tableModel.getValueAt(linhaSelecionada, 2).toString());
                campoQuantidade.setText(tableModel.getValueAt(linhaSelecionada, 3).toString());
            }
        });

        painelConteudo.add(scrollPane, BorderLayout.CENTER);

        // --- Painel de Botões (SUL) ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        painelBotoes.setOpaque(false); // Fundo transparente

        btnAdicionar = new JButton("Adicionar Novo");
        btnEditar = new JButton("Salvar Edição");
        btnRemover = new JButton("Remover Produto");
        btnVoltar = new JButton("Voltar");

        // Aplicando Estilo e Hover
        styleButton(btnAdicionar);
        styleButton(btnEditar);
        styleButton(btnRemover);
        styleButton(btnVoltar);
        
        applyButtonHoverEffect(btnAdicionar, COR_SUCESSO, COR_DESTAQUE_IDLE); // Verde
        applyButtonHoverEffect(btnEditar, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE); // Azul
        applyButtonHoverEffect(btnRemover, COR_ERRO, COR_DESTAQUE_IDLE); // Vermelho
        applyButtonHoverEffect(btnVoltar, COR_DESTAQUE_PROCESSANDO, COR_DESTAQUE_IDLE); // Azul

        // Adiciona ações aos botões (Funcionalidade 100% mantida)
        btnAdicionar.addActionListener(e -> adicionarProduto());
        btnEditar.addActionListener(e -> editarProduto());
        btnRemover.addActionListener(e -> removerProduto());
        btnVoltar.addActionListener(e -> voltar());

        painelBotoes.add(btnAdicionar);
        painelBotoes.add(btnEditar);
        painelBotoes.add(btnRemover);
        painelBotoes.add(btnVoltar);

        painelConteudo.add(painelBotoes, BorderLayout.SOUTH);
        
        return painelConteudo;
    }

    // --- Métodos de Lógica (Interagindo com DAO) ---
    // (Funcionalidade 100% mantida - Sem alteração)

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
            e.printStackTrace();
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
            e.printStackTrace();
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
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

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
                e.printStackTrace();
            }
        }
    }
    
    private void voltar() {
        // Dispara o WindowListener (windowClosed)
        this.dispose();
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
    
    // --- Métodos de Estilização (Helpers) ---

    private JLabel styleLabel(JLabel label) {
        label.setFont(FONTE_LABEL);
        label.setForeground(COR_LETRA_PRINCIPAL);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(COR_LETRA_PRINCIPAL);
        field.setCaretColor(Color.WHITE);
        field.setFont(FONTE_CAMPO);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_DESTAQUE_IDLE),
            new EmptyBorder(5, 8, 5, 8) // Padding interno
        ));
    }

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

    private void configurarJanela() {
        setUndecorated(true);
        setSize(850, 600); // Aumentado para acomodar o formulário e a tabela
        setMinimumSize(new Dimension(700, 500));
        // IMPORTANTE: Mantém DISPOSE_ON_CLOSE para o WindowListener funcionar
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }
                 
    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JPanel painelTituloIcone = new JPanel(new BorderLayout(10, 0));
        painelTituloIcone.setOpaque(false);
        // (Ícone da digital removido por não ser relevante para produtos)
         
        JLabel tituloLabel = new JLabel("Controle de Acesso"); 
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTituloIcone.add(tituloLabel, BorderLayout.CENTER);

        barraDeTitulo.add(painelTituloIcone, BorderLayout.CENTER);

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