package TelasView;

// Imports originais
import SMModel.Mercado;
import SMModel.Pessoa;
import DAO.PessoaDAO;
import javax.swing.*;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.sql.SQLException;

// Imports adicionados para a nova estética
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowStateListener;
import javax.swing.border.EmptyBorder;

public class TelaDeIdentificacao extends JFrame {

    private static final long serialVersionUID = 1L;

    // Campos originais
    private JTextField campoNome;
    private JTextField campoCpf;
    private JButton botaoEntrar;
    private JButton botaoCancelar;
    private PessoaDAO pessoaDAO; // Mantido do original

    // --- Constantes de Estilo (Copiadas de TelaAutenticacao) ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_DESTAQUE_PROCESSANDO = new Color(0, 174, 239); // Azul
    private static final Color COR_SUCESSO = new Color(0, 200, 83); // Verde
    private static final Color COR_ERRO = new Color(213, 0, 0); // Vermelho
    private static final Font FONTE_STATUS = new Font("Segoe UI", Font.BOLD, 26);
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);

    // --- Variáveis para Janela Customizada (Copiadas) ---
    private Point initialClick;
    private JButton btnMaximizar;

    public TelaDeIdentificacao(Mercado mercado) {
        // Inicialização da funcionalidade original
        this.pessoaDAO = new PessoaDAO();

        // 1. Configuração da Janela (Estilo aplicado)
        configurarJanela();

        // 2. Barra de Título Customizada (Estilo aplicado)
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada();
        // Ajusta o título para esta tela específica
        ((JLabel) ((JPanel) barraDeTitulo.getComponent(0)).getComponent(1)).setText("Identificação de Usuário");


        // 3. Painel de Conteúdo (Funcionalidade mantida, Estilo aplicado)
        JPanel painelConteudo = new JPanel();
        painelConteudo.setBackground(COR_FUNDO);
        painelConteudo.setLayout(new GridBagLayout()); // Layout original mantido
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Adiciona um padding geral ao painel de login
        painelConteudo.setBorder(new EmptyBorder(20, 40, 30, 40));

        // --- Título ---
        JLabel titulo = new JLabel("Bem-vindo!");
        titulo.setFont(FONTE_STATUS); // Estilo aplicado
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setForeground(COR_LETRA_PRINCIPAL); // Estilo aplicado
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        painelConteudo.add(titulo, gbc);

        gbc.gridwidth = 1;
        gbc.weightx = 0.0; // Reset
        
        // --- Campo Nome ---
        JLabel labelNome = new JLabel("Nome:");
        labelNome.setFont(FONTE_LABEL); // Estilo aplicado
        labelNome.setForeground(COR_LETRA_PRINCIPAL); // Estilo aplicado
        gbc.gridx = 0;
        gbc.gridy = 1;
        painelConteudo.add(labelNome, gbc);

        campoNome = new JTextField(20); // Aumentado para 20
        styleTextField(campoNome); // Helper para aplicar estilo
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 1.0; // Faz o campo esticar
        painelConteudo.add(campoNome, gbc);

        // --- Campo CPF ---
        JLabel labelCpf = new JLabel("CPF:");
        labelCpf.setFont(FONTE_LABEL); // Estilo aplicado
        labelCpf.setForeground(COR_LETRA_PRINCIPAL); // Estilo aplicado
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.0; // Reset
        painelConteudo.add(labelCpf, gbc);

        campoCpf = new JTextField(20); // Aumentado para 20
        styleTextField(campoCpf); // Helper para aplicar estilo

        // --- IMPLEMENTAÇÃO DA MÁSCARA DE CPF (Funcionalidade 100% mantida) ---
        ((PlainDocument) campoCpf.getDocument()).setDocumentFilter(new DocumentFilter() {
            private final int MAX_CHARS = 14; 

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                String text = fb.getDocument().getText(0, fb.getDocument().getLength());
                text = text.replaceAll("[^0-9]", "") + string.replaceAll("[^0-9]", "");

                if (text.length() > 11 || (fb.getDocument().getLength() + string.length()
                        - string.replaceAll("[^0-9]", "").length() >= MAX_CHARS)) {
                    return;
                }
                
                // Correção para apagar e re-inserir
                fb.remove(0, fb.getDocument().getLength());
                fb.insertString(0, formatCPF(text), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText = currentText.substring(0, offset) + currentText.substring(offset + length);
                currentText = currentText.replaceAll("[^0-9]", "") + (text != null ? text.replaceAll("[^0-9]", "") : "");

                if (currentText.length() > 11) {
                    currentText = currentText.substring(0, 11);
                }

                fb.replace(0, fb.getDocument().getLength(), formatCPF(currentText), attrs);
            }

            private String formatCPF(String digits) {
                if (digits.isEmpty()) return "";
                
                StringBuilder builder = new StringBuilder(digits);
                if (builder.length() > 3)
                    builder.insert(3, '.');
                if (builder.length() > 7)
                    builder.insert(7, '.');
                if (builder.length() > 11)
                    builder.insert(11, '-');
                return builder.toString();
            }
        });
        // --- FIM DA IMPLEMENTAÇÃO DA MÁSCARA ---

        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.weightx = 1.0; // Faz o campo esticar
        painelConteudo.add(campoCpf, gbc);

        // --- Painel de Botões ---
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        painelBotoes.setOpaque(false); // Fundo transparente

        // --- Botão Entrar ---
        botaoEntrar = new JButton("Entrar");
        styleButton(botaoEntrar); // Helper para aplicar estilo
        applyButtonHoverEffect(botaoEntrar, COR_SUCESSO, COR_DESTAQUE_IDLE); // Verde no hover
        
        // --- Botão Cancelar ---
        botaoCancelar = new JButton("Cancelar");
        styleButton(botaoCancelar); // Helper para aplicar estilo
        applyButtonHoverEffect(botaoCancelar, COR_ERRO, COR_DESTAQUE_IDLE); // Vermelho no hover

        painelBotoes.add(botaoEntrar);
        painelBotoes.add(botaoCancelar);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE; // Não esticar os botões
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(20, 10, 10, 10); // Mais espaço acima
        painelConteudo.add(painelBotoes, gbc);


        // --- Lógica de Ação do Botão Entrar (Funcionalidade 100% mantida) ---
        botaoEntrar.addActionListener(e -> {
            String nomeDigitado = campoNome.getText().trim();
            String cpfCru = campoCpf.getText().trim();
            String cpfLimpo = cpfCru.replaceAll("[^0-9]", "");

            if (nomeDigitado.isEmpty() || cpfLimpo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e CPF não podem ser vazios.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Validação simples de CPF (apenas contagem de dígitos)
            if (cpfLimpo.length() != 11) {
                JOptionPane.showMessageDialog(this, "CPF inválido. Deve conter 11 dígitos.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Pessoa usuario = pessoaDAO.buscarPorCpf(cpfLimpo);

                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro de Login",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (!usuario.getNome().equalsIgnoreCase(nomeDigitado)) {
                    JOptionPane.showMessageDialog(this, "Nome incorreto para o CPF informado.", "Erro de Login",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                this.dispose();

                if (usuario.isAdm()) {
                    new TelaDeAdmin(mercado, this).setVisible(true);
                } else {
                    new TelaDeCompra(mercado, usuario.getNome(), usuario.getCpf(),
                            this).setVisible(true);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erro de conexão com o Banco de Dados: " + ex.getMessage(),
                        "Erro de Acesso", JOptionPane.ERROR_MESSAGE);
            }
        });
        // --- Fim Lógica de Ação do Botão Entrar ---

        // --- Ação do Botão Cancelar (Funcionalidade 100% mantida) ---
        botaoCancelar.addActionListener(e -> {
            System.exit(0);
        });

        // 4. Montagem final da Janela (Estilo aplicado)
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
        
        // Permite arrastar pelo painel de conteúdo também
        painelConteudo.addMouseListener(draggableAdapter);
        painelConteudo.addMouseMotionListener(draggableAdapter);
    }

    /**
     * Aplica o estilo padrão da aplicação a um JTextField.
     */
    private void styleTextField(JTextField field) {
        field.setBackground(new Color(50, 50, 50));
        field.setForeground(COR_LETRA_PRINCIPAL);
        field.setCaretColor(Color.WHITE);
        field.setFont(FONTE_CAMPO);
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(COR_DESTAQUE_IDLE), // Borda externa
            new EmptyBorder(5, 8, 5, 8) // Padding interno
        ));
    }

    /**
     * Aplica o estilo padrão da aplicação a um JButton.
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


    // --- Métodos de Suporte da Janela (Copiados de TelaAutenticacao) ---

    private void configurarJanela() {
        setUndecorated(true);
        // Tamanho ajustado para um formulário de login
        setSize(500, 400); 
        setMinimumSize(new Dimension(450, 350));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }
                 
    private JPanel criarBarraDeTituloCustomizada() {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JPanel painelTituloIcone = new JPanel(new BorderLayout(10, 0));
        painelTituloIcone.setOpaque(false);

        FingerprintIconPanel iconPanel = new FingerprintIconPanel();
        iconPanel.setBorder(new EmptyBorder(2, 0, 0, 0));
        painelTituloIcone.add(iconPanel, BorderLayout.WEST);
         
        // Título Padrão (pode ser alterado após a criação)
        JLabel tituloLabel = new JLabel("Controle de Acesso"); 
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        painelTituloIcone.add(tituloLabel, BorderLayout.CENTER);

        barraDeTitulo.add(painelTituloIcone, BorderLayout.CENTER);

        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        painelBotoes.setOpaque(false);

        JButton btnMinimizar = new JButton("\u2014");
        btnMinimizar.setForeground(Color.WHITE);
        btnMinimizar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnMinimizar.setFocusPainted(false);
        btnMinimizar.setBorderPainted(false);
        btnMinimizar.setContentAreaFilled(false);
        btnMinimizar.addActionListener(e -> setState(JFrame.ICONIFIED));
         
        btnMaximizar = new JButton("\u25A1");
        btnMaximizar.setForeground(Color.WHITE);
        btnMaximizar.setFont(new Font("Segoe UI Symbol", Font.BOLD, 14));
        btnMaximizar.setFocusPainted(false);
        btnMaximizar.setBorderPainted(false);
        btnMaximizar.setContentAreaFilled(false);
        btnMaximizar.addActionListener(e -> toggleMaximize());
         
        JButton btnFechar = new JButton("\u00D7");
        btnFechar.setForeground(Color.WHITE);
        btnFechar.setFont(new Font("Segoe UI", Font.BOLD, 21));
        btnFechar.setFocusPainted(false);
        btnFechar.setBorderPainted(false);
        btnFechar.setContentAreaFilled(false);
        btnFechar.addActionListener(e -> System.exit(0)); // Alterado de dispose() para System.exit(0)
         
        applyButtonHoverEffect(btnMinimizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnMaximizar, COR_DESTAQUE_PROCESSANDO, Color.WHITE);
        applyButtonHoverEffect(btnFechar, COR_ERRO, Color.WHITE); // Vermelho no hover do fechar

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
        // Permite arrastar pelo título/ícone também
        painelTituloIcone.addMouseListener(titleBarAdapter);
        painelTituloIcone.addMouseMotionListener(titleBarAdapter);
        tituloLabel.addMouseListener(titleBarAdapter);
        tituloLabel.addMouseMotionListener(titleBarAdapter);


        this.addWindowStateListener((WindowStateListener) e -> {
            if ((e.getNewState() & JFrame.MAXIMIZED_BOTH) == JFrame.MAXIMIZED_BOTH) {
                btnMaximizar.setText("\u29C9");
            } else {
                btnMaximizar.setText("\u25A1");
            }
        });
         
        return barraDeTitulo;
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

    // --- Inner Class para Ícone (Copiada de TelaAutenticacao) ---
    private static class FingerprintIconPanel extends JPanel {
        private static final long serialVersionUID = 1L;
         
        public FingerprintIconPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(20, 20)); // tamanho
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int diametro = Math.min(getWidth(), getHeight()) - 4;
            int x = (getWidth() - diametro) / 2;
            int y = (getHeight() - diametro) / 2;

            g2d.setColor(Color.WHITE); // cor
            g2d.setStroke(new BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
             
            for (int i = 0; i < 4; i++) { // tem menos arcos que o grande
                int d = diametro - (i * (diametro / 4));
                int arcX = x + (i * (diametro / 8));
                int arcY = y + (i * (diametro / 8));
                g2d.drawArc(arcX, arcY, d, d, -45 - (i * 10), 270 + (i * 5));
            }
            g2d.dispose();
        }
    }
}