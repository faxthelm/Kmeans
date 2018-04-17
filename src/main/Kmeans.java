package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Random;

public class Kmeans {

	/**
	 * matriz de dados que o k-means receberá, cada linha um documento, cada
	 * coluna uma palavra
	 */
	private int[][] dados;

	/**
	 * quantidade k de prototipos passada
	 */
	private int k;
	
	/**
	 * matriz com os protótipos int[k][numeroDimensoes];
	 */

	private int[][] prototipos;

	/**
	 * matriz com a distância euclidiana entre os documentos e os protótipos
	 */

	private double[][] distanciasEuclidianas;


	/**
	 * matriz binaria de partição int[k][numeroLinhas]
	 */
	private int[][] matrizParticao;
	
	
	private double jcm;

	/**
	 * equivale ao numero de documentos
	 */
	private int numeroLinhas;
	
	/**
	 * equivale ao numero de palavras analisadas nos documentos
	 */
	private int numeroDimensoes;

	/**
	 * valor mávalorDadomo dos números que podem ser escolhidos aleatoriamente para a
	 * criação dos prototipos
	 */
	private int[] max;


	public Kmeans(String arquivo, int k, int linhas, int colunas) {
		try {
			BufferedReader leitor = new BufferedReader(new FileReader(arquivo));

			/*
			 * Para popular a matriz dados é necessário saber o número de linhas
			 * e o número de dimensões do corpus recebido
			 */
			numeroLinhas = linhas;
			numeroDimensoes = colunas;
			jcm = 0;
			this.k = k;


			// inicialização das matrizes
			dados = new int[numeroLinhas][numeroDimensoes];
			prototipos = new int[k][numeroDimensoes];
			distanciasEuclidianas = new double[numeroLinhas][k];
			matrizParticao = new int[k][numeroLinhas];

			// Leitura do arquivo e população da matriz de dados
			// O valor mávalorDadomo já será descoberto simultaneamente a população
			max = new int[numeroDimensoes];
			String linha = null;
			String[] numColunas = null;
			int i = 0;
			int j = 0;
			int co = 1;
			//Le a linha que contem o nome das palavras
			leitor.readLine();
			
			while ((linha = leitor.readLine()) != null) {
				numColunas = linha.split(",");
				for(;co <= colunas; co++) {
					dados[i][j] = Integer.parseInt(numColunas[co]);
					if (dados[i][j] > max[j]) {
						max[j] = dados[i][j];
					}
					j++;
				}
				j = 0;
				co = 1;
				i++;
			}
			
			leitor.close();
			
		} catch (Exception e) {

		}
	}


	public int[][] inicializarPrototipos() {
		int randomNumber = 0;
		Random rand = new Random();
		for(int prototipo = 0; prototipo < k; prototipo++){
			for(int palavra = 0; palavra < numeroDimensoes; palavra++){
				randomNumber = rand.nextInt(max[palavra]);
				System.out.println(randomNumber + "  " + max[palavra]);
				prototipos[prototipo][palavra] = randomNumber;
			}
		}
		return prototipos;
	}
	
	
	public void definirDistanciasEuclidianas() {
		double soma = 0;
		double diferencaQuadrado = 0;
		double valorDado = 0;
		double valorPrototipo = 0;
		// percorrendo prototipos
		for (int prototipo = 0; prototipo < k; prototipo++) {
			// percorrendo documentos
			// todos para cada prototipo
			for (int documento = 0; documento < numeroLinhas; documento++) {
				// calculo da distancia para um documento
				for(int palavra = 0; palavra < numeroDimensoes; palavra++){
					valorDado = dados[documento][palavra];
					valorPrototipo = prototipos[prototipo][palavra];
					diferencaQuadrado = (valorDado - valorPrototipo) * (valorDado - valorPrototipo);
					soma = soma + diferencaQuadrado;
				}
				distanciasEuclidianas[documento][prototipo] = Math.sqrt(soma);
				soma = 0;
			}
		}
	}
	
	public void clustering() {
		inicializarMatrizParticao();
		double menorDistancia = 0;
		double atual = 0;
		int cluster = 0;
		for (int documento = 0; documento < numeroLinhas; documento++) {
			/*
			 *  Calcula qual o prototipo com menor distancia e assim define um
			 *  cluster para o dado
			 */
			for (int prototipo = 0; prototipo < k; prototipo++) {
				atual = distanciasEuclidianas[documento][prototipo];
				if (prototipo == 0) {
					menorDistancia = atual;
					cluster = prototipo;
				}
				if (atual < menorDistancia) {
					menorDistancia = atual;
					cluster = prototipo;
				}
			}
			// adiciona o valor um a matriz de particao no local marcando o
			// cluster a qual pertence
			matrizParticao[cluster][documento] = 1;
		}

	}
	private void inicializarMatrizParticao() {

		for (int i = 0; i < k; i++) {
			for (int j = 0; j < numeroLinhas; j++) {
				matrizParticao[i][j] = 0;
			}
		}
	}


	public double calcularJCM() {
		double jcmAtual = 0;
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {

				jcmAtual = jcmAtual
						+ (matrizParticao[prototipo][documento] * (distanciasEuclidianas[documento][prototipo] * distanciasEuclidianas[documento][prototipo]));
			}
		}
		return jcmAtual;
	}
	
	public int [][] redefinirPrototipos() {
		int integrantes = 0; 
		for (int prototipo = 0; prototipo < k; prototipo++) {
			for (int documento = 0; documento < numeroLinhas; documento++) {
				if (matrizParticao[prototipo][documento] == 1) {
					if (integrantes == 0){
						inicializarPrototipo(prototipo);
					}
					integrantes++;
					for(int palavra = 0; palavra < numeroDimensoes; palavra++){
						prototipos[prototipo][palavra] = prototipos[prototipo][palavra] + dados[documento][palavra];
					}
					
				}
				
			}
			if(integrantes>0){
				for (int palavra = 0; palavra < numeroDimensoes; palavra++) {
					// evitar erro aritimético de divisão por 0				
					prototipos[prototipo][palavra] = (prototipos[prototipo][palavra]/integrantes);
				}
			}
			integrantes = 0;
		}
		return prototipos;
	}

	private void inicializarPrototipo(int prototipo) {
		for (int j = 0; j < numeroDimensoes; j++) {
			prototipos[prototipo][j] = 0;
		}
	}
	
	public double diferencaPrototipos(int [][] prototiposAnterior) {
		double resposta = 0;
		double soma = 0;
		for(int prototipo = 0; prototipo<k; prototipo++){
			for(int palavra = 0; palavra<numeroDimensoes; palavra++){
				soma = soma + (prototipos[prototipo][palavra] - prototiposAnterior[prototipo][palavra]) * (prototipos[prototipo][palavra] - prototiposAnterior[prototipo][palavra]);
			}
			resposta = resposta + Math.sqrt(Math.abs(soma));
			
			soma = 0;
		}
		resposta = resposta/k;
		return resposta;
	}


	public int[][] getPrototipos() {
		return this.prototipos;
	}


	public int[][] getMatrizParticao() {
		return matrizParticao;
	}


	
	
	
}
