package org.example;

import java.util.concurrent.ThreadLocalRandom;

class Table {
    boolean tobacco;
    boolean paper;
    boolean notify; // Used for the agent, should it be notified.


    public Table() {
        // Initial setup says nothing on the table and notifies the agent
        // to populate the table with ingredients.
        this.tobacco = false;
        this.paper = false;
        this.notify = true;
    }
}


class Agent extends Thread {
    Table table;


    public Agent(Table table) {
        this.table = table;
    }


    public void alert() {
        synchronized (table) {
            System.out.println("Agent alerted..");
            table.notify = true;
            table.notifyAll();
        }
    }


    public void run() {
        try {
            synchronized (table) {
                while (true) {
                    while (!table.notify) {
                        System.out.println("El agente está observando..!");
                        table.wait();
                    }
                    int numeroRandom = ThreadLocalRandom.current().nextInt(1,2);
                    if (numeroRandom==1){
                        System.out.println("Agent populates table with tobacco and paper");
                        table.tobacco = true;
                        table.paper = true;
                        table.notify = false;
                    }else if (numeroRandom==2){
                        System.out.println("Agent populates table with match and paper");
                        table.tobacco = true;
                        table.paper = true;
                        table.notify = false;
                    }
                    table.notify();

                }
            }




        } catch (InterruptedException e) {
            // Handle interruption
        }
    }
}


class Smoker extends Thread {
    String[] smStages = {"Rolling", "Lighting", "Smoking", "Putting out"};
    Table table;
    Agent agent;
    int id;


    public Smoker(Table table, Agent agent, int id) {
        this.table = table;
        this.agent = agent;
        this.id = id;
    }


    public void run() {
        try {
            synchronized (table) {
                while (true) {
                    while (!(table.tobacco && table.paper)) {
                        System.out.println("Smoker #" + id + " waiting!");
                        table.wait();
                    }
                    if (table.tobacco && table.paper) {
                        table.paper = false;
                        table.tobacco = false;
                        System.out.print("Smoker #" + id + " is: ");
                        for (int i = 0; i < 4; i++) {
                            System.out.print(smStages[i] + " ");
                            Thread.sleep(250);
                        }
                        System.out.println();
                        agent.alert();
                    }
                }
            }
        } catch (InterruptedException e) {
            // Handle interruption
        }
    }
}


public class MainConProblema {
    public static void main(String[] args) {
        Table table = new Table();
        Agent agent = new Agent(table);
        Smoker s1 = new Smoker(table, agent, 1);
        Smoker s2 = new Smoker(table, agent, 2);
        Smoker s3 = new Smoker(table, agent, 3);


        agent.start();
        s1.start();
        s2.start();
        s3.start();
    }
}
