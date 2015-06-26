package com.fdilke.topple.sugar.algebra.universal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;

import com.fdilke.topple.ToposArrow;
import com.fdilke.topple.ToposDot;
import com.fdilke.topple.sugar.Element;
import com.fdilke.topple.util.Pair;

public class ParsedAxioms<
	DOT extends ToposDot<DOT, ARROW>, 
	ARROW extends ToposArrow<DOT, ARROW>,
	AXIOMS extends Algebra<? super AXIOMS>
> {
	private final Class<? extends AXIOMS> axiomsClass;
	private final Map<String, Algebra<?>> pSpaceMap;
	private final ProxyFactory proxyFactory;
	private final Map<String, Op> ops = new HashMap<String, Op>();
	private final Map<String, ParsedLaw<DOT, ARROW, AXIOMS>> laws = new HashMap<String, ParsedLaw<DOT, ARROW, AXIOMS>>();
	private static final Map<Pair<Class<?>, Map<String, Algebra<?>>>, ParsedAxioms<?, ?, ?>> cache = 
		new HashMap<Pair<Class<?>, Map<String, Algebra<?>>>, ParsedAxioms<?, ?, ?>>(); 
	
	public static <
		DOT extends ToposDot<DOT, ARROW>, 
		ARROW extends ToposArrow<DOT, ARROW>,
		AXIOMS extends Algebra<? super AXIOMS>
	> ParsedAxioms<DOT, ARROW, AXIOMS>
	parse(Class<? extends AXIOMS> axiomsClass, Map<String, Algebra<?>> pSpaceMap){
		Pair<Class<?>, Map<String, Algebra<?>>> pair = new Pair<Class<?>, Map<String, Algebra<?>>>(axiomsClass, pSpaceMap); 
		@SuppressWarnings("unchecked")
		ParsedAxioms<DOT, ARROW, AXIOMS> parsedAxioms = (ParsedAxioms<DOT, ARROW, AXIOMS>)cache.get(pair);
		if (parsedAxioms == null) {
			parsedAxioms = new ParsedAxioms<DOT, ARROW, AXIOMS>(axiomsClass, pSpaceMap);
			cache.put(pair, parsedAxioms);
		}
		return parsedAxioms;
	}
	
	public ParsedAxioms(Class<? extends AXIOMS> axiomsClass, Map<String, Algebra<?>> pSpaceMap) {
		this.axiomsClass = axiomsClass;
		this.pSpaceMap = pSpaceMap;
		this.proxyFactory = setupProxyFactory(axiomsClass);
		checkAxiomsAnnotation();
		parseOps();
		parseLaws();
	}

	private static ProxyFactory setupProxyFactory(Class<?> axiomsClass) {
		ProxyFactory factory = new ProxyFactory();
	    factory.setSuperclass(axiomsClass);
	    factory.setFilter(
	            new MethodFilter() {
	                public boolean isHandled(Method method) {
	                    return Modifier.isAbstract(method.getModifiers());
	                }
	            }
	        );
	    return factory;
	}

	private void checkAxiomsAnnotation() {
		if (axiomsClass.getAnnotation(Axioms.class) == null) {
			throw new IllegalArgumentException("class " + axiomsClass + " lacks annotation "
					+ Axioms.class);
		}
	}

	private void parseOps() {
		for (Method method : axiomsClass.getMethods()) {
			if (method.isAnnotationPresent(Operator.class)) {
				String opName = method.getName();
				int opArity = method.getParameterTypes().length;
				ops.put(opName, new Op(opArity, paramSpaces(method, pSpaceMap)));
			}
		}
	}

	private void parseLaws() {
		for (Method method : axiomsClass.getMethods()) {
			if (method.isAnnotationPresent(Law.class)) {
				addLaw(axiomsClass, pSpaceMap, method, true);
			} else if(method.isAnnotationPresent(OptionalLaw.class)) {
				addLaw(axiomsClass, pSpaceMap, method, false);
			}
		}
	}
		
	private void addLaw(final Class<? extends AXIOMS> axiomsClass,
			Map<String, Algebra<?>> pSpaceMap, final Method method, boolean isStrict) {
		if (Modifier.isStatic(method.getModifiers())) {
			throw new IllegalArgumentException("Laws method " + method.getName() 
					+ " cannot be static in " + axiomsClass.getName());
		}
		
		Class<?>[] paramTypes = method.getParameterTypes();
		int arity = paramTypes.length;
		laws.put(method.getName(), new ParsedLaw<DOT, ARROW, AXIOMS>(method.getName(), isStrict,
				arity, paramSpaces(method, pSpaceMap)) {
			public boolean verify(AXIOMS algebra, List<Element<?>> elements) {
				try {
					return  (Boolean)method.invoke(algebra, elements.toArray(new Object[0]));
				} catch (Exception ex) {
					throw new IllegalArgumentException("Invocation failed for law method " + method.getName() 
							+ " in " + axiomsClass.getName(), ex);
				}
			}
		});
	}

	public Map<String, Op> getOps() {
		return ops;
	}
	
	public Map<String, ParsedLaw<DOT, ARROW, AXIOMS>> getLaws() {
		return laws;
	}
	
	private List<Algebra<?>> paramSpaces(Method method, Map<String, Algebra<?>> pSpaceMap) {
		List<Algebra<?>> paramSpaces = new ArrayList<Algebra<?>>();
		for (Annotation[] annotations : method.getParameterAnnotations()) {
			Algebra<?> paramSpace = null;
			for (Annotation annotation : annotations) {
				if (annotation instanceof Parameter) {
					Parameter paramSpaceAnnotation = (Parameter)annotation;
					String paramSpaceName = paramSpaceAnnotation.value();
					paramSpace = pSpaceMap.get(paramSpaceName);
					if (paramSpace == null) {
						throw new IllegalArgumentException("Unknown parameter space \"" + paramSpaceName + "\"");
					}
				}
			}
			paramSpaces.add(paramSpace);
		}
		return paramSpaces;
	}
	
	// make a separate class?
	public static class Op {
		private final int arity;
		private final List<Algebra<?>> paramSpaces;
		
		public Op(int arity, List<Algebra<?>> paramSpaces) {
			this.arity = arity;
			this.paramSpaces = paramSpaces;
		}

		public int getArity() {
			return arity;
		}
		
		public List<Algebra<?>> getParamSpaces() {
			return paramSpaces;
		}
	}

	public AXIOMS createProxy(MethodHandler handler) {
	    try {
	    	return axiomsClass.cast(proxyFactory.create(new Class<?>[0], new Object[0], handler));
	    } catch(Exception ex) {
	    	throw new IllegalArgumentException("can't create proxy", ex);
	    }
	}
}
