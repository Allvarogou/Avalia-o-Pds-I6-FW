package Controller;

import DAO.PessoaDAO;
import SMModel.Mercado;
import SMModel.Pessoa;
import TelasView.TelaDeAdmin;
import TelasView.TelaDeCompra;
import javax.swing.JOptionPane;
import javax.swing.JFrame; // Usado para referenciar a janela (View)
import java.sql.SQLException;

public class IdentificacaoController {

    private PessoaDAO pessoaDAO;

    public IdentificacaoController() {
        this.pessoaDAO = new PessoaDAO();
    }

    /**
     * Tenta realizar o login buscando o usuário no banco de dados e redireciona a
     * View.
     * 
     * @param nomeDigitado Nome fornecido pelo usuário.
     * @param cpfCru       CPF fornecido pelo usuário (pode conter pontuação).
     * @param telaAtual    A instância da TelaDeIdentificacao a ser fechada.
     * @param mercado      O modelo principal da aplicação.
     */
    public void realizarLogin(String nomeDigitado, String cpfCru, JFrame telaAtual, Mercado mercado) {

        // O Controller é responsável por sanear os dados antes de usar o DAO
        String cpf = cpfCru.replaceAll("[^0-9]", "");

        if (nomeDigitado.isEmpty() || cpf.isEmpty()) {
            JOptionPane.showMessageDialog(telaAtual, "Nome e CPF não podem ser vazios.", "Erro de Login",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Chama o DAO (Model) para buscar a Pessoa
            Pessoa usuario = pessoaDAO.buscarPorCpf(cpf);

            if (usuario == null) {
                JOptionPane.showMessageDialog(telaAtual, "Usuário não encontrado.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!usuario.getNome().equalsIgnoreCase(nomeDigitado)) {
                JOptionPane.showMessageDialog(telaAtual, "Nome incorreto para o CPF informado.", "Erro de Login",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Login bem-sucedido: O Controller decide a transição de View
            telaAtual.dispose();

            if (usuario.isAdm()) {
                new TelaDeAdmin(mercado, telaAtual).setVisible(true);
            } else {
                new TelaDeCompra(mercado, usuario.getNome(), usuario.getCpf(), telaAtual).setVisible(true);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(telaAtual, "Erro de conexão com o Banco de Dados: " + ex.getMessage(),
                    "Erro de Acesso", JOptionPane.ERROR_MESSAGE);
        }
    }
}