package tvHormigas;

public abstract class tvHormiga implements tvIHormiga {
    private String tipoHormiga;
    private Boolean estadoHormiga;

    public String tvgetTipoHormiga() {
        return tipoHormiga;
    }
    public Boolean tvgetEstadoHormiga() {
        return estadoHormiga;
    }
    

}
