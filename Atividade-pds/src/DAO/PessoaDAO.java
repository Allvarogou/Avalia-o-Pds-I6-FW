package DAO;

import SMModel.Pessoa;
import java.sql.ResultSet;
//import DAO.ConectioDB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

public class PessoaDAO {

    public ArrayList<Pessoa> listarTodos() throws SQLException {
        ArrayList<Pessoa> listaPessoas = new ArrayList<>();
        // Query SQL para selecionar todos os registros
        String sql = "SELECT nome, cpf, isAdm FROM pessoas";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql);
                ResultSet rs = pstm.executeQuery()) {

            // Itera sobre todos os resultados encontrados
            while (rs.next()) {
                Pessoa pessoa = new Pessoa();
                pessoa.setNome(rs.getString("nome"));
                pessoa.setCpf(rs.getString("cpf"));
                pessoa.setAdm(rs.getBoolean("isAdm"));

                // Adiciona o objeto pessoa à lista
                listaPessoas.add(pessoa);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
            e.printStackTrace();
            // Propaga a exceção para a camada anterior
            throw e;
        }

        // Retorna a lista completa (pode estar vazia se não houver registros)
        return listaPessoas;
    }

    public boolean excluir(String cpf) throws SQLException {

        String sql = "DELETE FROM pessoas WHERE cpf = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, cpf);

            int linhasAfetadas = pstm.executeUpdate();

            return linhasAfetadas > 0;

        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean salvar(Pessoa pessoa) throws SQLException {

        String sql = "INSERT INTO pessoas (nome, cpf, isAdm) VALUES (?, ?, ?)";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            // ALTERAÇÃO: O CPF não é mais limpo. É salvo com a formatação (ex:
            // 100.000.000-00)
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

        // TRUQUE SQL: Removemos pontos (.) e traços (-) do banco antes de comparar
        // Assim, tanto faz se está salvo formatado ou limpo, ele compara só os números.
        String sql = "SELECT nome, cpf, isAdm FROM pessoas WHERE REPLACE(REPLACE(cpf, '.', ''), '-', '') = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            // Garantimos que o CPF que estamos enviando também tem apenas números
            String cpfApenasNumeros = cpf.replaceAll("[^0-9]", "");

            pstm.setString(1, cpfApenasNumeros);

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

    // Adicione este método na classe PessoaDAO
    public boolean atualizar(Pessoa pessoa) throws SQLException {
        // Atualiza nome e isAdm baseado no CPF (que não muda)
        String sql = "UPDATE pessoas SET nome = ?, isAdm = ? WHERE cpf = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, pessoa.getNome());
            pstm.setBoolean(2, pessoa.isAdm());
            pstm.setString(3, pessoa.getCpf()); // Usa o CPF para achar quem atualizar

            pstm.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}