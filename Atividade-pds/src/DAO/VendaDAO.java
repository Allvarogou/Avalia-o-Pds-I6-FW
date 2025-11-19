package DAO;

import SMModel.Produto;
import SMModel.Venda;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.sql.Timestamp;

public class VendaDAO {

    // Lista o histórico
    public ArrayList<Venda> listarTodasVendas() throws SQLException {
        ArrayList<Venda> lista = new ArrayList<>();
        String sql = "SELECT id, cliente_nome, cliente_cpf, total, data_venda FROM vendas ORDER BY id DESC";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql);
                ResultSet rs = pstm.executeQuery()) {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                // Formata a data para ficar bonita na tela
                Timestamp ts = rs.getTimestamp("data_venda");
                String dataFormatada = (ts != null) ? sdf.format(ts) : "Data Inválida";

                Venda v = new Venda(
                        rs.getInt("id"),
                        rs.getString("cliente_nome"),
                        rs.getString("cliente_cpf"),
                        rs.getDouble("total"),
                        dataFormatada);
                lista.add(v);
            }
        }
        return lista;
    }

    // Busca os itens da nota fiscal
    public ArrayList<Produto> listarItensDaVenda(int idVenda) throws SQLException {
        ArrayList<Produto> itens = new ArrayList<>();
        String sql = "SELECT produto_nome, quantidade, preco_unitario FROM itens_venda WHERE venda_id = ?";

        try (Connection conn = ConectioDB.conectar();
                PreparedStatement pstm = conn.prepareStatement(sql)) {

            pstm.setInt(1, idVenda);

            try (ResultSet rs = pstm.executeQuery()) {
                while (rs.next()) {
                    Produto p = new Produto();
                    p.setProduto(rs.getString("produto_nome"));
                    p.setQuantidade(rs.getInt("quantidade"));
                    p.setPreco(rs.getDouble("preco_unitario"));
                    itens.add(p);
                }
            }
        }
        return itens;
    }
}