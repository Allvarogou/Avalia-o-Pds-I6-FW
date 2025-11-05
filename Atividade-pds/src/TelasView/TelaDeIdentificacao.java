package TelasView;

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

public class TelaDeIdentificacao extends JFrame {

    private static final long serialVersionUID = 1L;

    private JTextField campoNome;
    private JTextField campoCpf;
    private JButton botaoEntrar;
    private JButton botaoCancelar;

    private PessoaDAO pessoaDAO = new PessoaDAO();

    public TelaDeIdentificacao(Mercado mercado) {

        setTitle("Tela de Identificação");
        setSize(400, 240);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel painel = new JPanel();
        painel.setBackground(Color.BLACK);
        painel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titulo = new JLabel("Bem-vindo!");
        titulo.setFont(new Font("Arial", Font.BOLD, 22));
        titulo.setHorizontalAlignment(SwingConstants.CENTER);
        titulo.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        painel.add(titulo, gbc);

        gbc.gridwidth = 1;

        // Campo Nome
        JLabel labelNome = new JLabel("Nome:");
        labelNome.setFont(new Font("Arial", Font.PLAIN, 16));
        labelNome.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 1;
        painel.add(labelNome, gbc);

        campoNome = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 1;
        painel.add(campoNome, gbc);

        // Campo CPF
        JLabel labelCpf = new JLabel("CPF:");
        labelCpf.setFont(new Font("Arial", Font.PLAIN, 16));
        labelCpf.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(labelCpf, gbc);

        campoCpf = new JTextField(15);

        // --- IMPLEMENTAÇÃO DA MÁSCARA DE CPF ---
        ((PlainDocument) campoCpf.getDocument()).setDocumentFilter(new DocumentFilter() {
            private final int MAX_CHARS = 14; // Inclui a formatação ###.###.###-##

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                // Remove todos os caracteres não-dígitos da entrada
                String text = fb.getDocument().getText(0, fb.getDocument().getLength());
                text = text.replaceAll("[^0-9]", "") + string.replaceAll("[^0-9]", "");

                // Se exceder 11 dígitos (limite do CPF) ou 14 caracteres (máscara)
                if (text.length() > 11 || (fb.getDocument().getLength() + string.length()
                        - string.replaceAll("[^0-9]", "").length() >= MAX_CHARS)) {
                    return;
                }

                // Formata e insere
                super.insertString(fb, 0, formatCPF(text), attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                // Remove todos os caracteres não-dígitos da entrada
                String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());
                currentText = currentText.substring(0, offset) + currentText.substring(offset + length);
                currentText = currentText.replaceAll("[^0-9]", "") + text.replaceAll("[^0-9]", "");

                if (currentText.length() > 11) {
                    return;
                }

                super.replace(fb, 0, fb.getDocument().getLength(), formatCPF(currentText), attrs);
            }

            // Método auxiliar para formatar
            private String formatCPF(String digits) {
                if (digits.length() > 11)
                    digits = digits.substring(0, 11);

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
        painel.add(campoCpf, gbc);

        // Botão Entrar
        gbc.gridx = 0;
        gbc.gridy = 3;
        botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBackground(new Color(100, 149, 237));
        botaoEntrar.setForeground(Color.WHITE);
        botaoEntrar.setFocusPainted(false);
        painel.add(botaoEntrar, gbc);

        // Botão Cancelar
        gbc.gridx = 1;
        gbc.gridy = 3;
        botaoCancelar = new JButton("Cancelar");
        botaoCancelar.setBackground(new Color(220, 53, 69));
        botaoCancelar.setForeground(Color.WHITE);
        botaoCancelar.setFocusPainted(false);
        painel.add(botaoCancelar, gbc);

        // --- Lógica de Ação do Botão Entrar ---
        botaoEntrar.addActionListener(e -> {
            String nomeDigitado = campoNome.getText().trim();
            String cpfCru = campoCpf.getText().trim();

            // Lógica final: Limpa o CPF APENAS para a busca no DAO
            String cpfLimpo = cpfCru.replaceAll("[^0-9]", "");

            if (nomeDigitado.isEmpty() || cpfLimpo.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e CPF não podem ser vazios.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // 1. Usa o DAO para buscar o usuário no BD com o CPF limpo
                Pessoa usuario = pessoaDAO.buscarPorCpf(cpfLimpo);

                if (usuario == null) {
                    JOptionPane.showMessageDialog(this, "Usuário não encontrado.", "Erro de Login",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 2. Verifica a credencial (Nome)
                if (!usuario.getNome().equalsIgnoreCase(nomeDigitado)) {
                    JOptionPane.showMessageDialog(this, "Nome incorreto para o CPF informado.", "Erro de Login",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 3. Login bem-sucedido: Abre a tela correta
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

        // Ação do Botão Cancelar
        botaoCancelar.addActionListener(e -> {
            System.exit(0);
        });

        add(painel);
    }
}