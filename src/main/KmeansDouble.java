package main;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;

public class KmeansDouble {
	/**
	 * matriz de dados que o k-means receber�, cada linha um documento, cada
	 * coluna uma palavra
	 */
	private double[][] dados;

	/**
	 * quantidade k de prototipos passada
	 */
	private int k;
	
	/**
	 * matriz com os prot�tipos int[k][numeroDimensoes];
	 */
	private double[][] prototipos;
	
	/**
	 * matriz com a dist�ncia euclidiana entre os documentos e os prot�tipos
	 */
	private double[][] distanciasEuclidianas;

	/**
	 * matriz binaria de parti��o int[k][numeroLinhas]
	 */
	private int[][] matrizParticao;
	
	/**
	 * quantiza��o do erro de agrupamento
	 */
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
	 * valores m�ximos dos n�meros que podem ser escolhidos aleatoriamente para a
	 * cria��o dos prototipos
	 */
	private double[] max;
	
	public KmeansDouble(String arquivo, int k, int linhas, int colunas) {
		try {
			BufferedReader leitor = new BufferedReader(new FileReader(arquivo));
			/*
			 * Para popular a matriz dados � necess�rio saber o n�mero de linhas
			 * e o n�mero de dimens�es do corpus recebido
			 */
			numeroLinhas = linhas;
			numeroDimensoes = colunas;
			jcm = 0;
			this.k = k;


			/*
			 * Inicializa��o das matrizes
			 */
			dados = new double[numeroLinhas][numeroDimensoes];
			prototipos = new double[k][numeroDimensoes];
			distanciasEuclidianas = new double[numeroLinhas][k];
			matrizParticao = new int[k][numeroLinhas];
			
			/*
			 *  Leitura do arquivo e popula��o da matriz de dados
			 */
			max = new double[numeroDimensoes];
			String linha = null;
			String[] numColunas = null;
			int i = 0;
			int j = 0;
			/*
			 * L� a linha que cont�m o nome das palavras
			 */
			leitor.readLine();
			
			while ((linha = leitor.readLine()) != null) {
				numColunas = linha.split(",");
				for(int co = 1; co <= colunas; co++) {
					dados[i][j] = Double.parseDouble(numColunas[co]);
					/*
					 * Os valores m�ximos j� ser�o descobertos simultaneamente a popula��o
					 */
					if (dados[i][j] > max[j]) {
						max[j] = dados[i][j];
					}
					j++;
				}
				j = 0;
				i++;
			}
			
			leitor.close();
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		

	}
	
	/**
	 * inicializa aleatoriamente os prototipos na primeira itera��o
	 * considerando o valor m�ximo de cada dimens�o
	 * @return
	 */
	public double[][] inicializarPrototipos() {
		double min = 0;
		double randomNumber = 0;
		Random rand = new Random();
		for(int prototipo = 0; prototipo < k; prototipo++){
			for(int palavra = 0; palavra < numeroDimensoes; palavra++){
				randomNumber = rand.nextDouble();
				randomNumber = min + (randomNumber * (max[palavra] - min));
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
		/*
		 *  percorrendo prototipos
		 */
		for (int prototipo = 0; prototipo < k; prototipo++) {
			/*
			 *  percorrendo todos documentos para cada prototipo
			 */
			for (int documento = 0; documento < numeroLinhas; documento++) {
				/*
				 *  c�lculo da dist�ncia para um documento
				 */
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
			/*
			 *  adiciona o valor um a matriz de particao no local marcando o
			 *  cluster a qual pertence
			 */
			matrizParticao[cluster][documento] = 1;
		}

	}
	
	/**
	 * zera matriz de parti��o
	 */
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
	
	public double [][] redefinirPrototipos() {
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
			/*
			 *  evitar erro aritim�tico de divis�o por 0				
			 */
			if(integrantes>0){
				for (int palavra = 0; palavra < numeroDimensoes; palavra++) {
					prototipos[prototipo][palavra] = (prototipos[prototipo][palavra]/integrantes);
				}
			}
			integrantes = 0;
		}
		return prototipos;
	}
	
	/** 
	 * zera matriz de prototipos
	 * @param prototipo
	 */
	private void inicializarPrototipo(int prototipo) {
		for (int j = 0; j < numeroDimensoes; j++) {
			prototipos[prototipo][j] = 0;
		}
	}
	
	/** 
	 * dist�ncia euclidiana entre os prototipos da itera��o anterior com
	 * os da itera��o atual para saber a movimenta��o que ocorreu
	 * @param prototiposAnterior
	 * @return
	 */
	public double diferencaPrototipos(double [][] prototiposAnterior) {
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
	
	public double[][] getPrototipos() {
		return this.prototipos;
	}


	public int[][] getMatrizParticao() {
		return matrizParticao;
	}

}
