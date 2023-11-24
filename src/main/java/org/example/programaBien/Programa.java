package org.example.programaBien;

import java.util.*;
import java.util.concurrent.Semaphore;

class Agente implements Runnable {

    private MesaFumadores mesaFumadores;
    private Random random;
    private Semaphore semaphore;

    public Agente(MesaFumadores mesaFumadores) {
        this.mesaFumadores = mesaFumadores;
        this.random = new Random();
        this.semaphore = new Semaphore(1);
    }

    public EnumSet<Ingrediente> nextComponents() {
        EnumSet<Ingrediente> ingredientes = EnumSet.allOf(Ingrediente.class);
        Ingrediente[] array = ingredientes.toArray(new Ingrediente[0]);
        int i = random.nextInt(array.length);
        ingredientes.remove(array[i]);
        return ingredientes;
    }

    public void putComponents() {
        mesaFumadores.putComponents(nextComponents());
    }

    public void sleep() throws InterruptedException {
        semaphore.acquire();
    }

    public void awake() {
        semaphore.release();
    }

    public void run() {
        while (true) {
            try {
                sleep();
                putComponents();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class Fumador implements Runnable {

    private MesaFumadores mesaFumadores;
    private Ingrediente ingrediente;
    private Semaphore components;

    public Fumador(MesaFumadores mesaFumadores, Ingrediente ingrediente) {
        this.mesaFumadores = mesaFumadores;
        this.ingrediente = ingrediente;
        this.components = new Semaphore(0);
    }

    public Ingrediente getComponent() {
        return ingrediente;
    }

    public boolean hasComponent(Ingrediente ingrediente) {
        return this.ingrediente == ingrediente;
    }

    public void fumar() throws InterruptedException {
        System.out.println("El fumador con el componente " + ingrediente + " está fumando durante 3 segundos");
        Thread.sleep(300);

    }

    public void despierta() {
        components.release();
    }

    private void esperarComponentes() throws InterruptedException {
        components.acquire();
    }

    public void run() {
        while (true) {
            try {
                esperarComponentes();
                fumar();
                mesaFumadores.despiertaAgente();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

class MesaFumadores implements Runnable {

    private Agente agente;
    private List<Fumador> fumadors;
    private List<Pusher> pushers;
    private EnumMap<Ingrediente, Semaphore> components;
    private EnumMap<Ingrediente, Boolean> hasComponent;

    public MesaFumadores() {
        this.agente = new Agente(this);
        this.fumadors = new ArrayList<>();
        this.pushers = new ArrayList<>();
        this.components = new EnumMap<>(Ingrediente.class);
        this.hasComponent = new EnumMap<>(Ingrediente.class);
        for (Ingrediente ingrediente : Ingrediente.values()) {
            Fumador fumador = new Fumador(this, ingrediente);
            fumadors.add(fumador);

            Pusher pusher = new Pusher(this, ingrediente);
            pushers.add(pusher);

            components.put(ingrediente, new Semaphore(0));
            hasComponent.put(ingrediente, Boolean.FALSE);
        }
    }

    public Boolean hasComponent(Ingrediente ingrediente) {
        return hasComponent.get(ingrediente);
    }

    public void setComponent(Ingrediente ingrediente) {
        hasComponent.put(ingrediente, Boolean.TRUE);
    }

    public void take(Ingrediente ingrediente) throws InterruptedException {
        components.get(ingrediente).acquire();
    }

    public EnumSet<Ingrediente> getComponents() {
        EnumSet<Ingrediente> ingredientes = EnumSet.noneOf(Ingrediente.class);
        for (Map.Entry<Ingrediente, Boolean> entry : hasComponent.entrySet()) {
            if (entry.getValue()) {
                ingredientes.add(entry.getKey());
            }
        }
        return ingredientes;
    }

    public void putComponents(EnumSet<Ingrediente> ingredientes) {
        System.out.println("Agente generado con los ingredientes: " + ingredientes);

        synchronized (this) {
            for (Ingrediente ingrediente : Ingrediente.values()) {
                hasComponent.put(ingrediente, Boolean.FALSE);
            }
            for (Ingrediente ingrediente : ingredientes) {
                this.components.get(ingrediente).release();
            }
        }
    }

    public void despiertaAgente() {
        agente.awake();
    }

    public void despiertaFumador(Ingrediente ingrediente) throws InterruptedException {
        for (Fumador fumador : fumadors) {
            if (fumador.hasComponent(ingrediente)) {
                fumador.despierta();
                break;
            }
        }
    }

    public void run() {
        new Thread(agente).start();
        for (Fumador fumador : fumadors) {
            new Thread(fumador).start();
        }
        for (Pusher pusher : pushers) {
            new Thread(pusher).start();
        }
    }
}

class Pusher implements Runnable {

    private MesaFumadores mesaFumadores;
    private Ingrediente ingrediente;


    public Pusher(MesaFumadores mesaFumadores, Ingrediente ingrediente) {
        this.mesaFumadores = mesaFumadores;
        this.ingrediente = ingrediente;

    }

    public void run() {
        while (true) {
            try {
                mesaFumadores.take(ingrediente);

                synchronized (mesaFumadores) {
                    mesaFumadores.setComponent(ingrediente);
                    EnumSet<Ingrediente> ingredientes = EnumSet.complementOf(mesaFumadores.getComponents());

                    if (ingredientes.size() == 1) {
                        mesaFumadores.despiertaFumador(ingredientes.iterator().next());
                    }

                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

class Main {

    public static void main(String[] args) {
        new Thread(new MesaFumadores()).start();
    }
}
