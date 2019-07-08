import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;

import com.rometools.rome.feed.synd.SyndEnclosure;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;

/**
 * Classe com metodos utilitarios para manipulação de feed e episodios
 */
public class UtilFeed {

	//Faz a requisição para obter os dados do feed
	public SyndFeed lerFeed(String url) throws IllegalArgumentException, FeedException, IOException {
		URL feedSource = new URL(url);
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = input.build(new XmlReader(feedSource));
		return feed;
	}
	
	//Após informado o endereço, o arquivo XML deve ser baixado e o programa deve exibir o título (<title>) e data (<pubDate>) do último programa publicado no feed.
	public ConteudoFeed obterUltimoEpisodioPublicado(List<SyndEntry> itens) {
		Optional<SyndEntry> item = itens.stream().max(Comparator.comparing(SyndEntry::getPublishedDate));
		if (item.isPresent()) {
			return new ConteudoFeed(item.get().getTitleEx().getValue(), item.get().getPublishedDate());
		}
		return new ConteudoFeed();
	}

	/**
	 * A quantidade de episodios será a mesma quantidade de theads criadas 
	 * 
	 * Após informado este número, devem ser criadas threads para download de cada episódio (cada download deve ser executado em uma thread separada). Observe a tagenclosure no arquivo XML baixado, para saber os links dos episódios.
	 *
	 * Use streams para processar texto;
	 * @param listDownloadEpisodios
	 */
	public void fazerDownloadDaQuantidadeDeEpisodiosInformada(List<DownloadEpisodios> listDownloadEpisodios) {
		System.out.println("Será criado " + listDownloadEpisodios.size() + " thread(s), as mesmas serão executadas em paralelo e ao final de cada download uma mensagem será exibida. \nIniciando o(s) downloads, favor aguarde!");
		for (DownloadEpisodios downloadEpisodios : listDownloadEpisodios) {
			new Thread(() -> {
				synchronized (downloadEpisodios) {
					System.out.printf("|--Thread: " + Thread.currentThread().getName() + " Baixando Episódio: " + downloadEpisodios.getTitulo() + "--| \n");
					this.baixarEpisodio(downloadEpisodios.getUrl(), downloadEpisodios.getTitulo().concat(".").concat(downloadEpisodios.getExtensao()));
					System.out.printf("|-- Thread: " + Thread.currentThread().getName() + " Dowloand do Episódio: " + downloadEpisodios.getTitulo() + " <FINALIZADO COM SUCESSO> --|\n");
				}
			}).start();
		}
	}

	
	/**
	 * Utiliza stream para fazer o filtro (busca) por titulo e descrição.
	 * 
	 * Se o usuário escolher S, o programa deve aguardar pela String a ser pesquisada, e então buscar em todo o feed XML por ocorrências da String nas tags<title> e <description>;
	 * Use streams para processar texto;
	 * @param titulo
	 * @param itens
	 */
	public void fazerBuscaPorString(String titulo, List<SyndEntry> itens) {
		List<SyndEntry> listTitulos = itens.stream()
				.filter(item -> item.getTitleEx().getValue().toLowerCase().contains(titulo.toLowerCase())
						|| item.getDescription().getValue().toLowerCase().contains(titulo.toLowerCase()))
				.collect(Collectors.toList());
		if (listTitulos.isEmpty()) {
			System.out.println("Nenhum episódio foi encontrado com o titulo ou descrição: <---" + titulo + "--->");
		}else {
			System.out.println();
			listTitulos.forEach(
					item -> System.out.println("Titulo: " + item.getTitleEx().getValue() + "\nDescrição: " + item.getDescription().getValue() + "\n"));
		}
	}

	
	/**
	 * Utiliza stream para fazer o filtro (busca) por data
	 *
	 * Se o usuário escolher D, o programa deve pedir por duas datas a serem informadas pelo usuário (formato dia/mes/ano), 
	 * e então buscar no feed XML por todos os episódios publicados entre as datas informadas (vide tag<pubDate>).
	 *
	 * @param dtInicio
	 * @param dtFim
	 * @param itens
	 * @throws ParseException
	 */
	public void fazerBuscaPorData(String dtInicio, String dtFim, List<SyndEntry> itens) throws ParseException {
		Date dataApos = new SimpleDateFormat("dd/MM/yyyy").parse(dtInicio);
		Date dataAntes = new SimpleDateFormat("dd/MM/yyyy").parse(dtFim);

		List<ConteudoFeed> listConteudoFeed = itens.stream()
				.filter(item -> item.getPublishedDate().after(dataApos) && item.getPublishedDate().before(dataAntes))
				.map(item -> new ConteudoFeed(item.getTitle(), item.getPublishedDate())).collect(Collectors.toList());

		System.out.println("Busca por data :" + dtInicio + " - " + dtFim);
		listConteudoFeed.forEach(item -> System.out.println(
				item.getTitle() + " - " + new SimpleDateFormat("dd/MM/yyyy").format(item.getDataPublicacao())));

	}

