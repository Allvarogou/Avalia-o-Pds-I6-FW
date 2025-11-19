package TelasView;

import SMModel.Pessoa;
import DAO.PessoaDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

public class TelaDeCadastroUsuario extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoNome;
    private JTextField campoCpf;
    private JCheckBox checkAdm;
    private JButton botaoSalvar;
    private JButton botaoVoltar;

    private JFrame telaAnterior; // Pode ser TelaDeGerenciamento ou TelaDeAdmin
    private PessoaDAO pessoaDAO = new PessoaDAO();

    // Variável para saber se estamos editando
    private boolean modoEdicao = false;

    // --- Constantes de Estilo ---
    private static final Color COR_FUNDO = new Color(30, 30, 30);
    private static final Color COR_LETRA_PRINCIPAL = new Color(200, 200, 200);
    private static final Color COR_DESTAQUE_IDLE = new Color(100, 100, 100);
    private static final Color COR_SUCESSO = new Color(0, 200, 83);
    private static final Color COR_ERRO = new Color(213, 0, 0);
    private static final Color COR_PROCESSANDO = new Color(0, 174, 239);
    private static final Font FONTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font FONTE_LABEL = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_BOTAO = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FONTE_CAMPO = new Font("Segoe UI", Font.PLAIN, 14);

    private Point initialClick;
    private JButton btnMaximizar;

    // Construtor 1: Para CADASTRO NOVO (Vazio)
    public TelaDeCadastroUsuario(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;
        this.modoEdicao = false;
        inicializarJanela("Cadastro de Novo Usuário");
    }

    // Construtor 2: Para EDIÇÃO (Recebe os dados)
    public TelaDeCadastroUsuario(JFrame telaAnterior, Pessoa pessoaParaEditar) {
        this.telaAnterior = telaAnterior;
        this.modoEdicao = true;
        inicializarJanela("Editar Usuário");
        preencherDados(pessoaParaEditar);
    }

    private void inicializarJanela(String titulo) {
        configurarJanela();
        JPanel barraDeTitulo = criarBarraDeTituloCustomizada(titulo);
        JPanel painelConteudo = inicializarComponentes(titulo);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(barraDeTitulo, BorderLayout.NORTH);
        getContentPane().add(painelConteudo, BorderLayout.CENTER);
    }

    private void configurarJanela() {
        setUndecorated(true);
        setSize(450, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(COR_FUNDO);
    }

    private void preencherDados(Pessoa p) {
        campoNome.setText(p.getNome());
        campoCpf.setText(p.getCpf());
        campoCpf.setEditable(false); // Não permite mudar CPF na edição (pois é a chave no BD)
        campoCpf.setBackground(new Color(40, 40, 40)); // Cor visual de desabilitado
        checkAdm.setSelected(p.isAdm());
        botaoSalvar.setText("Atualizar"); // Muda o texto do botão
    }

    private JPanel inicializarComponentes(String textoTitulo) {
        JPanel painel = new JPanel(new GridBagLayout());
        painel.setBackground(COR_FUNDO);
        painel.setBorder(new EmptyBorder(20, 30, 20, 30));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Título
        JLabel titulo = new JLabel(textoTitulo);
        titulo.setFont(FONTE_TITULO);
        titulo.setForeground(Color.WHITE);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        painel.add(titulo, gbc);

        // Campo Nome
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(criarLabel("Nome:"), gbc);

        campoNome = new JTextField(20);
        styleTextField(campoNome);
        gbc.gridx = 1;
        gbc.gridy = 1;
        painel.add(campoNome, gbc);

        // Campo CPF
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(criarLabel("CPF:"), gbc);

        campoCpf = new JTextField(15);
        styleTextField(campoCpf);
        aplicarMascaraCPF(campoCpf);
        gbc.gridx = 1;
        gbc.gridy = 2;
        painel.add(campoCpf, gbc);

        // Checkbox Admin
        checkAdm = new JCheckBox("É Administrador?");
        checkAdm.setFont(FONTE_LABEL);
        checkAdm.setForeground(COR_LETRA_PRINCIPAL);
        checkAdm.setOpaque(false);
        checkAdm.setFocusPainted(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        painel.add(checkAdm, gbc);

        // Botões
        JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        painelBotoes.setOpaque(false);

        botaoSalvar = new JButton("Salvar");
        styleButton(botaoSalvar);
        applyButtonHoverEffect(botaoSalvar, COR_SUCESSO, COR_DESTAQUE_IDLE);

        botaoVoltar = new JButton("Voltar");
        styleButton(botaoVoltar);
        applyButtonHoverEffect(botaoVoltar, COR_ERRO, COR_DESTAQUE_IDLE);

        painelBotoes.add(botaoSalvar);
        painelBotoes.add(botaoVoltar);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 5, 10);
        painel.add(painelBotoes, gbc);

        botaoSalvar.addActionListener(e -> salvarOuAtualizar());
        botaoVoltar.addActionListener(e -> voltar());

        return painel;
    }

    private void salvarOuAtualizar() {
        String nome = campoNome.getText().trim();
        String cpfCru = campoCpf.getText().trim();
        // Remove caracteres não numéricos para verificar se está vazio
        String cpfApenasNumeros = cpfCru.replaceAll("[^0-9]", "");

        if (nome.isEmpty() || cpfApenasNumeros.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e CPF são obrigatórios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Se não estiver editando, valida o tamanho do CPF
        if (!modoEdicao && cpfApenasNumeros.length() != 11) {
            JOptionPane.showMessageDialog(this, "CPF inválido (digite os 11 números).", "Erro",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- TRATAMENTO DO CPF ---
        // Enviamos para o banco o CPF exatamente como está no campo (com a máscara),
        // pois o DAO e o Banco parecem estar configurados para VARCHAR que cabe
        // formatação.
        // Se o seu banco falhar, troque 'cpfCru' por 'cpfApenasNumeros' aqui.
        Pessoa pessoa = new Pessoa(nome, cpfCru, checkAdm.isSelected());

        try {
            boolean sucesso;
            if (modoEdicao) {
                sucesso = pessoaDAO.atualizar(pessoa);
            } else {
                sucesso = pessoaDAO.salvar(pessoa);
            }

            if (sucesso) {
                String msg = modoEdicao ? "Usuário atualizado!" : "Usuário cadastrado!";
                JOptionPane.showMessageDialog(this, msg, "Sucesso", JOptionPane.INFORMATION_MESSAGE);

                if (!modoEdicao) {
                    campoNome.setText("");
                    campoCpf.setText("");
                    checkAdm.setSelected(false);
                } else {
                    voltar(); // Se editou, volta para a lista
                }
            } else {
                JOptionPane.showMessageDialog(this, "Erro na operação. Verifique se o CPF já existe.", "Erro",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro de banco de dados: " + ex.getMessage(), "Erro",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voltar() {
        this.dispose();
        if (telaAnterior != null) {
            telaAnterior.setVisible(true);
            // Se a tela anterior for a lista, recarrega a tabela
            if (telaAnterior instanceof TelaDeGerenciamentoUsuarios) {
                ((TelaDeGerenciamentoUsuarios) telaAnterior).carregarTabela();
            }
        }
    }

    // --- Estilização e Utilitários (Mantidos) ---

    private JLabel criarLabel(String texto) {
        JLabel label = new JLabel(texto);
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
                BorderFactory.createLineBorder(COR_DESTAQUE_IDLE), new EmptyBorder(5, 8, 5, 8)));
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

    private void aplicarMascaraCPF(JTextField campo) {
        ((PlainDocument) campo.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                String text = fb.getDocument().getText(0, fb.getDocument().getLength());
                text = text.replaceAll("[^0-9]", "") + string.replaceAll("[^0-9]", "");
                if (text.length() > 11)
                    return;
                fb.remove(0, fb.getDocument().getLength());
                fb.insertString(0, formatCPF(text), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText = currentText.substring(0, offset) + currentText.substring(offset + length);
                currentText = currentText.replaceAll("[^0-9]", "")
                        + (text != null ? text.replaceAll("[^0-9]", "") : "");
                if (currentText.length() > 11)
                    currentText = currentText.substring(0, 11);
                fb.replace(0, fb.getDocument().getLength(), formatCPF(currentText), attrs);
            }

            private String formatCPF(String digits) {
                if (digits.isEmpty())
                    return "";
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
    }

    private JPanel criarBarraDeTituloCustomizada(String titulo) {
        JPanel barraDeTitulo = new JPanel(new BorderLayout());
        barraDeTitulo.setBackground(COR_FUNDO);
        barraDeTitulo.setBorder(new EmptyBorder(5, 10, 5, 5));

        JLabel tituloLabel = new JLabel(titulo);
        tituloLabel.setForeground(Color.WHITE);
        tituloLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        barraDeTitulo.add(tituloLabel, BorderLayout.WEST);

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
        btnClose.addActionListener(e -> voltar());
        applyButtonHoverEffect(btnClose, COR_ERRO, COR_DESTAQUE_IDLE);

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