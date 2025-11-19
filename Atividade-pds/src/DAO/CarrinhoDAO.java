package DAO;

import SMModel.Carrinho;
import SMModel.Pessoa;
import SMModel.Produto;
import java.sql.*;

public class CarrinhoDAO {

    public boolean finalizarCompra(Pessoa cliente, Carrinho carrinho) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmVenda = null;
        PreparedStatement pstmItem = null;
        PreparedStatement pstmEstoque = null;
        ResultSet rsKeys = null;

        String sqlVenda = "INSERT INTO vendas (cliente_nome, cliente_cpf, total, data_venda) VALUES (?, ?, ?, ?)";
        String sqlItem = "INSERT INTO itens_venda (venda_id, produto_nome, quantidade, preco_unitario) VALUES (?, ?, ?, ?)";
        String sqlEstoque = "UPDATE produtos SET quantidade = quantidade - ? WHERE nome = ?";

        try {
            conn = ConectioDB.conectar();
            // Desliga o salvamento automático para garantir que tudo seja salvo junto
            // (Transação)
            conn.setAutoCommit(false);

            // 1. INSERIR A VENDA (CABEÇALHO)
            pstmVenda = conn.prepareStatement(sqlVenda, Statement.RETURN_GENERATED_KEYS);
            pstmVenda.setString(1, cliente.getNome());
            pstmVenda.setString(2, cliente.getCpf());
            pstmVenda.setDouble(3, carrinho.calcularTotal());
            // Pega a data/hora atual
            pstmVenda.setTimestamp(4, new java.sql.Timestamp(System.currentTimeMillis()));

            pstmVenda.executeUpdate();

            // Pega o ID gerado para essa venda (ex: Venda número 50)
            rsKeys = pstmVenda.getGeneratedKeys();
            int idVenda = 0;
            if (rsKeys.next()) {
                idVenda = rsKeys.getInt(1);
            } else {
                throw new SQLException("Falha ao criar venda, nenhum ID obtido.");
            }

            // 2. INSERIR OS ITENS E ATUALIZAR ESTOQUE
            pstmItem = conn.prepareStatement(sqlItem);
            pstmEstoque = conn.prepareStatement(sqlEstoque);

            for (Produto p : carrinho.getProdutosCarrinho()) {
                // Salva o item no histórico
                pstmItem.setInt(1, idVenda);
                pstmItem.setString(2, p.getProduto()); // Nome do produto
                pstmItem.setInt(3, p.getQuantidade()); // Quantidade comprada
                pstmItem.setDouble(4, p.getPreco()); // Preço pago
                pstmItem.executeUpdate();

                // Baixa no estoque
                pstmEstoque.setInt(1, p.getQuantidade()); // Quantidade a diminuir
                pstmEstoque.setString(2, p.getProduto()); // Qual produto (pelo nome)
                pstmEstoque.executeUpdate();
            }

            // Se chegou até aqui sem erro, confirma tudo
            conn.commit();
            return true;

        } catch (SQLException e) {
            // Se deu erro, desfaz tudo
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            throw e;
        } finally {
            // Fecha tudo
            if (rsKeys != null)
                rsKeys.close();
            if (pstmVenda != null)
                pstmVenda.close();
            if (pstmItem != null)
                pstmItem.close();
            if (pstmEstoque != null)
                pstmEstoque.close();
            if (conn != null)
                conn.close();
        }
    }
}