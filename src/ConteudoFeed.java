import java.io.Serializable;
import java.util.Date;

/**
 * Classe com dados de titulo e data publicação, 
 * será utilizada também para ultima episodio publicado
 */
public class ConteudoFeed implements Serializable {

	private static final long serialVersionUID = -4048653962410100023L;

	private String title;
	private Date dataPublicacao;

	public ConteudoFeed() {
	}

	public ConteudoFeed(String title, Date dataPublicacao) {
		this.title = title;
		this.dataPublicacao = dataPublicacao;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDataPublicacao() {
		return dataPublicacao;
	}

	public void setDataPublicacao(Date dataPublicacao) {
		this.dataPublicacao = dataPublicacao;
	}

}
