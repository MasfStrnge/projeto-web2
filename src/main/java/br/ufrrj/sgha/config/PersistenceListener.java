package br.ufrrj.sgha.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Abre o EntityManagerFactory ao subir a aplicação, cadastra os dados iniciais e fecha ao parar. */
@WebListener
public class PersistenceListener implements ServletContextListener {
    private static final Logger LOG = Logger.getLogger(PersistenceListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent evento) {
        try {
            JPAUtil.iniciar();
            LOG.info("SGHA: conexão com o banco de dados aberta.");
        } catch (RuntimeException e) {
            // A aplicação sobe mesmo assim; o erro volta com a causa quando um DAO pedir o banco.
            LOG.log(Level.SEVERE, "SGHA: não foi possível abrir a conexão com o banco de dados. "
                    + "Verifique se o MySQL está no ar e se o banco foi criado (db/criar-banco.sql).", e);
            return;
        }

        try {
            DadosIniciais.semear();
        } catch (RuntimeException e) {
            LOG.log(Level.WARNING, "SGHA: não foi possível cadastrar os membros iniciais da comissão.", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent evento) {
        JPAUtil.encerrar();
        LOG.info("SGHA: conexão com o banco de dados fechada.");
    }
}
