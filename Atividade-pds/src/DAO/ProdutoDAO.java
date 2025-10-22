package DAO;

import SMModel.Produto;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ProdutoDAO {

    public void salvar(Produto produto) throws SQLException {
        String sql = "INSERT INTO produtos (nome, preco_venda, preco_compra, quantidade) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            
            pstm.setString(1, produto.getProduto());
            pstm.setFloat(2, produto.getPreco());
            pstm.setFloat(3, produto.getPrecoCompra());
            pstm.setInt(4, produto.getQuantidade());

            pstm.executeUpdate();
            System.out.println("Produto salvo com sucesso!");

        } catch (SQLException e) {
            System.err.println("Erro ao salvar produto: " + e.getMessage());
            e.printStackTrace(); // Alteração: Adiciona o stack trace para depuração
            throw e; 
        }
    }

    public ArrayList<Produto> listarTodos() throws SQLException {
        ArrayList<Produto> listaDeProdutos = new ArrayList<>();
        String sql = "SELECT nome, preco_venda, preco_compra, quantidade FROM produtos";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql);
                ResultSet rs = pstm.executeQuery()) {

            while (rs.next()) {
                Produto produto = new Produto();

                // Mapeamento: Coluna SQL -> Setter Java
                produto.setProduto(rs.getString("nome"));
                produto.setPreco(rs.getFloat("preco_venda"));
                produto.setPrecoCompra(rs.getFloat("preco_compra"));
                produto.setQuantidade(rs.getInt("quantidade"));

                listaDeProdutos.add(produto);
            }
        } 
        return listaDeProdutos;
    }

   
    public void editar(String nomeAntigo, Produto produtoNovo) throws SQLException {
        String sql = "UPDATE produtos SET nome = ?, preco_venda = ?, preco_compra = ?, quantidade = ? WHERE nome = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, produtoNovo.getProduto()); // Novo Nome
            pstm.setFloat(2, produtoNovo.getPreco()); // Novo Preço Venda
            pstm.setFloat(3, produtoNovo.getPrecoCompra()); // Novo Preço Compra
            pstm.setInt(4, produtoNovo.getQuantidade()); // Nova Quantidade
            pstm.setString(5, nomeAntigo); // Nome Antigo para a cláusula WHERE

            pstm.executeUpdate();
            System.out.println("Produto editado com sucesso!");
        }
        // Este método já propaga SQLException
    }

   
    public void remover(String nomeProduto) throws SQLException {
        String sql = "DELETE FROM produtos WHERE nome = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setString(1, nomeProduto);
            pstm.executeUpdate();
            System.out.println("Produto removido com sucesso!");
        }
        // Este método já propaga SQLException
    }
}