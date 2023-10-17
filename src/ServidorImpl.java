/**
 * Laboratorio 4
 * Autor: Lucio Agostinho Rocha
 * Ultima atualizacao: 04/04/2023
 */
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ServidorImpl implements IMensagem{

    ArrayList<Peer> alocados;

    private Principal principal =  new Principal();


    public ServidorImpl() {
        alocados = new ArrayList<>();
    }

    //Cliente: invoca o metodo remoto 'enviar'
    //Servidor: invoca o metodo local 'enviar'
    @Override
    public Mensagem enviar(Mensagem mensagem) throws RemoteException {
        Mensagem resposta;
        try {
            System.out.println("Mensagem recebida: " + mensagem.getMensagem());

            if (mensagem.getOpcao().equals("read")) {
                String fortune = principal.read();
                resposta = new Mensagem(fortune);
            } else if (mensagem.getOpcao().equals("write")) {
                String fortune = parserJSON(mensagem.getMensagem());
                principal.write(fortune);
                resposta = new Mensagem("Fortune added: " + mensagem.getMensagem());
            } else {
                resposta = new Mensagem("{\"result\": false}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resposta = new Mensagem("{\"result\": false}");
        }
        return resposta; //Retorno para o cliente
    }

    public static HashMap<String, String> parseJSONToMap(String jsonString) {
        HashMap<String, String> resultMap = new HashMap<String, String>();

        jsonString = jsonString.replaceAll("[{}\"]", "");
        String[] keyValuePairs = jsonString.split(",");

        for (String pair : keyValuePairs) {
            String[] entry = pair.split(":", 2);
            String key = entry[0].trim();
            String value = entry[1].trim();

            if (value.startsWith("[")) {
                // Handle JSON arrays
                value = value.substring(1, value.length() - 1); // Remove square brackets
                String arrayValues = value;
                resultMap.put(key, arrayValues);
            } else {
                resultMap.put(key, value);
            }
        }

        return resultMap;
    }

    public String parserJSON(String mensagem) throws RemoteException {
        HashMap<String, String> map = parseJSONToMap(mensagem);
        String msg = map.get("args");
        return msg;
    }
    public void iniciar(){

        try {
            //TODO: Adquire aleatoriamente um 'nome' do arquivo Peer.java

            List<Peer> listaPeers = new ArrayList<Peer>();
            listaPeers.add(Peer.PEER1);
            listaPeers.add(Peer.PEER2);
            listaPeers.add(Peer.PEER3);

            Registry servidorRegistro;
            try {
                servidorRegistro = LocateRegistry.createRegistry(1099);
            } catch (java.rmi.server.ExportException e){ //Registro jah iniciado
                System.out.print("Registro jah iniciado. Usar o ativo.\n");
            }
            servidorRegistro = LocateRegistry.getRegistry(); //Registro eh unico para todos os peers
            String [] listaAlocados = servidorRegistro.list();
            for(int i=0; i<listaAlocados.length;i++)
                System.out.println(listaAlocados[i]+" ativo.");

            SecureRandom sr = new SecureRandom();
            Peer peer = listaPeers.get(sr.nextInt(listaPeers.size()));

            int tentativas=0;
            boolean repetido = true;
            boolean cheio = false;
            while(repetido && !cheio){
                repetido=false;
                peer = listaPeers.get(sr.nextInt(listaPeers.size()));
                for(int i=0; i<listaAlocados.length && !repetido; i++){

                    if(listaAlocados[i].equals(peer.getNome())){
                        System.out.println(peer.getNome() + " ativo. Tentando proximo...");
                        repetido=true;
                        tentativas=i+1;
                    }

                }
                //System.out.println(tentativas+" "+listaAlocados.length);

                //Verifica se o registro estah cheio (todos alocados)
                if(listaAlocados.length>0 && //Para o caso inicial em que nao ha servidor alocado,
                        //caso contrario, o teste abaixo sempre serah true
                        tentativas==listaPeers.size()){
                    cheio=true;
                }
            }

            if(cheio){
                System.out.println("Sistema cheio. Tente mais tarde.");
                System.exit(1);
            }

            IMensagem skeleton  = (IMensagem) UnicastRemoteObject.exportObject(this, 0); //0: sistema operacional indica a porta (porta anonima)
            servidorRegistro.rebind(peer.getNome(), skeleton);
            System.out.print(peer.getNome() +" Servidor RMI: Aguardando conexoes...");

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ServidorImpl servidor = new ServidorImpl();
        servidor.iniciar();
    }
}