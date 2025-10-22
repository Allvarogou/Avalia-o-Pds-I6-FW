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

            pstm.setString(1, pessoa.getNome());
            pstm.setString(2, pessoa.getCpf());
            pstm.setBoolean(3, pessoa.isAdm());

            pstm.executeUpdate();
            return true;

        } catch (SQLException e) {

            System.err.println("Erro ao salvar usuário: " + e.getMessage());
            return false;
        }

    }

    public Pessoa buscarPorCpf(String cpf) throws SQLException {
        Pessoa pessoa = null;
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        String sql = "SELECT nome, cpf, isAdm FROM pessoas WHERE cpf = ?";

        try (Connection conn = ConectioDB.conectar(); 
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, cpfLimpo);

            try (ResultSet rs = pstm.executeQuery()) {
                if (rs.next()) {
                  
                    pessoa = new Pessoa();
                    pessoa.setNome(rs.getString("nome"));
                    pessoa.setCpf(rs.getString("cpf"));
                    pessoa.setAdm(rs.getBoolean("isAdm"));
                }
            }
        }
        
        return pessoa;
    }

}