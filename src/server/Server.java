package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import common.Transport;

public class Server implements AutoCloseable {

    private ServerSocket serverSocket;
    private ExecutorService executorService;

    @Override
    public void close() throws Exception {
        stop();
    }

    public void start(int port) throws IOException {
        stop();
        serverSocket = new ServerSocket(port);
        executorService = Executors.newFixedThreadPool(10 * Runtime.getRuntime().availableProcessors());
        final Map<String, Socket> clients = Collections.synchronizedMap(new HashMap<String, Socket>());
        executorService.execute(() -> {
            while (serverSocket != null && !serverSocket.isClosed()) {
                try {
                    final Socket client = serverSocket.accept();

                    executorService.submit(() -> {

                        try {
                            while (serverSocket != null && !serverSocket.isClosed()) {
                            String cheie = Transport.receive(client);
                            String[] items = cheie.strip().split("\\s");

                                //trimite o cheie unica
                                if (items.length == 1 && clients.get(items[0]) == null) {
                                    //l-am inregistrat cu cheia sa unica
                                    clients.put(items[0], client);
                                    Transport.send("ai ales un identificator unic, acum poti trimite alte comenzi", client);
                                    List<String> mesajeClient = new ArrayList<String>();
                                    while (serverSocket != null && !serverSocket.isClosed()) {
                                        //asteptam comenzi


                                        String message = Transport.receive(client);
                                        String[] componente = message.strip().split("\\s");

                                        //comanda pentru obtinerea listei de participanti
                                        if (componente.length == 1 && componente[0].equals("getParticipanti")) {
                                            StringBuilder builder = new StringBuilder();
                                            for (String indentificator : clients.keySet()) {
                                                builder.append(indentificator);
                                                builder.append(" ");
                                            }
                                            Transport.send(builder.toString(), client);
                                        } else if (componente.length == 1 && componente[0].equals("getListaMesaje")) {
                                            StringBuilder builder2 = new StringBuilder();

                                            for(String mesaj: mesajeClient){
                                                builder2.append(mesaj);
                                            }
                                            Transport.send(builder2.toString(), client);

                                        } else if (componente.length == 2 && componente[0].equals("getMesaj")) {

                                            try {
                                                if (mesajeClient.size() <=Integer.parseInt(componente[1])) {
                                                    Transport.send("nu aveti mesajul cu acest numar", client);

                                                } else {
                                                    Transport.send(mesajeClient.get(Integer.parseInt(componente[1])), client);
                                                }
                                            } catch (Exception e) {
                                                Transport.send(e, client);
                                            }


                                        } else if (componente.length >= 2 && clients.get(componente[0]) != null) {
                                            //aici trimitem un mesaj propriu zis

                                            StringBuilder builder = new StringBuilder();
                                            for (int i = 1; i < componente.length; i++) {
                                                builder.append(componente[i]);
                                                builder.append(" ");
                                            }
                                            Transport.send("mesaj de la "+items[0]+": "+builder.toString(), clients.get(componente[0]));
                                            mesajeClient.add("catre: " + componente[0] + "\n" + "continut mesaj: " + builder.toString()+"\n");

                                        } else if (componente.length >= 2 && clients.get(componente[0]) == null) {
                                            Transport.send("primul parametru nu este o cheie de participant, puteti folosi getParticipanti pentru a " +
                                                    "obitine lista", client);
                                        }
                                        else {
                                            Transport.send("Comenzi disponibile: "+"\n"+
                                                    "getParticipanti\n"+
                                                    "getListaMesaje\n"+
                                                    "getMesaj index\n"+
                                                    "destinatar mesaj\n", client);
                                        }

                                    }

                                } else if (items.length > 1) {
                                    Transport.send("trimite doar un string unic", client);
                                } else if (items.length == 1 && clients.get(items[0]) != null) {
                                    Transport.send("cheia ta nu este unica, trimite alta", client);
                                }
                            }

                        } catch (ClassNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } finally {
                            clients.remove(client);
                        }
                    });
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
        if (executorService != null) {
            executorService.shutdown();
        }
    }

}