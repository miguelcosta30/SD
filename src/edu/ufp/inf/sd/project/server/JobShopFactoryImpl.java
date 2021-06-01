package edu.ufp.inf.sd.project.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class JobShopFactoryImpl extends UnicastRemoteObject implements JobShopFactoryRI {
    private DBMockup dbMockup;
    private HashMap<String, JobShopSessionImpl> match_userSession;

    public JobShopFactoryImpl() throws RemoteException {
        super();
        this.dbMockup = new DBMockup(); //base de dados Mockup
        this.match_userSession = new HashMap<>(); //para fazer mapeamento de utlizadores e sessoes
    }

    /**
     * Regista utilizador na DBMockUp
     * @param username username a registar
     * @param password password a registar
     * @return false - Não registado | true - registado
     * @throws RemoteException
     */
    @Override
    public boolean register(String username, String password) throws RemoteException {
            return this.dbMockup.register(username,password);
    }

    /**
     * faz o login do user
     * @param username user para fazer login
     * @param password password do user
     * @return retorna sessao se tiver, retorna null se nao tiver
     * @throws RemoteException
     */
    @Override
    public JobShopSessionRI login(String username, String password) throws RemoteException {
        if(this.dbMockup.exists(username,password))  {
            if(match_userSession.containsKey(username)) {
                return match_userSession.get(username);
            } else {
                JobShopSessionImpl jobShopSession = new JobShopSessionImpl(this,username);
                match_userSession.put(username,jobShopSession);
                return jobShopSession;
            }
        } else {
            return null;
        }
    }

    /**
     * apaga sessao do utilizador
     * @param u
     * @throws RemoteException
     */
    @Override
    public void destroySession (String u) throws RemoteException {
        this.match_userSession.remove(u);
    }

    public DBMockup getDbMockup() { return dbMockup; }

    /**
     * transacao de creditos
     * @param username usernam para fazer a transacao de cretidos
     * @param credits creditos a adicionar ao user
     * @return true - bem sucedido | false - mal sucedido
     */
    @Override
    public boolean creditTrasaction(String username, int credits) {
        User u = this.getDbMockup().getUser(username);
        if(u != null) {
            if(u.getCredits() + credits >= 0) {
                u.setCredits(u.getCredits() + credits);
                return true;
            }
        }
        return false;
    }

}
