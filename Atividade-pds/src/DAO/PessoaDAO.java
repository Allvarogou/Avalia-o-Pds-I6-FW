package DAO;

import SMModel.Pessoa;
import java.sql.ResultSet;
//import DAO.ConectioDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PessoaDAO {

    public boolean salvar(Pessoa pessoa) throws SQLException {

        String sql = "INSERT INTO pessoas (nome, cpf, isAdm) VALUES (?, ?, ?)";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            // ALTERAÇÃO: O CPF não é mais limpo. É salvo com a formatação (ex: 100.000.000-00)
            pstm.setString(1, pessoa.getNome());
            pstm.setString(2, pessoa.getCpf()); // Salva o CPF bruto
            pstm.setBoolean(3, pessoa.isAdm());

            pstm.executeUpdate();
            return true;

        } catch (SQLException e) {

            System.err.println("Erro ao salvar usuário: " + e.getMessage());
            e.printStackTrace(); 
            return false;
        }

    }

    public Pessoa buscarPorCpf(String cpf) throws SQLException {
        Pessoa pessoa = null;
        // String cpfLimpo = cpf.replaceAll("[^0-9]", ""); // REMOVIDO: Não limpa o CPF
        
        // ALTERAÇÃO: A query agora pesquisa pelo CPF exatamente como o usuário digitou (com pontos/traços)
        String sql = "SELECT nome, cpf, isAdm FROM pessoas WHERE cpf = ?";

        try (Connection conn = ConectioDB.conectar(); 
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Log de depuração
            //System.out.println("Buscando CPF (bruto/formatado): " + cpf);

            pstm.setString(1, cpf); // Usa o CPF bruto (formatted)

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                  
                    pessoa = new Pessoa();
                    pessoa.setNome(rs.getString("nome"));
                    pessoa.setCpf(rs.getString("cpf"));
                    pessoa.setAdm(rs.getBoolean("isAdm"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); 
            throw e;
        }
        
        return pessoa;
    }

}