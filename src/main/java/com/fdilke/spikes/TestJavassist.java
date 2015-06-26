package com.fdilke.spikes;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.fdilke.topple.sugar.algebra.universal.Algebra;
import com.fdilke.topple.sugar.algebra.universal.Axioms;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

public class TestJavassist {

	public static void main0(String[] args) throws Exception {
		ProxyFactory factory = new ProxyFactory();
	    factory.setSuperclass(Dog.class);
	    factory.setFilter(
	            new MethodFilter() {
	                public boolean isHandled(Method method) {
	                    return Modifier.isAbstract(method.getModifiers());
	                }
	            }
	        );

	    MethodHandler handler = new MethodHandler() {
	        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
	            System.out.println("Handling " + thisMethod + " via the method handler");
	            return null;
	        }
	    };

	    Dog dog = (Dog) factory.create(new Class<?>[0], new Object[0], handler);
	    dog.bark();
	    dog.fetch();
	}
	
	public static abstract class Dog {

	    public void bark() {
	        System.out.println("Woof!");
	    }

	    public abstract void fetch();

	}

	public static void main(String[] args) throws Exception {
		ProxyFactory factory = new ProxyFactory();
	    factory.setSuperclass(EmptyAxiomScheme.class);
	    factory.setFilter(
	            new MethodFilter() {
	                public boolean isHandled(Method method) {
	                    return Modifier.isAbstract(method.getModifiers());
	                }
	            }
	        );

	    MethodHandler handler = new MethodHandler() {
	        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
	            System.out.println("Handling " + thisMethod + " via the method handler");
	            return null;
	        }
	    };

	    EmptyAxiomScheme<?> dog = (EmptyAxiomScheme<?>) factory.create(new Class<?>[0], new Object[0], handler);
	    dog.bark();
	    dog.fetch();
	}
	
	// A valid, but completely empty axiom scheme
	@Axioms
	public static abstract class EmptyAxiomScheme<T> implements Algebra<EmptyAxiomScheme<T>> {
		public abstract void fetch();
		public abstract void bark();
	}
}
