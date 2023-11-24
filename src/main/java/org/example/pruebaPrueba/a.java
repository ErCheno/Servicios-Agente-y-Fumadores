package org.example.pruebaPrueba;

import java.util.EnumSet;
import java.util.Random;

enum Ingrediente {
    TABACO,
    PAPEL,
    FOSFOROS
}

class MesaFumadores {

    private Ingrediente componenteFaltante;
    private boolean agenteNotificado;

    public MesaFumadores() {
        this.componenteFaltante = null;
        this.agenteNotificado = false;
    }

    public synchronized void setComponent(Ingrediente componente) {
        this.componenteFaltante = componente;
    }

    public synchronized EnumSet<Ingrediente> getComponents() {
        return EnumSet.of(componenteFaltante);
    }

    public synchronized void despiertaFumador(Ingrediente componente) {
        if (componenteFaltante != componente) {
            throw new IllegalStateException("Despierta al fumador equivocado.");
        }
        agenteNotificado = true;
    }

    public synchronized void take(Ingrediente componente) throws InterruptedException {
        while (componente != componenteFaltante || !agenteNotificado) {
            wait();
        }
        agenteNotificado = false;
    }

    public synchronized void despiertaAgente() {
        notifyAll();
    }
}

class Fumador implements Runnable {

    private MesaFumadores mesaFumadores;
    private Ingrediente ingrediente;

    public Fumador(MesaFumadores mesaFumadores, Ingrediente ingrediente) {
        this.mesaFumadores = mesaFumadores;
        this.ingrediente = ingrediente;
    }

    public void fumar() throws InterruptedException {
        System.out.println("Fumador con " + ingrediente + " está fumando.");
        Thread.sleep(2000);
    }

    public void run() {
        while (true) {
            try {
                mesaFumadores.take(ingrediente);
                fumar();
                mesaFumadores.despiertaAgente();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Agente implements Runnable {

    private MesaFumadores mesaFumadores;
    private Random random;

    public Agente(MesaFumadores mesaFumadores) {
        this.mesaFumadores = mesaFumadores;
        this.random = new Random();
    }

    public void ponerIngredientes() throws InterruptedException {
        Thread.sleep(1000);
        mesaFumadores.setComponent(Ingrediente.TABACO);
        mesaFumadores.despiertaFumador(Ingrediente.TABACO);

        Thread.sleep(1000);
        mesaFumadores.setComponent(Ingrediente.PAPEL);
        mesaFumadores.despiertaFumador(Ingrediente.PAPEL);

        Thread.sleep(1000);
        mesaFumadores.setComponent(Ingrediente.FOSFOROS);
        mesaFumadores.despiertaFumador(Ingrediente.FOSFOROS);





        mesaFumadores.despiertaAgente();
    }

    public void run() {
        while (true) {
            try {
                ponerIngredientes();
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

public class a {
    public static void main(String[] args) {
        MesaFumadores mesa = new MesaFumadores();
        Fumador f1 = new Fumador(mesa, Ingrediente.TABACO);
        Fumador f2 = new Fumador(mesa, Ingrediente.PAPEL);
        Fumador f3 = new Fumador(mesa, Ingrediente.FOSFOROS);
        Agente agente = new Agente(mesa);

        mesa.setComponent(Ingrediente.TABACO);

        Thread t1 = new Thread(f1);
        Thread t2 = new Thread(f2);
        Thread t3 = new Thread(f3);
        Thread tAgente = new Thread(agente);

        t1.start();
        t2.start();
        t3.start();
        tAgente.start();
    }
}
