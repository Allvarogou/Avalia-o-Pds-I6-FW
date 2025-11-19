package SMModel;

public class Venda {
    private int id;
    private String nomeCliente;
    private String cpfCliente;
    private double total;
    private String dataVenda; // Pode ser String ou LocalDateTime

    public Venda(int id, String nomeCliente, String cpfCliente, double total, String dataVenda) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.cpfCliente = cpfCliente;
        this.total = total;
        this.dataVenda = dataVenda;
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getCpfCliente() {
        return cpfCliente;
    }

    public double getTotal() {
        return total;
    }

    public String getDataVenda() {
        return dataVenda;
    }
}