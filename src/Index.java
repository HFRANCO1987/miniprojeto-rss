import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;


public class Index {

	public static void main(String[] args) throws IllegalArgumentException, FeedException, IOException, ParseException {
		
//		SyndFeed feed = utilFeed.lerFeed("http://leopoldomt.com/if710/fronteirasdaciencia.xml");

		UtilFeed utilFeed = new UtilFeed();
		
		Scanner sc = new Scanner(System.in);
		
		ConteudoFeed ultimaPublicacao = null;
		boolean isUrlValida = false;
		SyndFeed feed = null;
		System.out.println("Informa a url do feed para realização da requisição:");
		
		while (!isUrlValida) {
			try {
				String urlFeed = sc.nextLine();
				feed = utilFeed.lerFeed(urlFeed);
				ultimaPublicacao = utilFeed.obterUltimoEpisodioPublicado(feed.getEntries());
				imprimirUltimaPublicacao(ultimaPublicacao);
				isUrlValida = true;
			}catch (Exception e) { // O programa não deve ser encerrado caso um endereço de feed inválido seja informado
				System.out.println("Url inválida! Informe uma url válida para realização da requisição:");
				isUrlValida = false;
			}
		}
		
		String opcao = "";
		boolean opcaoValida = false;
		System.out.println("Favor informar ação desejada: D(Download de episódios) ou B(Buscar episódios)");
		opcao = sc.next();
		opcaoValida = opcao.toLowerCase().equals("d") || opcao.toLowerCase().equals("b") ? true : false; 
		
		while(opcaoValida==false) {
			System.out.println("Opção inválida! Informe D(Download de episódios) ou B(Buscar episódios), qualquer outra será inválida!");
			opcao = sc.next();
			opcaoValida = opcao.toLowerCase().equals("d") || opcao.toLowerCase().equals("b") ? true : false;
		}
		
		tratarOpcaoDownload(utilFeed, opcao, sc, feed.getEntries());
		tratarOpcaoBusca(utilFeed, feed, opcao, sc);
	}

	
	/**
	 * Imprime o ultimo programa pulicado 
	 * 
	 * Após informado o endereço, o arquivo XML deve ser baixado e o programa deve exibir o título (<title>) e data (<pubDate>) do último programa publicado no feed
	 *
	 * @param ultimaPublicacao
	 */
	private static void imprimirUltimaPublicacao(ConteudoFeed ultimaPublicacao) {
		System.out.println("|---------- Último episódio publicado ----------|");
		System.out.println(" Titulo: " + ultimaPublicacao.getTitle());
		System.out.println(" Data Públicação: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(ultimaPublicacao.getDataPublicacao()));
		System.out.println("|-----------------------------------------------|");
	}

	
	/**
	 * Faz o tratamento para busca por title, descricao ou data, conforme opção 
	 *
	 * @param utilFeed
	 * @param opcao
	 * @param sc
	 * @param itens
	 */
	private static void tratarOpcaoBusca(UtilFeed utilFeed, SyndFeed feed, String opcao, Scanner sc)
			throws ParseException {
		if (opcao.toLowerCase().equals("b")) { //Se opção de busca for igual a b, será possível buscar por titulo ou data
			String tipoBusca = "";
			boolean busca = false;
			
			System.out.println("Favor informar a forma de Busca: S(busca por titulo) ou D(busca por data)");
			//se o tipo de buca for definido com alguma letra diferente de S ou D o valor é false, com isso o while fica executando até informar o valor S ou D
			tipoBusca = sc.next();
			busca = tipoBusca.toLowerCase().equals("s") || tipoBusca.toLowerCase().equals("d") ? true : false; 
			while(!busca) {
				System.out.println("Opção inválida! S(busca por titulo) ou D(busca por data)");
				tipoBusca = sc.next();
				busca = tipoBusca.toLowerCase().equals("s") || tipoBusca.toLowerCase().equals("d") ? true : false;
			}
			
			
			//Se o tipo de busca for igual a S será feito uma buca no feed pelo campo title e description conforme a palavra digitada
			//Caso contrário será buscado pela data de inicio e data fim informada pelo usuário 
			if (tipoBusca.toLowerCase().equals("s")) {
				String filtro = "";
				System.out.println("Informe uma palavra, a mesma será usada para busca no titulo ou descriçao:");
				while(filtro.isEmpty()) {
					filtro = sc.next();
					if (!filtro.isEmpty()) {
						utilFeed.fazerBuscaPorString(filtro, feed.getEntries());
					}
				}
			}else {
				String dtInicio = "";
				String dtFim = "";
				
				boolean isDtValida = false;
				String mensagemRetorno = "";
				Object[] retorno;
				
				System.out.println("Informe a data de inicio, formato (dd/MM/yyyy)");
				dtInicio = sc.next();
				
				retorno = utilFeed.isDataFormatoValido(dtInicio); //Valida se a data informada é uma data valida ou não
				if (retorno.length > 0) {
					isDtValida = (boolean) retorno[0];
					mensagemRetorno = (String) retorno[1];
					while (!isDtValida) {
						System.out.println("Data inválida: |-- " + mensagemRetorno + "--|");
						System.out.println("Informa a data de inicio, formato (dd/MM/yyyy)");
						dtInicio = sc.next();
						retorno = utilFeed.isDataFormatoValido(dtInicio);
						isDtValida = (boolean) retorno[0];
						mensagemRetorno = (String) retorno[1];
					}
				}
				
				System.out.println("Informe a data fim, formato (dd/MM/yyyy)");
				dtFim = sc.next();
				
				retorno = utilFeed.isDataFormatoValido(dtFim);
				if (retorno.length > 0) {
					isDtValida = (boolean) retorno[0];
					mensagemRetorno = (String) retorno[1];
					while (!isDtValida) {
						System.out.println("Data inválida: |-- " + mensagemRetorno + "--|");
						System.out.println("Informa a data fim, formato (dd/MM/yyyy)");
						dtFim = sc.next();
						retorno = utilFeed.isDataFormatoValido(dtFim);
						isDtValida = (boolean) retorno[0];
						mensagemRetorno = (String) retorno[1];
					}
				}
				utilFeed.fazerBuscaPorData(dtInicio, dtFim, feed.getEntries());
			}
		}
	}

	
	/**
	 * Faz o tratamento para download dos Enclosure conforme url que o mesmo possui 
	 *
	 * @param utilFeed
	 * @param opcao
	 * @param sc
	 * @param itens
	 */
	private static void tratarOpcaoDownload(UtilFeed utilFeed, String opcao, Scanner sc, List<SyndEntry> itens) {
		if (opcao.toLowerCase().equals("d")) { //Se opção informar for igual a D será executada a opção de download
			int qntEpisodios = 0;
			
			boolean isNaoEhNumero = false;
			while(!isNaoEhNumero) { //Enquanto o valor informado não for numero, o while ficara executando
				try {
					System.out.println("Informe a quantidade de episódios que deseja baixar:");
					qntEpisodios = sc.nextInt();
					isNaoEhNumero = true;
				}catch (InputMismatchException e) { // O programa deve perguntar quantos episódios o usuário deseja baixar, e só devem ser aceitos números como entrada
					System.out.println("Só é permitido a digitação de número de 1-9!");
					sc.nextLine();
				}
			}
			
			boolean isBaixarEpisodios = false;
			while(!isBaixarEpisodios) {
				try {
					if(qntEpisodios<=0) { //Se a quantidade episodios a baixar for menor ou igual a zero é necessário inforarm um valor maior que 0
						System.out.println("Deve informar no minimo um episódio!");
						qntEpisodios = sc.nextInt();
						continue;
					}
					
					/**
					 * Considere os cenários extremos, onde o número de episódios a serem baixados é maior do que a quantidade de episódios disponíveis e evite que o programa encerre com falha caso isto aconteça. 
					 */
					if(qntEpisodios>utilFeed.obterTotalEpisodiosDoFeed(itens)){ //Verifica se a quantidade de episodios é maior que o numero de episodios que tem no feed
						System.out.println("Quantidade episódios a ser baixado não pode ser maior \n que o nº de episódios do feed! <Total>" + itens.size());
						System.out.println("Informe a quantidade de episódios deseja baixar:");
						qntEpisodios = sc.nextInt();
						continue;
					}
					isBaixarEpisodios = true;
				}catch (InputMismatchException e) { //	O programa deve perguntar quantos episódios o usuário deseja baixar, e só devem ser aceitos números como entrada
					System.out.println("Só é permitido a digitação de número de 0-9!");
					sc.nextLine();
					isBaixarEpisodios = false;
				}
			}
			List<DownloadEpisodios> lstDownloadEpisodios = new ArrayList<>(); 
			Boolean isFeedInvalido = utilFeed.obterEpisodiosSeremBaixados(qntEpisodios,itens, lstDownloadEpisodios);
			if (isFeedInvalido) {
				System.out.println("Este feed é inválido! Nenhuma tag |--- enclosure ---| foi encontrada!");
			}else {
				if (lstDownloadEpisodios.isEmpty()) {
					System.out.println("Nenhum episódio para ser baixado!");
				}else {
					utilFeed.fazerDownloadDaQuantidadeDeEpisodiosInformada(lstDownloadEpisodios);
				}
			}
		}
	}

}
