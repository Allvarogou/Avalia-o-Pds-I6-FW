package SMModel;

public class Pessoa {
    private String nome;
    private String cpf;
    private boolean isAdm;

    public Pessoa(String nome, String cpf, boolean isAdm) {
        this.nome = nome;
        this.cpf = cpf;
        this.isAdm = isAdm;
    }

    public Pessoa() {
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public boolean isAdm() {
        return isAdm;
    }

    public void setAdm(boolean isAdm) {
        this.isAdm = isAdm;
    }

}
