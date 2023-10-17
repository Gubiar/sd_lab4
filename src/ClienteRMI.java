/**
 * Laboratorio 4
 * Autor: Lucio Agostinho Rocha
 * Ultima atualizacao: 04/04/2023
 */

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class ClienteRMI {

    public static void main(String[] args) {

        //TODO: Obter a Lista de pares disponiveis do arquivo Peer.java

        List<Peer> listaPeers = new ArrayList<Peer>();
        listaPeers.add(Peer.PEER1);
        listaPeers.add(Peer.PEER2);
        listaPeers.add(Peer.PEER3);


        try {

            Registry registro = LocateRegistry.getRegistry("127.0.0.1", 1099);


            //Escolhe um peer aleatorio da lista de peers para conectar
            SecureRandom sr = new SecureRandom();

            IMensagem stub = null;
            Peer peer = null;

            boolean conectou=false;
            while(!conectou){
                peer = listaPeers.get(sr.nextInt(listaPeers.size()));
                try{
                    stub = (IMensagem) registro.lookup(peer.getNome());
                    conectou=true;
                } catch(java.rmi.ConnectException e){
                    System.out.println(peer.getNome() + " indisponivel. ConnectException. Tentanto o proximo...");
                } catch(java.rmi.NotBoundException e){
                    System.out.println(peer.getNome() + " indisponivel. NotBoundException. Tentanto o proximo...");
                }
            }
            System.out.println("Conectado no peer: " + peer.getNome());


            String opcao="";
            Scanner leitura = new Scanner(System.in);

            Boolean isRunning = true;
            while (isRunning) {
                System.out.println("1) Read");
                System.out.println("2) Write");
                System.out.println("3) Exit");
                System.out.print(">> ");
                opcao = leitura.next();


                switch(opcao) {
                    case "1": {
                        leitura.nextLine(); //Consome o input anteiror (limpa o que o usuário digitou)
                        Mensagem mensagem = new Mensagem("", "read");
                        System.out.println(mensagem.getMensagem());
                        Mensagem resposta = stub.enviar(mensagem); //Envio para o servidor
                        System.out.println("Resultado:");
                        System.out.println(resposta.getMensagem());

                        break;
                    }
                    case "2": {
                        leitura.nextLine(); //Consome o input anteiror (limpa o que o usuário digitou)
                        System.out.print("Add fortune: ");
                        String fortune = leitura.nextLine();
                        Mensagem mensagem = new Mensagem(fortune, "write");
                        System.out.println(mensagem.getMensagem());

                        Mensagem resposta = stub.enviar(mensagem); //Envio para o servidor

                        System.out.println(resposta.getMensagem());

                        break;
                    }
                    case "3": {
                        System.out.print("Processo finalizado!\n");
                        isRunning = false;
                        break;
                    }
                    default: {
                        System.out.print("Digite uma opção válida!\n");
                    }
                }
            }

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

}