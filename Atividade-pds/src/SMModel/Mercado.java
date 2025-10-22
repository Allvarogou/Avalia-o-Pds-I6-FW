package SMModel;

import DAO.ProdutoDAO;
import java.util.ArrayList;

public class Mercado {
    
   
    private ProdutoDAO produtoDAO;

    public Mercado() {
        
        this.produtoDAO = new ProdutoDAO();
    }
    
    
    public ArrayList<Produto> listarProdutos() throws Exception {
 
        try {
            return produtoDAO.listarTodos(); 
        } catch (Exception e) {
            throw new Exception("Falha ao buscar produtos no estoque: " + e.getMessage());
        }
    }
    
    

    public void adicionarProduto(Produto produto) {
        
        try {
            produtoDAO.salvar(produto);
        } catch (Exception e) {
            System.err.println("Erro ao adicionar produto: " + e.getMessage());
        }
    }

    public void removerProduto(String nomeProduto) {
        
        try {
            produtoDAO.remover(nomeProduto);
        } catch (Exception e) {
            System.err.println("Erro ao remover produto: " + e.getMessage());
        }
    }

  
    public void editarProduto(String nomeAntigo, Produto produtoNovo) {
        
        try {
            produtoDAO.editar(nomeAntigo, produtoNovo);
        } catch (Exception e) {
            System.err.println("Erro ao editar produto: " + e.getMessage());
        }
    }

}