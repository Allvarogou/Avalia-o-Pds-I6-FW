package TelasView;

import SMModel.Mercado;
import SMModel.Pessoa;
import DAO.PessoaDAO;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.*;

public class TelaIndentificação extends JFrame {

    private static final long serialVersionUID = 1L;
    private JTextField campoNome;
    private JTextField campoCpf;
    private JButton botaoEntrar;
    private JButton botaoCancelar;

    private PessoaDAO pessoaDAO = new PessoaDAO();

    public TelaIndentificação(Mercado mercado) {
        
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

        
        JLabel labelCpf = new JLabel("CPF:");
        labelCpf.setFont(new Font("Arial", Font.PLAIN, 16));
        labelCpf.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 2;
        painel.add(labelCpf, gbc);

        campoCpf = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        painel.add(campoCpf, gbc);

   
        gbc.gridx = 0;
        gbc.gridy = 3; 
        botaoEntrar = new JButton("Entrar");
        botaoEntrar.setBackground(new Color(100, 149, 237));
        botaoEntrar.setForeground(Color.WHITE);
        botaoEntrar.setFocusPainted(false);
        painel.add(botaoEntrar, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3; 
        botaoCancelar = new JButton("Cancelar");
        botaoCancelar.setBackground(new Color(220, 53, 69));
        botaoCancelar.setForeground(Color.WHITE);
        botaoCancelar.setFocusPainted(false);
        painel.add(botaoCancelar, gbc);


        botaoEntrar.addActionListener(e -> {
            String nomeDigitado = campoNome.getText().trim();
            String cpf = campoCpf.getText().trim(); // Mantém o CPF com formatação
            
            // REMOÇÃO: A linha abaixo foi removida para usar o CPF bruto:
            // String cpf = cpfCru.replaceAll("[^0-9]", ""); 

            if (nomeDigitado.isEmpty() || cpf.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nome e CPF não podem ser vazios.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                
                Pessoa usuario = pessoaDAO.buscarPorCpf(cpf); // Passa o CPF bruto/formatado

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
       

       
        botaoCancelar.addActionListener(e -> {
            System.exit(0);
        });

        add(painel);
    }
}