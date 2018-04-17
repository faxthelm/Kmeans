package main;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	// Parâmetros
	// args[0] = nome do arquivo
	// args[1] = k grupos
	// args[2] = taxa de erro
	// args[3] = tipo: binario, tf ou tfidf
	// args[4] = linhas
	// args[5] = colunas
	public static void main(String [] args){
		boolean bomResultado = false; // controla o fato de algoritmo já ter alcançado um bom resultado ou não
		int iteracoes = 0;
		double jcmAtual = 0;
		int[][] prototiposAnteriorInt;
		double[][] prototiposAnteriorDouble;
		double condicaoParada = 0;
		
		//Teste
		int[][] resp = null;
		
		if (args[3].equals("binario") || args[3].equals("tf")) {
			Kmeans kMeans = new Kmeans(args[0], Integer.parseInt(args[1]),
					Integer.parseInt(args[4]), Integer.parseInt(args[5]));
			
			prototiposAnteriorInt = kMeans.inicializarPrototipos();
			
			while(!bomResultado){
				iteracoes++;
				kMeans.definirDistanciasEuclidianas();
				kMeans.clustering();
				jcmAtual = kMeans.calcularJCM();
				kMeans.redefinirPrototipos();
				
				condicaoParada = kMeans.diferencaPrototipos(prototiposAnteriorInt);
				if (condicaoParada < Double.parseDouble(args[2])) {
					bomResultado = true;
					//Teste
					resp = kMeans.getMatrizParticao();
				}
				prototiposAnteriorInt = kMeans.getPrototipos();
			}
		}else{
			
		}
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter("resp.csv"));
			for (int i = 0; i < Integer.parseInt(args[1]); i++) {
				for (int j = 0; j < Integer.parseInt(args[4]); j++) {
					br.append(resp[i][j] + ",");
				}
				br.newLine();
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
}
