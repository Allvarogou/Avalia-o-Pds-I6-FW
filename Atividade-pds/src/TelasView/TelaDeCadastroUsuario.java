package TelasView;

import SMModel.Pessoa;
import DAO.PessoaDAO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;


public class TelaDeCadastroUsuario extends JFrame implements ActionListener {

    private static final long serialVersionUID = 1L;
    
    private JTextField campoNome;
    private JTextField campoCpf;
    private JCheckBox checkAdm;
    private JButton botaoSalvar;
    private JButton botaoVoltar;

    private JFrame telaAnterior;
    private PessoaDAO pessoaDAO = new PessoaDAO();

    public TelaDeCadastroUsuario(JFrame telaAnterior) {
        this.telaAnterior = telaAnterior;

        setTitle("Cadastro de Usuário");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        
        JLabel titulo = new JLabel("Cadastro de Novo Usuário");
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        add(titulo, gbc);

       
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Nome:"), gbc);
        campoNome = new JTextField(20);
        gbc.gridx = 1;
        gbc.gridy = 1;
        add(campoNome, gbc);

       
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("CPF:"), gbc);
        campoCpf = new JTextField(15);
        gbc.gridx = 1;
        gbc.gridy = 2;
        add(campoCpf, gbc);

        checkAdm = new JCheckBox("É Administrador?");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        add(checkAdm, gbc);

       
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gbc.gridy = 4;
        botaoSalvar = new JButton("Salvar");
        add(botaoSalvar, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        botaoVoltar = new JButton("Voltar");
        add(botaoVoltar, gbc);

        botaoSalvar.addActionListener(this);
        botaoVoltar.addActionListener(this);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == botaoSalvar) {
            salvarUsuario();
        } else if (e.getSource() == botaoVoltar) {
            voltarParaMenuAdmin();
        }
    }

    private void salvarUsuario() {
        String nome = campoNome.getText().trim();
        String cpf = campoCpf.getText().trim();
        boolean isAdm = checkAdm.isSelected();

        if (nome.isEmpty() || cpf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nome e CPF não podem estar vazios.", "Erro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Pessoa novaPessoa = new Pessoa(nome, cpf, isAdm);
        
        try {
            boolean sucesso = pessoaDAO.salvar(novaPessoa); // Chama o DAO
            
            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Usuário " + nome + " cadastrado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
                campoNome.setText("");
                campoCpf.setText("");
                checkAdm.setSelected(false);
            } else {
                 JOptionPane.showMessageDialog(this, "Falha ao cadastrar. O CPF pode já existir.", "Erro no Cadastro", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro de conexão ao salvar: " + ex.getMessage(), "Erro de Banco", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voltarParaMenuAdmin() {
        this.dispose();
        if (telaAnterior != null) {
            telaAnterior.setVisible(true);
        }
    }
}