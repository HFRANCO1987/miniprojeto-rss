import java.io.Serializable;

/**
 * Representa os episodios a serem baixadoss
 */
public class DownloadEpisodios implements Serializable {

	private static final long serialVersionUID = 1929835536672294148L;
	
	private String url;
	private String extensao;
	private String titulo;

	public DownloadEpisodios() {
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getExtensao() {
		return extensao;
	}

	public void setExtensao(String extensao) {
		this.extensao = extensao;
	}

	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	
}