	/**
	 * Retorna a quantidade de episodios que possui o feed
	 *
	 * @param itens
	 * @return
	 */
	public int obterTotalEpisodiosDoFeed(List<SyndEntry> itens) {
		if (itens != null && !itens.isEmpty()) {
			return itens.size();
		}
		return 0;
	}

	
	/**
	 * Valida se uma determinada string é uma data valida;
	 * Retorno é um array de object, onde a posicao [0] indica se é uma data valida ou não e a posição [1] a mensagem de erro,
	 * sempre que for posicao[0] = false é pq a data é inválida
	 *
	 * @param data
	 * @return
	 */
	public Object[] isDataFormatoValido(String data) {
		Object[] retorno = new Object[2];
		SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		try {
			format.setLenient(false);
			format.parse(data);
			retorno[0] = true;
		} catch (ParseException e) {
			retorno[0] = false;
			retorno[1] = e.getMessage();
		}
		return retorno;
	}

	/**
	 * Utiliza URLConnection para executa a url do enclosure e fazer o download 
	 *
	 * @param urlEpisodio
	 * @param nomeArquivo
	 */
	public void baixarEpisodio(String urlEpisodio, String nomeArquivo) {
		URLConnection connection;
		InputStream is;
		FileOutputStream fos;
		try {
			URL url = new URL(urlEpisodio);
			connection = url.openConnection();
			connection.setDoOutput(true);
			connection.connect();

			is = connection.getInputStream();
			fos = new FileOutputStream(new File(nomeArquivo));
			int inByte;
			while ((inByte = is.read()) != -1)
				fos.write(inByte);
			fos.flush();
			fos.close();
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	
	/**
	 * Cria uma lista de episodios com base na quantidade de episodios para serem baixadoss
	 *
	 * @param qntEpisodios
	 * @param itens
	 * @return
	 */
	public Boolean obterEpisodiosSeremBaixados(int qntEpisodios, List<SyndEntry> itens, List<DownloadEpisodios> listDownloads) {
		Boolean isFeedInvalido = false;
		List<SyndEntry> listComUltimosEpisodiosSelecionados = itens.subList(itens.size() - qntEpisodios,itens.size());
		for (SyndEntry syndEntry : listComUltimosEpisodiosSelecionados) {
			DownloadEpisodios downloadEpisodios = new DownloadEpisodios();
			downloadEpisodios.setTitulo(syndEntry.getTitleEx().getValue());
			for (SyndEnclosure enclosure : syndEntry.getEnclosures()) {
				downloadEpisodios.setExtensao(FilenameUtils.getExtension(enclosure.getUrl()));
				downloadEpisodios.setUrl(enclosure.getUrl());
			}
			if (downloadEpisodios.getExtensao() == null || downloadEpisodios.getExtensao().isEmpty()) {
				isFeedInvalido = Boolean.TRUE;
				break;
			}
			listDownloads.add(downloadEpisodios);
		}
		return isFeedInvalido;
	}
}
