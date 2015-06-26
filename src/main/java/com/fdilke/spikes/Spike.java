package com.fdilke.spikes;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: Felix
 * Date: 05-Mar-2008
 * Time: 00:09:04
 * To change this template use File | Settings | File Templates.
 */
public class Spike {
    public interface Visitor {
        public void visit(Host1 host);
        public void visit(Host2 host);
    }

    public interface Host {
        public void accept(Visitor visitor);
    }

    public class Host1 implements Host {
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public class Host2 implements Host {
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public class Visitor1 implements Visitor {

        public void visit(Host1 host) { // no action needed
        }
        public void visit(Host2 host) { // no action needed
        }
    }

    public static void oldmain(String[] args) {
        // check equality of lists works as it should
        String me = "FELIX";
        String me2 = "felix".toUpperCase();
        List<String> lMe= Collections.singletonList(me);
        List<String> lMe2= Collections.singletonList(me2);
        if (lMe.equals(lMe2)) {
            System.err.println("yess!");
        } else {
            System.err.println("oh dear");
        }
    }

    public static void XXmain(String[] args) {
    	String text = "Hello()";
    	for (byte b : text.getBytes()) {
    		switch(b) {
    		case 'e': System.out.println("found an e"); break;
    		case '(': System.out.println("found an ("); break;
    		case ')': System.out.println("found an e"); break;
    		}
    		
    	}
    }
    
    public static void main(String[] args) {

        Properties props = System.getProperties();
        for (Object propObj : props.keySet()) {
        	String prop = (String) propObj;
        	if (prop.contains("j")) {
        		System.out.println(prop + "`t" + props.getProperty(prop));
        	}
        }
    }
}
