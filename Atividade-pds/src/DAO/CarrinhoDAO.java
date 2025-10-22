package DAO;

import SMModel.Carrinho;
import SMModel.Pessoa;
import SMModel.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 


public class CarrinhoDAO {

    public boolean finalizarCompra(Pessoa cliente, Carrinho carrinho) throws SQLException {
        
        Connection conn = null;
        PreparedStatement pstm = null;
        
       
        String sqlCarrinho = "INSERT INTO carrinhos (id_pessoa, total_pago) VALUES (?, ?)";
        
        String sqlItens = "INSERT INTO itens_carrinho (id_carrinho, id_produto, quantidade_comprada, preco_unitario_na_compra) VALUES (?, ?, ?, ?)";
        
     
        String sqlEstoque = "UPDATE produtos SET quantidade = quantidade - ? WHERE nome = ?";

        try {
            conn = ConectioDB.conectar();
            
            conn.setAutoCommit(false); 

           
            pstm = conn.prepareStatement(sqlCarrinho, Statement.RETURN_GENERATED_KEYS);
            
            
            int idCliente = 1; 

            pstm.setInt(1, idCliente); 
            pstm.setFloat(2, carrinho.calcularTotal()); 
            pstm.executeUpdate();
            
          
            int idCarrinho = 0;
            try (ResultSet rs = pstm.getGeneratedKeys()) {
                if (rs.next()) {
                    idCarrinho = rs.getInt(1);
                }
            }
            pstm.close(); 
            
            
            for (Produto item : carrinho.getProdutosCarrinho()) {
                
            
                pstm = conn.prepareStatement(sqlEstoque);
                pstm.setInt(1, item.getQuantidade());     
                pstm.setString(2, item.getProduto());     
                pstm.executeUpdate();
                pstm.close();
                
                
                pstm = conn.prepareStatement(sqlItens);
                pstm.setInt(1, idCarrinho);
    
                pstm.setInt(2, 1); 
                pstm.setInt(3, item.getQuantidade());
                pstm.setFloat(4, item.getPreco()); 
                pstm.executeUpdate();
                pstm.close();
            }

            
            conn.commit(); 
            return true;
            
        } catch (SQLException e) {
            System.err.println("Erro na transação de compra. Desfazendo alterações: " + e.getMessage());
            e.printStackTrace(); // Alteração: Adiciona o stack trace para depuração
            
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Erro ao desfazer transação: " + rollbackEx.getMessage());
                    rollbackEx.printStackTrace(); // Adiciona o stack trace para depuração
                }
            }
            throw e;        } finally {
            
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Erro ao fechar conexão: " + closeEx.getMessage());
                }
            }
        }
    }
}